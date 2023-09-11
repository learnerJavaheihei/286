package manager;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.annotation.OnScriptInit;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.ReflectionUtils;
import l2s.gameserver.utils.TimeUtils;
import npc.model.SepulcherNpcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ reworked by Bonux
 */
public class FourSepulchersManager implements OnDeathListener
{
	private static class ProcessEventTask implements Runnable
	{
		@Override
		public void run() {
			if (STATE.compareAndSet(NONE_STATE, ENTRY_STATE)) {
				long changeWarmUpTime = startTime + TimeUnit.MINUTES.toMillis(3);
				ThreadPoolManager.getInstance().execute(new ManagerSay(0));
				eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), changeWarmUpTime - System.currentTimeMillis());
			} else if (STATE.compareAndSet(ENTRY_STATE, WARM_UP_STATE)) {
				long changeAttackTime = startTime + TimeUnit.MINUTES.toMillis(5);
				eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), changeAttackTime - System.currentTimeMillis());
			} else if (STATE.compareAndSet(WARM_UP_STATE, BATTLE_STATE)) {
				FourSepulchersSpawn.locationShadowSpawns();

				FourSepulchersSpawn.spawnMysteriousBox(31921);
				FourSepulchersSpawn.spawnMysteriousBox(31922);
				FourSepulchersSpawn.spawnMysteriousBox(31923);
				FourSepulchersSpawn.spawnMysteriousBox(31924);

				int durationMin = 5;

				long nextSayTime = startTime + TimeUnit.MINUTES.toMillis(10);
				while (System.currentTimeMillis() > nextSayTime) {
					durationMin += 5;
					nextSayTime += TimeUnit.MINUTES.toMillis(5);
				}
				managerSayTask = ThreadPoolManager.getInstance().schedule(new ManagerSay(durationMin), nextSayTime - System.currentTimeMillis());

				long attackEndTime = startTime + TimeUnit.MINUTES.toMillis(EVENT_DURATION + 5);
				eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), attackEndTime - System.currentTimeMillis());
			} else if (STATE.compareAndSet(BATTLE_STATE, NONE_STATE)) {
				cleanUp();

				startTime = START_TIME_PATTERN.next(System.currentTimeMillis());

				LOGGER.info("FourSepulchersManager: Entry time: " + TimeUtils.toSimpleFormat(startTime));

				long interval = startTime - System.currentTimeMillis();
				eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), interval);
			}
		}
	}

	private static class ManagerSay implements Runnable
	{
		private final int min;

		public ManagerSay(int min) {
			this.min = min;
		}

		@Override
		public void run()
		{
			if(isAttackTime())
			{
				if(min + 5 < 50)
				{
					managerSay(min); //byte because minute cannot be more than 59
					managerSayTask = ThreadPoolManager.getInstance().schedule(new ManagerSay(min + 5), TimeUnit.MINUTES.toMillis(5));
				}
				// attack time ending chat
				else
					managerSay(90); //sending a unique id :D
			}
			else if(isEntryTime())
				managerSay(0);
		}
	}

	public static final Logger LOGGER = LoggerFactory.getLogger(FourSepulchersManager.class);

	private static Zone[] _zone = new Zone[4];

	public static final int ENTRANCE_PASS = 91406;
	public static final int CHAPEL_KEY = 7260;

	private static final Location EXIT_LOC = new Location(178296, -85256, -7218);

	private static final SchedulingPattern START_TIME_PATTERN = new SchedulingPattern("55 * * * *");
	private static final int EVENT_DURATION = 50; // In minutes

	private static final int NONE_STATE = 0;
	private static final int ENTRY_STATE = 1;
	private static final int WARM_UP_STATE = 2;
	private static final int BATTLE_STATE = 3;

	private static final AtomicInteger STATE = new AtomicInteger(NONE_STATE);

	private static ScheduledFuture<?> eventProgressTask = null;
	private static ScheduledFuture<?> managerSayTask = null;

	private static long startTime = 0L;

	public void init()
	{
		CharListenerList.addGlobal(this);

		_zone[0] = ReflectionUtils.getZone("[four_sepulchers_1]");
		_zone[1] = ReflectionUtils.getZone("[four_sepulchers_2]");
		_zone[2] = ReflectionUtils.getZone("[four_sepulchers_3]");
		_zone[3] = ReflectionUtils.getZone("[four_sepulchers_4]");

		FourSepulchersSpawn.init();

		timeSelector();
	}

	// phase select on server launch
	private static void timeSelector()
	{
		startTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365);

		long currentTime = System.currentTimeMillis();
		long entryTimeEnd;
		long warmUpTimeEnd;
		long attackTimeEnd;
		do {
			startTime = START_TIME_PATTERN.next(startTime);

			entryTimeEnd = startTime + TimeUnit.MINUTES.toMillis(3);
			warmUpTimeEnd = entryTimeEnd + TimeUnit.MINUTES.toMillis(2);
			attackTimeEnd = warmUpTimeEnd + TimeUnit.MINUTES.toMillis(EVENT_DURATION);
		} while(entryTimeEnd < currentTime && warmUpTimeEnd < currentTime && attackTimeEnd < currentTime);

		// if current time >= time of entry beginning and if current time < time of entry beginning + time of entry end
		if(currentTime >= startTime && currentTime < entryTimeEnd) // entry time check
		{
			cleanUp();
			eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Entry time");
		}
		else if(currentTime >= entryTimeEnd && currentTime < warmUpTimeEnd) // warmup time check
		{
			cleanUp();
			STATE.set(ENTRY_STATE);
			eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in WarmUp time");
		}
		else if(currentTime >= warmUpTimeEnd && currentTime < attackTimeEnd) // attack time check
		{
			cleanUp();
			STATE.set(WARM_UP_STATE);
			eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Attack time");
		}
		else
		// else cooldown time and without cleanup because it's already implemented
		{
			STATE.set(BATTLE_STATE);
			eventProgressTask = ThreadPoolManager.getInstance().schedule(new ProcessEventTask(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Cooldown time");
		}
	}

	private static void cleanUp()
	{
		for(Player player : getPlayersInside())
			player.teleToClosestTown();

		FourSepulchersSpawn.deleteAllMobs();

		FourSepulchersSpawn.closeAllDoors();

		FourSepulchersSpawn.HALL_IN_USE.clear();
		FourSepulchersSpawn.HALL_IN_USE.put(31921, false);
		FourSepulchersSpawn.HALL_IN_USE.put(31922, false);
		FourSepulchersSpawn.HALL_IN_USE.put(31923, false);
		FourSepulchersSpawn.HALL_IN_USE.put(31924, false);

		if(eventProgressTask != null) {
			eventProgressTask.cancel(false);
			eventProgressTask = null;
		}

		if(managerSayTask != null) {
			managerSayTask.cancel(false);
			managerSayTask = null;
		}
	}

	public static boolean isEntryTime()
	{
		return STATE.get() == ENTRY_STATE || STATE.get() == WARM_UP_STATE;
	}

	public static boolean isAttackTime()
	{
		return STATE.get() == BATTLE_STATE;
	}

	public static void entry(int npcId, Player player, boolean unstable)
	{
		Location loc = FourSepulchersSpawn.START_HALL_SPAWNS.get(npcId);

		Party party = player.getParty();
		if(party != null) {
			for (Player member : party.getPartyMembers())
				onEntry(member, loc);
		}
		else if(player.isGM())
			onEntry(player, loc);
		else
			return;

		FourSepulchersSpawn.HALL_IN_USE.put(npcId, true);
	}

	private static void onEntry(Player player, Location loc) {
		player.teleToLocation(Location.findPointToStay(player, loc, 0, 80));
		ItemFunctions.deleteItem(player, ENTRANCE_PASS, 1);
		ItemFunctions.deleteItemsEverywhere(player, CHAPEL_KEY);
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && self.getZ() >= -7250 && self.getZ() <= -6841 && checkIfInZone(self))
			checkAnnihilated((Player) self);
	}

	public static void checkAnnihilated(final Player player)
	{
		if(isPlayersAnnihilated()) {
			ThreadPoolManager.getInstance().schedule(() -> {
				Party party = player.getParty();
				if (party != null) {
					for (Player member : party.getPartyMembers()) {
						if (member.isDead())
							exitPlayer(member);
					}
				}
				else
					exitPlayer(player);
			}, 5000);
		}
	}

	public static void exitPlayer(Player player) {
		player.teleToLocation(Location.findPointToStay(EXIT_LOC, 100, ReflectionManager.MAIN.getGeoIndex()), ReflectionManager.MAIN);
	}

	private static int minuteSelect(int min)
	{
		switch(min % 5)
		{
			case 0:
				return min;
			case 1:
				return min - 1;
			case 2:
				return min - 2;
			case 3:
				return min + 2;
			default:
				return min + 1;
		}
	}

	public static void managerSay(int min)
	{
		// for attack phase, sending message every 5 minutes
		if(isAttackTime())
		{
			if(min < 5) // do not shout when < 5 minutes
				return;

			min = minuteSelect(min);

			for(Spawner spawner : SpawnManager.getInstance().getSpawners(FourSepulchersSpawn.MANAGERS_SPAWN_GROUP)) {
				for(NpcInstance npc : spawner.getAllSpawned()) {
					// hall not used right now, so its manager will not tell you anything :)
					// if you don't need this - delete next two lines.
					Boolean inUse = FourSepulchersSpawn.HALL_IN_USE.get(npc.getNpcId());
					if (inUse == null || !inUse)
						continue;

					if (npc instanceof SepulcherNpcInstance) {
						if (min == 90)
							((SepulcherNpcInstance) npc).sayInShout(NpcString.GAME_OVER);
						else
							((SepulcherNpcInstance) npc).sayInShout(NpcString.MINUTES_HAVE_PASSED, String.valueOf(min));
					}
				}
			}
		}
		else if(isEntryTime())
		{
			for(Spawner spawner : SpawnManager.getInstance().getSpawners(FourSepulchersSpawn.MANAGERS_SPAWN_GROUP)) {
				for(NpcInstance npc : spawner.getAllSpawned()) {
					if (npc instanceof SepulcherNpcInstance) {
						((SepulcherNpcInstance) npc).sayInShout(NpcString.YOU_MAY_NOW_ENTER_THE_SEPULCHER);
						((SepulcherNpcInstance) npc).sayInShout(NpcString.IF_YOU_PLACE_YOUR_HAND_ON_THE_STONE_STATUE_IN_FRONT_OF_EACH_SEPULCHER_YOU_WILL_BE_ABLE_TO_ENTER);
					}
				}
			}
		}
	}

	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
			if(!pc.isDead())
				return false;
		return true;
	}

	private static List<Player> getPlayersInside()
	{
		List<Player> result = new ArrayList<Player>();
		for(Zone zone : getZones())
			result.addAll(zone.getInsidePlayers());
		return result;
	}

	private static boolean checkIfInZone(Creature cha)
	{
		for(Zone zone : getZones())
			if(zone.checkIfInZone(cha))
				return true;
		return false;
	}

	public static Zone[] getZones()
	{
		return _zone;
	}

	@OnScriptInit
	public void onInit()
	{
		init();
	}
}