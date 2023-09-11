package events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author Bonux
**/
public class LairOfAntharasEvent extends Event
{
	public class MonsterDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if(!isInProgress())
				return;

			if(killer == null || !actor.isMonster())
				return;

			Player player = killer.getPlayer();
			if(player == null)
				return;

			MonsterInstance monster = (MonsterInstance) actor;
			if(monster.getNpcId() == RAID_BOSS_NPC_ID)
				monster.dropItem(player, RAID_REWARD[Rnd.get(RAID_REWARD.length)], 1);
			else
				monster.dropItem(player, MAIN_REWARD[Rnd.get(MAIN_REWARD.length)], 1);
		}
	}

	// NPC's
	protected static final int RAID_BOSS_NPC_ID = 18007;	// Captain of Blood Knights

	private static final int[] MAIN_REWARD = { 
		49762,	// Armor Ingredient Box - A-grade
		49763	// Ornament Ingredient Box - A-grade
	};
	private static final int[] RAID_REWARD = {
		49760,	// Armor Supply Box (A-grade)
		49761	// Ornament Supply Box (A-grade)
	};

	// Event variables
	protected static final String SPAWN_GROUP_OBJECT = "spawn_group";
	private static final String SPAWN_RAID_BOSSES_ACTION = "spawn_raid_bosses";

	private final AtomicBoolean _isInProgress = new AtomicBoolean(false);
	private final SchedulingPattern _startTimePattern;
	private final Calendar _calendar = Calendar.getInstance();
	private final MonsterDeathListener _monsterDeathListener = new MonsterDeathListener();

	protected final Map<NpcInstance, Spawner> _spawnedRaidBosses = new HashMap<NpcInstance, Spawner>();

	public LairOfAntharasEvent(MultiValueSet<String> set)
	{
		super(set);
		_startTimePattern = new SchedulingPattern((set.getString("start_time_pattern")));
	}

	@Override
	public void onAddEvent(GameObject o)
	{
		super.onAddEvent(o);

		if(o.isMonster())
		{
			MonsterInstance monster = (MonsterInstance) o;
			monster.addListener(_monsterDeathListener);
		}
	}

	@Override
	public void onRemoveEvent(GameObject o)
	{
		super.onRemoveEvent(o);

		if(o.isMonster())
		{
			MonsterInstance monster = (MonsterInstance) o;
			monster.removeListener(_monsterDeathListener);
		}
	}

	@Override
	public void startEvent()
	{
		if(!_isInProgress.compareAndSet(false, true))
			return;

		super.startEvent();
	}

	@Override
	public void stopEvent(boolean force)
	{
		if(!_isInProgress.compareAndSet(true, false))
			return;

		super.stopEvent(force);
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		_calendar.setTimeInMillis(_startTimePattern.next(System.currentTimeMillis()));

		registerActions();
	}

	@Override
	public boolean isInProgress()
	{
		return _isInProgress.get();
	}

	@Override
	protected long startTimeMillis()
	{
		return _calendar.getTimeInMillis();
	}

	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(SPAWN_RAID_BOSSES_ACTION))
		{
			if(start)
				spawnRaidBosses();
			else
				despawnRaidBosses();
		}
		else
			super.action(name, start);
	}

	@Override
	public EventType getType()
	{
		return EventType.PVP_EVENT;
	}

	@Override
	public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
	{
		return true;
	}

	protected void spawnRaidBosses()
	{
		List<Spawner> spawners = new ArrayList<Spawner>();

		List<SpawnExObject> spawnExObjects = getObjects(SPAWN_GROUP_OBJECT);
		for(SpawnExObject spawnExObject : spawnExObjects)
		{
			spawners.addAll(spawnExObject.getSpawns());
		}

		Collections.shuffle(spawners);

		int raidCount = Math.min(Rnd.get(3, 4), spawners.size());
		for(int i = 0; i < raidCount; i++)
		{
			Spawner spawner = spawners.get(i);
			spawner.deleteAll();

			Location spawnLoc = spawner.getRandomSpawnRange().getRandomLoc(spawner.getReflection().getGeoIndex(), false);
			NpcInstance raidBoss = NpcUtils.spawnSingle(RAID_BOSS_NPC_ID, spawnLoc, spawner.getReflection());

			_spawnedRaidBosses.put(raidBoss, spawner);
		}
	}

	protected void despawnRaidBosses()
	{
		for(Map.Entry<NpcInstance, Spawner> entry : _spawnedRaidBosses.entrySet())
		{
			NpcInstance raidBoss = entry.getKey();
			raidBoss.deleteMe();

			Spawner spawner = entry.getValue();
			spawner.init();
		}
	}
}
