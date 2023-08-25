package l2s.gameserver.templates.elemental;

import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author Bonux
**/
public class ElementalAbsorbItem extends ItemData
{
	private final int _power;

	public ElementalAbsorbItem(int id, long count, int power)
	{
		super(id, count);
		_power = power;
	}

	public int getPower()
	{
		return _power;
	}
}