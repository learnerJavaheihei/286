package l2s.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMercenaryCastlewarCastleSiegeHudInfo;
import l2s.gameserver.network.l2.s2c.RelationChangedPacket;
import l2s.gameserver.network.l2.s2c.UIPacket;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntLongMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

/**
 * @author VISTALL
 * @date 15:11/14.02.2011
 */
public abstract class SiegeEvent<R extends Residence, S extends SiegeClanObject> extends Event
{
	protected class SiegeSummonInfo
	{
		private int _skillId;
		private int _ownerObjectId;

		private HardReference<SummonInstance> _summonRef = HardReferences.emptyRef();

		SiegeSummonInfo(SummonInstance summonInstance)
		{
			_skillId = summonInstance.getSkillId();
			_ownerObjectId = summonInstance.getPlayer().getObjectId();
			_summonRef = summonInstance.getRef();
		}

		public int getSkillId()
		{
			return _skillId;
		}

		public int getOwnerObjectId()
		{
			return _ownerObjectId;
		}
	}

	public class DoorDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if(!isInProgress())
				return;

			DoorInstance door = (DoorInstance)actor;
			if(door.getDoorType() == DoorTemplate.DoorType.WALL)
				return;

			broadcastTo(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED, SiegeEvent.ATTACKERS, SiegeEvent.DEFENDERS);
		}
	}

	public static final String HAVE_OWNER = "have_owner";
	public static final String HAVE_OLD_OWNER = "have_old_owner";

	public static final String ATTACKERS = "attackers";
	public static final String DEFENDERS = "defenders";
	public static final String SPECTATORS = "spectators";

	public static final String FROM_RESIDENCE_TO_BASE = "from_residence_to_base";

	public static final String SIEGE_ZONES = "siege_zones";
	public static final String FLAG_ZONES = "flag_zones";

	public static final String DAY_OF_WEEK = "day_of_week";
	public static final String HOUR_OF_DAY = "hour_of_day";
	public static final String SIEGE_INTERVAL_IN_WEEKS = "siege_interval_in_weeks";

	public static final String REGISTRATION = "registration";

	public static final String DOORS = "doors";

	// states
	public static final int PROGRESS_STATE		=	1 << 0;
	public static final int REGISTRATION_STATE	=	1 << 1;
	// block fame time
	public static final long BLOCK_FAME_TIME	=	5 * 60 * 1000L;

	public static final long DAY_IN_MILISECONDS = 86400000L;

	protected R _residence;

	private int _state;

	protected Clan _oldOwner;

	protected OnKillListener _killListener;
	protected OnDeathListener _doorDeathListener = new DoorDeathListener();
	protected IntObjectMap<SiegeSummonInfo> _siegeSummons = new CHashIntObjectMap<SiegeSummonInfo>();
	protected IntLongMap _blockedFameOnKill = new CHashIntLongMap();

	protected final SchedulingPattern _startTimePattern;
	protected final Calendar _validationDate;

	public SiegeEvent(MultiValueSet<String> set)
	{
		super(set);

		String startTime = set.getString("start_time", null);
		if(!StringUtils.isEmpty(startTime))
			_startTimePattern = new SchedulingPattern(startTime);
		else
			_startTimePattern = null;

		int[] validationTimeArray = set.getIntegerArray("validation_date", new int[] {2,4,2003});
		_validationDate = Calendar.getInstance();
		_validationDate.set(Calendar.DAY_OF_MONTH, validationTimeArray[0]);
		_validationDate.set(Calendar.MONTH, validationTimeArray[1] - 1);
		_validationDate.set(Calendar.YEAR, validationTimeArray[2]);
		_validationDate.set(Calendar.HOUR_OF_DAY, 0);
		_validationDate.set(Calendar.MINUTE, 0);
		_validationDate.set(Calendar.SECOND, 0);
		_validationDate.set(Calendar.MILLISECOND, 0);
	}

	public long generateSiegeDateTime(SchedulingPattern pattern)
	{
		if(pattern == null)
			return 0;

		final long currentTime = System.currentTimeMillis();
		long time = pattern.next(_validationDate.getTimeInMillis());
		while(time < currentTime)
		{
			time = pattern.next(time);
		}
		return time;
	}

	//========================================================================================================================================================================
	//                                                                   Start / Stop Siege
	//========================================================================================================================================================================

	@Override
	public void startEvent()
	{
		addState(PROGRESS_STATE);
		for (Player player : GameObjectsStorage.getPlayers(false, false))
		{
			player.sendPacket(new ExMercenaryCastlewarCastleSiegeHudInfo(getResidence().getSiegeEvent()));
		}

		super.startEvent();
	}

	@Override
	public void stopEvent(boolean force)
	{
		removeState(PROGRESS_STATE);
		
		for (Player player : GameObjectsStorage.getPlayers(false, false))
		{
			player.sendPacket(new ExMercenaryCastlewarCastleSiegeHudInfo(getResidence().getSiegeEvent()));
		}

		despawnSiegeSummons();
		reCalcNextTime(false);

		super.stopEvent(force);
	}

	public void processStep(Clan clan)
	{
		//
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		final Calendar startSiegeDate = getResidence().getSiegeDate();
		if(onInit)
		{
			// дата ниже текущей
			if(startSiegeDate.getTimeInMillis() <= System.currentTimeMillis())
			{
				startSiegeDate.setTimeInMillis(generateSiegeDateTime(_startTimePattern));
				getResidence().setJdbcState(JdbcEntityState.UPDATED);
			}
		}
		else
		{
			startSiegeDate.setTimeInMillis(generateSiegeDateTime(_startTimePattern));
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
		}

		registerActions();

		getResidence().update();
	}

	@Override
	protected long startTimeMillis()
	{
		return getSiegeDate().getTimeInMillis();
	}

	public Calendar getSiegeDate()
	{
		return getResidence().getSiegeDate();
	}

	//========================================================================================================================================================================
	//                                                                   Zones
	//========================================================================================================================================================================

	@Override
	public void teleportPlayers(String t)
	{
		List<Player> players = new ArrayList<Player>();
		Clan ownerClan = getResidence().getOwner();
		if(t.equalsIgnoreCase(HAVE_OWNER))
		{
			if(ownerClan != null)
				for(Player player : getPlayersInZone())
					if(player.getClan() == ownerClan)
						players.add(player);
		}
		else if(t.equalsIgnoreCase(ATTACKERS))
		{
			for(Player player : getPlayersInZone())
			{
				S siegeClan = getSiegeClan(ATTACKERS, player.getClan());
				if(siegeClan != null && siegeClan.isParticle(player))
					players.add(player);
			}
		}
		else if(t.equalsIgnoreCase(DEFENDERS))
		{
			for(Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;

				S siegeClan = getSiegeClan(DEFENDERS, player.getClan());
				if(siegeClan != null && siegeClan.isParticle(player))
					players.add(player);
			}
		}
		else if(t.equalsIgnoreCase(SPECTATORS))
		{
			for(Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;

				if(player.getClan() == null || getSiegeClan(ATTACKERS, player.getClan()) == null && getSiegeClan(DEFENDERS, player.getClan()) == null)
					players.add(player);
			}
		}
		// выносих всех с резиденции в город
		else if(t.equalsIgnoreCase(FROM_RESIDENCE_TO_BASE))
		{
			for(Player player : getResidence().getZone().getInsidePlayers())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;

				players.add(player);
			}
		}
		else
			players = getPlayersInZone();

		for(Player player : players)
		{
			Location loc = null;
			if(t.equalsIgnoreCase(HAVE_OWNER) || t.equalsIgnoreCase(DEFENDERS))
				loc = getResidence().getOwnerRestartPoint();
			else if(t.equalsIgnoreCase(FROM_RESIDENCE_TO_BASE))
				if (getResidence().getId() == 3)
					loc = Location.findPointToStay(107715, 143852, -3464, 50, 75, player.getGeoIndex());
				else if (getResidence().getId() == 7)
					loc =  Location.findPointToStay(147889, -39001, -1984, 50, 75, player.getGeoIndex());
			else
				loc = getResidence().getNotOwnerRestartPoint(player);

			player.teleToLocation(loc, ReflectionManager.MAIN);
		}
	}

	public List<Player> getPlayersInZone()
	{
		List<ZoneObject> zones = getObjects(SIEGE_ZONES);
		List<Player> result = new LazyArrayList<Player>();
		for(ZoneObject zone : zones)
			result.addAll(zone.getInsidePlayers());
		return result;
	}

	public void broadcastInZone(IBroadcastPacket... packet)
	{
		for(Player player : getPlayersInZone())
			player.sendPacket(packet);
	}

	public boolean checkIfInZone(Creature character)
	{
		List<ZoneObject> zones = getObjects(SIEGE_ZONES);
		for(ZoneObject zone : zones)
			if(zone.checkIfInZone(character))
				return true;
		return false;
	}

	public void broadcastInZone2(IBroadcastPacket... packet)
	{
		for(Player player : getResidence().getZone().getInsidePlayers())
			player.sendPacket(packet);
	}

	//========================================================================================================================================================================
	//                                                                   Siege Clans
	//========================================================================================================================================================================
	public void loadSiegeClans()
	{
		addObjects(ATTACKERS, SiegeClanDAO.getInstance().load(getResidence(), ATTACKERS));
		addObjects(DEFENDERS, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS));
	}

	@SuppressWarnings("unchecked")
	public S newSiegeClan(String type, int clanId, long param, long date)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : (S) new SiegeClanObject(type, clan, param, date);
	}

	public void updateParticles(boolean start, String... arg)
	{
		for(String a : arg)
		{
			List<SiegeClanObject> siegeClans = getObjects(a);
			for(SiegeClanObject s : siegeClans)
				s.setEvent(start, this);
		}
	}

	public S getSiegeClan(String name, Clan clan)
	{
		if(clan == null)
			return null;
		return getSiegeClan(name, clan.getClanId());
	}

	@SuppressWarnings("unchecked")
	public S getSiegeClan(String name, int objectId)
	{
		List<SiegeClanObject> siegeClanList = getObjects(name);
		if(siegeClanList.isEmpty())
			return null;
		for(int i = 0; i < siegeClanList.size(); i++)
		{
			SiegeClanObject siegeClan = siegeClanList.get(i);
			if(siegeClan.getObjectId() == objectId)
				return (S) siegeClan;
		}
		return null;
	}

	public void broadcastTo(IBroadcastPacket packet, String... types)
	{
		for(String type : types)
		{
			List<SiegeClanObject> siegeClans = getObjects(type);
			for(SiegeClanObject siegeClan : siegeClans)
				siegeClan.broadcast(packet);
		}
	}

	//========================================================================================================================================================================
	//                                                         Override Event
	//========================================================================================================================================================================

	@Override
	@SuppressWarnings("unchecked")
	public void initEvent()
	{
		_residence = (R) ResidenceHolder.getInstance().getResidence(getId());

		loadSiegeClans();

		clearActions();

		super.initEvent();
	}

	@Override
	public boolean ifVar(String name)
	{
		if(name.equals(HAVE_OWNER))
			return getResidence().getOwner() != null;
		if(name.equals(HAVE_OLD_OWNER))
			return _oldOwner != null;

		return false;
	}

	@Override
	public void findEvent(Player player)
	{
		if(!isInProgress() || player.getClan() == null)
			return;

		if(getSiegeClan(ATTACKERS, player.getClan()) != null || getSiegeClan(DEFENDERS, player.getClan()) != null)
		{
			player.addEvent(this);

			long val = _blockedFameOnKill.get(player.getObjectId());
			if(val > 0)
			{
				long diff = val - System.currentTimeMillis();
				if(diff > 0)
					player.startEnableUserRelationTask(diff, this);
			}
		}
	}

	@Override
	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		S clan = getSiegeClan(ATTACKERS, player.getClan());
		if(clan != null)
			r.put(RestartType.TO_FLAG, Boolean.TRUE);
	}

	@Override
	public Location getRestartLoc(Player player, RestartType type)
	{
		if (!player.getReflection().isMain())
			return null;

		if (type == RestartType.TO_FLAG)
		{
			final S attackerClan = getSiegeClan(ATTACKERS, player.getClan());
			if(attackerClan != null)
				if (getResidence().getId() == 3)
					return Location.findPointToStay(107715, 143852, -3464, 50, 75, player.getGeoIndex());
				else if (getResidence().getId() == 7)
					return Location.findPointToStay(147889, -39001, -1984, 50, 75, player.getGeoIndex());
			else
				player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
		}

		return null;
	}

	@Override
	public long getRelation(Player thisPlayer, Player targetPlayer, long result)
	{
		Clan clan1 = thisPlayer.getClan();
		Clan clan2 = targetPlayer.getClan();
		if(clan1 == null || clan2 == null)
			return result;

		if(targetPlayer.containsEvent(this))
		{
			result |= RelationChangedPacket.RelationChangedType.SIEGE_PARTICIPANT.getRelationState();

			SiegeClanObject siegeClan1 = getSiegeClan(SiegeEvent.ATTACKERS, clan1);
			SiegeClanObject siegeClan2 = getSiegeClan(SiegeEvent.ATTACKERS, clan2);

			if(siegeClan1 == null && siegeClan2 == null || siegeClan1 == siegeClan2 || siegeClan1 != null && siegeClan2 != null && isAttackersInAlly())
				result |= RelationChangedPacket.RelationChangedType.SIEGE_ALLY.getRelationState();
			else
				result |= RelationChangedPacket.RelationChangedType.SIEGE_ENEMY.getRelationState();

			if(siegeClan1 != null)
				result |= RelationChangedPacket.RelationChangedType.SIEGE_ATTACKER.getRelationState();
		}

		return result;
	}

	@Override
	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		oldRelation |= UIPacket.USER_RELATION_IN_SIEGE;

		SiegeClanObject siegeClan = getSiegeClan(SiegeEvent.ATTACKERS, thisPlayer.getClan());
		if(siegeClan != null)
			oldRelation |= UIPacket.USER_RELATION_ATTACKER;

		return oldRelation;
	}

	@Override
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(!checkIfInZone(target) || !checkIfInZone(attacker))
			return null;

		// или вообще не учасник, или учасники разных осад
		if(!target.containsEvent(this))
			return null;

		Player player = target.getPlayer();
		if(player == null)
			return null;

		SiegeClanObject siegeClan1 = getSiegeClan(SiegeEvent.ATTACKERS, player.getClan());
		if(siegeClan1 == null && attacker.isSiegeGuard())
			return SystemMsg.INVALID_TARGET;
		Player playerAttacker = attacker.getPlayer();
		if(playerAttacker == null)
			return SystemMsg.INVALID_TARGET;

		SiegeClanObject siegeClan2 = getSiegeClan(SiegeEvent.ATTACKERS, playerAttacker.getClan());
		// если оба аттакеры, и в осаде, аттакеры в Алли, невозможно бить
		if(force && siegeClan1 != null && siegeClan2 != null && isAttackersInAlly())
			return SystemMsg.FORCE_ATTACK_IS_IMPOSSIBLE_AGAINST_A_TEMPORARY_ALLIED_MEMBER_DURING_A_SIEGE;
		// если нету как Аттакры, это дефендеры, то невозможно бить
		if(siegeClan1 == null && siegeClan2 == null)
			return SystemMsg.INVALID_TARGET;

		return null;
	}

	@Override
	public boolean isInProgress()
	{
		return hasState(PROGRESS_STATE);
	}

	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(REGISTRATION))
		{
			if(start)
			{
				addState(REGISTRATION_STATE);
				for (Player player : GameObjectsStorage.getPlayers(false, false))
				{
					player.sendPacket(new ExMercenaryCastlewarCastleSiegeHudInfo(getResidence().getSiegeEvent()));
				}
			}
			else
				removeState(REGISTRATION_STATE);
		}
		else
			super.action(name, start);
	}

	public boolean isAttackersInAlly()
	{
		return false;
	}

	@Override
	public void onAddEvent(GameObject object)
	{
		if(_killListener == null)
			return;

		if(object.isPlayer())
			((Player)object).addListener(_killListener);
	}

	@Override
	public void onRemoveEvent(GameObject object)
	{
		if(_killListener == null)
			return;

		if(object.isPlayer())
			((Player)object).removeListener(_killListener);
	}

	@Override
	public List<Player> broadcastPlayers(int range)
	{
		return itemObtainPlayers();
	}

	@Override
	public EventType getType()
	{
		return EventType.SIEGE_EVENT;
	}

	@Override
	public List<Player> itemObtainPlayers()
	{
		List<Player> playersInZone = getPlayersInZone();

		List<Player> list = new LazyArrayList<Player>(playersInZone.size());
		for(Player player : getPlayersInZone())
		{
			if(player.containsEvent(this))
				list.add(player);
		}
		return list;
	}

	@Override
	public void giveItem(Player player, int itemId, long count)
	{
		if(Config.ALT_NO_FAME_FOR_DEAD && itemId == ItemTemplate.ITEM_ID_FAME && player.isDead())
			return;

		super.giveItem(player, itemId, count);
	}

	public Location getEnterLoc(Player player, Zone zone) // DS: в момент вызова игрок еще не вошел в игру и с него нельзя получить список зон, поэтому просто передаем найденную по локации
	{
		S siegeClan = getSiegeClan(ATTACKERS, player.getClan());
		if(siegeClan != null)
		{
			if(siegeClan.getFlag() != null)
				return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
			else
				return getResidence().getNotOwnerRestartPoint(player);
		}
		else
			return getResidence().getOwnerRestartPoint();
	}

	/**
	 * Вызывается для эвента киллера и показывает может ли киллер стать ПК
	 */
	public boolean canPK(Player target, Player killer)
	{
		if(!isInProgress())
			return true; // осада еще не началась

		if(!target.containsEvent(this))
			return true; // либо вообще не участник осад, либо разные осады

		final S targetClan = getSiegeClan(SiegeEvent.ATTACKERS, target.getClan());
		final S killerClan = getSiegeClan(SiegeEvent.ATTACKERS, killer.getClan());
		if(targetClan != null && killerClan != null && isAttackersInAlly()) // оба атакующие и в альянсе
			return true;
		if(targetClan == null && killerClan == null) // оба защитники
			return true;

		return false;
	}

	//========================================================================================================================================================================
	// Getters & Setters
	//========================================================================================================================================================================
	public R getResidence()
	{
		return _residence;
	}

	public void addState(int b)
	{
		_state |= b;
	}

	public void removeState(int b)
	{
		_state &= ~b;
	}

	public boolean hasState(int val)
	{
		return (_state & val) == val;
	}

	public boolean isRegistrationOver()
	{
		return !hasState(REGISTRATION_STATE);
	}

	//========================================================================================================================================================================
	public void addSiegeSummon(Player player, SummonInstance summon)
	{
		_siegeSummons.put(player.getObjectId(), new SiegeSummonInfo(summon));
	}

	public boolean containsSiegeSummon(Servitor cha)
	{
		SiegeSummonInfo siegeSummonInfo = _siegeSummons.get(cha.getPlayer().getObjectId());
		if(siegeSummonInfo == null)
			return false;
		return siegeSummonInfo._summonRef.get() == cha;
	}

	public void removeSiegeSummon(Player player, Servitor cha)
	{
		_siegeSummons.remove(player.getObjectId());
	}

	public void updateSiegeSummon(Player player, SummonInstance summon)
	{
		SiegeSummonInfo siegeSummonInfo = _siegeSummons.get(player.getObjectId());
		if(siegeSummonInfo == null)
			return;

		if(siegeSummonInfo.getSkillId() == summon.getSkillId())
		{
			summon.setSiegeSummon(true);
			siegeSummonInfo._summonRef = summon.getRef();
		}
	}

	public void despawnSiegeSummons()
	{
		for(IntObjectPair<SiegeSummonInfo> entry : _siegeSummons.entrySet())
		{
			SiegeSummonInfo summonInfo = entry.getValue();

			SummonInstance summon = summonInfo._summonRef.get();
			if(summon != null)
				summon.unSummon(false);
			else
			{
				/*TODO:
				CharacterServitorDAO.getInstance().delete(entry.getKey(), summonInfo._skillId, Servitor.SUMMON_TYPE);
				SummonDAO.getInstance().delete(entry.getKey(), summonInfo._skillId);
				SummonEffectDAO.getInstance().delete(entry.getKey(), summonInfo._skillId);
				*/
			}
		}

		_siegeSummons.clear();
	}

	public void removeBlockFame(Player player)
	{
		_blockedFameOnKill.remove(player.getObjectId());
	}
}
