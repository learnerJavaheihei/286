package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.templates.item.product.ProductItem;

/**
 * @author Bonux
**/
public class ProductHistoryItem
{
	private final ProductItem _product;

	private int _purchasedCount;
	private int _lastPurchaseTime;

	public ProductHistoryItem(ProductItem product, int purchasedCount, int lastPurchaseTime)
	{
		_product = product;
		_purchasedCount = purchasedCount;
		_lastPurchaseTime = lastPurchaseTime;
	}

	public ProductItem getProduct()
	{
		return _product;
	}

	public int getPurchasedCount()
	{
		return _purchasedCount;
	}

	public void setPurchasedCount(int value)
	{
		_purchasedCount = value;
	}

	public int getLastPurchaseTime()
	{
		return _lastPurchaseTime;
	}

	public void setLastPurchaseTime(int value)
	{
		_lastPurchaseTime = value;
	}

	public boolean isExpended()
	{
		return getProduct().getLimit() <= getPurchasedCount();
	}

	@Override
	public String toString()
	{
		return "ProductHistoryItem[product ID=" + _product.getId() + ", purchased count=" + _purchasedCount + ", last purchase time=" + _lastPurchaseTime + "]";
	}
}