package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 LIO 
 31.01.2016
 */
public class EffectArmorBreaker extends EffectHandler
{
	private class EffectArmorBreakerImpl extends EffectHandler
	{
		private ItemInstance _item;

		public EffectArmorBreakerImpl(EffectTemplate template)
		{
			super(template);
		}

		@Override
		protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
		{
			if(!effected.isPlayer())
				return false;

			if(effected.getPlayer().getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) == null)
				return false;

			return true;
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			_item = effected.getPlayer().getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			effected.getPlayer().getInventory().unEquipItem(_item);
		}

		@Override
		public void onExit(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.getPlayer().getInventory().equipItem(_item);
		}
	}

	public EffectArmorBreaker(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public EffectHandler getImpl()
	{
		return new EffectArmorBreakerImpl(getTemplate());
	}
}