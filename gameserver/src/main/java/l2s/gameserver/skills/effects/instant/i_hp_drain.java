package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_hp_drain extends i_abstract_effect
{
	private final double _absorbPercent;

	public i_hp_drain(EffectTemplate template)
	{
		super(template);
		_absorbPercent = getParams().getDouble("absorb_percent", 0.);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(getValue() <= 0)
			return false;
		if(!effected.isPlayable() && Config.DISABLE_VAMPIRIC_VS_MOB_ON_PVP)
			return false;
		if(effector.getPvpFlag() != 0)
			return false;
		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final Creature realTarget = reflected ? effector : effected;

		if(realTarget.isDead())
			return;

		final double targetHp = realTarget.getCurrentHp();
		final double targetCP = realTarget.getCurrentCp();

		double damage = 0.;
		if(getSkill().isMagic())
		{
			AttackInfo info = Formulas.calcMagicDam(effector, realTarget, getSkill(), getValue(), getSkill().isSSPossible(), true);
			realTarget.reduceCurrentHp(info.damage, effector, getSkill(), true, true, false, true, false, false, true, true, info.crit, info.miss, info.shld, info.elementalDamage, info.elementalCrit);
			if(info.damage >= 1)
			{
				double lethalDmg = Formulas.calcLethalDamage(effector, realTarget, getSkill());
				if(lethalDmg > 0)
					realTarget.reduceCurrentHp(lethalDmg, effector, getSkill(), true, true, false, false, false, false, false);
			}
			damage = info.damage;
		}
		else
		{
			AttackInfo info = Formulas.calcSkillPDamage(effector, realTarget, getSkill(), getValue(), false, getSkill().isSSPossible());
			if(info != null)
			{
				realTarget.reduceCurrentHp(info.damage, effector, getSkill(), true, true, false, true, false, false, true, true, info.crit || info.blow, info.miss, info.shld, info.elementalDamage, info.elementalCrit);
				if(!info.miss || info.damage >= 1)
				{
					double lethalDmg = Formulas.calcLethalDamage(effector, realTarget, getSkill());
					if(lethalDmg > 0)
						realTarget.reduceCurrentHp(lethalDmg, effector, getSkill(), true, true, false, false, false, false, false);
					else if(!reflected)
						realTarget.doCounterAttack(getSkill(), effector, false);
				}
				damage = info.damage;
			}
		}

		if(_absorbPercent > 0 && !effector.isHealBlocked())
		{
			double hp = 0.;

			// Нельзя восстанавливать HP из CP
			if(damage > targetCP || !realTarget.isPlayer())
				hp = (damage - targetCP) * (_absorbPercent / 100);

			// Нельзя восстановить больше hp, чем есть у цели.
			if(hp > targetHp)
				hp = targetHp;

			double addToHp = Math.max(0, Math.min(hp, effector.getStat().calc(Stats.HP_LIMIT, null, null) * effector.getMaxHp() / 100. - effector.getCurrentHp()));
			if(addToHp > 0)
				effector.setCurrentHp(effector.getCurrentHp() + addToHp, false);
		}
	}	
}