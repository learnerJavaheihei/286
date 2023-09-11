package npc.model.events;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.dao.AccountVariablesDAO;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class ChristmasHolidaysManagerInstance extends NpcInstance
{
	private static final int GIFT_ITEM_ID = 91237;	// Волшебный Сундук Снегурочки
	private static final long GIFT_ITEM_COUNT = 1;

	public ChristmasHolidaysManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -20171220)
		{
			if(player.getLevel() < 20)
			{
				showChatWindow(player, "events/christmas_holidays/" + getNpcId() + "-low_level.htm", false);
				return;
			}

			if(reply == 1)
			{
				showChatWindow(player, "events/christmas_holidays/" + getNpcId() + "-receive_gift.htm", false);
			}
			else if(reply == 2)
			{
				MultiSellHolder.getInstance().SeparateAndSend(34057, player, 0);
			}
			else if(reply == 3)
			{
				String receiver = AccountVariablesDAO.getInstance().select(player.getAccountName(), "@christmas_holidays_gift");
				if(receiver != null)
				{
					int receiverId = Integer.parseInt(receiver);
					if(receiverId == player.getObjectId())
					{
						if(ItemFunctions.haveItem(player, GIFT_ITEM_ID, 1)) // Вдруг потерял итем, дадим ему же второй раз взять.
						{
							showChatWindow(player, "events/christmas_holidays/" + getNpcId() + "-gift_received.htm", false);
							return;
						}
					}
					else
					{
						player.sendPacket(SystemMsg.THIS_ACCOUNT_HAS_ALREADY_RECEIVED_A_GIFT_THE_GIFT_CAN_ONLY_BE_GIVEN_ONCE_PER_ACCOUNT);
						showChatWindow(player, "events/christmas_holidays/" + getNpcId() + "-gift_given.htm", false);
						return;
					}
				}
				AccountVariablesDAO.getInstance().insert(player.getAccountName(), "@christmas_holidays_gift", player.getObjectId());
				ItemFunctions.addItem(player, GIFT_ITEM_ID, GIFT_ITEM_COUNT, true);
				showChatWindow(player, "events/christmas_holidays/" + getNpcId() + "-gift_given.htm", false);
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "events/christmas_holidays/";
	}
}