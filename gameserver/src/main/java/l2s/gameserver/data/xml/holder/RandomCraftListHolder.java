package l2s.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.RandomCraftCategory;

/**
 * @author nexvill
 */
public final class RandomCraftListHolder extends AbstractHolder
{
	private static final RandomCraftListHolder _instance = new RandomCraftListHolder();

	private final Map<Integer, RandomCraftCategory> _randomCraftInfos = new HashMap<>();

	public static RandomCraftListHolder getInstance()
	{
		return _instance;
	}

	public void addRandomCraftCategory(int id, RandomCraftCategory info)
	{
		_randomCraftInfos.put(id, info);
	}
	
	public Map<Integer, RandomCraftCategory> getRandomCraftList()
	{
		return _randomCraftInfos;
	}

	@Override
	public int size()
	{
		return _randomCraftInfos.size();
	}

	@Override
	public void clear()
	{
		_randomCraftInfos.clear();
	}
}
