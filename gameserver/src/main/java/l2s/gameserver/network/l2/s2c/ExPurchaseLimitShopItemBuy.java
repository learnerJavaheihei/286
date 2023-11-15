package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.LimitedShopHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.base.LimitedShopEntry;
import l2s.gameserver.model.base.LimitedShopIngredient;
import l2s.gameserver.model.base.LimitedShopProduction;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author nexvill
 */
public class ExPurchaseLimitShopItemBuy extends L2GameServerPacket
{
	private final Player _player;
	private final int _listId;
	private final List<LimitedShopEntry> _list;
	private final int _itemIndex;
	private int _itemCount;

	public ExPurchaseLimitShopItemBuy(Player player, int listId, int itemIndex, int itemCount)
	{
		_player = player;
		_list = LimitedShopHolder.getInstance().getList(listId).getEntries();
		_listId = listId;
		_itemIndex = itemIndex;
		_itemCount = itemCount;
	}

	@Override
	protected final void writeImpl()
	{
		final PcInventory inventory = _player.getInventory();
		
		int size = _list.size();
		for (int i = 0; i < size; i++)
		{
			final LimitedShopEntry entry = _list.get(i);
			LimitedShopProduction product = entry.getProduction().get(0);
			if (product.getInfo().getInteger("index") == _itemIndex)
			{
				for (LimitedShopIngredient ingredient : entry.getIngredients())
				{
					Clan clan = _player.getClan();
					
					if (ingredient.getItemId() == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
					{
						if (clan != null)
						{
							clan.incReputation((int) ingredient.getItemCount() * _itemCount, false, "Purchase in Limited Shop");
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_REPUTATION_SCORE);
							sm.addNumber(ingredient.getItemCount() * _itemCount);
							_player.sendPacket(sm);
						}
						else
						{
							return;
						}
					}
					else if (ingredient.getItemId() == ItemTemplate.ITEM_ID_FAME)
					{
						if (_player.getFame() >= (ingredient.getItemCount() * _itemCount))
						{
							int newFame = (int) (_player.getFame() - (ingredient.getItemCount() * _itemCount));
							_player.setFame(newFame, null, true);
							_player.broadcastUserInfo(true);
						}
						else
						{
							return;
						}
					}
					else if (ingredient.getItemId() == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
					{
						if (_player.getPcBangPoints() >= (ingredient.getItemCount() * _itemCount))
						{
							int newPoints = (int) (_player.getPcBangPoints() - (ingredient.getItemCount() * _itemCount));
							_player.setPcBangPoints(newPoints, true);
							_player.sendPacket(new ExPCCafePointInfoPacket(_player, (int) -(ingredient.getItemCount() * _itemCount), 1, 0, 2));
						}
						else
						{
							return;
						}
					}
					else
					{

						int productId = product.getInfo().getInteger("product1Id");

						int remainLimit = 0;
						if (product.getInfo().getInteger("dailyLimit") > 0)
						{
							remainLimit = _player.getVarInt(PlayerVariables.LIMIT_ITEM_REMAIN + "_" + productId, product.getInfo().getInteger("dailyLimit"));

							if (remainLimit <= _itemCount) {
								_itemCount = Math.max(0,remainLimit);
							}
						}

						boolean itemDestroyed = inventory.destroyItemByItemId(ingredient.getItemId(), ingredient.getItemCount()* _itemCount);
						if (!itemDestroyed)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage._2_UNITS_OF_THE_ITEM_S1_IS_REQUIRED);
							sm.addItemName(ingredient.getItemId());
							sm.addNumber(ingredient.getItemCount() * _itemCount);
							_player.sendPacket(sm);
							return;
						}
						_player.sendMessage("购买「"+ItemNameHolder.getInstance().getItemName(_player,productId)+"」成功,花费「"+ItemNameHolder.getInstance().getItemName(_player,ingredient.getItemId())+"」"+(ingredient.getItemCount()* _itemCount)+"个");
					}
				}
				break;
			}
		}
		
		for (int i = 0; i < size; i++)
		{
			final LimitedShopEntry entry = _list.get(i);
			LimitedShopProduction product = entry.getProduction().get(0);
			if (product.getInfo().getInteger("index") == _itemIndex)
			{
				int productId = product.getInfo().getInteger("product1Id");

				int remainLimit = 0;
				if (product.getInfo().getInteger("dailyLimit") > 0)
				{
					remainLimit = _player.getVarInt(PlayerVariables.LIMIT_ITEM_REMAIN + "_" + productId, product.getInfo().getInteger("dailyLimit"));

					if (remainLimit <= _itemCount) {
						_itemCount = Math.max(0,remainLimit);
						remainLimit = 0 ;
					}else
						remainLimit = remainLimit - _itemCount;
					_player.setVar(PlayerVariables.LIMIT_ITEM_REMAIN + "_" + productId, remainLimit,_player.getVarExpireTime(PlayerVariables.LIMIT_ITEM_REMAIN + "_" + productId));
				}
				else
				{
					remainLimit = 999;
				}

				int productsCount = 1;
				if (product.getInfo().getInteger("product2Id") > 0)
					productsCount++;
				if (product.getInfo().getInteger("product3Id") > 0)
					productsCount++;
				if (product.getInfo().getInteger("product4Id") > 0)
					productsCount++;
				if (product.getInfo().getInteger("product5Id") > 0)
					productsCount++;
				
				int resultProductId = 0;
				int resultProductCount = 0;
				byte craftedSlot = 0;
				
				switch(productsCount)
				{
					case 1:
					{
						resultProductId = productId;
						resultProductCount = product.getInfo().getInteger("product1Count") * _itemCount;
						break;
					}
					case 2:
					{
						if (Rnd.get(100) < product.getInfo().getDouble("product1Chance"))
						{
							resultProductId = product.getInfo().getInteger("product1Id");
							resultProductCount = product.getInfo().getInteger("product1Count") * _itemCount;
						}
						else
						{
							resultProductId = product.getInfo().getInteger("product2Id");
							resultProductCount = product.getInfo().getInteger("product2Count") * _itemCount;
							craftedSlot = 1;
						}
						break;
					}
					case 3:
					{
						double chance = Rnd.get(100);
						if (chance < product.getInfo().getDouble("product1Chance"))
						{
							resultProductId = product.getInfo().getInteger("product1Id");
							resultProductCount = product.getInfo().getInteger("product1Count") * _itemCount;
						}
						else if (chance < product.getInfo().getDouble("product2Chance"))
						{
							resultProductId = product.getInfo().getInteger("product2Id");
							resultProductCount = product.getInfo().getInteger("product2Count") * _itemCount;
							craftedSlot = 1;
						}
						else
						{
							resultProductId = product.getInfo().getInteger("product3Id");
							resultProductCount = product.getInfo().getInteger("product3Count") * _itemCount;
							craftedSlot = 2;
						}
						break;
					}
					case 4:
					{
						double chance = Rnd.get(100);
						if (chance < product.getInfo().getDouble("product1Chance"))
						{
							resultProductId = product.getInfo().getInteger("product1Id");
							resultProductCount = product.getInfo().getInteger("product1Count") * _itemCount;
						}
						else if (chance < product.getInfo().getDouble("product2Chance"))
						{
							resultProductId = product.getInfo().getInteger("product2Id");
							resultProductCount = product.getInfo().getInteger("product2Count") * _itemCount;
							craftedSlot = 1;
						}
						else if (chance < product.getInfo().getDouble("product3Chance"))
						{
							resultProductId = product.getInfo().getInteger("product3Id");
							resultProductCount = product.getInfo().getInteger("product3Count") * _itemCount;
							craftedSlot = 2;
						}
						else
						{
							resultProductId = product.getInfo().getInteger("product4Id");
							resultProductCount = product.getInfo().getInteger("product4Count") * _itemCount;
							craftedSlot = 3;
						}
						break;
					}
					case 5:
					{
						double chance = Rnd.get(100);
						if (chance < product.getInfo().getDouble("product1Chance"))
						{
							resultProductId = product.getInfo().getInteger("product1Id");
							resultProductCount = product.getInfo().getInteger("product1Count") * _itemCount;
						}
						else if (chance < product.getInfo().getDouble("product2Chance"))
						{
							resultProductId = product.getInfo().getInteger("product2Id");
							resultProductCount = product.getInfo().getInteger("product2Count") * _itemCount;
							craftedSlot = 1;
						}
						else if (chance < product.getInfo().getDouble("product3Chance"))
						{
							resultProductId = product.getInfo().getInteger("product3Id");
							resultProductCount = product.getInfo().getInteger("product3Count") * _itemCount;
							craftedSlot = 2;
						}
						else if (chance < product.getInfo().getDouble("product4Chance"))
						{
							resultProductId = product.getInfo().getInteger("product4Id");
							resultProductCount = product.getInfo().getInteger("product4Count") * _itemCount;
							craftedSlot = 3;
						}
						else
						{
							resultProductId = product.getInfo().getInteger("product5Id");
							resultProductCount = product.getInfo().getInteger("product5Count") * _itemCount;
							craftedSlot = 4;
						}
						break;
					}
				}
				for (int j = 1; j <=resultProductCount; j++) {
					inventory.addItem(resultProductId, 1);
				}

				

				
				writeC(0);
				writeC(_listId);
				writeD(_itemIndex);
				writeD(1);
				writeC(craftedSlot); // crafted slot
				writeD(resultProductId);
				writeD(resultProductCount);
				writeD(remainLimit); // remain limit
				break;
			}
		}
		_player.sendPacket(new ExBloodyCoinCount(_player));
	}
}
