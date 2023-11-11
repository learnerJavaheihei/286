package l2s.gameserver.model.pledge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.SkillEntryType;
import org.apache.commons.lang3.StringUtils;
import l2s.commons.collections.JoinedIterator;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.PledgeAttendanceType;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.entity.residence.clanhall.AuctionClanHall;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.model.items.ClanWarehouse;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.PlayerUtils;
import l2s.gameserver.utils.PledgeBonusUtils;
import l2s.gameserver.utils.SiegeUtils;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clan implements Iterable<UnitMember>
{
	private static final Logger _log = LoggerFactory.getLogger(Clan.class);

	private final int _clanId;

	private int _allyId;
	private int _level = Config.START_CLAN_LEVEL;

	private int _hasCastle;
	private int _castleDefendCount;

	private int _hasHideout;

	private int _crestId;
	private int _crestLargeId;

	private long _expelledMemberTime;
	private long _leavedAllyTime;
	private long _dissolvedAllyTime;
	private long _disbandEndTime;  // время удаления клана
	private long _disbandPenaltyTime; // время окончания штрафа на удаления клана

	private int _academyGraduatesCount;

	private final PledgeHuntingProgress _huntingProgress = new PledgeHuntingProgress(this);
	private int _yesterdayHuntingReward;
	private int _yesterdayAttendanceReward;

	// all these in milliseconds
	public static long EXPELLED_MEMBER_PENALTY = Config.ALT_EXPELLED_MEMBER_PENALTY_TIME * 60 * 60 * 1000L;
	public static long LEAVED_ALLY_PENALTY = Config.ALT_LEAVED_ALLY_PENALTY_TIME * 60 * 60 * 1000L;
	public static long DISSOLVED_ALLY_PENALTY = Config.ALT_DISSOLVED_ALLY_PENALTY_TIME * 60 * 60 * 1000L;
	public static long DISBAND_PENALTY = 7 * 24 * 60 * 60 * 1000L;

	public static SchedulingPattern DISBAND_TIME_PATTERN = new SchedulingPattern(Config.CLAN_DELETE_TIME);
	public static SchedulingPattern CHANGE_LEADER_TIME_PATTERN = new SchedulingPattern(Config.CLAN_CHANGE_LEADER_TIME);

	// for player
	public static long JOIN_PLEDGE_PENALTY = 1 * 24 * 60 * 60 * 1000L;
	public static long CREATE_PLEDGE_PENALTY = 10 * 24 * 60 * 60 * 1000L;

	private final ClanWarehouse _warehouse;
	private int _whBonus = -1;
	private String _notice = null;

	private String _announce;
	private boolean _showAnnounceOnEnter;

	private final IntObjectMap<ClanWar> _atWarWith = new CHashIntObjectMap<ClanWar>();

	protected IntObjectMap<SkillEntry> _skills = new CTreeIntObjectMap<SkillEntry>();
	protected IntObjectMap<RankPrivs> _privs = new CTreeIntObjectMap<RankPrivs>();
	protected IntObjectMap<SubUnit> _subUnits = new CTreeIntObjectMap<SubUnit>();

	private int _reputation = 0;

	//	Clan Privileges: system
	public static final int CP_NOTHING = 0;
	public static final int CP_CL_INVITE_CLAN = 2; // Join clan
	public static final int CP_CL_MANAGE_TITLES = 4; // Give a title
	public static final int CP_CL_WAREHOUSE_SEARCH = 8; // View warehouse content
	public static final int CP_CL_MANAGE_RANKS = 16; // manage clan ranks
	public static final int CP_CL_CLAN_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	public static final int CP_CL_EDIT_CREST = 128; // Edit clan crest
	public static final int CP_CL_APPRENTICE = 256;
	public static final int CP_CL_TROOPS_FAME = 512;

	//	Clan Privileges: clan hall
	public static final int CP_CH_ENTRY_EXIT = 2048; // open a door
	public static final int CP_CH_USE_FUNCTIONS = 4096;
	public static final int CP_CH_AUCTION = 8192;
	public static final int CP_CH_DISMISS = 16384; // Выгнать чужаков из КХ
	public static final int CP_CH_SET_FUNCTIONS = 32768;

	//	Clan Privileges: castle/fotress
	public static final int CP_CS_ENTRY_EXIT = 65536;
	public static final int CP_CS_MANOR_ADMIN = 131072;
	public static final int CP_CS_MANAGE_SIEGE = 262144;
	public static final int CP_CS_USE_FUNCTIONS = 524288;
	public static final int CP_CS_DISMISS = 1048576; // Выгнать чужаков из замка/форта
	public static final int CP_CS_TAXES = 2097152;
	public static final int CP_CS_MERCENARIES = 4194304;
	public static final int CP_CS_SET_FUNCTIONS = 8388606;
	public static final int CP_ALL = 16777214;

	public static final int RANK_FIRST = 1;
	public static final int RANK_LAST = 9;

	// Sub-unit types
	public static final int SUBUNIT_NONE = Byte.MIN_VALUE;
	public static final int SUBUNIT_ACADEMY = -1;
	public static final int SUBUNIT_MAIN_CLAN = 0;
	public static final int SUBUNIT_ROYAL1 = 100;
	public static final int SUBUNIT_ROYAL2 = 200;
	public static final int SUBUNIT_KNIGHT1 = 1001;
	public static final int SUBUNIT_KNIGHT2 = 1002;
	public static final int SUBUNIT_KNIGHT3 = 2001;
	public static final int SUBUNIT_KNIGHT4 = 2002;

	private final static ClanReputationComparator REPUTATION_COMPARATOR = new ClanReputationComparator();
	/** Количество мест в таблице рангов кланов */
	private final static int REPUTATION_PLACES = 100;

	private String _desc;
	private String _title;

	private int arenaStage = 0;
	
	private int _clanPoints;
	private int _killedMobs;

	private final String SELECT_ANNOUNCEMENT = "SELECT * FROM `clan_announcement` WHERE `clan_id` = ?";
	private final String INSERT_ANNOUNCEMENT = "INSERT INTO clan_announcement (clan_id, body, showOnEnter) VALUES (?,?,?)";

	/**
	 * Конструктор используется только внутри для восстановления из базы
	 */
	public Clan(int clanId)
	{
		_clanId = clanId;
		initializePrivs();
		_warehouse = new ClanWarehouse(this);
		_warehouse.restore();
		restoreAnnouncement();
	}

	private void restoreAnnouncement() {
		try
		{
			PreparedStatement st = DatabaseFactory.getInstance().getConnection().prepareStatement(SELECT_ANNOUNCEMENT);
			st.setInt(1, getClanId());
			ResultSet rs = st.executeQuery();
			if(rs.next()) {
				String announce = rs.getString("body");
				setAnnounce(announce == null ? "" : announce);
				setShowAnnounceOnEnter(rs.getBoolean("showOnEnter"));
			} else {
				setAnnounce("");
				setShowAnnounceOnEnter(false);
			}
		}
		catch(Exception e)
		{
			_log.warn("failed to restore clan (" + getClanId() + ") announcement.", e);
		}
	}

	public int getClanId()
	{
		return _clanId;
	}

	public int getLeaderId()
	{
		return getLeaderId(SUBUNIT_MAIN_CLAN);
	}

	public UnitMember getLeader()
	{
		return getLeader(SUBUNIT_MAIN_CLAN);
	}

	public String getLeaderName()
	{
		return getLeaderName(SUBUNIT_MAIN_CLAN);
	}

	public String getName()
	{
		return getUnitName(SUBUNIT_MAIN_CLAN);
	}

	public UnitMember getAnyMember(int id)
	{
		for(SubUnit unit : getAllSubUnits())
		{
			UnitMember m = unit.getUnitMember(id);
			if(m != null){ return m; }
		}
		return null;
	}

	public UnitMember getAnyMember(String name)
	{
		for(SubUnit unit : getAllSubUnits())
		{
			UnitMember m = unit.getUnitMember(name);
			if(m != null){ return m; }
		}
		return null;
	}

	public int getAllSize()
	{
		int size = 0;
		for(SubUnit unit : getAllSubUnits())
		{
			size += unit.size();
		}
		return size;
	}

	public String getUnitName(int unitType)
	{
		if(unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)){ return StringUtils.EMPTY; }

		return getSubUnit(unitType).getName();
	}

	public String getLeaderName(int unitType)
	{
		if(unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)){ return StringUtils.EMPTY; }

		return getSubUnit(unitType).getLeaderName();
	}

	public int getLeaderId(int unitType)
	{
		if(unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)){ return 0; }

		return getSubUnit(unitType).getLeaderObjectId();
	}

	public UnitMember getLeader(int unitType)
	{
		if(unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)){ return null; }

		return getSubUnit(unitType).getLeader();
	}

	public void flush()
	{
		for(UnitMember member : this)
			removeClanMember(member.getObjectId());
		_warehouse.writeLock();
		try
		{
			for(ItemInstance item : _warehouse.getItems())
				_warehouse.destroyItem(item);
		}
		finally
		{
			_warehouse.writeUnlock();
		}
		if(_hasCastle != 0)
			ResidenceHolder.getInstance().getResidence(Castle.class, _hasCastle).changeOwner(null);
	}

	public void removeClanMember(int id)
	{
		if(id == getLeaderId(SUBUNIT_MAIN_CLAN))
			return;

		// удаляем реквест на смену лидера - если он вышел из клана
		ClanChangeLeaderRequest changeLeaderRequest = ClanTable.getInstance().getRequest(getClanId());
		if(changeLeaderRequest != null && changeLeaderRequest.getNewLeaderId() == id)
			ClanTable.getInstance().cancelRequest(changeLeaderRequest, false);

		for(SubUnit unit : getAllSubUnits())
		{
			UnitMember member = unit.getUnitMember(id);
			if(member != null)
			{
				onLeaveClan(member.getPlayer());
				removeClanMember(unit.getType(), id);
				break;
			}
		}
	}

	public void removeClanMember(int subUnitId, int objectId)
	{
		SubUnit subUnit = getSubUnit(subUnitId);
		if(subUnit == null)
			return;

		subUnit.removeUnitMember(objectId);
	}

	public List<UnitMember> getAllMembers()
	{
		List<UnitMember> members = new ArrayList<UnitMember>();
		for(SubUnit unit : getAllSubUnits())
		{
			members.addAll(unit.getUnitMembers());
		}
		return members;
	}

	public List<Player> getOnlineMembers(int excludePlayerId)
	{
		List<Player> result = new ArrayList<Player>();
		for(UnitMember temp : this)
		{
			if(temp.isOnline() && temp.getObjectId() != excludePlayerId)
				result.add(temp.getPlayer());
		}
		return result;
	}

	public List<Player> getOnlineMembers()
	{
		return getOnlineMembers(0);
	}

	public int getOnlineMembersCount(int excludePlayerId)
	{
		int result = 0;
		for(UnitMember temp : this)
		{
			if(temp != null && temp.isOnline() && temp.getObjectId() != excludePlayerId)
				result++;
		}
		return result;
	}

	public int getOnlineMembersCount()
	{
		return getOnlineMembersCount(0);
	}

	public int getAllyId()
	{
		return _allyId;
	}

	public int getLevel()
	{
		return _level;
	}

	/**
	 * Возвращает замок, которым владеет клан
	 * @return ID замка
	 */
	public int getCastle()
	{
		return _hasCastle;
	}

	/**
	 * Возвращает кланхолл, которым владеет клан
	 * @return ID кланхолла
	 */
	public int getHasHideout()
	{
		return _hasHideout;
	}

	public int getResidenceId(ResidenceType r)
	{
		switch(r)
		{
			case CASTLE:
				return _hasCastle;
			case CLANHALL:
				return _hasHideout;
			default:
				return 0;
		}
	}

	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}

	/**
	 * Устанавливает замок, которым владеет клан.<BR>
	 * Одновременно владеть и замком и крепостью нельзя
	 * @param castle ID замка
	 */
	public void setHasCastle(int castle)
	{
		_hasCastle = castle;
	}

	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}

	public void setLevel(int level)
	{
		_level = level;
	}
	
	public void setPoints(int points)
	{
		_clanPoints = points;
		
		int oldLevel = getLevel();
		
		if (_clanPoints > 3_999_999)
			setLevel(10);
		else if (_clanPoints > 1_999_999)
			setLevel(9);
		else if (_clanPoints > 999_999)
			setLevel(8);
		else if (_clanPoints > 449_999)
			setLevel(7);
		else if (_clanPoints > 149_999)
			setLevel(6);
		else if (_clanPoints > 49_999)
			setLevel(5);
		else if (_clanPoints > 9_999)
			setLevel(4);
		else if (_clanPoints > 499)
			setLevel(3);
		else if (_clanPoints > 99)
			setLevel(2);
		else if (_clanPoints > 9)
			setLevel(1);
		
		if (oldLevel != getLevel())
		{
			updateClanInDB();
			onLevelChange(oldLevel, getLevel());
		}
	}
	
	public int getPoints()
	{
		return _clanPoints;
	}
	
	public int getKilledMobs()
	{
		return _killedMobs;
	}
	
	public void setKilledMobs(int value)
	{
		_killedMobs = value;
	}

	public boolean isAnyMember(int id)
	{
		for(SubUnit unit : getAllSubUnits())
		{
			if(unit.isUnitMember(id)){ return true; }
		}
		return false;
	}

	private void updateClanAttendanceInfoInDB()
	{
		if(getClanId() == 0)
		{
			_log.warn("updateClanAttendanceInDB with empty ClanId");
			Thread.dumpStack();
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET hunting_progress=?,yesterday_hunting_reward=?,yesterday_attendance_reward=? WHERE clan_id=?");
			statement.setInt(1, getHuntingProgress());
			statement.setInt(2, getYesterdayHuntingReward());
			statement.setInt(3, getYesterdayAttendanceReward());
			statement.setInt(4, getClanId());
			statement.execute();

			_huntingProgress.setJdbcState(JdbcEntityState.STORED);
		}
		catch(Exception e)
		{
			_log.warn("error while updating clan attendance '" + getClanId() + "' data in db");
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void updateClanScoreInDB()
	{
		if(getClanId() == 0)
		{
			_log.warn("updateClanScoreInDB with empty ClanId");
			Thread.dumpStack();
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?");
			statement.setInt(1, getReputationScore());
			statement.setInt(2, getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("error while updating clan reputation score '" + getClanId() + "' data in db");
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void updateClanInDB()
	{
		if(getLeaderId() == 0)
		{
			_log.warn("updateClanInDB with empty LeaderId");
			Thread.dumpStack();
			return;
		}

		if(getClanId() == 0)
		{
			_log.warn("updateClanInDB with empty ClanId");
			Thread.dumpStack();
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=?,reputation_score=?,expelled_member=?,leaved_ally=?,dissolved_ally=?,clan_level=?,warehouse=?,academy_graduates=?,castle_defend_count=?,disband_end=?,disband_penalty=?,arena_stage=?,clan_points=?,killed_mobs=? WHERE clan_id=?");
			statement.setInt(1, getAllyId());
			statement.setInt(2, getReputationScore());
			statement.setLong(3, getExpelledMemberTime() / 1000);
			statement.setLong(4, getLeavedAllyTime() / 1000);
			statement.setLong(5, getDissolvedAllyTime() / 1000);
			statement.setInt(6, getLevel());
			statement.setInt(7, getWhBonus());
			statement.setInt(8, getAcademyGraduatesCount());
			statement.setInt(9, getCastleDefendCount());
			statement.setInt(10, (int)(getDisbandEndTime() / 1000L));
			statement.setInt(11, (int)(getDisbandPenaltyTime() / 1000L));
			statement.setInt(12, getArenaStage());
			statement.setInt(13, getPoints());
			statement.setInt(14, getKilledMobs());
			statement.setInt(15, getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("error while updating clan '" + getClanId() + "' data in db");
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void store()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_level,ally_id,expelled_member,leaved_ally,dissolved_ally,academy_graduates,clan_points,killed_mobs) values (?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _clanId);
			statement.setInt(2, _level);
			statement.setInt(3, _allyId);
			statement.setLong(4, getExpelledMemberTime() / 1000);
			statement.setLong(5, getLeavedAllyTime() / 1000);
			statement.setLong(6, getDissolvedAllyTime() / 1000);
			statement.setInt(7, getAcademyGraduatesCount());
			statement.setInt(8, getPoints());
			statement.setInt(9, getKilledMobs());
			statement.execute();
			DbUtils.close(statement);

			SubUnit mainSubUnit = _subUnits.get(SUBUNIT_MAIN_CLAN);

			statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id, type, leader_id, name) VALUES (?,?,?,?)");
			statement.setInt(1, _clanId);
			statement.setInt(2, mainSubUnit.getType());
			statement.setInt(3, mainSubUnit.getLeaderObjectId());
			statement.setString(4, mainSubUnit.getName());
			statement.execute();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE characters SET clanid=?,pledge_type=? WHERE obj_Id=?");
			statement.setInt(1, getClanId());
			statement.setInt(2, mainSubUnit.getType());
			statement.setInt(3, getLeaderId());
			statement.execute();

			statement = con.prepareStatement(INSERT_ANNOUNCEMENT);
			statement.setInt(1, getClanId());
			statement.setString(2, getAnnounce());
			statement.setBoolean(3, isShowAnnounceOnEnter());
		}
		catch(Exception e)
		{
			_log.warn("Exception: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static Clan restore(int clanId)
	{
		if(clanId == 0) // no clan
			return null;

		Clan clan = null;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_level,ally_id,reputation_score,expelled_member,leaved_ally,dissolved_ally,warehouse,academy_graduates,castle_defend_count,disband_end,disband_penalty,hunting_progress,yesterday_hunting_reward,yesterday_attendance_reward,arena_stage,clan_points,killed_mobs FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			rset = statement.executeQuery();

			if(rset.next())
			{
				clan = new Clan(clanId);
				clan.setLevel(rset.getInt("clan_level"));
				clan.setAllyId(rset.getInt("ally_id"));
				clan._reputation = rset.getInt("reputation_score");
				clan.setExpelledMemberTime(rset.getLong("expelled_member") * 1000L);
				clan.setLeavedAllyTime(rset.getLong("leaved_ally") * 1000L);
				clan.setDissolvedAllyTime(rset.getLong("dissolved_ally") * 1000L);
				clan.setDisbandEndTime(rset.getLong("disband_end") * 1000L);
				clan.setDisbandPenaltyTime(rset.getLong("disband_penalty") * 1000L);
				clan.setWhBonus(rset.getInt("warehouse"));
				clan.setCastleDefendCount(rset.getInt("castle_defend_count"));
				clan.setAcademyGraduatesCount(rset.getInt("academy_graduates"));
				clan.setHuntingProgress(rset.getInt("hunting_progress"));
				clan.setYesterdayHuntingReward(rset.getInt("yesterday_hunting_reward"));
				clan.setYesterdayAttendanceReward(rset.getInt("yesterday_attendance_reward"));
				clan.setArenaStage(rset.getInt("arena_stage"));
				clan.setPoints(rset.getInt("clan_points"));
				clan.setKilledMobs(rset.getInt("killed_mobs"));

				DbUtils.close(statement, rset);

				statement = con.prepareStatement("SELECT id FROM castle WHERE owner_id=?");
				statement.setInt(1, clanId);
				rset = statement.executeQuery();
				if(rset.next())
					clan.setHasCastle(rset.getInt("id"));

				DbUtils.close(statement, rset);

				statement = con.prepareStatement("SELECT id FROM clanhall WHERE owner_id=?");
				statement.setInt(1, clanId);
				rset = statement.executeQuery();
				if(rset.next())
					clan.setHasHideout(rset.getInt("id"));

				if(clan.getHasHideout() == 0)
				{
					DbUtils.close(statement, rset);

					statement = con.prepareStatement("SELECT id FROM instant_clanhall_owners WHERE owner_id=?");
					statement.setInt(1, clanId);
					rset = statement.executeQuery();
					if(rset.next())
						clan.setHasHideout(Residence.getInstantResidenceId(rset.getInt("id")));
				}
			}
			else
			{
				_log.warn("Clan " + clanId + " doesnt exists!");
				return null;
			}
		}
		catch(Exception e)
		{
			_log.error("Error while restoring clan!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		if(clan == null)
		{
			_log.warn("Clan " + clanId + " does't exist");
			return null;
		}

		clan.restoreSkills();
		clan.restoreSubPledges();

		for(SubUnit unit : clan.getAllSubUnits())
		{
			unit.restore();
			unit.restoreSkills();
		}

		clan.restoreRankPrivs();
		clan.setCrestId(CrestCache.getInstance().getPledgeCrestId(clanId));
		clan.setCrestLargeId(CrestCache.getInstance().getPledgeCrestLargeId(clanId));

		clan.checkSkills();

		return clan;
	}
	public void addMembersBuff(String name, int skillId, int level, long time)
	{

		List<Integer> values= getAllMember(this.getClanId());
		for(int member : values)
		{
			addClanMemberGiftSkill(member, name, skillId, level, time);
		}
	}

	public void broadcastSayPacketToOnlineMembers(ChatType Chat, String who, String inputString)
	{
		for(UnitMember member : this)
			if(member.isOnline())
				member.getPlayer().sendPacket(new SayPacket2(member.getObjectId(), Chat, who, inputString));
	}
	private static List<Integer> getAllMember( int clanid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		List<Integer> values = new ArrayList<Integer>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters where clanid = ? ");
			statement.setInt(1, clanid);
			rset = statement.executeQuery();
			while(rset.next())
			{
				values.add(rset.getInt("obj_Id"));
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return values;
	}

	private void addClanMemberGiftSkill(int objid, String Name, int skillId, int skillLevel, long expTime)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO _player_give_clan_buff (obj_Id ,name ,skill_id ,skill_level,deletetime) VALUES(?,?,?,?,?)");
			statement.setInt(1, objid);
			statement.setString(2, Name);
			statement.setInt(3, skillId);
			statement.setInt(4, skillLevel);
			statement.setLong(5, expTime);
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	public void broadcastToOnlineMembers(IBroadcastPacket... packets)
	{
		for(UnitMember member : this)
			if(member.isOnline())
				member.getPlayer().sendPacket(packets);
	}

	public void broadcastToOtherOnlineMembers(IBroadcastPacket packet, Player player)
	{
		for(UnitMember member : this)
			if(member.isOnline() && member.getPlayer() != player)
				member.getPlayer().sendPacket(packet);
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public void setCrestId(int newcrest)
	{
		_crestId = newcrest;
	}

	public int getCrestId()
	{
		return _crestId;
	}

	public boolean hasCrest()
	{
		return _crestId > 0;
	}

	public int getCrestLargeId()
	{
		return _crestLargeId;
	}

	public void setCrestLargeId(int newcrest)
	{
		_crestLargeId = newcrest;
	}

	public boolean hasCrestLarge()
	{
		return _crestLargeId > 0;
	}

	public long getAdenaCount()
	{
		return _warehouse.getCountOfAdena();
	}

	public ClanWarehouse getWarehouse()
	{
		return _warehouse;
	}

	public boolean isAtWarWith(int clanId)
	{
		return _atWarWith.containsKey(clanId);
	}

	public boolean isAtWarWith(Clan clan)
	{
		if(clan == null)
			return false;

		return _atWarWith.containsKey(clan.getClanId());
	}

	public boolean isAtWar()
	{
		return !_atWarWith.isEmpty();
	}

	public IntObjectMap<ClanWar> getWars()
	{
		return _atWarWith;
	}

	public int getWarCount()
	{
		return _atWarWith.size();
	}

	public void addWar(int clanId, ClanWar war)
	{
		_atWarWith.put(clanId, war);
	}

	public void deleteWar(int clanId)
	{
		_atWarWith.remove(clanId);
	}

	public ClanWar getWarWith(int clanId)
	{
		return _atWarWith.get(clanId);
	}

	public void broadcastClanStatus(boolean updateList, boolean needUserInfo, boolean relation)
	{
		List<IBroadcastPacket> listAll = updateList ? listAll() : null;
		PledgeShowInfoUpdatePacket update = new PledgeShowInfoUpdatePacket(this);

		for(UnitMember member : this)
			if(member.isOnline())
			{
				if(updateList)
				{
					member.getPlayer().sendPacket(PledgeShowMemberListDeleteAllPacket.STATIC);
					member.getPlayer().sendPacket(listAll);
				}
				member.getPlayer().sendPacket(update);
				if(needUserInfo)
					member.getPlayer().broadcastCharInfo();
				if(relation)
					PlayerUtils.updateAttackableFlags(member.getPlayer());
			}
	}

	public Alliance getAlliance()
	{
		return _allyId == 0 ? null : ClanTable.getInstance().getAlliance(_allyId);
	}

	public void setExpelledMemberTime(long time)
	{
		_expelledMemberTime = time;
	}

	public long getExpelledMemberTime()
	{
		return _expelledMemberTime;
	}

	public void setExpelledMember()
	{
		_expelledMemberTime = System.currentTimeMillis();
		updateClanInDB();
	}

	public void setLeavedAllyTime(long time)
	{
		_leavedAllyTime = time;
	}

	public long getLeavedAllyTime()
	{
		return _leavedAllyTime;
	}

	public void setLeavedAlly()
	{
		_leavedAllyTime = System.currentTimeMillis();
		updateClanInDB();
	}

	public void setDissolvedAllyTime(long time)
	{
		_dissolvedAllyTime = time;
	}

	public long getDissolvedAllyTime()
	{
		return _dissolvedAllyTime;
	}

	public void setDissolvedAlly()
	{
		_dissolvedAllyTime = System.currentTimeMillis();
		updateClanInDB();
	}

	public boolean canInvite()
	{
		return System.currentTimeMillis() - _expelledMemberTime >= EXPELLED_MEMBER_PENALTY;
	}

	public boolean canJoinAlly()
	{
		return System.currentTimeMillis() - _leavedAllyTime >= LEAVED_ALLY_PENALTY;
	}

	public boolean canCreateAlly()
	{
		return System.currentTimeMillis() - _dissolvedAllyTime >= DISSOLVED_ALLY_PENALTY;
	}

	public boolean canDisband()
	{
		return System.currentTimeMillis() > _disbandPenaltyTime;
	}

	public int getRank()
	{
		Clan[] clans = ClanTable.getInstance().getClans();
		Arrays.sort(clans, REPUTATION_COMPARATOR);

		int place = 1;
		for(int i = 0; i < clans.length; i++)
		{
			if(i == REPUTATION_PLACES)
				return 0;

			Clan clan = clans[i];
			if(clan == this)
				return place + i;
		}

		return 0;
	}

	public int getReputationScore()
	{
		return _reputation;
	}

	private void setReputationScore(int rep)
	{
		if(_reputation >= 0 && rep < 0)
		{
			broadcastToOnlineMembers(SystemMsg.SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DEACTIVATED);
			for(UnitMember member : this)
				if(member.isOnline() && member.getPlayer() != null)
					disableSkills(member.getPlayer());
		}
		else if(_reputation < 0 && rep >= 0)
		{
			broadcastToOnlineMembers(SystemMsg.CLAN_SKILLS_WILL_NOW_BE_ACTIVATED_SINCE_THE_CLANS_REPUTATION_SCORE_IS_0_OR_HIGHER);
			for(UnitMember member : this)
				if(member.isOnline() && member.getPlayer() != null)
					enableSkills(member.getPlayer());
		}

		if(_reputation != rep)
		{
			_reputation = rep;
			broadcastToOnlineMembers(new PledgeShowInfoUpdatePacket(this));
		}

		updateClanScoreInDB();
	}

	public int incReputation(int inc, boolean rate, String source)
	{
		if(_level < 3)
			return 0;

		if(rate && Math.abs(inc) <= Config.RATE_CLAN_REP_SCORE_MAX_AFFECTED)
			inc = (int) Math.round(inc * Config.RATE_CLAN_REP_SCORE);

		setReputationScore(_reputation + inc);
		Log.add(getName() + "|" + inc + "|" + _reputation + "|" + source, "clan_reputation");

		return inc;
	}

	/* ============================ clan skills stuff ============================ */

	private void restoreSkills()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2Player from the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				// Create a L2Skill object for each record
				SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, id, level);
				if(skillEntry != null)
				{
					// Add the L2Skill object to the Clan _skills
					_skills.put(skillEntry.getId(), skillEntry);
				}
			}
		}
		catch(Exception e)
		{
			_log.warn("Could not restore clan skills: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public Collection<SkillEntry> getSkills()
	{
		return _skills.valueCollection();
	}

	/** used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db*/
	public SkillEntry addSkill(SkillEntry newSkillEntry, boolean store)
	{
		SkillEntry oldSkillEntry = null;
		if(newSkillEntry != null)
		{
			// Replace oldSkillEntry by newSkillEntry or Add the newSkillEntry
			oldSkillEntry = _skills.put(newSkillEntry.getId(), newSkillEntry);

			if(store)
			{
				Connection con = null;
				PreparedStatement statement = null;

				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("REPLACE INTO clan_skills (clan_id,skill_id,skill_level) VALUES (?,?,?)");
					statement.setInt(1, getClanId());
					statement.setInt(2, newSkillEntry.getId());
					statement.setInt(3, newSkillEntry.getLevel());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.warn("Error could not store char skills: " + e);
					_log.error("", e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
			}

			PledgeSkillListAddPacket p = new PledgeSkillListAddPacket(newSkillEntry.getId(), newSkillEntry.getLevel());
			PledgeSkillListPacket p2 = new PledgeSkillListPacket(this);
			for(UnitMember temp : this)
			{
				if(temp.isOnline())
				{
					Player player = temp.getPlayer();
					if(player != null)
					{
						addSkill(player, newSkillEntry);
						player.sendPacket(p, p2);
						player.sendSkillList();
					}
				}
			}
		}

		return oldSkillEntry;
	}

	public void addSkillsQuietly(Player player)
	{
		for(SkillEntry skillEntry : _skills.valueCollection())
			addSkill(player, skillEntry);

		final SubUnit subUnit = getSubUnit(player.getPledgeType());
		if(subUnit != null)
			subUnit.addSkillsQuietly(player);

		if(player.isClanLeader())
		{
			if(getLevel() >= SiegeUtils.MIN_CLAN_SIEGE_LEVEL)
				SiegeUtils.addSiegeSkills(player);
		}
	}

	public void enableSkills(Player player)
	{
		if(player.isInOlympiadMode()) // не разрешаем кланскиллы на олимпе
			return;

		for(SkillEntry skillEntry : _skills.valueCollection())
		{
			Skill skill = skillEntry.getTemplate();
			if(skill.getMinPledgeRank().ordinal() <= player.getPledgeRank().ordinal())
			{
				if(!skill.clanLeaderOnly() || skill.clanLeaderOnly() && player.isClanLeader())
					player.removeUnActiveSkill(skill);
			}
		}

		final SubUnit subUnit = getSubUnit(player.getPledgeType());
		if(subUnit != null)
			subUnit.enableSkills(player);
	}

	public void disableSkills(Player player)
	{
		for(SkillEntry skillEntry : _skills.valueCollection())
			player.addUnActiveSkill(skillEntry.getTemplate());

		final SubUnit subUnit = getSubUnit(player.getPledgeType());
		if(subUnit != null)
			subUnit.disableSkills(player);
	}

	private void addSkill(Player player, SkillEntry skillEntry)
	{
		Skill skill = skillEntry.getTemplate();
		if(skill.getMinPledgeRank().ordinal() <= player.getPledgeRank().ordinal())
		{
			if(!skill.clanLeaderOnly() || skill.clanLeaderOnly() && player.isClanLeader())
			{
				player.addSkill(skillEntry, false);
				if(_reputation < 0 || player.isInOlympiadMode())
					player.addUnActiveSkill(skill);
			}
		}
	}

	/**
	 * Удаляет скилл у клана, без удаления из базы. Используется для удаления скилов резиденций.
	 * После удаления скила(ов) необходимо разослать boarcastSkillListToOnlineMembers()
	 * @param skill
	 */
	public void removeSkill(int skill, boolean store)
	{
		if(_skills.remove(skill) == null)
			return;

		if(store)
		{
			Connection con = null;
			PreparedStatement statement = null;

			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM clan_skills WHERE skill_id=? AND clan_id=?");
				statement.setInt(1, skill);
				statement.setInt(2, getClanId());
				statement.execute();
			}
			catch(Exception e)
			{
				_log.warn("Error could not delete char skills: " + e);
				_log.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}

		PledgeSkillListAddPacket p = new PledgeSkillListAddPacket(skill, 0);
		for(UnitMember temp : this)
		{
			Player player = temp.getPlayer();
			if(player != null && player.isOnline())
			{
				player.removeSkillById(skill);
				player.sendPacket(p);
				player.sendSkillList();
			}
		}
	}

	public void broadcastSkillListToOnlineMembers()
	{
		for(UnitMember temp : this)
		{
			Player player = temp.getPlayer();
			if(player != null && player.isOnline())
			{
				player.sendPacket(new PledgeSkillListPacket(this));
				player.sendSkillList();
			}
		}
	}

	/* ============================ clan subpledges stuff ============================ */

	public static boolean isAcademy(int pledgeType)
	{
		return pledgeType == SUBUNIT_ACADEMY;
	}

	public static boolean isRoyalGuard(int pledgeType)
	{
		return pledgeType == SUBUNIT_ROYAL1 || pledgeType == SUBUNIT_ROYAL2;
	}

	public static boolean isOrderOfKnights(int pledgeType)
	{
		return pledgeType == SUBUNIT_KNIGHT1 || pledgeType == SUBUNIT_KNIGHT2 || pledgeType == SUBUNIT_KNIGHT3 || pledgeType == SUBUNIT_KNIGHT4;
	}

	public int getAffiliationRank(int pledgeType)
	{
		if(isAcademy(pledgeType))
			return 9;
		else if(isOrderOfKnights(pledgeType))
			return 8;
		else if(isRoyalGuard(pledgeType))
			return 7;
		else
			return 6;
	}

	public final SubUnit getSubUnit(int pledgeType)
	{
		return _subUnits.get(pledgeType);
	}

	public final void addSubUnit(SubUnit sp, boolean updateDb)
	{
		_subUnits.put(sp.getType(), sp);

		if(updateDb)
		{
			broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(sp));
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO `clan_subpledges` (clan_id,type,leader_id,name) VALUES (?,?,?,?)");
				statement.setInt(1, getClanId());
				statement.setInt(2, sp.getType());
				statement.setInt(3, sp.getLeaderObjectId());
				statement.setString(4, sp.getName());
				statement.execute();
			}
			catch(Exception e)
			{
				_log.warn("Could not store clan Sub pledges: " + e);
				_log.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}

	public int createSubPledge(Player player, int pledgeType, UnitMember leader, String name)
	{
		pledgeType = getAvailablePledgeTypes(pledgeType);

		if(pledgeType == SUBUNIT_NONE)
			return SUBUNIT_NONE;

		switch(pledgeType)
		{
			case SUBUNIT_ACADEMY:
				break;
			case SUBUNIT_ROYAL1:
			case SUBUNIT_ROYAL2:
				if(getReputationScore() < 5000)
				{
					player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return SUBUNIT_NONE;
				}
				incReputation(-5000, false, "SubunitCreate");
				break;
			case SUBUNIT_KNIGHT1:
			case SUBUNIT_KNIGHT2:
			case SUBUNIT_KNIGHT3:
			case SUBUNIT_KNIGHT4:
				if(getReputationScore() < 10000)
				{
					player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return SUBUNIT_NONE;
				}
				incReputation(-10000, false, "SubunitCreate");
				break;
		}

		addSubUnit(new SubUnit(this, pledgeType, leader, name, false), true);

		return pledgeType;
	}

	public int getAvailablePledgeTypes(int pledgeType)
	{
		if(pledgeType == SUBUNIT_MAIN_CLAN)
			return SUBUNIT_NONE;

		if(_subUnits.get(pledgeType) != null)
			switch(pledgeType)
			{
				case SUBUNIT_ACADEMY:
					return SUBUNIT_NONE;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return SUBUNIT_NONE;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return SUBUNIT_NONE;
			}
		return pledgeType;
	}

	private void restoreSubPledges()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int type = rset.getInt("type");
				int leaderId = rset.getInt("leader_id");
				String name = rset.getString("name");
				SubUnit pledge = new SubUnit(this, type, leaderId, name);
				pledge.setUpgraded(rset.getBoolean("upgraded"), false);
				addSubUnit(pledge, false);
			}
		}
		catch(Exception e)
		{
			_log.warn("Could not restore clan SubPledges: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public int getClanMembersLimit()
	{
		int limit = 0;
		for(SubUnit su : getAllSubUnits())
			limit += getSubPledgeLimit(su.getType());
		return limit;
	}

	public int getSubPledgeLimit(int pledgeType)
	{
		int limit = 0;

		SubUnit subUnit = _subUnits.get(pledgeType);
		switch(pledgeType)
		{
			case SUBUNIT_MAIN_CLAN:
				switch(_level)
				{
					case 0:
						limit = 10;
						break;
					case 1:
						limit = 15;
						break;
					case 2:
						limit = 20;
						break;
					case 3:
						limit = 30;
						break;
					default:
						limit = 40;
						break;
				}
				break;
			case SUBUNIT_ACADEMY:
				limit = 20;
				break;
			case SUBUNIT_ROYAL1:
			case SUBUNIT_ROYAL2:
				if(subUnit != null && subUnit.isUpgraded())
					limit = 30;
				else
					limit = 20;
				break;
			case SUBUNIT_KNIGHT1:
			case SUBUNIT_KNIGHT2:
				if(subUnit != null && subUnit.isUpgraded())
					limit = 25;
				else
					limit = 10;
				break;
			case SUBUNIT_KNIGHT3:
			case SUBUNIT_KNIGHT4:
				if(subUnit != null && subUnit.isUpgraded())
					limit = 25;
				else
					limit = 10;
				break;
		}
		return limit;
	}

	public int getUnitMembersSize(int pledgeType)
	{
		if(pledgeType == Clan.SUBUNIT_NONE || !_subUnits.containsKey(pledgeType)){ return 0; }
		return getSubUnit(pledgeType).size();
	}

	/* ============================ clan privilege ranks stuff ============================ */

	private void restoreRankPrivs()
	{
		if(_privs == null)
			initializePrivs();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2Player from the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `privilleges`, `rank` FROM `clan_privs` WHERE `clan_id`=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int rank = rset.getInt("rank");
				//int party = rset.getInt("party"); - unused?
				int privileges = rset.getInt("privilleges");
				//noinspection ConstantConditions
				RankPrivs p = _privs.get(rank);
				if(p != null)
					p.setPrivs(privileges);
				else
					_log.warn("Invalid rank value (" + rank + "), please check clan_privs table");
			}
		}
		catch(Exception e)
		{
			_log.warn("Could not restore clan privs by rank: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void initializePrivs()
	{
		for(int i = RANK_FIRST; i <= RANK_LAST; i++)
			_privs.put(i, new RankPrivs(i, 0, CP_NOTHING));
	}

	public void updatePrivsForRank(int rank)
	{
		for(UnitMember member : this)
			if(member.isOnline() && member.getPlayer() != null && member.getPlayer().getPowerGrade() == rank)
			{
				if(member.getPlayer().isClanLeader())
					continue;
				member.getPlayer().sendUserInfo();
			}
	}

	public RankPrivs getRankPrivs(int rank)
	{
		if(rank < RANK_FIRST || rank > RANK_LAST)
		{
			_log.warn("Requested invalid rank value: " + rank);
			Thread.dumpStack();
			return null;
		}
		if(_privs.get(rank) == null)
		{
			_log.warn("Request of rank before init: " + rank);
			Thread.dumpStack();
			setRankPrivs(rank, CP_NOTHING);
		}
		return _privs.get(rank);
	}

	public int countMembersByRank(int rank)
	{
		int ret = 0;
		for(UnitMember m : this)
			if(m.getPowerGrade() == rank)
				ret++;
		return ret;
	}

	public void setRankPrivs(int rank, int privs)
	{
		if(rank < RANK_FIRST || rank > RANK_LAST)
		{
			_log.warn("Requested set of invalid rank value: " + rank);
			Thread.dumpStack();
			return;
		}

		if(_privs.get(rank) != null)
			_privs.get(rank).setPrivs(privs);
		else
			_privs.put(rank, new RankPrivs(rank, countMembersByRank(rank), privs));

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			//_log.warn("requested store clan privs in db for rank: " + rank + ", privs: " + privs);
			// Retrieve all skills of this L2Player from the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO clan_privs (`clan_id`,`rank`,`privilleges`) VALUES (?,?,?)");
			statement.setInt(1, getClanId());
			statement.setInt(2, rank);
			statement.setInt(3, privs);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("Could not store clan privs for rank: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/** used to retrieve all privilege ranks */
	public final RankPrivs[] getAllRankPrivs()
	{
		if(_privs == null)
			return new RankPrivs[0];
		return _privs.values(new RankPrivs[_privs.size()]);
	}

	private static class ClanReputationComparator implements Comparator<Clan>
	{
		@Override
		public int compare(Clan o1, Clan o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o2.getReputationScore() - o1.getReputationScore();
		}
	}

	public int getWhBonus()
	{
		return _whBonus;
	}

	public void setWhBonus(int i)
	{
		if(_whBonus != -1)
			mysql.set("UPDATE `clan_data` SET `warehouse`=? WHERE `clan_id`=?", i, getClanId());
		_whBonus = i;
	}

	public final Collection<SubUnit> getAllSubUnits()
	{
		return _subUnits.valueCollection();
	}

	public List<IBroadcastPacket> listAll()
	{
		List<IBroadcastPacket> p = new ArrayList<IBroadcastPacket>(_subUnits.size());
		for(SubUnit unit : getAllSubUnits())
			p.add(new PledgeShowMemberListAllPacket(this, unit));

		return p;
	}

	public String getNotice()
	{
		return _notice;
	}

	/**
	 * Назначить новое сообщение
	 */
	public void setNotice(String notice)
	{
		_notice = notice;
	}



	public int getSkillLevel(int id, int def)
	{
		SkillEntry skillEntry = _skills.get(id);
		return skillEntry == null ? def : skillEntry.getLevel();
	}

	public int getSkillLevel(int id)
	{
		return getSkillLevel(id, -1);
	}

	public String getDesc()
	{
		return _desc;
	}

	public void setDesc(String desc)
	{
		_desc = desc;
	}

	public String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	@Override
	public Iterator<UnitMember> iterator()
	{
		List<Iterator<UnitMember>> iterators = new ArrayList<Iterator<UnitMember>>(_subUnits.size());
		for(SubUnit subUnit : _subUnits.valueCollection())
			iterators.add(subUnit.getUnitMembers().iterator());
		return new JoinedIterator<UnitMember>(iterators);
	}

	public int getAcademyGraduatesCount()
	{
		return _academyGraduatesCount;
	}

	public void setAcademyGraduatesCount(int val)
	{
		_academyGraduatesCount = val;
	}

	public void loginClanCond(Player player, boolean login)
	{
		if(login)
		{
			//Clan clan = player.getClan();
			SubUnit subUnit = player.getSubUnit();
			if(subUnit == null)
				return;

			UnitMember member = subUnit.getUnitMember(player.getObjectId());
			if(member == null)
				return;

			member.setPlayerInstance(player, false);

			int sponsor = player.getSponsor();
			int apprentice = player.getApprentice();
			L2GameServerPacket msg = new SystemMessagePacket(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(player);
			PledgeShowMemberListUpdatePacket memberUpdate = new PledgeShowMemberListUpdatePacket(player);
			for(Player clanMember : getOnlineMembers(player.getObjectId()))
			{
				clanMember.sendPacket(memberUpdate);
				if(clanMember.getObjectId() == sponsor)
					clanMember.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(player));
				else if(clanMember.getObjectId() == apprentice)
					clanMember.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(player));
				else
					clanMember.sendPacket(msg);
			}

			if(player.isClanLeader())
			{
				if(getLevel() >= 1)
				{
					for(Player clanMember : getOnlineMembers())
					{
						SkillEntry clanAdvent = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 19009, getLevel());
						clanAdvent.getEffects(player, clanMember);
					}
				}

				AuctionClanHall clanHall = getHasHideout() != 0 ? ResidenceHolder.getInstance().getResidence(AuctionClanHall.class, getHasHideout()) : null;
				if(clanHall != null && clanHall.getAuctionLength() == 0)
				{
					if(clanHall.getSiegeEvent().getClass() == ClanHallAuctionEvent.class)
					{
						if(getWarehouse().getCountOf(clanHall.getFeeItemId()) < clanHall.getRentalFee())
							player.sendPacket(new SystemMessagePacket(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addLong(clanHall.getRentalFee()));
					}
				}
			}
			else
			{
				if(getLevel() >= 5)
				{
					if(getLeader().isOnline())
					{
						SkillEntry clanAdvent = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 19009, getLevel());
						clanAdvent.getEffects(getLeader().getPlayer(), player);
					}
				}
			}

			if(subUnit.getLeaderObjectId() == player.getObjectId())
			{
				String changedOldName = player.getVar(Player.CHANGED_PLEDGE_NAME);
				if(changedOldName != null && !StringUtils.isEmpty(changedOldName))
					player.sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLEDGE, ExNeedToChangeName.NONE_REASON, changedOldName));
			}
			if(isShowAnnounceOnEnter())
			{
				String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/clan_popup.htm", player);
				html = html.replace("%pledge_name%", getName());
				html = html.replace("%content%", getAnnounce());

				player.sendPacket(new HtmlMessage(0).setHtml(html));
			}
		}
		else
		{
			if(player.isClanLeader())
			{
				for(Player clanMember : getOnlineMembers(player.getObjectId()))
					clanMember.getAbnormalList().stop(19009);
			}
		}

		final ExPledgeCount pledgeCount = new ExPledgeCount(getOnlineMembersCount(login ? 0 : player.getObjectId()));
		for(Player clanMember : getOnlineMembers(login ? 0 : player.getObjectId()))
			clanMember.sendPacket(pledgeCount);
	}

	public void onLevelChange(int oldLevel, int newLevel)
	{
		if(getLeader().isOnline())
		{
			Player clanLeader = getLeader().getPlayer();
			if(oldLevel < SiegeUtils.MIN_CLAN_SIEGE_LEVEL && newLevel >= SiegeUtils.MIN_CLAN_SIEGE_LEVEL)
				SiegeUtils.addSiegeSkills(clanLeader);

			if(newLevel == 3)
				clanLeader.sendPacket(SystemMsg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);

			if(newLevel > oldLevel && newLevel >= 1)
			{
				for(Player clanMember : getOnlineMembers())
				{
					SkillEntry clanAdvent = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 19009, getLevel());
					clanAdvent.getEffects(clanLeader, clanMember);
				}
			}
			else if(newLevel < 1 && oldLevel >= 1)
			{
				for(Player member : getOnlineMembers())
					member.getAbnormalList().stop(19009);
			}
		}

		checkSkills();

		// notify all the members about it
		PledgeShowInfoUpdatePacket pu = new PledgeShowInfoUpdatePacket(this);
		PledgeStatusChangedPacket ps = new PledgeStatusChangedPacket(this);
		for(Player member : getOnlineMembers())
		{
			member.updatePledgeRank();
			member.sendPacket(SystemMsg.YOUR_CLANS_LEVEL_HAS_INCREASED, pu, ps);
			member.broadcastUserInfo(true);
		}
	}

	public void onEnterClan(Player player)
	{
		if(getLevel() >= 1)
		{
			if(getLeader().isOnline())
			{
				SkillEntry clanAdvent = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 19009, getLevel());
				clanAdvent.getEffects(getLeader().getPlayer(), player);
			}
		}

		final ExPledgeCount pledgeCount = new ExPledgeCount(getOnlineMembersCount());
		for(Player clanMember : getOnlineMembers())
			clanMember.sendPacket(pledgeCount);

		ClanSearchManager.getInstance().removeApplicant(getClanId(), player.getObjectId());

		player.getListeners().onClanInvite();
	}

	public void onLeaveClan(Player player)
	{
		int playerId = 0;
		if(player != null)
		{
			playerId = player.getObjectId();
			player.sendPacket(new ExPledgeCount(0));
			player.getAbnormalList().stop(19009);
		}

		final ExPledgeCount pledgeCount = new ExPledgeCount(getOnlineMembersCount(playerId));
		for(Player clanMember : getOnlineMembers(playerId))
			clanMember.sendPacket(pledgeCount);
	}

	public boolean isSpecialAbnormal(Skill skill)
	{
		return getLevel() >= 1 && getLeader().isOnline() && (19009 == skill.getId());
	}

	public boolean checkJoinPledgeCondition(Player player, int pledgeType)
	{
		if(pledgeType == SUBUNIT_ACADEMY)
		{
			if(player.isAcademyGraduated())
				return false;
			if(player.getLevel() >= 85)
				return false;
		}
		return true;
	}

	public boolean joinInPledge(Player player, int pledgeType)
	{
		player.sendPacket(new JoinPledgePacket(getClanId()));

		SubUnit subUnit = getSubUnit(pledgeType);
		if(subUnit == null)
			return false;

		UnitMember member = new UnitMember(this, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), pledgeType, player.getPowerGrade(), player.getApprentice(), player.getSex().ordinal(), SUBUNIT_NONE, PledgeAttendanceType.NEW_RECRUIT);
		subUnit.addUnitMember(member);

		player.setPledgeType(pledgeType);
		player.setClan(this);

		member.setPlayerInstance(player, false);

		if(pledgeType == SUBUNIT_ACADEMY)
			player.setLvlJoinedAcademy(player.getLevel());

		member.setPowerGrade(getAffiliationRank(player.getPledgeType()));

		broadcastToOtherOnlineMembers(new PledgeShowMemberListAddPacket(member), player);
		broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(player.getName()), new PledgeShowInfoUpdatePacket(this));

		// this activates the clan tab on the new member
		player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
		player.sendPacket(player.getClan().listAll());
		player.setLeaveClanTime(0);
		player.updatePledgeRank();

		// добавляем скилы игроку, ток тихо
		addSkillsQuietly(player);
		// отображем
		player.sendPacket(new PledgeSkillListPacket(this));
		player.sendSkillList();

		EventHolder.getInstance().findEvent(player);

		player.broadcastCharInfo();

		onEnterClan(player);
		player.store(false);
		return true;
	}

	public int getCastleDefendCount()
	{
		return _castleDefendCount;
	}

	public void setCastleDefendCount(int castleDefendCount)
	{
		_castleDefendCount = castleDefendCount;
	}

	public boolean isPlacedForDisband()
	{
		return _disbandEndTime != 0;
	}

	public void placeForDisband()
	{
		_disbandEndTime = DISBAND_TIME_PATTERN.next(System.currentTimeMillis());

		updateClanInDB();
	}

	public void unPlaceDisband()
	{
		_disbandEndTime = 0;
		_disbandPenaltyTime = System.currentTimeMillis() + DISBAND_PENALTY;

		updateClanInDB();
	}

	public long getDisbandEndTime()
	{
		return _disbandEndTime;
	}

	public void setDisbandEndTime(long disbandEndTime)
	{
		_disbandEndTime = disbandEndTime;
	}

	public long getDisbandPenaltyTime()
	{
		return _disbandPenaltyTime;
	}

	public void setDisbandPenaltyTime(long disbandPenaltyTime)
	{
		_disbandPenaltyTime = disbandPenaltyTime;
	}

	public int getAttendanceProgress()
	{
		int result = 0;
		for(UnitMember member : getAllMembers())
		{
			if(member.getAttendanceType() == PledgeAttendanceType.ACQUIRED)
				result++;
		}
		return result;
	}

	public int getHuntingProgress()
	{
		return _huntingProgress.getValue();
	}

	public void setHuntingProgress(int value)
	{
		_huntingProgress.setValue(value);
	}

	public void addHuntingProgress(int value)
	{
		if(value <= 0)
			return;
		if ((_huntingProgress.getValue() + value) >= PledgeBonusUtils.MAX_HUNTING_PROGRESS)
			return;
		int oldLevel = PledgeBonusUtils.getHuntingProgressLevel(getHuntingProgress());
		_huntingProgress.setValue(_huntingProgress.getValue() + value);
		_huntingProgress.setJdbcState(JdbcEntityState.UPDATED);
		broadcastToOnlineMembers(new ExPledgeBonusUpdate(ExPledgeBonusUpdate.BonusType.HUNTING, getHuntingProgress()));
		int newLevel = PledgeBonusUtils.getHuntingProgressLevel(getHuntingProgress());
		if(newLevel > oldLevel)
			broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOUR_CLAN_HAS_ACHIEVED_HUNTING_BONUS_LV_S1).addInteger(newLevel));
	}

	public int getYesterdayHuntingReward()
	{
		return _yesterdayHuntingReward;
	}

	public void setYesterdayHuntingReward(int value)
	{
		_yesterdayHuntingReward = value;
	}

	public int getYesterdayAttendanceReward()
	{
		return _yesterdayAttendanceReward;
	}

	public void setYesterdayAttendanceReward(int value)
	{
		_yesterdayAttendanceReward = value;
	}

	public void refreshAttendanceInfo()
	{
		setYesterdayAttendanceReward(PledgeBonusUtils.getAttendanceProgressLevel(getAttendanceProgress()));
		setYesterdayHuntingReward(PledgeBonusUtils.getHuntingProgressLevel(getHuntingProgress()));

		for(UnitMember member : getAllMembers())
		{
			if(member.getAttendanceType() == PledgeAttendanceType.NEW_RECRUIT)
			{
				if(member.isOnline())
					member.setAttendanceType(PledgeAttendanceType.ACQUIRED);
				else
					member.setAttendanceType(PledgeAttendanceType.NOT_ACQUIRED);

				broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(member));
			}
			else if(!member.isOnline())
			{
				member.setAttendanceType(PledgeAttendanceType.NOT_ACQUIRED);
				broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(member));
			}
		}
		setHuntingProgress(0);

		updateClanAttendanceInfoInDB();

		broadcastToOnlineMembers(ExPledgeBonusMarkReset.STATIC); // TODO: Правильно ли посылаем данный пакет?
		broadcastToOnlineMembers(new ExPledgeBonusUpdate(ExPledgeBonusUpdate.BonusType.ATTENDANCE, getAttendanceProgress()));
		broadcastToOnlineMembers(new ExPledgeBonusUpdate(ExPledgeBonusUpdate.BonusType.HUNTING, getHuntingProgress()));
	}

	public void saveHuntingProgress()
	{
		_huntingProgress.save();
	}

	public void checkSkills()
	{
		// Удаляем скиллы, если уровень был понижен.
		for(SkillEntry skill : getSkills())
		{
			SkillLearn sl = SkillAcquireHolder.getInstance().getSkillLearn(null, skill.getId(), skill.getLevel(), AcquireType.CLAN);
			if(sl == null || sl.getMinLevel() <= getLevel())
				continue;

			removeSkill(skill.getId(), true);
		}

		// Выдаем автоизучающиеся умения.
		List<SkillLearn> skillLearns = new ArrayList<SkillLearn>(SkillAcquireHolder.getInstance().getAvailableNextLevelsSkills(null, null, AcquireType.CLAN, this, null));
		Collections.sort(skillLearns);
		Collections.reverse(skillLearns);

		IntObjectMap<SkillLearn> skillsToLearnMap = new HashIntObjectMap<SkillLearn>();
		for(SkillLearn sl : skillLearns)
		{
			if(!sl.isFreeAutoGet(AcquireType.CLAN))
			{
				// Если предыдущий уровень умения учится НЕ БЕСПЛАТНО, то не учим бесплатно больший уровень умения.
				skillsToLearnMap.remove(sl.getId());
				continue;
			}

			if(!skillsToLearnMap.containsKey(sl.getId()))
				skillsToLearnMap.put(sl.getId(), sl);
		}

		for(SkillLearn sl : skillsToLearnMap.valueCollection())
		{
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, sl.getId(), sl.getLevel());
			if(skillEntry == null)
				continue;

			addSkill(skillEntry, true);
		}

		int arenaSkillLevel = getArenaStage() / 5;
		if(getSkillLevel(55887) != arenaSkillLevel)
			removeSkill(55887, false);

		if(arenaSkillLevel > 0)
		{
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE,55887, arenaSkillLevel);
			if(skillEntry != null)
				addSkill(skillEntry, false);
		}
	}

	public int getArenaStage()
	{
		return arenaStage;
	}

	public void setArenaStage(int value)
	{
		arenaStage = value;
	}

	public String getAnnounce() {
		return _announce;
	}

	public void setAnnounce(String _announce) {
		this._announce = _announce;
	}

	public boolean isShowAnnounceOnEnter() {
		return _showAnnounceOnEnter;
	}

	public void setShowAnnounceOnEnter(boolean _showAnnounceOnEnter) {
		this._showAnnounceOnEnter = _showAnnounceOnEnter;
	}
}