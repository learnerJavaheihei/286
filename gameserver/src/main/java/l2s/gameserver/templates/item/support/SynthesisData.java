package l2s.gameserver.templates.item.support;

import l2s.gameserver.templates.item.data.ItemData;

public class SynthesisData
{
	private final int _item1Id;
	private final int _item2Id;
	private final double _chance;
	private final ItemData _successItemData;
	private final ItemData _failItemData;
	private final int _resultEffecttype;
	private final int[] _locationIds;

	public SynthesisData(int item1Id, int item2Id, double chance, ItemData successItemData, ItemData failItemData, int resultEffecttype, int[] locationIds)
	{
		_item1Id = item1Id;
		_item2Id = item2Id;
		_chance = chance;
		_successItemData = successItemData;
		_failItemData = failItemData;
		_resultEffecttype = resultEffecttype;
		_locationIds = locationIds;
	}

	public int getItem1Id()
	{
		return _item1Id;
	}

	public int getItem2Id()
	{
		return _item2Id;
	}

	public double getChance()
	{
		return _chance;
	}

	public ItemData getSuccessItemData()
	{
		return _successItemData;
	}

	public ItemData getFailItemData()
	{
		return _failItemData;
	}

	public int getResultEffecttype() {
		return _resultEffecttype;
	}

	public int[] getLocationIds() {
		return _locationIds;
	}
}
