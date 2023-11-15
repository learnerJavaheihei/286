package l2s.gameserver.network.l2.s2c;

import java.util.Calendar;
import java.util.List;

import l2s.gameserver.model.LimitedShopContainer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.base.LimitedShopEntry;
import l2s.gameserver.model.base.LimitedShopIngredient;
import l2s.gameserver.model.base.LimitedShopProduction;
import l2s.gameserver.utils.LimitShopDailyLimitTask;

/**
 * @author nexvill
 */
public class ExPurchaseLimitShopItemListNew extends L2GameServerPacket
{
	private final Player _player;
	private final int _listId;
	private final List<LimitedShopEntry> _list;
	private final int _size;

	public ExPurchaseLimitShopItemListNew(Player player, LimitedShopContainer list)
	{
		_player = player;
		_list = list.getEntries();
		_listId = list.getListId();
		_size = list.getEntries().size();
	}

	@Override
	protected final void writeImpl()
	{
		//@formatter:off
		writeC(_listId);
		writeD(_size);
		
		for (int i = 0; i < _size; i++)
		{
			final LimitedShopEntry entry = _list.get(i);
			LimitedShopProduction product = entry.getProduction().get(0);
			
			writeD(product.getInfo().getInteger("index")); // item index
			writeD(product.getInfo().getInteger("product1Id"));
			
			int ingredientsSize = entry.getIngredients().size();
			if ((ingredientsSize > 1) || (ingredientsSize == 0))
			{
				return;
			}
			
			for (LimitedShopIngredient ingredient : entry.getIngredients())
			{
				writeD(ingredient.getItemId());
			}
			
			writeQ(0); // unk
			
			for (LimitedShopIngredient ingredient : entry.getIngredients())
			{
				writeQ(ingredient.getItemCount());
			}
			
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			
			writeD(0);
			
			writeH(0);

			if (product.getInfo().getInteger("dailyLimit")>0 && _player.getVar(PlayerVariables.LIMIT_ITEM_REMAIN + "_" + product.getInfo().getInteger("product1Id")) == null){
				// Math.min(Math.max(0,product.getInfo().getInteger("dailyLimit")),1000)  限定数量 最小 0，最大 1000
				_player.setVar(PlayerVariables.LIMIT_ITEM_REMAIN + "_" + product.getInfo().getInteger("product1Id")
						,Math.min(Math.max(0,product.getInfo().getInteger("dailyLimit")),1000)
						, LimitShopDailyLimitTask.calendar.getTimeInMillis());
			}
			int count = _player.getVarInt(PlayerVariables.LIMIT_ITEM_REMAIN + "_" + product.getInfo().getInteger("product1Id"), product.getInfo().getInteger("dailyLimit"));

			writeH(Math.max(0,count));

			writeH(0);
			
			writeD(0); // sale remain sec
			writeD(0); // global limit left
		}
	}
}
