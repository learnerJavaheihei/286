package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.actor.player.OnExpReceiveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public final class p_get_item_by_exp extends EffectHandler
{
	private class p_get_item_by_exp_impl extends EffectHandler
	{
		private class Listener implements OnExpReceiveListener
		{
			@Override
			public void onExpReceive(Player player, long value, boolean hunting)
			{
				if(hunting)
				{
					_receivedExp += value;

					if(_receivedExp >= _exp)
					{
						_receivedExp = 0L;
						ItemFunctions.addItem(player, _itemId, _itemCount, true);
					}
				}
			}
		}

		private final Listener _listener = new Listener();
		private long _receivedExp = 0L;

		public p_get_item_by_exp_impl(EffectTemplate template)
		{
			super(template);
		}

		@Override
		protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
		{
			return effected.isPlayer();
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.addListener(_listener);
		}

		@Override
		public void onExit(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.removeListener(_listener);
		}
	}

	private final long _exp;
	private final int _itemId;
	private final long _itemCount;

	public p_get_item_by_exp(EffectTemplate template)
	{
		super(template);

		_exp = getTemplate().getParams().getLong("exp");
		_itemId = getTemplate().getParams().getInteger("item_id");
		_itemCount = getTemplate().getParams().getLong("item_count");
	}

	@Override
	public EffectHandler getImpl()
	{
		return new p_get_item_by_exp_impl(getTemplate());
	}
}