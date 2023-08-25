package l2s.gameserver.skills.effects.consume;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class c_mp extends EffectHandler
{
	public c_mp(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead())
			return false;

		final double consume = getValue() * getInterval();
		if(consume > effected.getCurrentMp())
		{
			effected.sendPacket(SystemMsg.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP);
			return false;
		}

		effected.reduceCurrentMp(consume, null);
		return getSkill().isToggle();
	}
}