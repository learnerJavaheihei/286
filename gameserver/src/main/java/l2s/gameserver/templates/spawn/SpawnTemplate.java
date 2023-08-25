package l2s.gameserver.templates.spawn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.geometry.Territory;
import org.apache.commons.lang3.StringUtils;

/**
 * @author VISTALL
 * @date  15:22/15.12.2010
 */
public class SpawnTemplate
{
	private String _name;
	private final PeriodOfDay _periodOfDay;
	private final int _count;
	private final int _respawn;
	private final int _respawnRandom;
	private final SchedulingPattern _respawnPattern;

	private final List<SpawnNpcInfo> _npcList = new ArrayList<SpawnNpcInfo>(1);

	private List<SpawnPoint> _spawnPointList = Collections.emptyList();
	private List<Territory> _territoryList = Collections.emptyList();

	public SpawnTemplate(String name, PeriodOfDay periodOfDay, int count, int respawn, int respawnRandom, String respawnPattern)
	{
		_name = name;
		_periodOfDay = periodOfDay;
		_count = count;
		_respawn = respawn;
		_respawnRandom = respawnRandom;
		_respawnPattern = respawnPattern == null || respawnPattern.isEmpty() ? null : new SchedulingPattern(respawnPattern);
	}

	//----------------------------------------------------------------------------------------------------------
	public void addSpawnPoint(SpawnPoint loc)
	{
		if(_spawnPointList.isEmpty()) {
			_spawnPointList = new ArrayList<SpawnPoint>(1);
			if(StringUtils.isEmpty(_name))
				_name = "point: " + loc.getLoc().toXYZString();
		}
		_spawnPointList.add(loc);
	}

	public SpawnPoint getSpawnPoint(int index)
	{
		return _spawnPointList.get(index);
	}

	//----------------------------------------------------------------------------------------------------------
	public void addNpc(SpawnNpcInfo info)
	{
		_npcList.add(info);
	}

	public SpawnNpcInfo getNpcId(int index)
	{
		return _npcList.get(index);
	}

	//----------------------------------------------------------------------------------------------------------
	public void addTerritory(String name, Territory territory)
	{
		if(_territoryList.isEmpty()) {
			_territoryList = new ArrayList<Territory>(1);
			if (StringUtils.isEmpty(_name))
				_name = name;
		}
		_territoryList.add(territory);
	}

	public Territory getTerritory(int index)
	{
		return _territoryList.get(index);
	}

	//----------------------------------------------------------------------------------------------------------

	public List<SpawnNpcInfo> getNpcList()
	{
		return _npcList;
	}

	public List<SpawnPoint> getSpawnPointList()
	{
		return _spawnPointList;
	}

	public List<Territory> getTerritoryList()
	{
		return _territoryList;
	}

	public String getName()
	{
		return _name;
	}

	public int getCount()
	{
		return _count;
	}

	public int getRespawn()
	{
		return _respawn;
	}

	public int getRespawnRandom()
	{
		return _respawnRandom;
	}

	public SchedulingPattern getRespawnPattern()
	{
		return _respawnPattern;
	}

	public PeriodOfDay getPeriodOfDay()
	{
		return _periodOfDay;
	}
}
