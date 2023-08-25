package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * Target is immune to death.
 *
 * @author Yorie
 */
public final class EffectDeathImmunity extends EffectHandler
{
	public EffectDeathImmunity(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getDeathImmunity().start(this);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getDeathImmunity().stop(this);
	}
}