package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

/**
 * @author Bonux
**/
public final class RodListener implements OnEquipListener
{
	private static final RodListener _instance = new RodListener();

	public static RodListener getInstance()
	{
		return _instance;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		return 0;
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!actor.isPlayer())
			return 0;

		if(!item.isEquipable() || slot != Inventory.PAPERDOLL_RHAND)
			return 0;

		if(item.getItemType() != WeaponType.ROD)
			return 0;

		Player player = actor.getPlayer();
		if(player.isFishing())
			player.getFishing().stop();

		return 0;
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return 0;
	}
}