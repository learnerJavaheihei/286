package handler.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.Config;/*PK自动关监狱*/

//By Evil_dnk

public class PkDecrease extends SimpleItemHandler
{
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		switch(itemId)
		{
			//Scroll: PK
			case 35741:
				DecreasPk(player, 1, itemId);
			break;
			case 35752:
				DecreasPk(player, 3, itemId);
			break;
			case 35753:
				DecreasPk(player, 5, itemId);
			break;
			case 35754:
				DecreasPk(player, 10, itemId);
			break;
			case 49025:
				DecreasPk(player, 1, itemId);
				break;
			case 49026:
				DecreasPk(player, 1, itemId);
				break;
			case 49027:
				DecreasPk(player, 1, itemId);
				break;
			case 49028:
				DecreasPk(player, 1, itemId);
				break;
			case 49029:
				DecreasPk(player, 1, itemId);
				break;
			case 49030:
				DecreasPk(player, 1, itemId);
				break;
			default:
				return false;
		}
		return true;
	}

	private void DecreasPk(Player player, int count, int item)
	{
		if(player.getPkKills() > 0)
		{
			boolean jal = Config.PK_MAX_JAILAIl == -1 ? false : (player.isInJail());/*PK自动关监狱*/
			if (player.getPkKills() - count < 0)
				player.setPkKills(0);
			else
				player.setPkKills(player.getPkKills() - count);
			ItemFunctions.deleteItem(player, item, 1);
			/*PK自动关监狱--*/
			if(jal)
			{
				if(player.getPkKills() < Config.PK_MIN_JAILAIl)
				{
					player.fromJail();
					//Announcements.announceToAll("屠夫「" + player.getName() + "」已從監獄釋放。");
				}
				else
				{
					player.sendMessage("PK值低於 " + Config.PK_MIN_JAILAIl + " 點,自動出獄.");
				}
			}
		/*--PK自动关监狱*/
			player.broadcastCharInfo();
		}
		else
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item));
	}
}
