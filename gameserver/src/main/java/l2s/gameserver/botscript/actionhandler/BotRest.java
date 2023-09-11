package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.botscript.MonsterSelectUtil;
import l2s.gameserver.core.*;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.SkillEntry;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;

public class BotRest implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		boolean hasEnemy;
		Party party = actor.getParty();
		if(config.isIdleRest())
		{
			boolean hasAnySupport = this.hasAnySupportTask(actor);
			if(hasAnySupport)
			{
				actor.standUp();
				return false;
			}
			boolean targetEmpty= false;
			if (actor.getTarget()!=actor) {
				targetEmpty =true;
			}
			if (!targetEmpty) {
				actor.setTarget(null);
			}

			MonsterInstance hatingMonster = MonsterSelectUtil.findHatingMonster(actor);
			if (hatingMonster !=null && hatingMonster.getRandomDamage()>0) {
				actor.standUp();
				return false;
			}else {
				actor.sitDown(null);
				actor.setTarget(null);
				return true;
			}
		}
		boolean mpFull = actor.getCurrentMpPercents() >= 99.0;
		boolean hpFull = actor.getCurrentHpPercents() >= 99.0;
		boolean bl = hasEnemy = MonsterSelectUtil.findHatingMonster(actor) != null;
		MonsterInstance hatingMonster = attackableMonster(actor);
		if (hatingMonster !=null && hatingMonster.getRandomDamage()>0) {
			if(actor.isSitting())
				actor.standUp();
			return false;
		}
		if(actor.isSitting())
		{
			actor.setTarget(null);
			/* 如有可用的 botSupport 站立使用*/
			boolean hasAnySupport = this.hasAnySupportTask(actor);
			if (hasAnySupport) {
				actor.standUp();
				return false;
			}
			if(mpFull && hpFull || (hasEnemy && hatingMonster !=null && hatingMonster.getAI().getAttackTarget() == actor))
			{
				actor.standUp();
			}
			return false;
		}
		if(mpFull && hpFull)
		{
			return false;
		}
		if(hasEnemy)
		{
			return false;
		}
		if(party != null && config.isFollowRest() && party.getPartyLeader().isSitting())
		{
			actor.sitDown(null);
			actor.setTarget(null);
			return true;
		}
		if(actor.getCurrentMpPercents() > (double) config.getMpProtected() && actor.getCurrentHpPercents() > (double) config.getHpProtected())
		{
			return false;
		}else {
			boolean hasAnySupport = this.hasAnySupportTask(actor);
			if (hasAnySupport) {
				actor.standUp();
				return false;
			}
			boolean targetEmpty= false;
			if (actor.getTarget()!=actor) {
				targetEmpty =true;
			}
			if (!targetEmpty) {
				actor.setTarget(null);
			}
		}
		actor.sitDown(null);
		actor.setTarget(null);
		return true;
	}

	private MonsterInstance attackableMonster(Player actor) {
		MonsterInstance hatingMonster = null;
		List<Creature> aroundObjs = World.getAroundCharacters(actor, 1000, 500);
		for(Creature creature : aroundObjs)
		{
			if(creature == null || !creature.isMonster())
				continue;
			if (creature.getAI().getAttackTarget() == actor) {
				hatingMonster = this.getMonster(creature);
			}
		}
		return hatingMonster;
	}

	private boolean hasAnySupportTask(Player actor)
	{
		BotConfigImp botConfig;
		SkillEntry entry;
		Party party = actor.getParty();
		if(party != null && actor.getDistance((GameObject) party.getPartyLeader()) >= 700.0 && actor.getDistance((GameObject) party.getPartyLeader()) <= 3000.0)
		{
			return true;
		}
		BotConfig config = BotEngine.getInstance().getBotConfig(actor);
		double selfPercent = actor.getCurrentHpPercents();
		if(config.getSelfHpHeal() != 0 && selfPercent <= (double) config.getSelfHpHeal() && config.getHealSkill1() != 0 && !actor.isSkillDisabled((entry = actor.getKnownSkill(config.getHealSkill1())).getTemplate()) && entry != null && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition((Creature) actor, (Creature) actor, false, false, true, false, false))
		{
			return true;
		}
		if(config.getPetHpHeal() != 0 && config.getHealSkill3() != 0)
		{
			entry = actor.getKnownSkill(config.getHealSkill3());
			SummonInstance pet = actor.getSummon();
			if(pet != null && !pet.isDead() && pet.getCurrentHpPercents() <= (double) config.getPetHpHeal() && !actor.isSkillDisabled(entry.getTemplate()) && entry != null && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition((Creature) actor, (Creature) pet, false, false, true, false, false))
			{
				return true;
			}
		}
		if(config.getPartyHpHeal() != 0 && (entry = actor.getKnownSkill(config.getHealSkill2())) != null && party != null)
		{
			for(Player member : party)
			{
				if(!member.isDead() && member != actor && member.getDistance((GameObject) actor) <= 4000.0 && member.getCurrentHpPercents() <= (double) config.getPartyHpHeal() && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition((Creature) actor, (Creature) member, false, false, true, false, false))
				{
					return true;
				}
				SummonInstance pet = member.getSummon();
				if(pet == null || pet.isDead() || !(pet.getCurrentHpPercents() <= (double) config.getPartyHpHeal()) || actor.isSkillDisabled(entry.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition((Creature) actor, (Creature) member, false, false, true, false, false))
					continue;
				return true;
			}
		}
		if(!config.getPartyMpHeal().isEmpty() && actor.getCurrentMpPercents() >= (double) config.getKeepMp() && (entry = actor.getKnownSkill(1013)) != null && party != null)
			/*技能ID1013 回復術*/
		{
			for(Player member : party)
			{
				if(!config.getPartyMpHeal().containsKey(member.getObjectId()) || member.isDead() || member == actor || !(member.getDistance((GameObject) actor) <= 4000.0) || !(member.getCurrentMpPercents() <= (double) config.getPartyMpHeal().get(member.getObjectId()).intValue()) || actor.isSkillDisabled(entry.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition((Creature) actor, (Creature) member, false, false, true, false, false))
					continue;
				return true;
			}
		}
		if(config.isPartyAntidote() && (entry = actor.getKnownSkill(1012)) != null && party != null)
		/*技能ID1012 療毒術*/
		{
			for(Player member : party)
			{
				if(member.isDead() || !(member.getDistance((GameObject) actor) <= 4000.0) || !member.getAbnormalList().contains(AbnormalType.POISON) || actor.isSkillDisabled(entry.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition((Creature) actor, (Creature) member, false, false, true, false, false))
					continue;
				return true;
			}
		}
		if(config.isPartyBondage() && (entry = actor.getKnownSkill(1018)) != null && party != null)
		/*技能ID1018 淨化*/
		{
			for(Player member : party)
			{
				if(member.isDead() || !(member.getDistance((GameObject) actor) <= 4000.0) || !member.getAbnormalList().contains(AbnormalType.BLEEDING) || actor.isSkillDisabled(entry.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition((Creature) actor, (Creature) member, false, false, true, false, false))
					continue;
				return true;
			}
		}
		if(config.isPartyParalysis() && (entry = actor.getKnownSkill(1018)) != null && party != null)
		/*技能ID1018 淨化*/
		{
			for(Player member : party)
			{
				if(member.isDead() || !(member.getDistance((GameObject) actor) <= 4000.0) || !member.getAbnormalList().contains(AbnormalType.PARALYZE) || actor.isSkillDisabled(entry.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition((Creature) actor, (Creature) member, false, false, true, false, false))
					continue;
				return true;
			}
		}
		if(!config.getAutoItemBuffs().isEmpty())
		{
			for(int itemId : config.getAutoItemBuffs())
			{
				ItemInstance item;
				Skill skill;
				//int itemId = (Integer)member.next();
				if(!ArrayUtils.contains(BotProperties.BUFF_ITEM_IDS, itemId) || (item = actor.getInventory().getItemByItemId(itemId)) == null || (skill = item.getTemplate().getFirstSkill().getTemplate()).getAbnormalType() == AbnormalType.NONE || actor.getAbnormalList().contains(skill.getAbnormalType()))
					continue;
				return true;
			}
		}
		if((botConfig = (BotConfigImp) config).getBuffSets().isEmpty())
		{
			return false;
		}
		List<Player> players = party != null ? party.getPartyMembers() : Collections.singletonList(actor);
		for(Player player : players)
		{
			SummonInstance pet;
			Skill skill;
			Creature target;
			String buffGroupName;
			SkillEntry skillEntry;
			if(!player.isDead() && player.getDistance((GameObject) actor) <= 3000.0 && (buffGroupName = botConfig.getBuffConfig().get(player.getName())) != null)
			{
				for(int id : botConfig.getBuffSets().get(buffGroupName))
				{
					skillEntry = actor.getKnownSkill(id);
					if(skillEntry == null || actor.isSkillDisabled(skill = skillEntry.getTemplate()))
						continue;
					if(!BotThinkTask.checkSkillMpCost(actor, skillEntry))
					{
						return false;
					}
					target = skill.getAimingTarget((Creature) actor, (GameObject) player);
					if(target != player || player.getAbnormalList().contains(skill.getAbnormalType()))
						continue;
					return true;
				}
			}
			if((pet = player.getSummon()) == null || pet.isDead() || !(pet.getDistance((GameObject) actor) <= 3000.0) || (buffGroupName = botConfig.getBuffConfig().get(player.getName())) == null)
				continue;
			for(int id : botConfig.getBuffSets().get(buffGroupName))
			{
				skillEntry = actor.getKnownSkill(id);
				if(skillEntry == null || actor.isSkillDisabled(skill = skillEntry.getTemplate()))
					continue;
				if(!BotThinkTask.checkSkillMpCost(actor, skillEntry))
				{
					return false;
				}
				target = skill.getAimingTarget((Creature) actor, (GameObject) pet);
				if(target != pet || pet.getAbnormalList().contains(skill.getAbnormalType()))
					continue;
				return true;
			}
		}
		return false;
	}
}