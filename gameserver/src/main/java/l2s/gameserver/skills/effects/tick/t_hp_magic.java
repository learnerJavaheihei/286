package l2s.gameserver.skills.effects.tick;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 * its custom, not from PTS
**/
public class t_hp_magic extends EffectHandler
{
	public t_hp_magic(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead())
			return true;

		double hp = getValue() * getInterval(); // В PTS скриптах сила эффекта указывается без учета интервала.
		double damage = Formulas.calcMagicDam(effector, effected, getSkill(), Math.abs(hp), getSkill().isSSPossible(), false).damage;
		damage = Math.min(damage, effected.getCurrentHp() - 1);

		if(getSkill().getAbsorbPart() > 0)
			effector.setCurrentHp(getSkill().getAbsorbPart() * Math.min(effected.getCurrentHp(), damage) + effector.getCurrentHp(), false);

		boolean awake = !effected.isNpc() && effected != effector; // TODO: Check this.
		boolean standUp = effected != effector; // TODO: Check this.
		boolean directHp = effector.isNpc() || effected == effector; // TODO: Check this.
		effected.reduceCurrentHp(damage, effector, getSkill(), awake, standUp, directHp, false, false, true, false);
		return true;
	}
}