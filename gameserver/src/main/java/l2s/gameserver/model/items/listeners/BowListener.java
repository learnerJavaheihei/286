package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class BowListener implements OnEquipListener
{
	private static final BowListener _instance = new BowListener();

	public static BowListener getInstance()
	{
		return _instance;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable() || slot != Inventory.PAPERDOLL_RHAND)
			return 0;

		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

//		if(item.getItemType() == WeaponType.BOW)
//		{
//			ItemInstance arrow = player.getInventory().findArrowForBow(item.getTemplate());
//			if(arrow != null)
//				//player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, arrow);
//		}
//		if(item.getItemType() == WeaponType.CROSSBOW || item.getItemType() == WeaponType.TWOHANDCROSSBOW)
//		{
//			ItemInstance bolt = player.getInventory().findArrowForCrossbow(item.getTemplate());
//			if(bolt != null)
//				player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, bolt);
//		}
		return 0;
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		return 0;
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return 0;
	}
}