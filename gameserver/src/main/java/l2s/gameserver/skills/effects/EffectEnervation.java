package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectEnervation extends EffectHandler
{
	public EffectEnervation(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isNpc())
			((NpcInstance) effected).setParameter("DebuffIntention", 0.5);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isNpc())
			((NpcInstance) effected).setParameter("DebuffIntention", 1.);
	}
}