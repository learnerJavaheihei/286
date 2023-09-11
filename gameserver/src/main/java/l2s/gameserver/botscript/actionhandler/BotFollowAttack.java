package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotSkillStrategy;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.skills.SkillEntry;

public class BotFollowAttack implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		BotConfigImp configImp = (BotConfigImp) config;
		if(config.isAutoAttack())
		{
			return false;
		}
		if(!config.isFollowAttack())
		{
			return false;
		}
		Party party = actor.getParty();
		if(party == null)
		{
			return false;
		}
		if(this.isActionsDisabledExcludeAttack(actor) || actor.isSitting())
		{
			return false;
		}
		Player leader = party.getPartyLeader();
		if(actor == leader)
		{
			return false;
		}
		double distance = leader.getDistance((GameObject) actor);
		if(distance > 2000.0)
		{
			return false;
		}
		GameObject target = leader.getTarget();
		if(target == null)
		{
			return false;
		}
		MonsterInstance mob = this.getMonster(target);
		if(mob == null || mob.isDead())
		{
			return false;
		}
		if(actor.getTarget() != mob)
		{
			actor.setTarget(null);
			actor.setTarget((GameObject) mob);
		}
		if(configImp.isFollowAttackWhenChoosed() || this.getHate(mob, leader) > 0)
		{
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
							if(!mob.isDead() && !mob.isSpoiled())
							{
								actor.getMovement().moveToLocation(player.getLoc(), config.getFollowInstance(), !actor.getVarBoolean("no_pf"));
								return false;
							}
						}
					}
				}
			}
			SummonInstance pet;
			boolean useStrategy = false;
			for(BotSkillStrategy s : config.getAttackStrategy())
			{
				useStrategy = s.useMe(actor, mob);
				if(useStrategy)
					break;
			}
			if(!useStrategy && config.isUsePhysicalAttack())
			{
				mob.onAction(actor, false);
			}
			if((pet = actor.getSummon()) != null && config.isSummonAttack() && !pet.isActionsDisabled())
			{
				pet.setTarget((GameObject) mob);
				pet.getAI().Attack((GameObject) mob, false, false);
			}
		}
		return true;
	}

	public int getHate(MonsterInstance mob, Player player)
	{
		AggroList.AggroInfo ai = mob.getAggroList().get((Creature) player);
		if(ai == null)
		{
			return 0;
		}
		return ai.damage;
	}
}