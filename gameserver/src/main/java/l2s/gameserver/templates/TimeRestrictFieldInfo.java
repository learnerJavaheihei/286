package l2s.gameserver.templates;

import l2s.gameserver.geometry.Location;

/**
 * @author nexvill
**/
public class TimeRestrictFieldInfo
{
	private final int _fieldId, _itemId, _resetCycle, _minLevel, _maxLevel, _remainTimeBase;
	private final int _remainTimeMax, _enterX, _enterY, _enterZ, _exitX, _exitY, _exitZ;
	private final long _itemCount;

	public TimeRestrictFieldInfo(int fieldId, int itemId, long itemCount, int resetCycle, int minLevel, int maxLevel, int remainTimeBase, int remainTimeMax, int enterX, int enterY, int enterZ, int exitX, int exitY, int exitZ)
	{
		_fieldId = fieldId;
		_itemId = itemId;
		_itemCount = itemCount;
		_resetCycle = resetCycle;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_remainTimeBase = remainTimeBase;
		_remainTimeMax = remainTimeMax;
		_enterX = enterX;
		_enterY = enterY;
		_enterZ = enterZ;
		_exitX = exitX;
		_exitY = exitY;
		_exitZ = exitZ;
	}

	public int getFieldId()
	{
		return _fieldId;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public long getItemCount()
	{
		return _itemCount;
	}
	
	public int getResetCycle()
	{
		return _resetCycle;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public int getRemainTimeBase()
	{
		return _remainTimeBase;
	}
	
	public int getRemainTimeMax()
	{
		return _remainTimeMax;
	}
	
	public Location getEnterLoc()
	{
		Location loc = new Location(_enterX, _enterY, _enterZ);
		return loc;
	}
	
	public Location getExitLoc()
	{
		Location loc = new Location(_exitX, _exitY, _exitZ);
		return loc;
	}
}