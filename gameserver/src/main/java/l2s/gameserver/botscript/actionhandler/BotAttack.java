
package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.botscript.MonsterSelectUtil;
import l2s.gameserver.botscript.PetTargetChoose;
import l2s.gameserver.core.*;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.player.AutoFarm;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.skills.SkillEntry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BotAttack implements IBotActionHandler
{
	private static /* synthetic */ int[] $SWITCH_TABLE$botscript$PetTargetChoose;

	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		boolean doAttack = this.doAttack(actor, (BotConfigImp) config, isSitting, movable, simpleActionDisable);
		boolean doSummon = this.doSummonAttack(actor, (BotConfigImp) config, isSitting, movable, simpleActionDisable);
		return doAttack || doSummon;
	}

	private boolean doAttack(Player actor, BotConfigImp config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isAutoAttack())
		{
			return false;
		}
		if(actor.isInPeaceZone())
		{
			return false;
		}
		if(actor.isSitting())
		{
			return false;
		}
		if(this.isActionsDisabledExcludeAttack(actor) && config.isCoverMember() || !actor.isActionsDisabled())
		{
			Optional<MonsterInstance> mob = Optional.empty();
			MonsterInstance monster = null;
			MonsterInstance petTarget = this.petTarget(actor);
			if(config.isCoverMember() || (actor.getTarget() == null || !actor.getTarget().isMonster() || this.getMonster(actor.getTarget()).isDead()) && petTarget == null)
			{
				mob = MonsterSelectUtil.apply(actor);
				if(!mob.isPresent())
				{
					if(!actor.isImmobilized())
					{
						this.returnHome(actor);
					}
					return false;
				}
				monster = mob.get();
			}
			else
			{
				mob = MonsterSelectUtil.apply(actor);
				if (mob.isPresent()) {
					monster = mob.get();
				}else {
					monster = petTarget != null ? petTarget : this.getMonster(actor.getTarget());
				}

			}
			Creature monsterTarget = null;
			if (monster!=null) {
				monsterTarget = monster.getAI().getAttackTarget();
			}
//			// 如果 怪物 有目标 且 目标 是一个玩家 且 (目标的不是当前玩家 或者 不是 我的队友  )
//			if (monsterTarget!=null && monsterTarget.isPlayer() && monsterTarget.getObjectId() != actor.getObjectId() && !monsterTarget.getPlayer().isInSameParty(actor) ) {
//				config.addBlockTargetId(monster.getObjectId());
//				actor.setTarget(null);
//				ThreadPoolManager.getInstance().schedule(new Runnable() {
//					@Override
//					public void run() {
//						config.releaseMemory(actor);
//
//					}
//				}, 60 * 1000L, TimeUnit.MILLISECONDS);
//
//				return false;
//			}
			if(actor.getTarget() != monster)
			{
				actor.setTarget(monster);
			}
			// 有放花攻击的情况
			// 54 收集者 55 赏金猎人 117 財富獵人
			// 56 工匠 57 战争工匠 118 巨匠
			if (config.is_autoSpoiledAttack() && actor.getParty()!=null) {
				int [] classIds = new int[]{53,54,55,56,57,117,118};
				for (Player player : actor.getParty()) {
					for (int i = 0; i < classIds.length; i++) {
						if (player.getClassId()!=null && player.getClassId().getId() == classIds[i] && player!=actor && actor.getDistance(player) <= 3000.0 && !player.isDead()) {
							SkillEntry skill = player.getKnownSkill(254);
							/*技能ID254 自體變化*/
							if(skill == null)
							{
								continue;
							}
							if(!BotThinkTask.checkSkillMpCost(player, skill))
							{
								continue;
							}

							if(monster!=null && !monster.isDead() && !monster.isSpoiled())
							{
								actor.getMovement().moveToLocation(player.getLoc(), config.getFollowInstance(), !actor.getVarBoolean("no_pf"));
								return false;
							}
						}
					}
				}
			}

			//礼仪模式
			AutoFarm autoFarm = actor.getAutoFarm();
			if (autoFarm.isPoliteFarm()) {
				MonsterInstance target = (MonsterInstance)actor.getTarget();
				if (target!=null) {
					List<Creature> aroundCharacters = World.getAroundCharacters(actor, config.getFindMobMaxDistance(), config.getFindMobMaxHeight());
					for (Creature aroundCharacter : aroundCharacters) {
						if (aroundCharacter.isPlayer() && !aroundCharacter.isDead()) {
							if (aroundCharacter.getPlayer().isInSameParty(actor)) {
								continue;
							}
							if (aroundCharacter.getTarget() == target && !actor.isAttackingNow()) {
								if (monster == null)
									continue;
								config.addBlockTargetId(monster.getObjectId());
								actor.setTarget(null);
								ThreadPoolManager.getInstance().schedule(new Runnable() {
									@Override
									public void run() {
										config.releaseMemory(actor);
									}
								}, 60 * 1000L, TimeUnit.MILLISECONDS);

								return false;
							}
						}
					}
				}
			}

			if(!GeoEngine.canSeeTarget(actor, monster))
			{
				if (monster == null)
					return false;

				actor.getMovement().moveToLocation(monster.getLoc(), 0, !actor.getVarBoolean("no_pf"), true, false);
				if(monster.getObjectId() != config.getCurrentTargetObjectId())
				{
					config.setCurrentTargetObjectId(monster.getObjectId());
				}
				config.setTryTimes(config.getTryTimes() + 1);
				if(config.getTryTimes() >= 15)
				{
					actor.sendMessage("\u653b\u51fb\u8d85\u65f6\uff0c\u5207\u6362\u76ee\u6807");
					/*\u653b\u51fb\u8d85\u65f6\uff0c\u5207\u6362\u76ee\u6807 攻击超时，切换目标*/
					config.setTryTimes(0);
					config.addBlockTargetId(monster.getObjectId());
					actor.setTarget(null);
				}
				return false;
			}
			if(config.getTryTimes() != 0)
			{
				config.setTryTimes(0);
			}
			boolean useStrategy = false;
			for(BotSkillStrategy s : config.getAttackStrategy())
			{
				useStrategy = s.useMe(actor, monster);
//				 去除后 1秒 内 把所有技能都检测使用 可以达到 增快使用技能的频率 目前测试是 3个技能 会将所有技能都使用完全 而且 没有空闲
//				if(useStrategy)
//					break;
			}
			if(!useStrategy && config.isUsePhysicalAttack())
			{
				actor.getTarget().onAction(actor, false);
			}
		}
		return true;
	}

	private boolean doSummonAttack(Player actor, BotConfigImp config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isSummonAttack())
		{
			return false;
		}
		if(actor.isInPeaceZone())
		{
			return false;
		}
		SummonInstance pet = actor.getSummon();
		if(pet == null || pet.isActionsDisabled())
		{
			return false;
		}
		GameObject target = pet.getTarget();
		if(target != null)
		{
			MonsterInstance mob = null;
			mob = this.getMonster(target);
			if(mob == null || mob.isDead() || !Geometry.calc(actor, mob))
			{
				this.resetTarget(pet, actor, config);
			}else {
				if (config.getPetTargetChoose().compareTo(PetTargetChoose.跟随主人) == 0) {
					GameObject actorTarget = actor.getTarget();
					if (target != actorTarget) {
						pet.setTarget(null);
						pet.setTarget(actorTarget);
					}
				}
			}
		}
		else
		{
			this.resetTarget(pet, actor, config);
		}
		if((target = pet.getTarget()) != null)
		{
			if (this.getMonster(target)!= null) {
				// 宠物使用技能
				if (!config.getPetBuffs().isEmpty() && !pet.isDead() && pet.getDistance(target) < 2500.0 ) {
					boolean isAttack = true;
					for (Integer petBuff : config.getPetBuffs()) {
						SkillEntry knownSkill = pet.getKnownSkill(petBuff);
						// 主人回复术
						if (knownSkill!=null && knownSkill.getId()==4025) {
							if (knownSkill.checkCondition(pet,actor,false,false,true)) {
								if(actor.getCurrentMpPercents() <= 90){
									pet.getAI().Cast(knownSkill, actor, false, false);
									isAttack = false;
									break;
								}
							}
						}
						if (knownSkill!=null && knownSkill.checkCondition(pet,this.getMonster(target),false,false,true)) {
							pet.getAI().Cast(knownSkill, this.getMonster(target), false, false);
							isAttack = false;
							break;
						}
					}
					if (isAttack) {
						pet.getAI().Attack(this.getMonster(target), false, false);
					}
				}else
					pet.getAI().Attack(this.getMonster(target), false, false);
			}
			if(config.getBpoidleAction() != BotPetOwnerIdleAction.\u539f\u5730\u4e0d\u52a8 && (!config.isFollowAttack() || config.isAutoAttack() && !config.isUsePhysicalAttack() && config.getAttackStrategy().isEmpty()))
			/*\u539f\u5730\u4e0d\u52a8 原地不动*/
			{
				if(config.getBpoidleAction() == BotPetOwnerIdleAction.\u9760\u8fd1\u53ec\u5524\u517d)
				/*\u9760\u8fd1\u53ec\u5524\u517d 靠近召唤兽*/
				{
					actor.getMovement().moveToLocation(pet.getLoc(), 400, !actor.getVarBoolean("no_pf"));
				}
				else
				{
					double dist;
					Party party = actor.getParty();
					if(party != null && (dist = actor.getDistance(party.getPartyLeader())) > 400.0 && dist < 3500.0)
					{
						actor.getMovement().moveToLocation(party.getPartyLeader().getLoc(), 400, !actor.getVarBoolean("no_pf"));
					}
				}
			}
			return true;
		}else {
			// 宠物使用技能
			if (!config.getPetBuffs().isEmpty() && !pet.isDead() && pet.getDistance(actor) < 2500.0 ) {
				for (Integer petBuff : config.getPetBuffs()) {
					SkillEntry knownSkill = pet.getKnownSkill(petBuff);
					// 主人回复术
					if (knownSkill!=null && knownSkill.getId()==4025) {
						if(actor.getCurrentMpPercents() > 90){
							break;
						}
						if (!knownSkill.checkCondition(pet,actor,false,false,true)) {
							break;
						}
						pet.getAI().Cast(knownSkill, actor, false, false);
						break;
					}
				}
			}
		}
		return false;
	}

	private void resetTarget(SummonInstance pet, Player actor, BotConfigImp config)
	{
		GameObject target;
		if(pet.getTarget() != null)
		{
			pet.setTarget(null);
		}
		if((target = actor.getTarget()) != null && target.isMonster() && !this.getMonster(target).isDead() && Geometry.calc(actor, this.getMonster(target)))
		{
			pet.setTarget(target);
		}
		else if(target == null)
		{
			Optional<MonsterInstance> mob = Optional.empty();
			switch(BotAttack.$SWITCH_TABLE$botscript$PetTargetChoose()[config.getPetTargetChoose().ordinal()])
			{
				case 1:
				{
					mob = MonsterSelectUtil.apply(actor);
					break;
				}
				case 2:
				{
					mob = target == null ? Optional.empty() : Optional.of(this.getMonster(target));
					break;
				}
				case 3:
				{
					Party party = actor.getParty();
					if(party == null)
						break;
					target = party.getPartyLeader().getTarget();
					mob = target == null || !target.isMonster() ? Optional.empty() : Optional.of(this.getMonster(target));
				}
			}
			if(!mob.isPresent())
			{
				if(!actor.isImmobilized())
				{
					this.returnHome(actor);
				}
				return;
			}
			MonsterInstance monster = mob.get();
			pet.setTarget(monster);
		}
	}

	private void returnHome(Player actor)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(actor);
		if(actor.getDistance(config.getStartX(), config.getStartY(), config.getStartZ()) < 5000.0)
		{
			actor.standUp();
			actor.getMovement().moveToLocation(config.getStartX(), config.getStartY(), config.getStartZ(), 0, !actor.getVarBoolean("no_pf"), true, false);
		}
	}

	private MonsterInstance petTarget(Player actor)
	{
		SummonInstance instance = actor.getSummon();
		if(instance == null)
		{
			return null;
		}
		if(instance.getTarget() == null)
		{
			return null;
		}
		MonsterInstance target = this.getMonster(instance.getTarget());
		if(target == null || target.isDead())
		{
			return null;
		}
		return target;
	}

	static /* synthetic */ int[] $SWITCH_TABLE$botscript$PetTargetChoose()
	{
		if($SWITCH_TABLE$botscript$PetTargetChoose != null)
		{
			//int[] arrn;
			return $SWITCH_TABLE$botscript$PetTargetChoose;
		}
		int[] arrn = new int[PetTargetChoose.values().length];
		try
		{
			arrn[PetTargetChoose.自主选怪.ordinal()] = 1;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[PetTargetChoose.跟随主人.ordinal()] = 2;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[PetTargetChoose.跟随队长.ordinal()] = 3;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		$SWITCH_TABLE$botscript$PetTargetChoose = arrn;
		return $SWITCH_TABLE$botscript$PetTargetChoose;
	}
}