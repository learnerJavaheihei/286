package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_m_attack extends i_abstract_effect
{
	public i_m_attack(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(effected.isDead())
			return false;
		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final Creature realTarget = reflected ? effector : effected;
		final AttackInfo info = Formulas.calcMagicDam(effector, realTarget, getSkill(), getValue(), getSkill().isSSPossible(), true);

		realTarget.reduceCurrentHp(info.damage, effector, getSkill(), true, true, false, true, false, false, getTemplate().isInstant(), getTemplate().isInstant(), info.crit, info.miss, info.shld, info.elementalDamage, info.elementalCrit);
		if(info.damage >= 1)
		{
			double lethalDmg = Formulas.calcLethalDamage(effector, realTarget, getSkill());
			if(lethalDmg > 0)
				realTarget.reduceCurrentHp(lethalDmg, effector, getSkill(), true, true, false, false, false, false, false);
		}
	}
}