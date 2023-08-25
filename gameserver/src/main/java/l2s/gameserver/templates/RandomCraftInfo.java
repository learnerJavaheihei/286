package l2s.gameserver.templates;

/**
 * @author nexvill
 */
public class RandomCraftInfo
{
	private final int _itemId;
	private final int _resultId;
	private final long _count;
	private final byte _enchantLevel;
	private boolean _isLocked;
	private byte _refreshToUnlock;

	public RandomCraftInfo(int itemId, int resultId, long count, byte enchantLevel, boolean isLocked, byte refreshToUnlock)
	{
		_itemId = itemId;
		_resultId = resultId;
		_count = count;
		_enchantLevel = enchantLevel;
		_isLocked = isLocked;
		_refreshToUnlock = refreshToUnlock;
	}

	public int getId()
	{
		return _itemId;
	}
	
	public int getResultId()
	{
		return _resultId;
	}

	public long getCount()
	{
		return _count;
	}

	public byte getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public void setIsLocked(boolean locked)
	{
		_isLocked = locked;
	}
	
	public boolean isLocked()
	{
		return _isLocked;
	}
	
	public void setRefreshToUnlockCount(byte count)
	{
		_refreshToUnlock = count;
	}
	
	public byte getRefreshToUnlockCount()
	{
		return _refreshToUnlock;
	}
}
