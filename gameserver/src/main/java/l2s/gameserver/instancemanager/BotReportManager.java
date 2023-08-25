package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.BotReportPropertiesHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.BotPunishment;

import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.HashIntLongMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BiggBoss
 */
public final class BotReportManager
{
	/**
	 * Represents the info about a reporter
	 */
	private final class ReporterCharData
	{
		private long _lastReport;
		private byte _reportPoints;

		public ReporterCharData()
		{
			_reportPoints = 7;
			_lastReport = 0;
		}

		public void registerReport(long time)
		{
			_reportPoints -= 1;
			_lastReport = time;
		}

		public long getLastReporTime()
		{
			return _lastReport;
		}

		public byte getPointsLeft()
		{
			return _reportPoints;
		}

		public void setPoints(int points)
		{
			_reportPoints = (byte) points;
		}
	}

	/**
	 * Represents the info about a reported character
	 */
	private final class ReportedCharData
	{
		private final IntLongMap _reporters;

		public ReportedCharData()
		{
			_reporters = new HashIntLongMap();
		}

		public IntLongMap getReporters()
		{
			return _reporters;
		}

		public int getReportCount()
		{
			return _reporters.size();
		}

		public boolean alredyReportedBy(int objectId)
		{
			return _reporters.containsKey(objectId);
		}

		public void addReporter(int objectId, long reportTime)
		{
			_reporters.put(objectId, reportTime);
		}

		public boolean reportedBySameClan(Clan clan)
		{
			if(clan == null)
				return false;

			for(int reporterId : _reporters.keySet().toArray())
			{
				if(clan.isAnyMember(reporterId))
					return true;
			}
			return false;
		}
	}

	private class ResetPointTask implements Runnable
	{
		@Override
		public void run()
		{
			resetPointsAndSchedule();
		}
	}

	private class SaveReportedTask implements Runnable
	{
		@Override
		public void run()
		{
			saveReportedCharData();
			_log.info("BotReportManager: Reports saved.");
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(BotReportManager.class);

	private static final BotReportManager _instance = new BotReportManager();

	public static BotReportManager getInstance()
	{
		return _instance;
	}

	private static final int SAVE_REPORTED_TASK_DELAY = 30 * 60 * 1000; // 30 Minutes

	private static final int COLUMN_BOT_ID = 1;
	private static final int COLUMN_REPORTER_ID = 2;
	private static final int COLUMN_REPORT_TIME = 3;

	private static final String SQL_LOAD_REPORTED_CHAR_DATA = "SELECT * FROM bot_reported_char_data";
	private static final String SQL_INSERT_REPORTED_CHAR_DATA = "INSERT INTO bot_reported_char_data VALUES (?,?,?)";
	private static final String SQL_CLEAR_REPORTED_CHAR_DATA = "DELETE FROM bot_reported_char_data";

	private final IntLongMap _ipRegistry;
	private final IntObjectMap<ReporterCharData> _charRegistry;
	private final IntObjectMap<ReportedCharData> _reports;

	private BotReportManager()
	{
		if(Config.BOTREPORT_ENABLED)
		{
			_ipRegistry = new HashIntLongMap();
			_charRegistry = new CHashIntObjectMap<ReporterCharData>();
			_reports = new CHashIntObjectMap<ReportedCharData>();

			loadReportedCharData();
			scheduleResetPointTask();

			ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveReportedTask(), SAVE_REPORTED_TASK_DELAY, SAVE_REPORTED_TASK_DELAY);
		}
		else
		{
			_ipRegistry = null;
			_charRegistry = null;
			_reports = null;
		}
	}

	/**
	 * Loads all reports of each reported bot into this cache class.<br>
	 * Warning: Heavy method, used only on server start up
	 */
	private void loadReportedCharData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_LOAD_REPORTED_CHAR_DATA);
			rset = statement.executeQuery();

			while(rset.next())
			{
				final int botId = rset.getInt(COLUMN_BOT_ID);
				final int reporter = rset.getInt(COLUMN_REPORTER_ID);
				final long date = rset.getLong(COLUMN_REPORT_TIME);
				if(_reports.containsKey(botId))
					_reports.get(botId).addReporter(reporter, date);
				else
				{
					final ReportedCharData rcd = new ReportedCharData();
					rcd.addReporter(reporter, date);
					_reports.put(rset.getInt(COLUMN_BOT_ID), rcd);
				}

				SchedulingPattern resetTimePattern = new SchedulingPattern(Config.BOTREPORT_REPORTS_RESET_TIME);
				if(resetTimePattern.next(date) > System.currentTimeMillis())
				{
					ReporterCharData rcd = _charRegistry.get(reporter);
					if(rcd != null)
						rcd.setPoints(rcd.getPointsLeft() - 1);
					else
					{
						rcd = new ReporterCharData();
						rcd.setPoints(6);
						_charRegistry.put(reporter, rcd);
					}
				}
			}
			_log.info("BotReportManager: Loaded " + _reports.size() + " bot reports.");
		}
		catch(Exception e)
		{
			_log.warn("BotReportManager: Could not load reported char data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	/**
	 * Save all reports for each reported bot down to database.<br>
	 * Warning: Heavy method, used only at server shutdown
	 */
	public void saveReportedCharData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_CLEAR_REPORTED_CHAR_DATA);
			statement.execute();

			DbUtils.closeQuietly(statement);

			statement = con.prepareStatement(SQL_INSERT_REPORTED_CHAR_DATA);
			for(IntObjectPair<ReportedCharData> entrySet : _reports.entrySet())
			{
				final IntLongMap reportTable = entrySet.getValue().getReporters();
				for(int reporterId : reportTable.keySet().toArray())
				{
					statement.setInt(1, entrySet.getKey());
					statement.setInt(2, reporterId);
					statement.setLong(3, reportTable.get(reporterId));
					statement.execute();
				}
			}
		}
		catch(Exception e)
		{
			_log.error("BotReportManager: Could not update reported char data in database!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Attempts to perform a bot report. R/W to ip and char id registry is synchronized. Triggers bot punish management<br>
	 * @param reporter (Player who issued the report)
	 * @return True, if the report was registered, False otherwise
	 */
	public boolean reportBot(Player reporter)
	{
		if(!Config.BOTREPORT_ENABLED)
			return false;

		final GameObject target = reporter.getTarget();
		if(target == null || !target.isPlayer())
			return false;

		final Player bot = target.getPlayer();
		if(bot == null || target.getObjectId() == reporter.getObjectId())
			return false;

		if(bot.isInPeaceZone() || bot.isInZoneBattle())
		{
			reporter.sendPacket(SystemMsg.YOU_CANNOT_REPORT_A_CHARACTER_WHO_IS_IN_A_PEACE_ZONE_OR_A_BATTLEGROUND);
			return false;
		}

		if(bot.isInOlympiadMode())
		{
			reporter.sendPacket(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_YOU_CANNOT_MAKE_A_REPORT_WHILE_LOCATED_INSIDE_A_PEACE_ZONE_OR_A_BATTLEGROUND_WHILE_YOU_ARE_AN_OPPOSING_CLAN_MEMBER_DURING_A_CLAN_WAR_OR_WHILE_PARTICIPATING_IN_THE_OLYMPIAD);
			return false;
		}

		if(bot.getClan() != null && bot.getClan().isAtWarWith(reporter.getClan()))
		{
			reporter.sendPacket(SystemMsg.YOU_CANNOT_REPORT_WHEN_A_CLAN_WAR_HAS_BEEN_DECLARED);
			return false;
		}

		if(bot.getReceivedExp() == 0)
		{
			reporter.sendPacket(SystemMsg.YOU_CANNOT_REPORT_A_CHARACTER_WHO_HAS_NOT_ACQUIRED_ANY_XP_AFTER_CONNECTING);
			return false;
		}

		ReportedCharData rcd = _reports.get(bot.getObjectId());
		ReporterCharData rcdRep = _charRegistry.get(reporter.getObjectId());
		final int reporterId = reporter.getObjectId();

		synchronized(this)
		{
			if(_reports.containsKey(reporterId))
			{
				reporter.sendPacket(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_AND_CANNOT_REPORT_OTHER_USERS);
				return false;
			}

			final int ip = hashIp(reporter);
			if(!timeHasPassed(_ipRegistry, ip))
			{
				reporter.sendPacket(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_THE_TARGET_HAS_ALREADY_BEEN_REPORTED_BY_EITHER_YOUR_CLAN_OR_ALLIANCE_OR_HAS_ALREADY_BEEN_REPORTED_FROM_YOUR_CURRENT_IP);
				return false;
			}

			if(rcd != null)
			{
				if(rcd.alredyReportedBy(reporterId))
				{
					reporter.sendPacket(SystemMsg.YOU_CANNOT_REPORT_THIS_PERSON_AGAIN_AT_THIS_TIME);
					return false;
				}

				if(!Config.BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS && rcd.reportedBySameClan(reporter.getClan()))
				{
					reporter.sendPacket(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_THE_TARGET_HAS_ALREADY_BEEN_REPORTED_BY_EITHER_YOUR_CLAN_OR_ALLIANCE_OR_HAS_ALREADY_BEEN_REPORTED_FROM_YOUR_CURRENT_IP);
					return false;
				}
			}

			if(rcdRep != null)
			{
				if(rcdRep.getPointsLeft() == 0)
				{
					reporter.sendPacket(SystemMsg.YOU_HAVE_USED_ALL_AVAILABLE_POINTS_POINTS_ARE_RESET_EVERYDAY_AT_NOON);
					return false;
				}

				final long reuse = (System.currentTimeMillis() - rcdRep.getLastReporTime());
				if(reuse < Config.BOTREPORT_REPORT_DELAY)
				{
					final SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.YOU_CAN_MAKE_ANOTHER_REPORT_IN_S1MINUTES_YOU_HAVE_S2_POINTS_REMAINING_ON_THIS_ACCOUNT);
					sm.addInteger((int) (reuse / 60000));
					sm.addInteger(rcdRep.getPointsLeft());
					reporter.sendPacket(sm);
					return false;
				}
			}

			final long curTime = System.currentTimeMillis();

			if(rcd == null)
			{
				rcd = new ReportedCharData();
				_reports.put(bot.getObjectId(), rcd);
			}

			rcd.addReporter(reporterId, curTime);

			if(rcdRep == null)
				rcdRep = new ReporterCharData();

			rcdRep.registerReport(curTime);

			_ipRegistry.put(ip, curTime);
			_charRegistry.put(reporterId, rcdRep);
		}

		reporter.sendPacket(new SystemMessagePacket(SystemMsg.C1_WAS_REPORTED_AS_A_BOT).addName(bot));

		SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.YOU_HAVE_USED_A_REPORT_POINT_ON_C1_YOU_HAVE_S2_POINTS_REMAINING_ON_THIS_ACCOUNT);
		sm.addName(bot);
		sm.addInteger(rcdRep.getPointsLeft());
		reporter.sendPacket(sm);

		handleReport(bot, rcd);

		return true;
	}

	/**
	 * Find the punishs to apply to the given bot and triggers the punish method.
	 * @param bot (Player to be punished)
	 * @param rcd (RepotedCharData linked to this bot)
	 */
	private void handleReport(Player bot, ReportedCharData rcd)
	{
		// Report count punishment
		BotPunishment punishment = BotReportPropertiesHolder.getInstance().getBotPunishment(rcd.getReportCount());
		if(punishment != null)
			punishBot(bot, punishment);

		// Range punishments
		Collection<BotPunishment> punishments = BotReportPropertiesHolder.getInstance().getBotPunishments();
		for(BotPunishment p : punishments)
		{
			if(p.getNeedReportPoints() < 0 && Math.abs(p.getNeedReportPoints()) <= rcd.getReportCount())
				punishBot(bot, p);
		}
	}

	/**
	 * Applies the given punish to the bot if the action is secure
	 * @param bot (Player to punish)
	 * @param punishment (BotPunishment containing the debuff and a possible system message to send)
	 */
	private void punishBot(Player bot, BotPunishment punishment)
	{
		SkillEntry skill = SkillEntry.makeSkillEntry(SkillEntryType.NONE, punishment.getSkillId(), punishment.getSkillLevel());
		if(skill != null)
			bot.forceUseSkill(skill, bot);

		if(punishment.getMessage() != null)
			bot.sendPacket(punishment.getMessage());
	}

	private void resetPointsAndSchedule()
	{
		synchronized(_charRegistry)
		{
			for(ReporterCharData rcd : _charRegistry.valueCollection())
				rcd.setPoints(7);
		}
		scheduleResetPointTask();
	}

	private void scheduleResetPointTask()
	{
		SchedulingPattern resetTimePattern = new SchedulingPattern(Config.BOTREPORT_REPORTS_RESET_TIME);
		ThreadPoolManager.getInstance().schedule(new ResetPointTask(), resetTimePattern.next(System.currentTimeMillis()) - System.currentTimeMillis());
	}

	/**
	 * Returns a integer representative number from a connection
	 * @param player (The Player owner of the connection)
	 * @return int (hashed ip)
	 */
	private static int hashIp(Player player)
	{
		final String ip = player.getIP();
		final String[] rawByte = ip.split("\\.");
		final int[] rawIp = new int[4];
		for(int i = 0; i < 4; i++)
			rawIp[i] = Integer.parseInt(rawByte[i]);

		return rawIp[0] | (rawIp[1] << 8) | (rawIp[2] << 16) | (rawIp[3] << 24);
	}

	/**
	 * Checks and return if the abstrat barrier specified by an integer (map key) has accomplished the waiting time
	 * @param map (a Map to study (Int = barrier, Long = fully qualified unix time)
	 * @param objectId (an existent map key)
	 * @return true if the time has passed.
	 */
	private static boolean timeHasPassed(IntLongMap map, int objectId)
	{
		if(map.containsKey(objectId))
			return (System.currentTimeMillis() - map.get(objectId)) > Config.BOTREPORT_REPORT_DELAY;
		return true;
	}
}
