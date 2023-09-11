package handler.items;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
/**
 * @author nexvill
 */
public class Elixir extends SimpleItemHandler
{
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();
		
		if (player.getLevel() < 76)
			return false;
		
		int usedElixirs = player.getVarInt("elixirs_used", 0);
		if (usedElixirs == 5)
			return false;

		if(!reduceItem(player, item))
			return false;

		sendUseMessage(player, item);

		switch(itemId)
		{
			case 94314:
				{
					player.setVar("elixirs_used", ++usedElixirs);
					player.broadcastUserInfo(true);
				}
				break;
			default:
				return false;
		}
		return true;
	}
}