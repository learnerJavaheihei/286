package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;

/**
 * @author nexvill
 */
public final class DroppedItemsHolder extends AbstractHolder
{
	private final int _itemId;
	private final int _enchantLevel;
	private final int _count;
	
	public DroppedItemsHolder(int itemId, int enchantLevel, int count)
	{
		_itemId = itemId;
		_enchantLevel = enchantLevel;
		_count = count;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public int getCount()
	{
		return _count;
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{		
	}
}
