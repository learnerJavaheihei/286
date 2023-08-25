package l2s.gameserver.utils;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.npc.WalkerRoute;
import l2s.gameserver.templates.npc.WalkerRoutePoint;
import l2s.gameserver.templates.npc.WalkerRouteType;
import l2s.gameserver.templates.spawn.SpawnRange;

/**
 * @author Bonux
 */
public class NpcUtils
{
	@SuppressWarnings("unchecked")
	public static <T extends NpcInstance> T newInstance(int npcId)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
			throw new NullPointerException("Npc template id : " + npcId + " not found!");

		return (T)template.getNewInstance();
	}

	public static NpcInstance canPassPacket(Player player, L2GameClientPacket packet, Object... arg)
	{
		final NpcInstance npcInstance = player.getLastNpc();
		return (npcInstance != null && player.checkInteractionDistance(npcInstance) && npcInstance.canPassPacket(player, packet.getClass(), arg)) ? npcInstance : null;
	}

	public static NpcInstance createNpc(int npcId)
	{
		return createNpc(npcId, null);
	}

	public static NpcInstance createNpc(int npcId, String title)
	{
		NpcInstance npc = newInstance(npcId);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
		if(title != null)
			npc.setTitle(title);

		return npc;
	}

	public static void spawnNpc(NpcInstance npc, int x, int y, int z)
	{
		spawnNpc(npc, new Location(x, y, z, -1), ReflectionManager.MAIN, 0);
	}

	public static void spawnNpc(NpcInstance npc, int x, int y, int z, long despawnTime)
	{
		spawnNpc(npc, new Location(x, y, z, -1), ReflectionManager.MAIN, despawnTime);
	}

	public static void spawnNpc(NpcInstance npc, int x, int y, int z, int h, long despawnTime)
	{
		spawnNpc(npc, new Location(x, y, z, h), ReflectionManager.MAIN, despawnTime);
	}

	public static void spawnNpc(NpcInstance npc, SpawnRange spawnRange)
	{
		spawnNpc(npc, spawnRange, ReflectionManager.MAIN, 0);
	}

	public static void spawnNpc(NpcInstance npc, SpawnRange spawnRange, long despawnTime)
	{
		spawnNpc(npc, spawnRange, ReflectionManager.MAIN, despawnTime);
	}

	public static void spawnNpc(NpcInstance npc, SpawnRange spawnRange, Reflection reflection)
	{
		spawnNpc(npc, spawnRange, reflection, 0);
	}

	public static void spawnNpc(NpcInstance npc, SpawnRange spawnRange, Reflection reflection, long despawnTime)
	{
		Location loc = spawnRange.getRandomLoc(reflection.getGeoIndex(), npc.isFlying());
		npc.setHeading(loc.h);
		npc.setSpawnedLoc(loc);
		npc.setReflection(reflection);
		npc.spawnMe(npc.getSpawnedLoc());

		if(despawnTime > 0)
			npc.startDeleteTask(despawnTime);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.MAIN, 0, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, Reflection reflection)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), reflection, 0, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.MAIN, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, int h, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, h), ReflectionManager.MAIN, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, int h, Reflection reflection, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, h), reflection, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, SpawnRange spawnRange)
	{
		return spawnSingle(npcId, spawnRange, ReflectionManager.MAIN, 0, null);
	}

	public static NpcInstance spawnSingle(int npcId, SpawnRange spawnRange, long despawnTime)
	{
		return spawnSingle(npcId, spawnRange, ReflectionManager.MAIN, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, SpawnRange spawnRange, Reflection reflection)
	{
		return spawnSingle(npcId, spawnRange, reflection, 0, null);
	}

	public static NpcInstance spawnSingle(int npcId, SpawnRange spawnRange, Reflection reflection, long despawnTime)
	{
		return spawnSingle(npcId, spawnRange, reflection, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, SpawnRange spawnRange, Reflection reflection, long despawnTime, String title)
	{
		NpcInstance npc = createNpc(npcId, title);
		if(npc == null)
			return null;

		spawnNpc(npc, spawnRange, reflection, despawnTime);
		return npc;
	}

	public static WalkerRoute makeWalkerRoute(Location[] path, boolean running, WalkerRouteType type)
	{
		WalkerRoute walkerRoute = new WalkerRoute(IdFactory.getInstance().getNextId(), type);
		for(Location loc : path)
			walkerRoute.addPoint(new WalkerRoutePoint(loc, new NpcString[0], -1, 0, running, false));
		return walkerRoute;
	}

	public static WalkerRoute makeWalkerRoute(Location[] path, boolean running)
	{
		return makeWalkerRoute(path, running, WalkerRouteType.FINISH);
	}

	public static Spawner spawnSimple(int npcId, SpawnRange spawnRange, Reflection reflection, int amount, int respawnDelay, int respawnDelayRandom, SchedulingPattern respawnPattern)
	{
		SimpleSpawner spawner = new SimpleSpawner(npcId);
		spawner.setSpawnRange(spawnRange);
		spawner.setReflection(reflection);
		spawner.setAmount(amount);
		spawner.setRespawnDelay(respawnDelay, respawnDelayRandom);
		spawner.setRespawnPattern(respawnPattern);
		spawner.init();
		return spawner;
	}

	public static Spawner spawnSimple(int npcId, SpawnRange spawnRange, Reflection reflection, int amount, int respawnDelay, int respawnDelayRandom)
	{
		return spawnSimple(npcId, spawnRange, reflection, amount, respawnDelay, respawnDelayRandom, null);
	}

	public static Spawner spawnSimple(int npcId, SpawnRange spawnRange, int amount, int respawnDelay, int respawnDelayRandom)
	{
		return spawnSimple(npcId, spawnRange, ReflectionManager.MAIN, amount, respawnDelay, respawnDelayRandom, null);
	}

	public static Spawner spawnSimple(int npcId, SpawnRange spawnRange, int respawnDelay, int respawnDelayRandom)
	{
		return spawnSimple(npcId, spawnRange, ReflectionManager.MAIN, 1, respawnDelay, respawnDelayRandom, null);
	}

	public static Spawner spawnSimple(int npcId, SpawnRange spawnRange, int amount)
	{
		return spawnSimple(npcId, spawnRange, ReflectionManager.MAIN, amount, 0, 0, null);
	}

	public static Spawner spawnSimple(int npcId, SpawnRange spawnRange)
	{
		return spawnSimple(npcId, spawnRange, ReflectionManager.MAIN, 1, 0, 0, null);
	}
}
