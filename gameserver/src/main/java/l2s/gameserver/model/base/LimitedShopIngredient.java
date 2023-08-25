package l2s.gameserver.model.base;

import l2s.gameserver.data.xml.holder.ItemHolder;

/**
 * @author nexvill
 */
public class LimitedShopIngredient implements Cloneable
{
	private int _itemId;
	private long _itemCount;

	public LimitedShopIngredient(int itemId, long itemCount)
	{
		_itemId = itemId;
		_itemCount = itemCount;
	}

	@Override
	public LimitedShopIngredient clone()
	{
		LimitedShopIngredient mi = new LimitedShopIngredient(_itemId, _itemCount);
		return mi;
	}

	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public void setItemCount(long itemCount)
	{
		_itemCount = itemCount;
	}

	public long getItemCount()
	{
		return _itemCount;
	}

	public boolean isStackable()
	{
		return _itemId <= 0 || ItemHolder.getInstance().getTemplate(_itemId).isStackable();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (_itemCount ^ _itemCount >>> 32);
		result = prime * result + _itemId;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		LimitedShopIngredient other = (LimitedShopIngredient) obj;
		if(_itemId != other._itemId)
			return false;
		if(_itemCount != other._itemCount)
			return false;
		return true;
	}
}