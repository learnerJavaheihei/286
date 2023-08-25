package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;

/**
 * @reworked by Bonux
**/
public class SimpleSpawner extends Spawner implements Cloneable
{
	private NpcTemplate _npcTemplate;
	private SpawnRange _spawnRange;

	public SimpleSpawner(NpcTemplate mobTemplate)
	{
		if(mobTemplate == null)
			throw new NullPointerException();

		_npcTemplate = mobTemplate;
		_spawned = new ArrayList<NpcInstance>(1);
	}

	public SimpleSpawner(int npcId)
	{
		NpcTemplate mobTemplate = NpcHolder.getInstance().getTemplate(npcId);
		if(mobTemplate == null)
			throw new NullPointerException("Not find npc: " + npcId);

		_npcTemplate = mobTemplate;
		_spawned = new ArrayList<NpcInstance>(1);
	}

	public SpawnRange getSpawnRange()
	{
		return _spawnRange;
	}

	public void setSpawnRange(SpawnRange spawnRange)
	{
		_spawnRange = spawnRange;
	}

	public void restoreAmount()
	{
		_maximumCount = _referenceCount;
	}

	@Override
	public int getMainNpcId()
	{
		return _npcTemplate.getId();
	}

	@Override
	public SpawnRange getRandomSpawnRange()
	{
		return _spawnRange;
	}

	@Override
	public void decreaseCount(NpcInstance oldNpc)
	{
		decreaseCount0(_npcTemplate, oldNpc, oldNpc.getDeathTime());
	}

	@Override
	public NpcInstance doSpawn(boolean spawn)
	{
		return doSpawn0(_npcTemplate, spawn, StatsSet.EMPTY, Collections.emptyList());
	}

	@Override
	protected NpcInstance initNpc(NpcInstance mob, boolean spawn)
	{
		SpawnRange range = getRandomSpawnRange();
		mob.setSpawnRange(range);
		return initNpc0(mob, range.getRandomLoc(getReflection().getGeoIndex(), mob.isFlying()), spawn);
	}

	@Override
	public void respawnNpc(NpcInstance oldNpc)
	{
		oldNpc.refreshID();
		initNpc(oldNpc, true);
	}

	@Override
	public SimpleSpawner clone()
	{
		SimpleSpawner spawnDat = new SimpleSpawner(_npcTemplate);
		spawnDat.setSpawnRange(_spawnRange);
		spawnDat.setAmount(_maximumCount);
		spawnDat.setRespawnDelay(getRespawnDelay(), getRespawnDelayRandom());
		spawnDat.setRespawnPattern(getRespawnPattern());
		return spawnDat;
	}
}