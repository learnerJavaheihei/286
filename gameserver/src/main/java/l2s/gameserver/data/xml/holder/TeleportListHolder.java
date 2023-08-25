package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.TeleportInfo;

import java.util.HashMap;
import java.util.Map;

public final class TeleportListHolder extends AbstractHolder
{
	private static final TeleportListHolder _instance = new TeleportListHolder();

	private final Map<Integer, TeleportInfo> _teleportsInfos = new HashMap<>();

	public static TeleportListHolder getInstance()
	{
		return _instance;
	}

	public void addTeleportInfo(TeleportInfo info)
	{
		_teleportsInfos.put(info.getId(), info);
	}

	public TeleportInfo getTeleportInfo(int id)
	{
		return _teleportsInfos.get(id);
	}

	@Override
	public int size()
	{
		return _teleportsInfos.size();
	}

	@Override
	public void clear()
	{
		_teleportsInfos.clear();
	}
}
