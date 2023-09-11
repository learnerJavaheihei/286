package manager;

import l2s.commons.util.Rnd;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;
import npc.model.SepulcherMonsterInstance;
import npc.model.SepulcherRaidInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FourSepulchersSpawn
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FourSepulchersSpawn.class);

	public static final String MANAGERS_SPAWN_GROUP = "4_sepul_manager";
	public static final String GATEKEEPERS_SPAWN_GROUP = "4_sepul_gatekeeper";
	private static final String SHADOW_SPAWN_GROUP = "4_sepul_shadow_%d_%d";

	private static final int[] SEPULCHERS_DOORS = {
			// Conquerors Sepulcher
			25150012,
			25150013,
			25150014,
			25150015,
			25150016,
			// Sepulcher of Rulers
			25150002,
			25150003,
			25150004,
			25150005,
			25150006,
			// Great Sages Sepulcher
			25150032,
			25150033,
			25150034,
			25150035,
			25150036,
			// Judges Sepulcher
			25150022,
			25150023,
			25150024,
			25150025,
			25150026
	};

	public static final Map<Integer, Location> START_HALL_SPAWNS = new HashMap<>();
	static {
		START_HALL_SPAWNS.put(31921, new Location(181632, -85587, -7218));
		START_HALL_SPAWNS.put(31922, new Location(179963, -88978, -7218));
		START_HALL_SPAWNS.put(31923, new Location(173217, -86132, -7218));
		START_HALL_SPAWNS.put(31924, new Location(175608, -82296, -7218));
	};

	private static final Map<Integer, String> DUKE_MOB_GROUPS = new HashMap<>();
	private static final Map<Integer, String> VISCOUNT_MOB_GROUPS = new HashMap<>();

	public static final List<NpcInstance> SPAWNED_NPCS = new ArrayList<>();
	private static final Set<String> SPAWNED_GROUPS = new HashSet<>();

	private static final List<Integer> SHADOW_SPAWN_ORDER = Arrays.asList(31929, 31934, 31939, 31944);

	public static final Map<Integer, Boolean> HALL_IN_USE = new HashMap<>();
	private static final Map<Integer, Integer> KEY_BOX_NPC = new HashMap<>();
	public static final Map<Integer, Integer> VICTIM = new HashMap<>();

	static {
		HALL_IN_USE.put(31921, false);
		HALL_IN_USE.put(31922, false);
		HALL_IN_USE.put(31923, false);
		HALL_IN_USE.put(31924, false);

		KEY_BOX_NPC.put(18120, 31455);
		KEY_BOX_NPC.put(18121, 31455);
		KEY_BOX_NPC.put(18122, 31455);
		KEY_BOX_NPC.put(18123, 31455);
		KEY_BOX_NPC.put(18124, 31456);
		KEY_BOX_NPC.put(18125, 31456);
		KEY_BOX_NPC.put(18126, 31456);
		KEY_BOX_NPC.put(18127, 31456);
		KEY_BOX_NPC.put(18128, 31457);
		KEY_BOX_NPC.put(18129, 31457);
		KEY_BOX_NPC.put(18130, 31457);
		KEY_BOX_NPC.put(18131, 31457);

		// 2nd room key box
		KEY_BOX_NPC.put(18141, 31458);
		KEY_BOX_NPC.put(18142, 31458);
		KEY_BOX_NPC.put(18143, 31458);
		KEY_BOX_NPC.put(18144, 31458);

		KEY_BOX_NPC.put(18150, 31459);
		KEY_BOX_NPC.put(18151, 31459);
		KEY_BOX_NPC.put(18152, 31459);
		KEY_BOX_NPC.put(18153, 31459);
		KEY_BOX_NPC.put(18154, 31460);
		KEY_BOX_NPC.put(18155, 31460);
		KEY_BOX_NPC.put(18156, 31460);
		KEY_BOX_NPC.put(18157, 31460);
		KEY_BOX_NPC.put(18158, 31461);
		KEY_BOX_NPC.put(18159, 31461);
		KEY_BOX_NPC.put(18160, 31461);
		KEY_BOX_NPC.put(18161, 31461);
		KEY_BOX_NPC.put(18162, 31462);
		KEY_BOX_NPC.put(18163, 31462);
		KEY_BOX_NPC.put(18164, 31462);
		KEY_BOX_NPC.put(18165, 31462);
		KEY_BOX_NPC.put(18183, 31463);
		KEY_BOX_NPC.put(18184, 31464);
		KEY_BOX_NPC.put(18212, 31465);
		KEY_BOX_NPC.put(18213, 31465);
		KEY_BOX_NPC.put(18214, 31465);
		KEY_BOX_NPC.put(18215, 31465);
		KEY_BOX_NPC.put(18216, 31466);
		KEY_BOX_NPC.put(18217, 31466);
		KEY_BOX_NPC.put(18218, 31466);
		KEY_BOX_NPC.put(18219, 31466);

		VICTIM.put(18150, 18158);
		VICTIM.put(18151, 18159);
		VICTIM.put(18152, 18160);
		VICTIM.put(18153, 18161);
		VICTIM.put(18154, 18162);
		VICTIM.put(18155, 18163);
		VICTIM.put(18156, 18164);
		VICTIM.put(18157, 18165);
	}

	public static void init()
	{
		SpawnManager.getInstance().spawn(MANAGERS_SPAWN_GROUP, true);
		SpawnManager.getInstance().spawn(FourSepulchersSpawn.GATEKEEPERS_SPAWN_GROUP, true);
	}

	public static void closeAllDoors() {
		for(int doorId : SEPULCHERS_DOORS) {
			DoorInstance door = ReflectionManager.MAIN.getDoor(doorId);
			if(door != null)
				door.closeMe();
		}
	}

	public static void deleteAllMobs()
	{
		for(String group : SPAWNED_GROUPS)
			SpawnManager.getInstance().despawn(group);

		SPAWNED_GROUPS.clear();

		for(NpcInstance mob : SPAWNED_NPCS)
			mob.deleteMe();

		SPAWNED_NPCS.clear();
	}

	public static void spawnShadow(int parentNpcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		int index = SHADOW_SPAWN_ORDER.indexOf(parentNpcId);
		if(index == -1)
			return;

		String groupName = String.format(SHADOW_SPAWN_GROUP, parentNpcId, index + 1);
		List<Spawner> spawners = SpawnManager.getInstance().spawn(groupName, false);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				if (npc instanceof SepulcherRaidInstance)
					((SepulcherRaidInstance) npc).mysteriousBoxId = parentNpcId;
			}
		}
		SPAWNED_GROUPS.add(groupName);
	}

	public static void locationShadowSpawns()
	{
		Collections.shuffle(SHADOW_SPAWN_ORDER);
	}

	public static void spawnEmperorsGraveNpc(NpcInstance npc, int parentNpcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		SPAWNED_NPCS.add(NpcUtils.spawnSingle(31452, npc.getLoc(), npc.getReflection()));

		String group = String.format("4_sepul_emperor_npc_%d", parentNpcId);
		if(SPAWNED_GROUPS.add(group))
			SpawnManager.getInstance().spawn(group, false);
	}

	public static void spawnArchonOfHalisha(int parentNpcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		String group = String.format("4_sepul_duke_final_monst_%d", parentNpcId);
		if(!SPAWNED_GROUPS.add(group))
			return;

		List<Spawner> spawners = SpawnManager.getInstance().spawn(group, false);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				if(npc instanceof SepulcherMonsterInstance)
					((SepulcherMonsterInstance) npc).mysteriousBoxId = parentNpcId;
			}
		}
	}

	public static void spawnExecutionerOfHalisha(NpcInstance npc)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		SPAWNED_NPCS.add(NpcUtils.spawnSingle(VICTIM.get(npc.getNpcId()), npc.getLoc()));
	}

	public static void spawnKeyBox(NpcInstance npc)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		Integer keyBoxId = KEY_BOX_NPC.get(npc.getNpcId());
		if(keyBoxId == null) {
			LOGGER.warn(FourSepulchersSpawn.class.getSimpleName() + ": Cannot found key box id for NPC ID[" + npc.getNpcId() +"]!");
			return;
		}
		SPAWNED_NPCS.add(NpcUtils.spawnSingle(keyBoxId, npc.getLoc()));
	}

	private static String getNextSpawnGroup(int parentNpcId, String previousGroup) {
		String group = null;
		if(parentNpcId == 31469 || parentNpcId == 31474 || parentNpcId == 31479 || parentNpcId == 31484) {
			String prefix = null;
			if(parentNpcId == 31469)
				prefix = "1st";
			else if(parentNpcId == 31474)
				prefix = "2nd";
			else if(parentNpcId == 31479)
				prefix = "3rd";
			else if(parentNpcId == 31484)
				prefix = "4th";

			if(previousGroup == null) {
				if(Rnd.get(2) > 0) {
					int rnd = Rnd.get(10);
					if(rnd > 7) // 8,9
						group = "4_sepul_%s_room2_type1_b1";
					else if(rnd > 2) // 3,4,5,6,7
						group = "4_sepul_%s_room2_type1_b2";
					else // 0,1,2
						group = "4_sepul_%s_room2_type1_b3";
				} else {
					int rnd = Rnd.get(10);
					if(rnd > 7) // 8,9
						group = "4_sepul_%s_room2_type2_b1";
					else if(rnd > 2) // 3,4,5,6,7
						group = "4_sepul_%s_room2_type2_b2";
					else // 0,1,2
						group = "4_sepul_%s_room2_type2_b3";
				}
			} else {
				if(previousGroup.equalsIgnoreCase(String.format("4_sepul_%s_room2_type1_b1", prefix)))
					group = "4_sepul_%s_room2_type1_b2";
				else if(previousGroup.equalsIgnoreCase(String.format("4_sepul_%s_room2_type1_b2", prefix)))
					group = "4_sepul_%s_room2_type1_b3";
				else if(previousGroup.equalsIgnoreCase(String.format("4_sepul_%s_room2_type1_b3", prefix)))  // Переспавниваем финишную волну, вдруг сундук не появился.
					group = "4_sepul_%s_room2_type1_b3";
			}
			if(!StringUtils.isEmpty(group))
				group = String.format(group, prefix);
		}
		return group;
	}

	public static void spawnMonster(int parentNpcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		String spawnedGroup = VISCOUNT_MOB_GROUPS.remove(parentNpcId);
		if(spawnedGroup != null) {
			if(SPAWNED_GROUPS.remove(spawnedGroup))
				SpawnManager.getInstance().despawn(spawnedGroup);
		}

		String group = getNextSpawnGroup(parentNpcId, spawnedGroup);

		spawnedGroup = DUKE_MOB_GROUPS.remove(parentNpcId);
		if(spawnedGroup != null) {
			if(SPAWNED_GROUPS.remove(spawnedGroup))
				SpawnManager.getInstance().despawn(spawnedGroup);
		}

		if(group == null) {
			group = String.format("4_sepul_%s_monst_%d", Rnd.get(2) == 0 ? "phys" : "magic", parentNpcId);
		}

		if(!SPAWNED_GROUPS.add(group))
			return;

		List<Spawner> spawners = SpawnManager.getInstance().spawn(group, false);
		for(Spawner spawner : spawners) {
			for (NpcInstance npc : spawner.getAllSpawned()) {
				if (npc instanceof SepulcherMonsterInstance)
					((SepulcherMonsterInstance) npc).mysteriousBoxId = parentNpcId;
			}
		}

		switch(parentNpcId)
		{
			case 31469:
			case 31474:
			case 31479:
			case 31484:
				VISCOUNT_MOB_GROUPS.put(parentNpcId, group);
				break;
			case 31472:
			case 31477:
			case 31482:
			case 31487:
				DUKE_MOB_GROUPS.put(parentNpcId, group);
				break;
		}
	}

	public static void spawnMysteriousBox(int parentNpcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		String group = String.format("4_sepul_myst_box_%d", parentNpcId);
		if(SPAWNED_GROUPS.add(group))
			SpawnManager.getInstance().spawn(group, false);
	}

	public static synchronized boolean isDukeMobsAnnihilated(int parentNpcId)
	{
		String group = DUKE_MOB_GROUPS.get(parentNpcId);
		if(group == null)
			return true;

		List<Spawner> spawners = SpawnManager.getInstance().getSpawners(group);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				if (!npc.isDead())
					return false;
			}
		}
		return true;
	}

	public static synchronized boolean isViscountMobsAnnihilated(int parentNpcId)
	{
		String group = VISCOUNT_MOB_GROUPS.get(parentNpcId);
		if(group == null)
			return true;

		List<Spawner> spawners = SpawnManager.getInstance().getSpawners(group);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				if (!npc.isDead())
					return false;
			}
		}
		return true;
	}

	public static boolean isShadowAlive(int id)
	{
		for(int order = 1; order <= SHADOW_SPAWN_ORDER.size(); order++) {
			List<Spawner> spawners = SpawnManager.getInstance().getSpawners(String.format(SHADOW_SPAWN_GROUP, id, order));
			for (Spawner spawner : spawners) {
				for (NpcInstance npc : spawner.getAllSpawned()) {
					if (!npc.isDead())
						return true;
				}
			}
		}
		return false;
	}
}