package l2s.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.TimeRestrictFieldInfo;

/**
 * @author nexvill
**/
public class TimeRestrictFieldHolder extends AbstractHolder
{
	private static final TimeRestrictFieldHolder _instance = new TimeRestrictFieldHolder();

	private final Map<Integer, TimeRestrictFieldInfo> _timeRestrictFieldInfos = new HashMap<>();

	public static TimeRestrictFieldHolder getInstance()
	{
		return _instance;
	}
	
	public void addTimeRestrictFieldInfo(TimeRestrictFieldInfo info)
	{
		_timeRestrictFieldInfos.put(info.getFieldId(), info);
	}
	
	public Map<Integer, TimeRestrictFieldInfo> getFields()
	{
		return _timeRestrictFieldInfos;
	}

	@Override
	public int size()
	{
		return _timeRestrictFieldInfos.size();
	}

	@Override
	public void clear()
	{
		_timeRestrictFieldInfos.clear();
	}
}
