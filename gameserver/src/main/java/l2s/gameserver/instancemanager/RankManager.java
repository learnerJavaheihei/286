package l2s.gameserver.instancemanager;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.WorldRegion;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.StatsSet;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static sun.audio.AudioPlayer.player;

/**
 * @author NviX
 */
 //排名系統 排行榜
public class RankManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(RankManager.class);

	private static final SkillEntry SERVER_LEVEL_RANKING_1ST_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60003, 1);
	private static final SkillEntry SERVER_LEVEL_RANKING_2ND_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60004, 1);
	private static final SkillEntry SERVER_LEVEL_RANKING_3RD_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60005, 1);

	private static final SkillEntry HUMAN_LEVEL_RANKING_1ST_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60006, 1);
	private static final SkillEntry ELF_LEVEL_RANKING_1ST_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60007, 1);
	private static final SkillEntry DARK_ELF_LEVEL_RANKING_1ST_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60008, 1);
	private static final SkillEntry ORC_LEVEL_RANKING_1ST_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60009, 1);
	private static final SkillEntry DWARF_LEVEL_RANKING_1ST_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60010, 1);
	private static final SkillEntry KAMAEL_LEVEL_RANKING_1ST_CLASS = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60011, 1);

	private static final SkillEntry RACE_HUMAN_RANKING_BENEFIT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54204, 1);
	private static final SkillEntry RACE_ELF_RANKING_BENEFIT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54210, 1);
	private static final SkillEntry RACE_DARKELF_RANKING_BENEFIT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54211, 1);
	private static final SkillEntry RACE_ORC_RANKING_BENEFIT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54209, 1);
	private static final SkillEntry RACE_DWARF_RANKING_BENEFIT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54212, 1);
	private static final SkillEntry RACE_KAMAEL_RANKING_BENEFIT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54205, 1);
	
	private static final SkillEntry PVP_RANKING_BENEFIT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 52019, 1);

	public static final int PLAYER_BASIC_LIMIT = 100;
	public static final int PLAYER_LIMIT = 100;

	private static final String SELECT_CHARACTERS = "SELECT c.obj_Id,c.char_name,c.clanid,cs.class_id,cs.level FROM character_subclasses AS cs LEFT JOIN characters AS c ON cs.char_obj_id=c.obj_Id WHERE cs.type=? ORDER BY cs.exp DESC";

	private static final String GET_CURRENT_CYCLE_DATA = "SELECT c.obj_Id,c.char_name,c.clanid,cs.level,cs.class_id,op.olympiad_points,op.competitions_win,op.competitions_loose FROM olympiad_participants AS op LEFT JOIN character_subclasses AS cs ON op.char_id=cs.char_obj_id AND cs.type=? LEFT JOIN characters AS c ON op.char_id=c.obj_Id ORDER BY op.olympiad_points DESC LIMIT " + PLAYER_LIMIT;
	private static final String GET_CHARACTERS_BY_CLASS = "SELECT cs.char_obj_id FROM character_subclasses AS cs, olympiad_participants AS op WHERE cs.char_obj_id = op.char_id AND cs.type=? AND cs.class_id=? ORDER BY op.olympiad_points DESC LIMIT " + PLAYER_LIMIT;
	
	private static final String GET_PREVIOUS_OLY_DATA = "SELECT characters.sex, character_subclasses.class_id, character_subclasses.level, olympiad_participants_old.char_id, olympiad_participants_old.olympiad_points, olympiad_participants_old.competitions_win, olympiad_participants_old.competitions_loose FROM characters, character_subclasses, olympiad_participants_old WHERE characters.obj_Id = character_subclasses.char_obj_id AND character_subclasses.char_obj_id = olympiad_participants_old.char_id ORDER BY olympiad_points DESC";

	private static final String SELECT_PVP_RANKING = "SELECT c.obj_Id,c.char_name,c.clanid,cs.class_id,cs.level,prd.kills,prd.deaths,prd.points FROM character_subclasses AS cs LEFT JOIN characters AS c ON cs.char_obj_id=c.obj_Id LEFT JOIN pvp_ranking_data AS prd ON prd.obj_Id = cs.char_obj_id WHERE prd.week=? ORDER BY prd.points DESC LIMIT " + PLAYER_BASIC_LIMIT;
	
	private final Map<Integer, StatsSet> _mainList = new ConcurrentHashMap<>();
	private Map<Integer, StatsSet> _snapshotList = new ConcurrentHashMap<>();
	private final Map<Integer, StatsSet> _mainOlyList = new ConcurrentHashMap<>();
	private Map<Integer, StatsSet> _snapshotOlyList = new ConcurrentHashMap<>();
	private final Map<Integer, StatsSet> _previousOlyList = new ConcurrentHashMap<>();
	private final Map<Integer, StatsSet> _mainPvpList = new ConcurrentHashMap<>();
	private final Map<Integer, StatsSet> _oldPvpList = new ConcurrentHashMap<>();

	public static Map<String,StatsSet> rankLimit150 = new ConcurrentHashMap<>();

	public final Map<Integer,StatsSet> lastRankLimit150 = new ConcurrentHashMap<>();

	public static Map<Integer,Map<Integer,StatsSet>> _raceRankList = new ConcurrentHashMap<>();

	public static long rankLastExp = Experience.getExpForLevel(40);

	public static long[] _raceRankLastExp = new long[]{
			Experience.getExpForLevel(40),
			Experience.getExpForLevel(40),
			Experience.getExpForLevel(40),
			Experience.getExpForLevel(40),
			Experience.getExpForLevel(40),
			Experience.getExpForLevel(40)};

	protected RankManager() {
		init();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::update, 0, 300000);
	}
	private void init(){

		List<Integer> secondClass = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (ClassId classId : ClassId.values()) {
			if(classId.getClassLevel()!=null && classId.getClassLevel().ordinal() < ClassLevel.SECOND.ordinal()){
				secondClass.add(classId.getId());
				builder.append(classId.getId()+",");
			}
		}
		builder.deleteCharAt(builder.length()-1);

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT c.obj_Id,c.char_name,c.clanid,cs.class_id,cs.LEVEL,cs.exp,c.createtime " +
					"FROM character_subclasses AS cs LEFT JOIN characters AS c ON cs.char_obj_id = c.obj_Id\n" +
					"WHERE cs.type =? AND cs.`level`>=? AND accesslevel=0 AND cs.class_id NOT IN ("+builder.toString()+") ORDER BY cs.exp DESC LIMIT "+PLAYER_BASIC_LIMIT);
			statement.setInt(1, SubClassType.BASE_CLASS.ordinal());
			statement.setInt(2, 40);
			rset = statement.executeQuery();
			int i =0;
			while (rset.next()) {
				int charId = rset.getInt("obj_Id");
				i++;

				StatsSet statsSet = new StatsSet();
				statsSet.put("charId", charId);
				String charName = rset.getString("char_name");
				statsSet.put("name", charName);
				int clanId = rset.getInt("clanid");
				if (clanId > 0) {
					statsSet.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
				} else {
					statsSet.set("clanName", "");
				}
				int Id = rset.getInt("class_id");
				ClassId classId = ClassId.valueOf(Id);
				statsSet.put("classId", Id);
				statsSet.put("level", rset.getInt("LEVEL"));
				long exp = rset.getLong("exp");
				statsSet.put("exp", exp);
				statsSet.put("rank", i);
				final int race = classId.getRace().ordinal();
				statsSet.put("race", race);
				statsSet.put("createtime",rset.getLong("createtime"));

				rankLimit150.put(charName,statsSet);
				_mainList.put(i,statsSet);
				lastRankLimit150.put(i,statsSet);
				loadRaceRank(charId, race, statsSet);
				if (i==PLAYER_BASIC_LIMIT) {
					rankLastExp = exp;
				}

			}
		} catch (SQLException e) {
			LOGGER.error("RankManager: Could not load chars total rank data: " + this + " - " + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}

		loadAllRaceRank();
	}

	private void loadAllRaceRank() {

		String SELECT_CHARACTERS_BY_RACE = "";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		for (int p = 0; p < 6; p++) {
			if (p == Race.HUMAN.ordinal())
				SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level,cs.exp,c.createtime FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND cs.level>=40 AND accesslevel=0 AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 2 OR cs.class_id = 3 OR cs.class_id = 5 OR cs.class_id = 6 OR cs.class_id = 8 OR cs.class_id = 9 OR cs.class_id = 12 OR cs.class_id = 13 OR cs.class_id = 14 OR cs.class_id = 16 OR cs.class_id = 17 OR cs.class_id = 88 OR cs.class_id = 89 OR cs.class_id = 90 OR cs.class_id = 91 OR cs.class_id = 92 OR cs.class_id = 93 OR cs.class_id = 94 OR cs.class_id = 95 OR cs.class_id = 96 OR cs.class_id = 97 OR cs.class_id = 98) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC LIMIT "+PLAYER_LIMIT;
			else if (p == Race.ELF.ordinal())
				SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level,cs.exp,c.createtime FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND cs.level>=40 AND accesslevel=0 AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 20 OR cs.class_id = 21 OR cs.class_id = 23 OR cs.class_id = 24 OR cs.class_id = 27 OR cs.class_id = 28 OR cs.class_id = 30 OR cs.class_id = 99 OR cs.class_id = 100 OR cs.class_id = 101 OR cs.class_id = 102 OR cs.class_id = 103 OR cs.class_id = 104 OR cs.class_id = 105) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC LIMIT "+PLAYER_LIMIT;
			else if (p == Race.DARKELF.ordinal())
				SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level,cs.exp,c.createtime FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND cs.level>=40 AND accesslevel=0 AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 33 OR cs.class_id = 34 OR cs.class_id = 36 OR cs.class_id = 37 OR cs.class_id = 40 OR cs.class_id = 41 OR cs.class_id = 43 OR cs.class_id = 106 OR cs.class_id = 107 OR cs.class_id = 108 OR cs.class_id = 109 OR cs.class_id = 110 OR cs.class_id = 111 OR cs.class_id = 112) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC LIMIT "+PLAYER_LIMIT;
			else if (p == Race.ORC.ordinal())
				SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level,cs.exp,c.createtime FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND cs.level>=40 AND accesslevel=0 AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 46 OR cs.class_id = 48 OR cs.class_id = 51 OR cs.class_id = 52 OR cs.class_id = 113 OR cs.class_id = 114 OR cs.class_id = 115 OR cs.class_id = 116) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC LIMIT "+PLAYER_LIMIT;
			else if (p == Race.DWARF.ordinal())
				SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level,cs.exp,c.createtime FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND cs.level>=40 AND accesslevel=0 AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 55 OR cs.class_id = 57 OR cs.class_id = 117 OR cs.class_id = 118) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC LIMIT "+PLAYER_LIMIT;
			else if (p == Race.KAMAEL.ordinal())
				SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level,cs.exp,c.createtime FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND cs.level>=40 AND accesslevel=0 AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 127 OR cs.class_id = 130 OR cs.class_id = 131 OR cs.class_id = 134 OR cs.class_id = 194 OR cs.class_id = 195) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC LIMIT "+PLAYER_LIMIT;

			try {
				con = DatabaseFactory.getInstance().getConnection();
				ps = con.prepareStatement(SELECT_CHARACTERS_BY_RACE);
				rset = ps.executeQuery();
				int i = 0;
				Map<Integer,StatsSet> raceRank = new HashMap<>();
				while (rset.next()) {

					final int objId = rset.getInt("cs.char_obj_id");
					final long exp = rset.getLong("cs.exp");
					i++;
					StatsSet statsSet = new StatsSet();
					statsSet.set("charId",objId);
					statsSet.set("race",p);
					statsSet.set("exp",exp);
					statsSet.put("createtime",rset.getLong("createtime"));
					raceRank.put(objId,statsSet);
					if (i==PLAYER_LIMIT) {
						_raceRankLastExp[p] = exp;
					}
				}
				_raceRankList.put(p,raceRank);
			} catch (SQLException e) {
				LOGGER.error("Could not load chars race rank data: " + this + " - " + e.getMessage(), e);
			} finally {
				DbUtils.closeQuietly(con, ps, rset);
			}
		}
	}

	private synchronized void update() {
		List<StatsSet> _lastRaceRank1sts = _mainList.values().stream().filter(o -> 
				o.getInteger("raceRank",0) == 1).collect(Collectors.toList());
		List<StatsSet> _lastRaceRank123 = _mainList.values().stream().filter(o -> 
				o.getInteger("raceRank",0) > 0 && o.getInteger("raceRank",0)< 4).collect(Collectors.toList());

		// Load charIds All
		_snapshotList = _mainList;
		_mainList.clear();
		_snapshotOlyList = _mainOlyList;
		_mainOlyList.clear();
		_mainPvpList.clear();
		_oldPvpList.clear();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
//		try {
//			con = DatabaseFactory.getInstance().getConnection();
//			statement = con.prepareStatement(SELECT_CHARACTERS);
//			statement.setInt(1, SubClassType.BASE_CLASS.ordinal());
//			rset = statement.executeQuery();
//			int i = 1;
//			while (rset.next()) {
//				if(i >= PLAYER_LIMIT)
//					break;
//
//				final int level = rset.getInt("cs.level");
//				if(level < 40)
//					continue;
//
//				final ClassId classId = ClassId.valueOf(rset.getInt("cs.class_id"));
//				if(classId.getClassLevel().ordinal() < ClassLevel.SECOND.ordinal())
//					continue;
//
//				final int charId = rset.getInt("c.obj_Id");
//				if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
//					PlayerAccess playerAccess = Config.gmlist.get(charId);
//					if (playerAccess != null && playerAccess.IsGM)
//						continue;
//				}
//				final StatsSet player = new StatsSet();
//				player.set("charId", charId);
//				player.set("name", rset.getString("c.char_name"));
//				player.set("level", level);
//				player.set("classId", classId.getId());
//				final int race = classId.getRace().ordinal();
//				player.set("race", race);
//
//				loadRaceRank(charId, race, player);
//				final int clanId = rset.getInt("c.clanid");
//				if (clanId > 0) {
//					player.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
//				} else {
//					player.set("clanName", "");
//				}
//
//				_mainList.put(i, player);
//				i++;
//			}
//		} catch (SQLException e) {
//			LOGGER.error("RankManager: Could not load chars total rank data: " + this + " - " + e.getMessage(), e);
//		} finally {
//			DbUtils.closeQuietly(con, statement, rset);
//		}	12
//		ThreadPoolManager.getInstance().execute(this::sortGlobalRank);
//		ThreadPoolManager.getInstance().execute(this::sortRaceRank);
		sortGlobalRank();
		sortRaceRank();
		LOGGER.info("RankManager: Restored " + _mainList.size() + " ranking data(s).");

		ThreadPoolManager.getInstance().execute(this::checkUpdateRankBuff);
		updateRaceBuff(_lastRaceRank1sts);
		updateRaceSkill(_lastRaceRank123);

		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_CURRENT_CYCLE_DATA);
			statement.setInt(1, SubClassType.BASE_CLASS.ordinal());
			rset = statement.executeQuery();
			int i = 1;
			while (rset.next()) {
				final StatsSet player = new StatsSet();
				final int charId = rset.getInt("c.obj_Id");
				player.set("charId", charId);
				player.set("name", rset.getString("c.char_name"));
				final int clanId = rset.getInt("c.clanid");
				if (clanId > 0) {
					player.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
				} else {
					player.set("clanName", "");
				}
				player.set("level", rset.getInt("cs.level"));
				final int classId = rset.getInt("cs.class_id");
				player.set("classId", classId);
				if (clanId > 0) {
					player.set("clanLevel", ClanTable.getInstance().getClan(clanId).getLevel());
				} else {
					player.set("clanLevel", 0);
				}
				player.set("competitions_win", rset.getInt("op.competitions_win"));
				player.set("competitions_loose", rset.getInt("op.competitions_loose"));
				player.set("olympiad_points", rset.getInt("op.olympiad_points"));

				StatsSet hero = Hero.getInstance().getHeroes().get(charId);
				if (hero != null) {
					player.set(Hero.COUNT, hero.getInteger(Hero.COUNT, 0));
					player.set("legend_count", 0); // TODO
				} else {
					player.set(Hero.COUNT, 0);
					player.set("legend_count", 0);
				}

				loadClassRank(charId, classId, player);

				_mainOlyList.put(i, player);
				i++;
			}
		} catch (SQLException e) {
			LOGGER.error("RankManager: Could not load olympiad total rank data: " + this + " - " + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}

		LOGGER.info("RankManager: Restored " + _mainOlyList.size() + " olympiad ranking data(s).");
		
		// load previous month oly data
		_previousOlyList.clear();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_PREVIOUS_OLY_DATA);
			rset = statement.executeQuery();
			int i = 1;
			while (rset.next())
			{
				final StatsSet player = new StatsSet();
				
				player.set("objId", rset.getInt("olympiad_participants_old.char_id"));
				player.set("classId", rset.getInt("character_subclasses.class_id"));
				player.set("sex", rset.getInt("characters.sex"));
				player.set("level", rset.getInt("character_subclasses.level"));
				player.set("olympiad_points", rset.getInt("olympiad_participants_old.olympiad_points"));
				player.set("competitions_win", rset.getInt("olympiad_participants_old.competitions_win"));
				player.set("competitions_lost", rset.getInt("olympiad_participants_old.competitions_loose"));
				_previousOlyList.put(i, player);
				i++;
			}
		}
		catch (Exception e)
		{
			LOGGER.error("RankManager: Could not load previous month olympiad data: " + this + " - " + e.getMessage(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		// load pvp ranking data this week
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_PVP_RANKING);
			statement.setInt(1, 0);
			rset = statement.executeQuery();
			int i = 1;
			while (rset.next())
			{
				if(i > PLAYER_BASIC_LIMIT)
					break;

				final int level = rset.getInt("cs.level");
				if(level < 40)
					continue;

				final ClassId classId = ClassId.valueOf(rset.getInt("cs.class_id"));
				if(classId.getClassLevel().ordinal() < ClassLevel.SECOND.ordinal())
					continue;

				final int charId = rset.getInt("c.obj_Id");
				if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
					PlayerAccess playerAccess = Config.gmlist.get(charId);
					if (playerAccess != null && playerAccess.IsGM)
						continue;
				}
				
				final StatsSet player = new StatsSet();
				player.set("charId", charId);
				player.set("name", rset.getString("c.char_name"));
				final int clanId = rset.getInt("c.clanid");
				if (clanId > 0) 
				{
					player.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
				} 
				else 
				{
					player.set("clanName", "");
				}
				player.set("classId", classId.getId());
				final int race = classId.getRace().ordinal();
				player.set("race", race);
				player.set("level", rset.getInt("cs.level"));
				player.set("kills", rset.getInt("prd.kills"));
				player.set("deaths", rset.getInt("prd.deaths"));
				player.set("points", rset.getInt("prd.points"));
				
//				loadRaceRank(charId, race, player);
				
				_mainPvpList.put(i, player);
				i++;
			}
		}
		catch (SQLException e) 
		{
			LOGGER.error("RankManager: Could not load pvp rank data: " + this + " - " + e.getMessage(), e);
		} 
		finally 
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		LOGGER.info("RankManager: Restored " + _mainPvpList.size() + " pvp ranking data(s) this week.");
		
		// load pvp ranking data previous week
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_PVP_RANKING);
			statement.setInt(1, 1);
			rset = statement.executeQuery();
			int i = 1;
			while (rset.next())
			{
				if(i > PLAYER_BASIC_LIMIT)
					break;

				final int level = rset.getInt("cs.level");
				if(level < 40)
					continue;

				final ClassId classId = ClassId.valueOf(rset.getInt("cs.class_id"));
				if(classId.getClassLevel().ordinal() < ClassLevel.SECOND.ordinal())
					continue;

				final int charId = rset.getInt("c.obj_Id");
				if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
					PlayerAccess playerAccess = Config.gmlist.get(charId);
					if (playerAccess != null && playerAccess.IsGM)
						continue;
				}
				
				final StatsSet player = new StatsSet();
				player.set("charId", charId);
				player.set("name", rset.getString("c.char_name"));
				final int clanId = rset.getInt("c.clanid");
				if (clanId > 0) 
				{
					player.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
				} 
				else 
				{
					player.set("clanName", "");
				}
				player.set("classId", classId.getId());
				final int race = classId.getRace().ordinal();
				player.set("race", race);
				player.set("level", rset.getInt("cs.level"));
				player.set("kills", rset.getInt("prd.kills"));
				player.set("deaths", rset.getInt("prd.deaths"));
				player.set("points", rset.getInt("prd.points"));
				
//				loadRaceRank(charId, race, player);
				
				_oldPvpList.put(i, player);
				i++;
			}
		}
		catch (SQLException e) 
		{
			LOGGER.error("RankManager: Could not load pvp rank data: " + this + " - " + e.getMessage(), e);
		} 
		finally 
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		LOGGER.info("RankManager: Restored " + _oldPvpList.size() + " pvp ranking data(s) previous week.");
	}

	private void sortRaceRank() {
		for (int i = 0; i < _raceRankList.size(); i++) {
			Map<Integer, StatsSet> setMap = _raceRankList.get(i);

			Collection<StatsSet> kindList = setMap.values();
			if (kindList.isEmpty()){
				_raceRankLastExp[i] = Experience.getExpForLevel(40);
				continue;
			}

			List<StatsSet> statsSets = new LinkedList<>(kindList);
			statsSets.sort((o1, o2) -> {
                if (o2.getLong("exp") == o1.getLong("exp")) {
                    return Long.compare(o1.getLong("createtime",0), o2.getLong("createtime",0));
                }
                return Long.compare(o2.getLong("exp"), o1.getLong("exp"));
            });

			statsSets = statsSets.subList(0, Math.min(statsSets.size(), PLAYER_LIMIT));
			Map<Integer, StatsSet> map = new HashMap<>();

			for (int j = 0; j < statsSets.size(); j++) {
				StatsSet race = statsSets.get(j);
				for (Integer integer : _mainList.keySet()) {
					StatsSet main = _mainList.get(integer);
					if (main.getInteger("charId") == race.getInteger("charId")) {
						main.set("raceRank",j + 1);
						_mainList.put(integer,main);
						break;
					}
				}
				map.put(race.getInteger("charId"),race);
			}
			_raceRankList.put(i,map);
			_raceRankLastExp[i] = statsSets.get(Math.max(0,statsSets.size() -1)).getLong("exp");
		}
	}

	private void sortGlobalRank() {
		Collection<StatsSet> values = rankLimit150.values();
		if (values.isEmpty())
			return;

		List<StatsSet> list = new LinkedList<>(values);
		Collections.sort(list, (o1, o2) -> {
            if (o2.getLong("exp") == o1.getLong("exp")) {
                return Long.compare(o1.getLong("createtime",0), o2.getLong("createtime",0));
            }
            return Long.compare(o2.getLong("exp"), o1.getLong("exp"));
        });
		list = list.subList(0, Math.min(list.size(),PLAYER_BASIC_LIMIT));
		rankLastExp = list.get(Math.max(0,list.size() -1)).getLong("exp");
		rankLimit150.clear();
		for (int i = 0; i < list.size(); i++) {
			StatsSet set = list.get(i);
			int rank = i+1;
			set.set("rank",rank);
			_mainList.put(set.getInteger("rank"),set);

			rankLimit150.put(set.getString("name"),set);
		}
	}

	private void checkUpdateRankBuff() {
		String[] oldRank = new String[lastRankLimit150.size()];
		String[] newRank = new String[_mainList.size()];

		for (Integer i : lastRankLimit150.keySet()) {
			StatsSet statsSet1 = lastRankLimit150.get(i);
			String name1 = statsSet1.getString("name","");
			oldRank[i-1]=name1;

			StatsSet statsSet2 = _mainList.get(i);
			String name2 = statsSet2.getString("name","");
			newRank[i-1]=name2;
		}

// method 1
		loop1:
		for (int i = 0; i < oldRank.length; i++) {
			loop2:
			for (int j = 0; j <newRank.length; j++) {
				if (oldRank[i].equals(newRank[j])) {
					int oRank = i+1;
					int nRank = j+1;
					if (oRank == nRank) {
						continue loop1;
					}
					else if(oRank==1){
						if (nRank>1 && nRank <=30){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_2ND_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else if(nRank <=100){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_3RD_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else
							break loop2;
					}
					else if(oRank<=30){
						if (nRank == 1){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else if (nRank <=30)
							continue loop1;
						else if(nRank <=100){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_3RD_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else
							break loop2;
					}
					else if(oRank<=100){
						if (nRank == 1){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else if (nRank <=30){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_2ND_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else if(nRank <=100)
							continue loop1;
						else
							break loop2;
					}
					else{
						if (nRank == 1){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else if (nRank <=30){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_2ND_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else if(nRank <=100){
							Player player = GameObjectsStorage.getPlayer(newRank[j]);
							if (player!=null && player.isOnline()){
								clearEffect(player);
								SERVER_LEVEL_RANKING_3RD_CLASS.getEffects(player, player);
							}
							continue loop1;
						}
						else
							continue loop1;
					}
				}
			}
			Player oldPlayer = GameObjectsStorage.getPlayer(oldRank[i]);
			if (oldPlayer!=null && oldPlayer.isOnline()){
				clearEffect(oldPlayer);
			}
		}


		loop1:
		for (int i = 0; i < newRank.length; i++) {
			loop2:
			for (int j = 0; j < oldRank.length; j++) {
				if (oldRank[j].equals(newRank[i])) {
					continue loop1;
				}
			}
			int rank = i+1;
			if (rank==1) {
				Player player = GameObjectsStorage.getPlayer(newRank[i]);
				if (player!=null && player.isOnline()){
					clearEffect(player);
					SERVER_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
				}
			}
			else if (rank <= 30){
				Player player = GameObjectsStorage.getPlayer(newRank[i]);
				if (player!=null && player.isOnline()){
					clearEffect(player);
					SERVER_LEVEL_RANKING_2ND_CLASS.getEffects(player, player);
				}
			}
			else if (rank <= 100){
				Player player = GameObjectsStorage.getPlayer(newRank[i]);
				if (player!=null && player.isOnline()){
					clearEffect(player);
					SERVER_LEVEL_RANKING_3RD_CLASS.getEffects(player, player);
				}
			}
		}
		lastRankLimit150.clear();
		for (Integer i : _mainList.keySet()) {
			lastRankLimit150.put(i,_mainList.get(i));
		}
// method 2
//		for (int i = 0; i <newRank.length; i++) {
//			if (oldRank[i].equals(newRank[i])) {
//				continue;
//			}
//			Player oldPlayer = GameObjectsStorage.getPlayer(oldRank[i]);
//			if (oldPlayer!=null && oldPlayer.isOnline()){
//				clearEffect(oldPlayer);
//			}
//			int rank = i+1;
//			if (rank==1) {
//				Player player = GameObjectsStorage.getPlayer(newRank[i]);
//				if (player!=null && player.isOnline() && !player.getAbnormalList().contains(SERVER_LEVEL_RANKING_1ST_CLASS)){
//					clearEffect(player);
//					SERVER_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
//				}
//			}
//			else if (rank <= 30){
//				Player player = GameObjectsStorage.getPlayer(newRank[i]);
//				if (player!=null && player.isOnline() && !player.getAbnormalList().contains(SERVER_LEVEL_RANKING_2ND_CLASS)){
//					clearEffect(player);
//					SERVER_LEVEL_RANKING_2ND_CLASS.getEffects(player, player);
//				}
//			}
//			else if (rank <= 100){
//				Player player = GameObjectsStorage.getPlayer(newRank[i]);
//				if (player!=null && player.isOnline() && !player.getAbnormalList().contains(SERVER_LEVEL_RANKING_3RD_CLASS)){
//					clearEffect(player);
//					SERVER_LEVEL_RANKING_3RD_CLASS.getEffects(player, player);
//				}
//			}
//		}
	}
	void clearEffect(Player player){
		player.getAbnormalList().stop(SERVER_LEVEL_RANKING_1ST_CLASS, false);
		player.getAbnormalList().stop(SERVER_LEVEL_RANKING_2ND_CLASS, false);
		player.getAbnormalList().stop(SERVER_LEVEL_RANKING_3RD_CLASS, false);
	}

	private void updateRaceBuff(List<StatsSet> lastRaceRank1sts) {
		try{
			List<StatsSet> cRaceRank1sts = _mainList.values().stream().filter(o -> o.getInteger("raceRank",0) == 1).collect(Collectors.toList());

			loop1:
			for (StatsSet cRaceRank1st : cRaceRank1sts) {
				Integer race = (Integer) cRaceRank1st.get("race");
				Integer CharId = (Integer) cRaceRank1st.get("charId");
				loop2:
				for (StatsSet lastRaceRank1st : lastRaceRank1sts) {
					Integer race1 = (Integer) lastRaceRank1st.get("race");
					if (race.equals(race1)) {
						Integer lastCharId = (Integer) lastRaceRank1st.get("charId");
						if (CharId.equals(lastCharId)) {
							break loop2;
						}
						Player lPlayer = GameObjectsStorage.getPlayer(lastCharId);
						Player cPlayer = GameObjectsStorage.getPlayer(CharId);

						if (race == Race.HUMAN.ordinal()) {
							if (lPlayer!=null && lPlayer.isOnline())
								lPlayer.getAbnormalList().stop(HUMAN_LEVEL_RANKING_1ST_CLASS, false);

							if (cPlayer!=null && cPlayer.isOnline())
								HUMAN_LEVEL_RANKING_1ST_CLASS.getEffects(cPlayer, cPlayer);

						} else if (race == Race.ELF.ordinal()) {
							if (lPlayer!=null && lPlayer.isOnline())
								lPlayer.getAbnormalList().stop(ELF_LEVEL_RANKING_1ST_CLASS, false);

							if (cPlayer!=null && cPlayer.isOnline())
								ELF_LEVEL_RANKING_1ST_CLASS.getEffects(cPlayer, cPlayer);

						} else if (race == Race.DARKELF.ordinal()) {
							if (lPlayer!=null && lPlayer.isOnline())
								lPlayer.getAbnormalList().stop(DARK_ELF_LEVEL_RANKING_1ST_CLASS, false);

							if (cPlayer!=null && cPlayer.isOnline())
								DARK_ELF_LEVEL_RANKING_1ST_CLASS.getEffects(cPlayer, cPlayer);

						} else if (race == Race.ORC.ordinal()) {
							if (lPlayer!=null && lPlayer.isOnline())
								lPlayer.getAbnormalList().stop(ORC_LEVEL_RANKING_1ST_CLASS, false);

							if (cPlayer!=null && cPlayer.isOnline())
								ORC_LEVEL_RANKING_1ST_CLASS.getEffects(cPlayer, cPlayer);

						} else if (race == Race.DWARF.ordinal()) {
							if (lPlayer!=null && lPlayer.isOnline())
								lPlayer.getAbnormalList().stop(DWARF_LEVEL_RANKING_1ST_CLASS, false);

							if (cPlayer!=null && cPlayer.isOnline())
								DWARF_LEVEL_RANKING_1ST_CLASS.getEffects(cPlayer, cPlayer);

						} else if (race == Race.KAMAEL.ordinal()) {
							if (lPlayer!=null && lPlayer.isOnline())
								lPlayer.getAbnormalList().stop(KAMAEL_LEVEL_RANKING_1ST_CLASS, false);

							if (cPlayer!=null && cPlayer.isOnline())
								KAMAEL_LEVEL_RANKING_1ST_CLASS.getEffects(cPlayer, cPlayer);
						}
					}
				}
			}
		}
		catch (Exception e) {
			LOGGER.error("RankManager: update raceRank buff", e);
		}
	}
	private void updateRaceSkill(List<StatsSet> lastRaceRank123) {
		for (StatsSet statsSet : lastRaceRank123) {
			int race = statsSet.getInteger("race", 0);
			int charId = statsSet.getInteger("charId");
			Player lPlayer = GameObjectsStorage.getPlayer(charId);
			if (race == Race.HUMAN.ordinal()) {
				if (lPlayer!=null && lPlayer.isOnline())
					lPlayer.removeSkill(RACE_HUMAN_RANKING_BENEFIT, false);
			}
			else if (race == Race.ELF.ordinal()){
				if (lPlayer!=null && lPlayer.isOnline())
					lPlayer.removeSkill(RACE_ELF_RANKING_BENEFIT, false);
			}
			else if (race == Race.DARKELF.ordinal()){
				if (lPlayer!=null && lPlayer.isOnline())
					lPlayer.removeSkill(RACE_DARKELF_RANKING_BENEFIT, false);
			}
			else if (race == Race.ORC.ordinal()){
				if (lPlayer!=null && lPlayer.isOnline())
					lPlayer.removeSkill(RACE_ORC_RANKING_BENEFIT, false);
			}
			else if (race == Race.DWARF.ordinal()){
				if (lPlayer!=null && lPlayer.isOnline())
					lPlayer.removeSkill(RACE_DWARF_RANKING_BENEFIT, false);
			}
			else if (race == Race.KAMAEL.ordinal()){
				if (lPlayer!=null && lPlayer.isOnline())
					lPlayer.removeSkill(RACE_KAMAEL_RANKING_BENEFIT, false);
			}
		}
		List<StatsSet> cRaceRank123 = _mainList.values().stream().filter(o ->
				o.getInteger("raceRank",0) > 0 && o.getInteger("raceRank",0)< 4).collect(Collectors.toList());

		for (StatsSet statsSet : cRaceRank123) {
			int race = statsSet.getInteger("race", 0);
			int charId = statsSet.getInteger("charId");
			Player cPlayer = GameObjectsStorage.getPlayer(charId);
			if (race == Race.HUMAN.ordinal()) {
				if (cPlayer!=null && cPlayer.isOnline())
					cPlayer.addSkill(RACE_HUMAN_RANKING_BENEFIT, false);
			}
			else if (race == Race.ELF.ordinal()){
				if (cPlayer!=null && cPlayer.isOnline())
					cPlayer.addSkill(RACE_ELF_RANKING_BENEFIT, false);
			}
			else if (race == Race.DARKELF.ordinal()){
				if (cPlayer!=null && cPlayer.isOnline())
					cPlayer.addSkill(RACE_DARKELF_RANKING_BENEFIT, false);
			}
			else if (race == Race.ORC.ordinal()){
				if (cPlayer!=null && cPlayer.isOnline())
					cPlayer.addSkill(RACE_ORC_RANKING_BENEFIT, false);
			}
			else if (race == Race.DWARF.ordinal()){
				if (cPlayer!=null && cPlayer.isOnline())
					cPlayer.addSkill(RACE_DWARF_RANKING_BENEFIT, false);
			}
			else if (race == Race.KAMAEL.ordinal()){
				if (cPlayer!=null && cPlayer.isOnline())
					cPlayer.addSkill(RACE_KAMAEL_RANKING_BENEFIT, false);
			}
		}
	}
	private void loadClassRank(int charId, int classId, StatsSet player) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			ps = con.prepareStatement(GET_CHARACTERS_BY_CLASS);
			ps.setInt(1, classId);
			ps.setInt(2, SubClassType.BASE_CLASS.ordinal());
			rset = ps.executeQuery();
			int i = 0;
			while (rset.next()) {
				i++;
				if (rset.getInt("cs.char_obj_id") == charId) {
					player.set("classRank", i);
					break;
				}
			}
			if (i == 0) {
				player.set("classRank", 0);
			}
		} catch (Exception e) {
			LOGGER.error("Could not load chars classId olympiad rank data: " + this + " - " + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(con, ps, rset);
		}
	}

	private void loadRaceRank(int charId, int race, StatsSet player) {
		String SELECT_CHARACTERS_BY_RACE = "";
		if (race == Race.HUMAN.ordinal())
			SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 2 OR cs.class_id = 3 OR cs.class_id = 5 OR cs.class_id = 6 OR cs.class_id = 8 OR cs.class_id = 9 OR cs.class_id = 12 OR cs.class_id = 13 OR cs.class_id = 14 OR cs.class_id = 16 OR cs.class_id = 17 OR cs.class_id = 88 OR cs.class_id = 89 OR cs.class_id = 90 OR cs.class_id = 91 OR cs.class_id = 92 OR cs.class_id = 93 OR cs.class_id = 94 OR cs.class_id = 95 OR cs.class_id = 96 OR cs.class_id = 97 OR cs.class_id = 98) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC";
		else if (race == Race.ELF.ordinal())
			SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 20 OR cs.class_id = 21 OR cs.class_id = 23 OR cs.class_id = 24 OR cs.class_id = 27 OR cs.class_id = 28 OR cs.class_id = 30 OR cs.class_id = 99 OR cs.class_id = 100 OR cs.class_id = 101 OR cs.class_id = 102 OR cs.class_id = 103 OR cs.class_id = 104 OR cs.class_id = 105) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC";
		else if (race == Race.DARKELF.ordinal())
			SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 33 OR cs.class_id = 34 OR cs.class_id = 36 OR cs.class_id = 37 OR cs.class_id = 40 OR cs.class_id = 41 OR cs.class_id = 43 OR cs.class_id = 106 OR cs.class_id = 107 OR cs.class_id = 108 OR cs.class_id = 109 OR cs.class_id = 110 OR cs.class_id = 111 OR cs.class_id = 112) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC";
		else if (race == Race.ORC.ordinal())
			SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 46 OR cs.class_id = 48 OR cs.class_id = 51 OR cs.class_id = 52 OR cs.class_id = 113 OR cs.class_id = 114 OR cs.class_id = 115 OR cs.class_id = 116) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC";
		else if (race == Race.DWARF.ordinal())
			SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 55 OR cs.class_id = 57 OR cs.class_id = 117 OR cs.class_id = 118) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC";
		else if (race == Race.KAMAEL.ordinal())
			SELECT_CHARACTERS_BY_RACE = "SELECT cs.char_obj_id, cs.level FROM character_subclasses AS cs, characters AS c WHERE cs.char_obj_id = c.obj_Id AND c.last_login > " + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) + " AND (cs.class_id = 127 OR cs.class_id = 130 OR cs.class_id = 131 OR cs.class_id = 134 OR cs.class_id = 194 OR cs.class_id = 195) GROUP BY cs.char_obj_id ORDER BY cs.exp DESC";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			ps = con.prepareStatement(SELECT_CHARACTERS_BY_RACE);
			rset = ps.executeQuery();
			int i = 0;
			while (rset.next()) {
				if(i >= PLAYER_LIMIT)
					break;

				final int level = rset.getInt("cs.level");
				if(level < 40)
					continue;

				final int objId = rset.getInt("cs.char_obj_id");
				if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
					PlayerAccess playerAccess = Config.gmlist.get(objId);
					if (playerAccess != null && playerAccess.IsGM)
						continue;
				}

				i++;

				if (objId == charId)
				{
					player.set("raceRank", i);
					break;
				}
			}
			if (i == 0) {
				player.set("raceRank", 0);
			}
		} catch (SQLException e) {
			LOGGER.error("Could not load chars race rank data: " + this + " - " + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(con, ps, rset);
		}
	}

	public Map<Integer, StatsSet> getRankList() {
		return _mainList;
	}

	public Map<Integer, StatsSet> getSnapshotList() {
		return _snapshotList;
	}

	public Map<Integer, StatsSet> getOlyRankList() {
		return _mainOlyList;
	}

	public Map<Integer, StatsSet> getSnapshotOlyList() {
		return _snapshotOlyList;
	}
	
	public Map<Integer, StatsSet> getPreviousOlyList()
	{
		return _previousOlyList;
	}
	
	public Map<Integer, StatsSet> getPvpRankList()
	{
		return _mainPvpList;
	}
	
	public Map<Integer, StatsSet> getOldPvpRankList()
	{
		return _oldPvpList;
	}

	public int getPlayerGlobalRank(int objectId) {
		for (Map.Entry<Integer, StatsSet> entry : _mainList.entrySet()) {
			final StatsSet stats = entry.getValue();
			if (stats.getInteger("charId") != objectId) {
				continue;
			}
			return entry.getKey();
		}
		return 0;
	}

	public int getPlayerGlobalRank(Player player) {
		return getPlayerGlobalRank(player.getObjectId());
	}

	public int getPlayerRaceRank(Player player) {
		final int playerOid = player.getObjectId();
		for (StatsSet stats : _mainList.values()) {
			if (stats.getInteger("charId") != playerOid) {
				continue;
			}
			return stats.getInteger("raceRank",0);
		}
		return 0;
	}

	public void onPlayerEnter(Player player) {
		// Remove existing effects and skills.
		player.getAbnormalList().stop(SERVER_LEVEL_RANKING_1ST_CLASS, false);
		player.getAbnormalList().stop(SERVER_LEVEL_RANKING_2ND_CLASS, false);
		player.getAbnormalList().stop(SERVER_LEVEL_RANKING_3RD_CLASS, false);
		player.getAbnormalList().stop(HUMAN_LEVEL_RANKING_1ST_CLASS, false);
		player.getAbnormalList().stop(ELF_LEVEL_RANKING_1ST_CLASS, false);
		player.getAbnormalList().stop(DARK_ELF_LEVEL_RANKING_1ST_CLASS, false);
		player.getAbnormalList().stop(ORC_LEVEL_RANKING_1ST_CLASS, false);
		player.getAbnormalList().stop(DWARF_LEVEL_RANKING_1ST_CLASS, false);
		player.getAbnormalList().stop(KAMAEL_LEVEL_RANKING_1ST_CLASS, false);
		player.removeSkill(RACE_HUMAN_RANKING_BENEFIT, false);
		player.removeSkill(RACE_ELF_RANKING_BENEFIT, false);
		player.removeSkill(RACE_DARKELF_RANKING_BENEFIT, false);
		player.removeSkill(RACE_ORC_RANKING_BENEFIT, false);
		player.removeSkill(RACE_DWARF_RANKING_BENEFIT, false);
		player.removeSkill(RACE_KAMAEL_RANKING_BENEFIT, false);
		player.getAbnormalList().stop(PVP_RANKING_BENEFIT, false);

		// Add global rank skills.
		int rank = RankManager.getInstance().getPlayerGlobalRank(player);
		if (rank > 0) {
			if (rank == 1) {
				SERVER_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
			} else if (rank <= 30) {
				SERVER_LEVEL_RANKING_2ND_CLASS.getEffects(player, player);
			} else if (rank <= 100) {
				SERVER_LEVEL_RANKING_3RD_CLASS.getEffects(player, player);
			}
		}

		// Apply race rank effects.
		final int raceRank = RankManager.getInstance().getPlayerRaceRank(player);
		if (raceRank == 1) {
			switch (player.getRace()) {
				case HUMAN: {
					HUMAN_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
					break;
				}
				case ELF: {
					ELF_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
					break;
				}
				case DARKELF: {
					DARK_ELF_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
					break;
				}
				case ORC: {
					ORC_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
					break;
				}
				case DWARF: {
					DWARF_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
					break;
				}
				case KAMAEL: {
					KAMAEL_LEVEL_RANKING_1ST_CLASS.getEffects(player, player);
					break;
				}
			}
			if (player.getRace() == Race.KAMAEL)
				player.addSkill(RACE_KAMAEL_RANKING_BENEFIT, false);
			else if (player.getRace() == Race.HUMAN)
				player.addSkill(RACE_HUMAN_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.ELF)
				player.addSkill(RACE_ELF_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.DARKELF)
				player.addSkill(RACE_DARKELF_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.ORC)
				player.addSkill(RACE_ORC_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.DWARF)
				player.addSkill(RACE_DWARF_RANKING_BENEFIT, false);
			
			int pvpRank = player.getPreviousPvpRank();
			if ((pvpRank > 0) && (pvpRank < 4))
			{
				PVP_RANKING_BENEFIT.getEffects(player, player);
			}
		}
		if(raceRank == 2 || raceRank == 3) 
		{
			if (player.getRace() == Race.KAMAEL)
				player.addSkill(RACE_KAMAEL_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.HUMAN)
				player.addSkill(RACE_HUMAN_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.ELF)
				player.addSkill(RACE_ELF_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.DARKELF)
				player.addSkill(RACE_DARKELF_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.ORC)
				player.addSkill(RACE_ORC_RANKING_BENEFIT, false);
			else if(player.getRace() == Race.DWARF)
				player.addSkill(RACE_DWARF_RANKING_BENEFIT, false);
		}
	}

	public static RankManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		protected static final RankManager INSTANCE = new RankManager();
	}
}
