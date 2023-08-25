package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class EffectMoveToEffector extends EffectHandler
{
	public EffectMoveToEffector(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isFearImmune()) // TODO: Пересмотреть.
			return false;

		// Нельзя наложить на осадных саммонов
		Player player = effected.getPlayer();
		if(player != null)
		{
			if(effected.isSummon())
			{
				for(SiegeEvent<?, ?> siegeEvent : player.getEvents(SiegeEvent.class))
				{
					if(siegeEvent.containsSiegeSummon((SummonInstance) effected))
						return false;
				}
			}
		}

		if(effected.isInPeaceZone())
			return false;

		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.getFlags().getAfraid().start(this)) // TODO: Пересмотреть.
		{
			effected.abortAttack(true, true);
			effected.abortCast(true, true);
			effected.getMovement().stopMove();
		}

		onActionTime(abnormal, effector, effected);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getAfraid().stop(this); // TODO: Пересмотреть.
		effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getMovement().moveToLocation(effector.getLoc(), 40, true);
		return true;
	}
}