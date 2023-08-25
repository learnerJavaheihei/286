package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.util.Rnd;
import l2s.gameserver.geometry.Territory;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.MinionData;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.templates.spawn.SpawnTemplate;


/**
 * Список минионов моба. 
 * @author G1ta0
 * @reworked by Bonux
**/
public class MinionList
{
	private final NpcInstance _master;

	private final Lock lock = new ReentrantLock();
	private final Map<MinionData, MinionSpawner> _minionSpawners = new HashMap<MinionData, MinionSpawner>();

	public MinionList(NpcInstance master)
	{
		_master = master;
	}

	public MinionSpawner addMinion(MinionData minionData)
	{
		lock.lock();
		try
		{
			if(_minionSpawners.containsKey(minionData))
				return null;

			MinionSpawner spawner = new MinionSpawner(minionData, _master);
			_minionSpawners.put(minionData, spawner);
			return spawner;
		}
		finally
		{
			lock.unlock();
		}
	}

	public MinionSpawner addMinion(int minionId, String ai, int minionCount, int respawnTime)
	{
		lock.lock();
		try
		{
			MinionData minionData = new MinionData(minionId, ai, minionCount, respawnTime, null);
			MinionSpawner spawner = new MinionSpawner(minionData, _master);
			_minionSpawners.put(minionData, spawner);
			return spawner;
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean hasMinions()
	{
		lock.lock();
		try
		{
			return _minionSpawners.size() > 0;
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean hasAliveMinions()
	{
		lock.lock();
		try
		{
			for(MinionSpawner spawner : _minionSpawners.values())
			{
				for(NpcInstance m : spawner.getAllSpawned())
				{
					if(m.isVisible() && !m.isDead())
						return true;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
		return false;
	}

	public List<NpcInstance> getAliveMinions()
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();

		lock.lock();
		try
		{
			for(MinionSpawner spawner : _minionSpawners.values())
			{
				for(NpcInstance m : spawner.getAllSpawned())
				{
					if(m.isVisible() && !m.isDead())
						result.add(m);
				}
			}
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	public void spawnMinions()
	{
		lock.lock();
		try
		{
			for(MinionSpawner spawner : _minionSpawners.values())
				spawner.init();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void despawnMinions()
	{
		lock.lock();
		try
		{
			for(MinionSpawner spawner : _minionSpawners.values())
				spawner.deleteAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void onMasterDeath()
	{
		lock.lock();
		try
		{
			if(_master.isRaid())
				despawnMinions();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void onMasterDelete()
	{
		lock.lock();
		try
		{
			despawnMinions();
			_minionSpawners.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void onMinionDelete(NpcInstance minion)
	{
		lock.lock();
		try
		{
			// Начинаем респавн лидера после смерти всех миньонов.
			if(!_master.isVisible() && !hasAliveMinions())
			{
				Spawner spawn = _master.getSpawn();
				if(spawn != null)
					spawn.decreaseCount(_master);
				else
					_master.deleteMe();
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public SpawnRange getMinionSpawnRange(MinionData minionData)
	{
		//TODO: Миньоны должны спавниться по определенному алгоритму:
		// По порядку, полукругом за спиной хозяина, радиус зависит от количества миньонов. Если миньон 1, то он спавниться сбоку (перепроверить на оффе)
		Territory territory = minionData.getTerritory();
		if(territory != null)
			return territory;
		return _master.getRndMinionPosition();
	}
}