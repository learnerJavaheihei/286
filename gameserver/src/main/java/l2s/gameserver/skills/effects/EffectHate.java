package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectHate extends EffectHandler
{
	public EffectHate(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isNpc() && effected.isMonster())
			effected.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, effector, getValue());
		return true;
	}
}