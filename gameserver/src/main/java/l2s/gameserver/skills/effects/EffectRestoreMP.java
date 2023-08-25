package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 **/
public class EffectRestoreMP extends EffectRestore
{
	public EffectRestoreMP(EffectTemplate template)
	{
		super(template);
	}

	private int calcAddToMp(Creature effector, Creature effected)
	{
		double power = getValue();
		if(power <= 0)
			return 0;

		if(_percent)
			power = effected.getMaxMp() / 100. * power;

		if(getSkill().isHandler())
		{
			if(!_staticPower)
			{
				if(!_ignoreBonuses)
				{
					power += effector.getStat().getAdd(Stats.POTION_MP_HEAL_EFFECT, effected, getSkill());
					power *= effected.getStat().getMul(Stats.POTION_MP_HEAL_EFFECT, effector, getSkill());
				}
			}
		}
		else
		{
			if(!_staticPower)
			{
				if(!_percent)
				{
					//TODO: Check formulas.
					if(getSkill().isSSPossible() && Config.MANAHEAL_SPS_BONUS)
						power *= 1 + ((200 + effector.getChargedSpiritshotPower()) * 0.001);
				}

				if(!_ignoreBonuses)
				{
					if(_percent || effector != effected) // TODO: Check this:
						power *= effected.getStat().calc(Stats.MANAHEAL_EFFECTIVNESS, 100., effector, getSkill()) / 100.;
				}
				else if(!_percent)
				{
					// TODO: Check this:
					power *= 1.7;
				}

				if(!_percent && getSkill().getTargetType() != SkillTargetType.TARGET_SELF)
				{
					if(getSkill().getMagicLevel() > 0)
					{
						if(effected.getLevel() > getSkill().getMagicLevel())
						{
							int lvlDiff = effected.getLevel() - getSkill().getMagicLevel();
							// if target is too high compared to skill level, the amount of recharged mp gradually decreases.
							if(lvlDiff == 6)
								power *= 0.9; // only 90% effective
							else if(lvlDiff == 7)
								power *= 0.8; // 80%
							else if(lvlDiff == 8)
								power *= 0.7; // 70%
							else if(lvlDiff == 9)
								power *= 0.6; // 60%
							else if(lvlDiff == 10)
								power *= 0.5; // 50%
							else if(lvlDiff == 11)
								power *= 0.4; // 40%
							else if(lvlDiff == 12)
								power *= 0.3; // 30%
							else if(lvlDiff == 13)
								power *= 0.2; // 20%
							else if(lvlDiff == 14)
								power *= 0.1; // 10%
							else if(lvlDiff >= 15)
								power = 0; // 0mp recharged
						}
					}
				}
			}
		}
		return (int) power;
	}

	private int checkRestoreMpLimits(Creature effected, double power)
	{
		int newMp = (int) (effected.getCurrentMp() + power);
		newMp = Math.max(0, Math.min(newMp, (int) (effected.getMaxMp() / 100. * effected.getStat().calc(Stats.MP_LIMIT, null, null))));
		newMp = Math.max(0, newMp - (int) effected.getCurrentMp());
		newMp = Math.min(effected.getMaxMp() - (int) effected.getCurrentMp(), newMp);
		return newMp;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isHealBlocked())
			return;

		if(!getTemplate().isInstant())
			return;

		int addToMp = calcAddToMp(effector, effected);
		if(addToMp > 0)
		{
			addToMp = checkRestoreMpLimits(effected, addToMp);
			if(effector != effected)
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1).addName(effector).addInteger(addToMp));
			else
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(addToMp));

			effected.setCurrentMp(effected.getCurrentMp() + addToMp, false);
			StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_MP);
			effector.sendPacket(su);
			effected.sendPacket(su);
			effected.broadcastStatusUpdate();
			effected.sendChanges();
		}
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(getTemplate().isInstant())
			return false;

		if(effected.isHealBlocked())
			return true;

		int addToMp = calcAddToMp(effector, effected);
		if(addToMp > 0)
		{
			addToMp = checkRestoreMpLimits(effected, addToMp);
			effected.setCurrentMp(effected.getCurrentMp() + addToMp, false);
			StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_MP);
			effector.sendPacket(su);
			effected.sendPacket(su);
			effected.broadcastStatusUpdate();
			effected.sendChanges();
		}
		return true;
	}
}