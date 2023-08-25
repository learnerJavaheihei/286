package l2s.gameserver.templates;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nexvill
 */
public class RandomCraftCategory
{
	private final double _probability;
	private final List<RandomCraftItem> _items = new ArrayList<RandomCraftItem>();

	public RandomCraftCategory(double probability)
	{
		_probability = probability;
	}

	public double getProbability()
	{
		return _probability;
	}
	
	public void addItem(RandomCraftItem item)
	{
		_items.add(item);
	}
	
	public RandomCraftItem[] getItems()
	{
		return _items.toArray(new RandomCraftItem[_items.size()]);
	}
}
