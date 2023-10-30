package instances;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.ai.OnAiEventListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExSendUIEventPacket;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.WalkerRoute;
import l2s.gameserver.templates.npc.WalkerRoutePoint;
import l2s.gameserver.templates.npc.WalkerRouteType;
import l2s.gameserver.utils.ChatUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.NpcUtils;

import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
 */
public class ClanArena extends Reflection
{
	public class RaidListeners implements OnDeathListener, OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			if(!actor.isNpc())
				return;

			if(actor.getCurrentHpPercents() > 50)
				return;

			if(actor == _spawnedRaidBossMain)
			{
				if(_spawnedRaidBossFake != null)
				{
					_spawnedRaidBossFake.deleteMe();
					_spawnedRaidBossFake = null;
				}

				NpcInstance npc = (NpcInstance) actor;
				if(npc.getParameter("said_50per_hp_msg", false))
					return;

				npc.setParameter("said_50per_hp_msg", true);

				//TODO: Уточнить фразы и условия.
				int phraseId = 0;
				if(npc.getNpcId() >= 25794 && npc.getNpcId() <= 25813)
					phraseId = npc.getNpcId() - 25794 + 1000406;
				/*TODO: else if(npc.getNpcId() >= 25834 && npc.getNpcId() <= 25838)
					phraseId = raidNpc.getNpcId() - 25834 + ;*/

				if(phraseId > 0)
				{
					NpcString phrase = NpcString.valueOf(phraseId);
					if(phrase != null) {
						if(phrase.getSize() > 0) {
							String targetName = "";
							GameObject target = actor.getTarget();
							if(target != null)
								targetName = target.getName();
							else if(attacker != null)
								targetName = attacker.getName();
							else {
								for(Creature creature : actor.getAroundCharacters(1000, 300)) {
									targetName = creature.getName();
									break;
								}
							}
							ChatUtils.say(npc, phrase, targetName);
						}
						else
							ChatUtils.say(npc, phrase);
					}
				}
			}
			else if(actor == _spawnedRaidBossFake)
			{
				actor.startAbnormalEffect(AbnormalEffect.BIG_HEAD); // TODO: Должен кидаться бафф?
			}
		}

		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			actor.removeListener(_raidListeners);

			if(actor == _spawnedRaidBossMain)
			{
				if(_currentStage > _clan.getArenaStage())
				{
					_clan.setArenaStage(_currentStage);
					_clan.updateClanInDB();
				}
				_stagesPassed++;
				startSupplies();
			}
		}
	}

	private class AiEventListener implements OnAiEventListener
	{
		private final int _doorId;

		public AiEventListener(int doorId)
		{
			_doorId = doorId;
		}

		@Override
		public void onAiEvent(Creature actor, CtrlEvent evt, Object[] args)
		{
			if(evt == CtrlEvent.EVT_FINISH_WALKER_ROUTE || (evt == CtrlEvent.EVT_ARRIVED || evt == CtrlEvent.EVT_ARRIVED_TARGET) && actor.isInZone(ARENA_ZONE_NAME))
			{
				actor.removeListener(_raidAiEventListener1);
				actor.removeListener(_raidAiEventListener2);
				getDoor(_doorId).closeMe();
			}
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(ClanArena.class);

	public static final int NONE_STATE = 0;
	public static final int RAID_STATE = 1;
	public static final int SUPPLIES_STATE = 2;
	public static final int FINISH_STATE = 3;

	private static final WalkerRoute RAID_BOSS_WALKER_ROUTE_1 = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	private static final WalkerRoute RAID_BOSS_WALKER_ROUTE_2 = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	static
	{
		RAID_BOSS_WALKER_ROUTE_1.addPoint(new WalkerRoutePoint(new Location(12696, 183688, -3712), new NpcString[0], -1, 0, true, false));
		RAID_BOSS_WALKER_ROUTE_2.addPoint(new WalkerRoutePoint(new Location(12376, 183688, -3712), new NpcString[0], -1, 0, true, false));
	}

	private static final Location RAID_SPAWN_LOC_1 = new Location(13352, 183688, -3680, 32000);
	private static final Location RAID_SPAWN_LOC_2 = new Location(11720, 183688, -3680, 0);

	private static final String ARENA_ZONE_NAME = "[clan_arena]";

	private static final int ARENA_MACHINE_NPC_ID = 30203;

	private final RaidListeners _raidListeners = new RaidListeners();
	private final OnAiEventListener _raidAiEventListener1 = new AiEventListener(20230201);
	private final OnAiEventListener _raidAiEventListener2 = new AiEventListener(20230202);

	private final IntSet _rewardedPlayers = new HashIntSet();

	private final AtomicInteger _state = new AtomicInteger(NONE_STATE);

	// Arena Params
	private int[] _raidBossesIds;
	private int _totalTimeLimit;
	private int _takeSuppliesTimeLimit;
	private int _timeExtendCount;
	private long _timeExtendPrice;
	private int _stagesСount;

	private Clan _clan = null;
	private int _currentStage = 1;
	private int _stagesPassed = 0;
	private int _extendedTimes = 0;
	private int _totalLeftTime = 0;
	private int _suppliesElapsedTime = 0;
	private NpcInstance _spawnedRaidBossMain = null;
	private NpcInstance _spawnedRaidBossFake = null;
	private ScheduledFuture<?> _progressTask = null;

	@Override
	protected void onCreate()
	{
		StatsSet params = getInstancedZone().getAddParams();

		_raidBossesIds = params.getIntegerArray("raid_bosses_ids");
		_totalTimeLimit = params.getInteger("total_time_limit");
		_takeSuppliesTimeLimit = params.getInteger("take_supplies_time_limit");
		_timeExtendCount = params.getInteger("time_extend_count");
		_timeExtendPrice = params.getLong("time_extend_price");
		_stagesСount = params.getInteger("stages_count");

		super.onCreate();
	}

	@Override
	protected void onCollapse()
	{
		stopProgressTask();
		super.onCollapse();
	}

	@Override
	public void onAddObject(GameObject object)
	{
		super.onAddObject(object);

		if(object.isPlayer())
		{
			//
		}
	}

	@Override
	public void onRemoveObject(GameObject object)
	{
		super.onRemoveObject(object);

		if(object.isPlayer())
		{
			removeTimer(object.getPlayer());
		}
	}

	public boolean isStarted()
	{
		return _state.get() > NONE_STATE;
	}

	public int getTimeExtendCount()
	{
		return _timeExtendCount;
	}

	public int getExtendedTimes()
	{
		return _extendedTimes;
	}

	public boolean startArena(Clan clan, boolean previous)
	{
		if(clan == null)
			return false;

		if(_raidBossesIds.length == 0)
			return false;

		_clan = clan;
		_currentStage = clan.getArenaStage();

		// Если мы на последнем уровне, то откатываемся на один уровень назад
		_currentStage = Math.min(_currentStage, _raidBossesIds.length - 1);

		if(!previous)
			_currentStage++;

		_currentStage = Math.min(_currentStage, _raidBossesIds.length);
		_totalLeftTime = (int) TimeUnit.MINUTES.toSeconds(_totalTimeLimit);

		startProgressTask();
		startRaid();
		return true;
	}

	public boolean extendTime(NpcInstance npc, Player player)
	{
		if(_extendedTimes >= _timeExtendCount)
			return false;

		if(_state.get() != RAID_STATE && _state.get() != SUPPLIES_STATE)
			return false;

		if(!player.reduceAdena(_timeExtendPrice, true))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return false;
		}

		boolean error = Rnd.chance(10);
		int seconds = error ? 300 : Rnd.get(60, 180);
		_totalLeftTime += seconds;
		_extendedTimes++;

		if(error)
			ChatUtils.say(npc, NpcString.TIME_EXTENSION_ERROR);

		ExShowScreenMessage msg = new ExShowScreenMessage(NpcString.S1_HAS_EXTENDED_THE_DURATION_FOR_S2_SEC, 5000, ScreenMessageAlign.TOP_CENTER, true, true, player.getName(), String.valueOf(seconds));
		for(Player p : getPlayers())
			p.sendPacket(msg);

		return true;
	}

	public boolean rewardSupplies(Player player)
	{
		if(_state.get() != SUPPLIES_STATE && _state.get() != FINISH_STATE)
			return false;

		if(_rewardedPlayers.contains(player.getObjectId()))
			return false;

		_rewardedPlayers.add(player.getObjectId());

		// TODO: Вынести ID наград в настройки инстанса

		int battleBoxItemId = 70920;
		if(_currentStage >= 1 && _currentStage <= 5)
			battleBoxItemId = 70917;
		else if(_currentStage >= 6 && _currentStage <= 10)
			battleBoxItemId = 70918;
		else if(_currentStage >= 11 && _currentStage <= 15)
			battleBoxItemId = 70919;

		ItemFunctions.addItem(player, battleBoxItemId, _currentStage > 20 ? 2 : 1);

		int rnd = Rnd.get(100);//血盟狩猎奖励设置
		if(rnd < 50)
			ItemFunctions.addItem(player, 90945, Rnd.get(1, 3));
		else if(rnd < 70)
			ItemFunctions.addItem(player, 90946, Rnd.get(1, 3));
		else if(rnd < 85)
			ItemFunctions.addItem(player, 90947, Rnd.get(1, 3));
		else if(rnd < 95)
			ItemFunctions.addItem(player, 35741, Rnd.get(1, 3));
		else
			ItemFunctions.addItem(player, 49758, Rnd.get(1, 3));

		return true;
	}

	private void startRaid()
	{
		if(_state.compareAndSet(NONE_STATE, RAID_STATE))
		{
			// TODO: Уточнить момент установки отката зоны.
			setReenterTime(System.currentTimeMillis(), false);

			for(NpcInstance npc : getNpcs(true, ARENA_MACHINE_NPC_ID))
				ChatUtils.say(npc, NpcString.THE_COUNTDOWN_IS_ON_THE_RAID_BEGINS_TO_EXTEND_THE_DURATION_YOU_CAN_CLICK_THE_EXTEND_DURATION_BUTTON);
		}
		else if(_state.compareAndSet(SUPPLIES_STATE, RAID_STATE))
		{
			despawnByGroup("clan_arena_suplies");

			_suppliesElapsedTime = 0;
			_rewardedPlayers.clear();
			_currentStage++;
			_currentStage = Math.min(_currentStage, _raidBossesIds.length);

			for(NpcInstance npc : getNpcs(true, ARENA_MACHINE_NPC_ID))
				ChatUtils.say(npc, NpcString.THE_COUNTDOWN_IS_ON_THE_RAID_BEGINS);
		}
		else
			return;

		int raidNpcId = _raidBossesIds[_currentStage - 1];
		boolean spawnFake = (_currentStage % 5) == 0;
		boolean shuffle = spawnFake && Rnd.chance(50);

		getDoor(20230201).openMe();

		if(spawnFake)
			getDoor(20230202).openMe();

		_spawnedRaidBossMain = spawnRaid(raidNpcId, shuffle);
		_spawnedRaidBossFake = spawnFake ? spawnRaid(raidNpcId, !shuffle) : null;
	}

	private NpcInstance spawnRaid(int npcId, boolean shuffle)
	{
		NpcInstance raidNpc = NpcUtils.spawnSingle(npcId, shuffle ? RAID_SPAWN_LOC_2 : RAID_SPAWN_LOC_1, this);
		if(raidNpc == null)
		{
			_log.warn(getClass().getSimpleName() + ": cannot spawn the Raid Boss ID[" + npcId + "]!");
			clearReflection(1, true);
			return null;
		}

		raidNpc.addListener(_raidListeners);
		raidNpc.addListener(shuffle ? _raidAiEventListener2 : _raidAiEventListener1);
		raidNpc.getAI().setWalkerRoute(shuffle ? RAID_BOSS_WALKER_ROUTE_2 : RAID_BOSS_WALKER_ROUTE_1);

		//TODO: Уточнить фразы.
		int phraseId = 0;
		if(raidNpc.getNpcId() >= 25794 && raidNpc.getNpcId() <= 25813)
			phraseId = raidNpc.getNpcId() - 25794 + 1803643;
		else if(raidNpc.getNpcId() >= 25834 && raidNpc.getNpcId() <= 25838)
			phraseId = raidNpc.getNpcId() - 25834 + 1803692;

		if(phraseId > 0)
		{
			NpcString phrase = NpcString.valueOf(phraseId);
			if(phrase != null) {
				if(phrase.getSize() > 0) {
					String targetName = "";
					GameObject target = raidNpc.getTarget();
					if(target != null)
						targetName = target.getName();
					else {
						for(Creature creature : raidNpc.getAroundCharacters(1000, 300)) {
							targetName = creature.getName();
							break;
						}
					}
					ChatUtils.say(raidNpc, phrase, targetName);
				} else
					ChatUtils.say(raidNpc, phrase);
			}
		}
		return raidNpc;
	}

	private void startSupplies()
	{
		if(!_state.compareAndSet(RAID_STATE, SUPPLIES_STATE))
			return;

		if(_stagesPassed >= _stagesСount)
		{
			stopArena();
			spawnByGroup("clan_arena_suplies");
			return;
		}

		spawnByGroup("clan_arena_suplies");

		for(NpcInstance npc : getNpcs(true, ARENA_MACHINE_NPC_ID))
			ChatUtils.say(npc, NpcString.THE_COUNTDOWN_TIMER_IS_PAUSED_FOR_1_MIN_EVERY_CHARACTER_MAY_RECEIVE_SUPPLIES_ONCE_PER_STAGE);
	}

	private void stopArena()
	{
		if(_state.getAndSet(FINISH_STATE) == FINISH_STATE)
			return;

		stopProgressTask();
		removeTimer();
		clearReflection(1, true);
	}

	private void startProgressTask()
	{
		if(_progressTask != null)
		{
			_log.warn(getClass().getSimpleName() + ": cannot start the progress task twice!");
			return;
		}

		_progressTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
		{
			showTimer();
			switch(_state.get())
			{
				case RAID_STATE:
					if(_totalLeftTime == 0)
					{
						stopArena();
						return;
					}
					_totalLeftTime--;
					break;
				case SUPPLIES_STATE:
					if(_suppliesElapsedTime >= TimeUnit.MINUTES.toSeconds(_takeSuppliesTimeLimit))
					{
						startRaid();
						return;
					}
					_suppliesElapsedTime++;
					break;
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	private void stopProgressTask()
	{
		if(_progressTask != null)
		{
			_progressTask.cancel(false);
			_progressTask = null;
		}
	}

	private void showTimer()
	{
		for(Player player : getPlayers())
			showTimer(player);
	}

	private void showTimer(Player player)
	{
		switch(_state.get())
		{
			case RAID_STATE:
			case SUPPLIES_STATE:
				player.sendPacket(new ExSendUIEventPacket(player, 4, 0, _totalLeftTime, 0, 0, 0, NpcString.REMAINING_TIME));
				break;
		}
	}

	private void removeTimer()
	{
		for(Player player : getPlayers())
			removeTimer(player);
	}

	private void removeTimer(Player player)
	{
		player.sendPacket(new ExSendUIEventPacket(player, 1, 0, 0, 0));
	}
}