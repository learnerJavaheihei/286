package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

//TODO: [Bonux] Пересмотреть.
public class EffectFlyUp extends EffectHandler
{
	public EffectFlyUp(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isPeaceNpc() || effected.isRaid())
			return false;
		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getFlyUp().start(this);

		effected.abortAttack(true, true);
		effected.abortCast(true, true);
		effected.getMovement().stopMove();

		effected.getAI().notifyEvent(CtrlEvent.EVT_FLY_UP, effected);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.getFlags().getFlyUp().stop(this))
		{
			if(!effected.isPlayer())
				effected.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
}