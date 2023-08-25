package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_p_hit extends i_abstract_effect
{
	private final boolean _canCrit;

	public i_p_hit(EffectTemplate template)
	{
		super(template);
		_canCrit = getParams().getBool("can_critical", false);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return !effected.isDead();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		boolean dual = false;
		boolean bow = false;

		switch(effector.getBaseStats().getAttackType())
		{
			case DUAL:
			case DUALFIST:
			case DUALDAGGER:
			case DUALBLUNT:
				dual = true;
				break;
			case BOW:
			case CROSSBOW:
			case TWOHANDCROSSBOW:
				bow = true;
				break;
		}

		double power = getValue();
		if(dual)
			power /= 2;

		int damage = 0;
		boolean shld = false;
		boolean crit = false;
		int elementalDamage = 0;
		boolean elementalCrit = false;

		AttackInfo info = Formulas.calcAutoAttackDamage(effector, effected, power, bow, effector.getChargedSoulshotPower() > 0, _canCrit);
		if(info != null)
		{
			damage = (int) info.damage;
			shld = info.shld;
			crit = info.crit;
			elementalDamage = (int) info.elementalDamage;
			elementalCrit = info.elementalCrit;
		}

		effected.reduceCurrentHp(damage, effector, null, true, true, false, true, false, false, true, true, crit, false, shld, elementalDamage, elementalCrit);

		if(dual)
		{
			damage = 0;
			shld = false;
			crit = false;

			info = Formulas.calcAutoAttackDamage(effector, effected, power, bow, effector.getChargedSoulshotPower() > 0, _canCrit);
			if(info != null)
			{
				damage = (int) info.damage;
				shld = info.shld;
				crit = info.crit;
				elementalDamage = (int) info.elementalDamage;
				elementalCrit = info.elementalCrit;
			}

			effected.reduceCurrentHp(damage, effector, null, true, true, false, true, false, false, true, true, crit, false, shld, elementalDamage, elementalCrit);
		}
	}
}