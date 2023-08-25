package l2s.gameserver.templates.item.data;

/**
 * @author Bonux
 */
public class CapsuledItemData extends RewardItemData
{
	private final int _minEnchantLevel;
	private final int _maxEnchantLevel;

	public CapsuledItemData(int id, long minCount, long maxCount, double chance, int minEnchantLevel, int maxEnchantLevel)
	{
		super(id, minCount, maxCount, chance);
		_minEnchantLevel = minEnchantLevel;
		_maxEnchantLevel = maxEnchantLevel;
	}

	public int getMinEnchantLevel()
	{
		return _minEnchantLevel;
	}
	
	public int getMaxEnchantLevel()
	{
		return _maxEnchantLevel;
	}
}
