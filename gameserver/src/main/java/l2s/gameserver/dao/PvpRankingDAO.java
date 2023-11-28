package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nexvill
 */
public class PvpRankingDAO
{
	private static final Logger _log = LoggerFactory.getLogger(PvpRankingDAO.class);
	private static final PvpRankingDAO _instance = new PvpRankingDAO();

	public static final String INSERT_QUERY = "INSERT INTO pvp_ranking_data (obj_Id, kills, deaths, points, week) VALUES (?,?,?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE pvp_ranking_data SET kills=?, deaths=?, points=? WHERE obj_id=? AND week=0";
	public static final String UPDATE_WEEK_QUERY = "UPDATE pvp_ranking_data SET week=1 WHERE week=0";
	
	public static PvpRankingDAO getInstance()
	{
		return _instance;
	}
	
	public void insert(int playerObjId, int kills, int deaths, int points)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_QUERY);
			statement.setInt(1, playerObjId);
			statement.setInt(2, kills);
			statement.setInt(3, deaths);
			statement.setInt(4, points);
			statement.setInt(5, 0);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("PvpRankingDAO:insert(playerObjId,kills,deaths,points)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void update(int playerObjId, int kills, int deaths, int points)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_QUERY);
			statement.setInt(1, kills);
			statement.setInt(2, deaths);
			statement.setInt(3, points);
			statement.setInt(4, playerObjId);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("PvpRankingDAO:update(playerObjId,kills,deaths,points)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void updateWeek()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_WEEK_QUERY);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("PvpRankingDAO:updateWeek()", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int selectById(int killerId) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int count = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select count(*) from pvp_ranking_data where obj_id=?");
			statement.setInt(1, killerId);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}
			resultSet.close();
		}
		catch(final Exception e)
		{
			_log.error("PvpRankingDAO:updateWeek()", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return count;
	}
}
