package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectCPDamPercent extends EffectHandler
{
	public EffectCPDamPercent(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead() || !effected.isPlayer())
			return false;
		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead())
			return;
		double newCp = (100. - getValue()) * effected.getMaxCp() / 100.;
		newCp = Math.min(effected.getCurrentCp(), Math.max(0, newCp));
		effected.setCurrentCp(newCp);
	}
}