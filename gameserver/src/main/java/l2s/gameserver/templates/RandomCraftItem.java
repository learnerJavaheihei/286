package l2s.gameserver.templates;

/**
 * @author nexvill
 */
public class RandomCraftItem
{
	private final int _itemId;
	private final int _resultId;
	private final long _count;
	private final byte _enchantLevel;
	private final double _chance;

	public RandomCraftItem(int itemId, int resultId, long count, byte enchantLevel, double chance)
	{
		_itemId = itemId;
		_resultId = resultId;
		_count = count;
		_enchantLevel = enchantLevel;
		_chance = chance;
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
	
	public double getChance()
	{
		return _chance;
	}
}
