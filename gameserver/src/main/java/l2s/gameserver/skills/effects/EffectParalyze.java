package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectParalyze extends EffectHandler
{
	public EffectParalyze(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return !effected.isParalyzeImmune();
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getParalyzed().start(this);
		effected.abortAttack(true, true);
		effected.abortCast(true, true);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getParalyzed().stop(this);
	}
}