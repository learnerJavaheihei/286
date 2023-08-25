package l2s.gameserver.templates.elemental;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author Bonux
**/
public class ElementalLevelData implements Comparable<ElementalLevelData>
{
	private final int _level;
	private final int _attack;
	private final int _defence;
	private final int _critRate;
	private final int _critAttack;
	private final long _exp;

	private ItemData _extractItem = null;
	private final List<ItemData> _extractCost = new ArrayList<ItemData>();

	public ElementalLevelData(int level, int attack, int defence, int critRate, int critAttack, long exp)
	{
		_level = level;
		_attack = attack;
		_defence = defence;
		_critRate = critRate;
		_critAttack = critAttack;
		_exp = exp;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getAttack()
	{
		return _attack;
	}

	public int getDefence()
	{
		return _defence;
	}

	public int getCritRate()
	{
		return _critRate;
	}

	public int getCritAttack()
	{
		return _critAttack;
	}

	public long getExp()
	{
		return _exp;
	}

	public void setExtractItem(ItemData item)
	{
		_extractItem = item;
	}

	public ItemData getExtractItem()
	{
		return _extractItem;
	}

	public void addExtractCost(ItemData item)
	{
		_extractCost.add(item);
	}

	public List<ItemData> getExtractCost()
	{
		return _extractCost;
	}

	@Override
	public int compareTo(ElementalLevelData o)
	{
		return Integer.compare(getLevel(), o.getLevel());
	}
}