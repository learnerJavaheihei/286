package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public abstract class i_abstract_effect extends EffectHandler
{
	public i_abstract_effect(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected final boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return true;
	}

	@Override
	protected final boolean checkActingCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return true;
	}

	@Override
	public final void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		//
	}

	@Override
	public final boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		return true;
	}

	@Override
	public final void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		//
	}

	@Override
	public abstract void instantUse(Creature effector, Creature effected, boolean reflected);
}