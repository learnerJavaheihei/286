package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectPetrification extends EffectHandler
{
	public EffectPetrification(EffectTemplate template)
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
		effected.getFlags().getDebuffImmunity().start(this);
		effected.getFlags().getBuffImmunity().start(this);
		if(effected != effector)
		{
			effected.abortAttack(true, true);
			effected.abortCast(true, true);
		}
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getParalyzed().stop(this);
		effected.getFlags().getDebuffImmunity().stop(this);
		effected.getFlags().getBuffImmunity().stop(this);
	}
}