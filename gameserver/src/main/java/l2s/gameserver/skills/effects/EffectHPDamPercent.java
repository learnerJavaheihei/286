package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectHPDamPercent extends EffectHandler
{
	public EffectHPDamPercent(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead() || effected.isRaid())
			return false;
		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead())
			return;

		double newHp = (100. - getValue()) * effected.getMaxHp() / 100.;
		newHp = Math.min(effected.getCurrentHp(), Math.max(0, newHp));
		effected.setCurrentHp(newHp, false);
	}
}