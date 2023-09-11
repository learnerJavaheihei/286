package handler.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.blessing.ExOpenBlessOptionScroll;

public class BlessingScroll extends SimpleItemHandler
{
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		switch(itemId)
		{
			case 94184: // Blessing Scroll
			case 94208: // Blessing Scroll (Event)
				player.sendPacket(SystemMsg.SELECT_THE_ITEM_TO_BE_AUGMENTED, new ExOpenBlessOptionScroll(player, item));
				break;
			default:
				return false;
		}

		return true;
	}
}
