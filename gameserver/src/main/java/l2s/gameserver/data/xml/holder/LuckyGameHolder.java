package l2s.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.luckygame.LuckyGameData;
import l2s.gameserver.templates.luckygame.LuckyGameType;

/**
 * @author Bonux
**/
public final class LuckyGameHolder extends AbstractHolder
{
	private static final LuckyGameHolder _instance = new LuckyGameHolder();

	private final Map<LuckyGameType, LuckyGameData> _data = new HashMap<LuckyGameType, LuckyGameData>();

	public static LuckyGameHolder getInstance()
	{
		return _instance;
	}

	public void addData(LuckyGameData data)
	{
		if(_data.containsKey(data.getType()))
		{
			warn("Conflict while parsing lucky game data! Dublicate game data by type: " + data.getType());
			return;
		}
		if(data.getCommonRewards().isEmpty())
			warn("Lucky game dont have common rewards type: " + data.getType());
		_data.put(data.getType(), data);
	}

	public LuckyGameData getData(LuckyGameType type)
	{
		return _data.get(type);
	}

	@Override
	public int size()
	{
		return _data.size();
	}

	@Override
	public void clear()
	{
		_data.clear();
	}
}
