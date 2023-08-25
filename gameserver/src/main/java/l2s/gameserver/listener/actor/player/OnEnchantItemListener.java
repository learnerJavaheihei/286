package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

/**
 * @author Bonux
**/
public interface OnEnchantItemListener extends PlayerListener
{
	public void onEnchantItem(Player player, ItemInstance item, boolean success);
}
