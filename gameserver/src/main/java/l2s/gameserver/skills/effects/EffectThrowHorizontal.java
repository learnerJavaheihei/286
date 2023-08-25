package l2s.gameserver.skills.effects;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket.FlyType;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class EffectThrowHorizontal extends EffectFlyAbstract
{
	private class EffectThrowHorizontalImpl extends EffectHandler
	{
		private Location _flyLoc = null;

		public EffectThrowHorizontalImpl(EffectTemplate template)
		{
			super(template);
		}

		@Override
		protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
		{
			if(effected.isThrowAndKnockImmune())
			{
				effected.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}

			// Тычок/отброс нельзя наложить на осадных саммонов
			Player player = effected.getPlayer();
			if(player != null)
			{
				if(effected.isSummon())
				{
					for(SiegeEvent<?, ?> siegeEvent : player.getEvents(SiegeEvent.class))
					{
						if(siegeEvent.containsSiegeSummon((SummonInstance) effected))
						{
							effector.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
							return false;
						}
					}
				}
			}

			if(effected.isInPeaceZone())
			{
				effector.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
				return false;
			}

			_flyLoc = effected.getFlyLocation(effector, getSkill());
			if(_flyLoc == null)
				return false;

			return true;
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.abortAttack(true, true);
			effected.abortCast(true, true);
			effected.getMovement().stopMove();
			effected.block();
			effected.broadcastPacket(new FlyToLocationPacket(effected, _flyLoc, FlyType.THROW_HORIZONTAL, getFlySpeed(), getFlyDelay(), getFlyAnimationSpeed()));
			effected.setLoc(_flyLoc);

		}

		@Override
		public void onExit(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.unblock();
		}
	}

	public EffectThrowHorizontal(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public EffectHandler getImpl()
	{
		return new EffectThrowHorizontalImpl(getTemplate());
	}
}