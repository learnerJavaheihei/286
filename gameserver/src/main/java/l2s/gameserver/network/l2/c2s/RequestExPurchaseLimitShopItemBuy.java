package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.LimitedShopHolder;
import l2s.gameserver.model.LimitedShopContainer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.LimitedShopEntry;
import l2s.gameserver.model.base.LimitedShopIngredient;
import l2s.gameserver.model.base.LimitedShopProduction;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.ExPurchaseLimitShopItemBuy;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * @author nexvill
 */
public class RequestExPurchaseLimitShopItemBuy extends L2GameClientPacket
{
	private int _listId;
	private int _itemIndex;
	private int _itemCount;
	private LimitedShopContainer _list;

	@Override
	protected boolean readImpl()
	{
		_listId = readC();
		_itemIndex = readD();
		_itemCount = readD();
		_list = LimitedShopHolder.getInstance().getList(_listId);
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		int size = _list.getEntries().size();
		for (int i = 0; i < size; i++)
		{
			final LimitedShopEntry entry = _list.getEntries().get(i);
			LimitedShopProduction product = entry.getProduction().get(0);
			if (product.getInfo().getInteger("index") == _itemIndex)
			{
				for (LimitedShopIngredient ingredient : entry.getIngredients())
				{
					if (!checkIngredients(activeChar, activeChar.getInventory(), activeChar.getClan(), ingredient.getItemId(), ingredient.getItemCount() * _itemCount))
					{
						LimitedShopHolder.getInstance().SeparateAndSend(_listId, activeChar);
						return;
					}
				}
				break;
			}
		}
		
		activeChar.sendPacket(new ExPurchaseLimitShopItemBuy(activeChar, _listId, _itemIndex, _itemCount));
	}
	
	private boolean checkIngredients(Player player, PcInventory inventory, Clan clan, int ingredientId, long totalCount)
	{
		if (ingredientId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
		{
			if (clan == null)
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_A_CLAN_MEMBER));
				return false;
			}
			else if (!player.isClanLeader())
			{
				player.sendPacket(new SystemMessage(SystemMessage.ONLY_THE_CLAN_LEADER_IS_ENABLED));
				return false;
			}
			else if (clan.getReputationScore() < totalCount)
			{
				player.sendPacket(new SystemMessage(SystemMessage.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
				return false;
			}
			return true;
		}
		else if (ingredientId == ItemTemplate.ITEM_ID_FAME)
		{
			if (player.getFame() < totalCount)
			{
				player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_FAME_POINTS));
				return false;
			}
			return true;
		}
		else if (ingredientId == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
		{
			if (player.getPcBangPoints() < totalCount)
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS));
				return false;
			}
			return true;
		}
		else if (inventory.getItemByItemId(ingredientId).getCount() < totalCount)
		{
			final SystemMessage sm = new SystemMessage(SystemMessage._2_UNITS_OF_THE_ITEM_S1_IS_REQUIRED);
			sm.addItemName(ingredientId);
			sm.addNumber(totalCount);
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
}