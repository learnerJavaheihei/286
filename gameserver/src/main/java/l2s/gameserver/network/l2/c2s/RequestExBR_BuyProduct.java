package l2s.gameserver.network.l2.c2s;

import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ProductHistoryItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExBR_BuyProductPacket;
import l2s.gameserver.network.l2.s2c.ExBR_GamePointPacket;
import l2s.gameserver.network.l2.s2c.ExBR_NewIConCashBtnWnd;
import l2s.gameserver.network.l2.s2c.ReciveVipInfo;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;

public class RequestExBR_BuyProduct extends L2GameClientPacket
{
	private int _productId;
	private int _count;

	@Override
	protected boolean readImpl()
	{
		_productId = readD();
		_count = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return;

		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_count > 100 || _count <= 0)
			return;

		ProductItem product = ProductDataHolder.getInstance().getProduct(_productId);
		if(product == null)
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
			return;
		}

		if(!product.isOnSale() || (System.currentTimeMillis() < product.getStartTimeSale()) || (System.currentTimeMillis() > product.getEndTimeSale()))
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_SALE_PERIOD_ENDED);
			return;
		}

		activeChar.getProductHistoryList().writeLock();
		try
		{
			if(product.getLimit() >= 0)
			{
				ProductHistoryItem productHistoryItem = activeChar.getProductHistoryList().get(product.getId());
				if(productHistoryItem != null)
					_count = Math.min(_count, product.getLimit() - productHistoryItem.getPurchasedCount());
				else
					_count = Math.min(_count, product.getLimit());

				if(_count <= 0)
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_ITEM_LIMITED);
					return;
				}
			}

			final int pointsRequired = product.getPrice() * _count;
			if(pointsRequired <= 0 && product.getLimit() == -1) // Лимитированные вещи можно выдавать бесплатно.
			{
				activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
				return;
			}

			activeChar.getInventory().writeLock();
			try
			{
				if(pointsRequired > activeChar.getPremiumPoints())
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
					return;
				}

				int totalWeight = 0;
				for(ProductItemComponent com : product.getComponents())
					totalWeight += com.getWeight();

				totalWeight *= _count; //увеличиваем вес согласно количеству

				int totalCount = 0;

				for(ProductItemComponent com : product.getComponents())
				{
					ItemTemplate item = ItemHolder.getInstance().getTemplate(com.getId());
					if(item == null)
					{
						activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
						return; //what
					}
					totalCount += item.isStackable() ? 1 : com.getCount() * _count;
				}

				if(!activeChar.getInventory().validateCapacity(totalCount) || !activeChar.getInventory().validateWeight(totalWeight))
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_INVENTORY_FULL);
					return;
				}

				if(pointsRequired > 0 && !activeChar.reducePremiumPoints(pointsRequired))
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
					return;
				}

				activeChar.getVIP().addPoints((int) (pointsRequired * activeChar.getVIP().getPointsRefillPercent() / 100.));

				activeChar.getProductHistoryList().onPurchaseProduct(product, _count);

				activeChar.sendPacket(new ExBR_NewIConCashBtnWnd(activeChar));

				for(ProductItemComponent $comp : product.getComponents())
				{
					List<ItemInstance> items = ItemFunctions.addItem(activeChar, $comp.getId(), $comp.getCount() * _count, true);
					for(ItemInstance item : items)
						Log.LogItem(activeChar, Log.ItemMallBuy, item);
				}

				activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_OK);
				activeChar.sendPacket(new ReciveVipInfo(activeChar));
			}
			finally
			{
				activeChar.getInventory().writeUnlock();
			}
		}
		finally
		{
			activeChar.getProductHistoryList().writeUnlock();
		}
	}
}