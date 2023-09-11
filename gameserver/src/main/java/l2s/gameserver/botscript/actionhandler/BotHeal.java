package l2s.gameserver.botscript.actionhandler;

import l2s.commons.util.Rnd;
import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.botscript.MpHealOrder;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.SkillEntry;

import java.util.Comparator;
import java.util.LinkedList;

public class BotHeal implements IBotActionHandler
{
	private static /* synthetic */ int[] $SWITCH_TABLE$botscript$MpHealOrder;

	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		ItemInstance potion;
		ItemInstance potion2;
		SkillEntry lifebalance;
		SkillEntry entry;
		if(this.isActionsDisabledExcludeAttack(actor) || isSitting)
		{
			return false;
		}
		Party party = actor.getParty();
		BotConfigImp botConfigImp = (BotConfigImp) config;
		double selfPercent = actor.getCurrentHpPercents();
		if(config.getPotionHpHeal() != 0 && selfPercent <= config.getPotionHpHeal() && config.getHpPotionId() != 0 && (potion = actor.getInventory().getItemByItemId(config.getHpPotionId())) != null && !actor.getAbnormalList().contains(potion.getTemplate().getAttachedSkills()[0].getId()))
		{
			actor.useItem(potion, false, false);
		}
		double selfMpPercent = actor.getCurrentMpPercents();
		if(config.getPotionMpHeal() != 0 && selfMpPercent <= config.getPotionMpHeal() && config.getMpPotionId() != 0 && (potion2 = actor.getInventory().getItemByItemId(config.getMpPotionId())) != null && !actor.getAbnormalList().contains(potion2.getTemplate().getAttachedSkills()[0].getId()))
		{
			actor.useItem(potion2, false, false);
		}
		if(config.isAntidote() && actor.getAbnormalList().contains(AbnormalType.POISON) && (potion2 = actor.getInventory().getItemByItemId(1832)) != null)
			/*物品ID1832 濃縮解毒藥*/
		{
			actor.useItem(potion2, false, false);
		}
		if(config.isBondage() && actor.getAbnormalList().contains(AbnormalType.BLEEDING) && (potion2 = actor.getInventory().getItemByItemId(1834)) != null)
			/*物品ID1834 強力繃帶*/
		{
			actor.useItem(potion2, false, false);
		}
		if(botConfigImp.getEvaPercent() != 0 && botConfigImp.getEvaPercent() >= actor.getCurrentMpPercents() && (entry = actor.getKnownSkill(1506)) != null && !actor.isSkillDisabled(entry.getTemplate()) && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition(actor, actor, false, false, true, false, false) && this.checkHealSkill((Playable) actor, entry))
		/*技能ID1506 伊娃祝福*/
		{
			actor.setTarget(actor);
			actor.getAI().Cast(entry, actor, false, false);
			return true;
		}
		if(config.getSelfHpHeal() != 0 && selfPercent <= config.getSelfHpHeal() && config.getHealSkill1() != 0 && (entry = actor.getKnownSkill(config.getHealSkill1())) != null && !actor.isSkillDisabled(entry.getTemplate()) && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition(actor, actor, false, false, true, false, false) && this.checkHealSkill((Playable) actor, entry))
		{
			actor.setTarget(actor);
			actor.getAI().Cast(entry, actor, false, false);
			return true;
		}
		if(config.getPetHpHeal() != 0 && config.getHealSkill3() != 0)
		{
			entry = actor.getKnownSkill(config.getHealSkill3());
			SummonInstance pet = actor.getSummon();
			if(pet != null && !pet.isDead() && pet.getCurrentHpPercents() <= config.getPetHpHeal() && entry != null && !actor.isSkillDisabled(entry.getTemplate()) && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition(actor, pet, false, false, true, false, false))
			{
				actor.setTarget(pet);
				actor.getAI().Cast(entry, pet, false, false);
				return true;
			}
		}
		if(config.isPartyAntidote() && (entry = actor.getKnownSkill(1012)) != null && !actor.isSkillDisabled(entry.getTemplate()))
		/*技能ID1012 療毒術*/
		{
			if(party == null)
			{
				if(!actor.isDead() && actor.getAbnormalList().contains(AbnormalType.POISON) && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition(actor, actor, false, false, true, false, false))
				{
					actor.setTarget(actor);
					Creature target = entry.getTemplate().getAimingTarget(actor, actor);
					actor.getAI().Cast(entry, target, false, false);
					return true;
				}
			}else {
				for(Player member : party)
				{
					if(member.isDead() || !(member.getDistance(actor) <= 4000.0) || !member.getAbnormalList().contains(AbnormalType.POISON) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition(actor, member, false, false, true, false, false))
						continue;
					actor.setTarget(member);
					Creature target = entry.getTemplate().getAimingTarget(actor, member);
					actor.getAI().Cast(entry, target, false, false);
					return true;
				}
			}
		}
		if(party == null)
		{
			return false;
		}
		if(config.isPartyBondage() && (entry = actor.getKnownSkill(1018)) != null && !actor.isSkillDisabled(entry.getTemplate()))
		/*技能ID1018 淨化*/
		{
			for(Player member : party)
			{
				if(member.isDead() || !(member.getDistance(actor) <= 4000.0) || !member.getAbnormalList().contains(AbnormalType.BLEEDING) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition(actor, member, false, false, true, false, false))
					continue;
				actor.setTarget(member);
				Creature target = entry.getTemplate().getAimingTarget(actor, member);
				actor.getAI().Cast(entry, target, false, false);
				return true;
			}
		}
		if(config.isPartyParalysis() && (entry = actor.getKnownSkill(1018)) != null && !actor.isSkillDisabled(entry.getTemplate()))
		/*技能ID1018 淨化*/
		{
			for(Player member : party)
			{
				if(member.isDead() || !(member.getDistance(actor) <= 4000.0) || !member.getAbnormalList().contains(AbnormalType.PARALYZE) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition(actor, member, false, false, true, false, false))
					continue;
				actor.setTarget(member);
				Creature target = entry.getTemplate().getAimingTarget(actor, member);
				actor.getAI().Cast(entry, target, false, false);
				return true;
			}
		}
		if(botConfigImp.getBalanceSize() != 0 && (lifebalance = actor.getKnownSkill(1335)) != null && !actor.isSkillDisabled(lifebalance.getTemplate()) && BotThinkTask.checkSkillMpCost(actor, lifebalance) && lifebalance.checkCondition(actor, actor, false, false, true))
		/*技能ID1335 生命之衡*/
		{
			int size = 0;
			for(Player member : party)
			{
				SummonInstance pet;
				if(member.isDead() || member.getDistance(actor) > 1100.0)
					continue;
				if(member.getCurrentHpPercents() <= botConfigImp.getBalancePercent())
				{
					++size;
				}
				if((pet = member.getSummon()) != null && pet.getCurrentHpPercents() <= botConfigImp.getBalancePercent())
				{
					++size;
				}
				if(size < botConfigImp.getBalanceSize())
					continue;
				actor.getAI().Cast(lifebalance, actor, false, false);
				return true;
			}
		}
		if(botConfigImp.getPartyHealSkillId() != 0)
		{
			int size = 0;
			for(Player member : party)
			{
				if(member.isDead() || member.getDistance(actor) > 4000.0)
					continue;
				if(member.getCurrentHpPercents() <= botConfigImp.getPartyHealPercent())
				{
					++size;
				}
				if(member.hasSummon() && member.getSummon().getCurrentHpPercents() <= botConfigImp.getPartyHealPercent())
				{
					++size;
				}
				if(size < botConfigImp.getPartyHealSize())
					continue;
				SkillEntry entry2 = actor.getKnownSkill(botConfigImp.getPartyHealSkillId());
				if(entry2 == null || actor.isSkillDisabled(entry2.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, entry2) || !entry2.checkCondition(actor, actor, false, false, true, false, false))
					break;
				actor.getAI().Cast(entry2, actor, false, false);
				return true;
			}
		}
		if(config.getPartyHpHeal() != 0 && (entry = actor.getKnownSkill(config.getHealSkill2())) != null && !actor.isSkillDisabled(entry.getTemplate()))
		{
			for(Player member : party)
			{
				if(!member.isDead() && member != actor && member.getDistance(actor) <= 4000.0 && member.getCurrentHpPercents() <= config.getPartyHpHeal() && BotThinkTask.checkSkillMpCost(actor, entry) && entry.checkCondition(actor, member, false, false, true, false, false))
				{
					if(!this.checkHealSkill((Playable) member, entry))
						continue;
					actor.setTarget(member);
					Creature target = entry.getTemplate().getAimingTarget(actor, member);
					actor.getAI().Cast(entry, target, false, false);
					return true;
				}
				SummonInstance pet = member.getSummon();
				if(pet == null || pet.isDead() || !(pet.getCurrentHpPercents() <= config.getPartyHpHeal()) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition(actor, pet, false, false, true, false, false) || !this.checkHealSkill((Playable) pet, entry))
					continue;
				actor.setTarget(pet);
				Creature target = entry.getTemplate().getAimingTarget(actor, pet);
				actor.getAI().Cast(entry, target, false, false);
				return true;
			}
		}
		if(!config.getPartyMpHeal().isEmpty() && actor.getCurrentMpPercents() >= config.getKeepMp() && (entry = actor.getKnownSkill(1013)) != null && !actor.isSkillDisabled(entry.getTemplate()))
		/*技能ID1013 回復術*/
		{
			LinkedList<Player> list = new LinkedList<Player>();
			for(Player member : party)
			{
				if(!config.getPartyMpHeal().containsKey(member.getObjectId()) || member.isDead() || member == actor || !(member.getDistance(actor) <= 4000.0) || !(member.getCurrentMpPercents() <= config.getPartyMpHeal().get(member.getObjectId()).intValue()) || !BotThinkTask.checkSkillMpCost(actor, entry) || !entry.checkCondition(actor, member, false, false, true, false, false))
					continue;
				list.add(member);
			}
			if(!list.isEmpty())
			{
				Player target = null;
				if(list.size() == 1)
				{
					target = list.get(0);
				}
				else
				{
					switch(BotHeal.$SWITCH_TABLE$botscript$MpHealOrder()[botConfigImp.getMpHealOrder().ordinal()])
					{
						case 2:
						{
							list.sort(new Comparator<Player>()
							{

								@Override
								public int compare(Player o1, Player o2)
								{
									return Double.valueOf(o1.getCurrentMpPercents()).compareTo(o2.getCurrentMpPercents());
								}
							});
							target = list.get(0);
							break;
						}
						case 1:
						{
							int index = botConfigImp.getMpHealIndex();
							if(index >= list.size())
							{
								index = 0;
							}
							target = list.get(index);
							botConfigImp.setMpHealIndex(++index);
							break;
						}
						case 3:
						{
							target = Rnd.get(list);
						}
					}
				}
				actor.setTarget(target);
				Creature castTarget = entry.getTemplate().getAimingTarget(actor, target);
				actor.getAI().Cast(entry, castTarget, false, false);
				return true;
			}
		}
		return false;
	}

	boolean checkHealSkill(Playable target, SkillEntry entry)
	{
		if(entry.getTemplate().getAbnormalTime() > 0)
		{
			if(entry.getSkillType() != Skill.SkillType.HOT)
			{
				return true;
			}
			return !target.getAbnormalList().contains(entry.getTemplate().getAbnormalType());
		}
		return true;
	}

	static /* synthetic */ int[] $SWITCH_TABLE$botscript$MpHealOrder()
	{
		if($SWITCH_TABLE$botscript$MpHealOrder != null)
		{
			//int[] arrn;
			return $SWITCH_TABLE$botscript$MpHealOrder;
		}
		int[] arrn = new int[MpHealOrder.values().length];
		try
		{
			arrn[MpHealOrder.\u4f24\u52bf\u4f18\u5148.ordinal()] = 2;
			/*\u4f24\u52bf\u4f18\u5148 伤势优先*/
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[MpHealOrder.\u96e8\u9732\u5747\u6cbe.ordinal()] = 1;
			/*\u96e8\u9732\u5747\u6cbe 雨露均沾*/
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[MpHealOrder.\u9760\u8138\u5403\u996d.ordinal()] = 3;
			/*\u9760\u8138\u5403\u996d 靠脸吃饭*/
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		$SWITCH_TABLE$botscript$MpHealOrder = arrn;
		return $SWITCH_TABLE$botscript$MpHealOrder;
	}
}