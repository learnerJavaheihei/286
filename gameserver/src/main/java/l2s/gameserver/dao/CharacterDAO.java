package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;

import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterDAO.class);

	private static CharacterDAO _instance = new CharacterDAO();

	public static CharacterDAO getInstance()
	{
		return _instance;
	}

	public void deleteCharByObjId(int objid)
	{
		if(objid < 0)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean insert(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `characters` (account_name, obj_Id, char_name, face, beautyFace, hairStyle, beautyHairStyle, hairColor, beautyHairColor, sex, karma, pvpkills, pkkills, clanid, createtime, deletetime, title, accesslevel, online, leaveclan, deleteclan, nochannel, pledge_type, pledge_rank, lvl_joined_academy, apprentice, used_world_chat_points, hide_head_accessories) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, player.getObjectId());
			statement.setString(3, player.getName());
			statement.setInt(4, player.getFace());
			statement.setInt(5, player.getBeautyFace());
			statement.setInt(6, player.getHairStyle());
			statement.setInt(7, player.getBeautyHairStyle());
			statement.setInt(8, player.getHairColor());
			statement.setInt(9, player.getBeautyHairColor());
			statement.setInt(10, player.getSex().ordinal());
			statement.setInt(11, player.getKarma());
			statement.setInt(12, player.getPvpKills());
			statement.setInt(13, player.getPkKills());
			statement.setInt(14, player.getClanId());
			statement.setLong(15, player.getCreateTime() / 1000);
			statement.setInt(16, player.getDeleteTimer());
			statement.setString(17, player.getTitle());
			statement.setInt(18, player.getAccessLevel());
			statement.setInt(19, player.isOnline() ? 1 : 0);
			statement.setLong(20, player.getLeaveClanTime() / 1000);
			statement.setLong(21, player.getDeleteClanTime() / 1000);
			statement.setLong(22, player.getNoChannel() > 0 ? player.getNoChannel() / 1000 : player.getNoChannel());
			statement.setInt(23, player.getPledgeType());
			statement.setInt(24, player.getPowerGrade());
			statement.setInt(25, player.getLvlJoinedAcademy());
			statement.setInt(26, player.getApprentice());
			statement.setInt(27, player.getUsedWorldChatPoints());
			statement.setInt(28, player.hideHeadAccessories() ? 1 : 0);
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.error("", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public int getObjectIdByName(String name)
	{
		int result = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("CharNameTable.getObjectIdByName(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return result;
	}

	public String getNameByObjectId(int objectId, boolean nullable)
	{
		String result = nullable ? null : StringUtils.EMPTY;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getString(1);
		}
		catch(Exception e)
		{
			_log.error("CharNameTable.getObjectIdByName(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return result;
	}

	public String getNameByObjectId(int objectId)
	{
		return getNameByObjectId(objectId, false);
	}

	public String getAccNameByName(String n)
	{
		String result = StringUtils.EMPTY;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getString(1);
		}
		catch(Exception e)
		{
			_log.error("CharNameTable.getAccNameByName(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public int accountCharNumber(String account)
	{
		int number = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			rset = statement.executeQuery();
			if(rset.next())
				number = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return number;
	}

	public List<String> getPlayersNameByAccount(String account, int minAccessLevel)
	{
		final List<String> charNames = new ArrayList<String>(8);
		try (Connection con = DatabaseFactory.getInstance().getConnection();
		     PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?" + (minAccessLevel > Integer.MIN_VALUE ? " AND accesslevel >= 0" : "")))
		{
			statement.setString(1, account);
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
					charNames.add(rset.getString("char_name"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while loading Char Names From Account: " + account, e);
		}
		return charNames;
	}

	public List<String> getPlayersNameByAccount(String account)
	{
		return getPlayersNameByAccount(account, Integer.MIN_VALUE);
	}

	public List<Integer> getPlayersIdByAccount(String account, int minAccessLevel)
	{
		final List<Integer> charIds = new ArrayList<Integer>(8);
		try (Connection con = DatabaseFactory.getInstance().getConnection();
		     PreparedStatement statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE account_name=?" + (minAccessLevel > Integer.MIN_VALUE ? " AND accesslevel >= 0" : "")))
		{
			statement.setString(1, account);
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
					charIds.add(rset.getInt("obj_Id"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while loading Char IDs From Account: " + account, e);
		}
		return charIds;
	}

	public List<Integer> getPlayersIdByAccount(String account)
	{
		return getPlayersIdByAccount(account, Integer.MIN_VALUE);
	}

	public IntSet getAllPlayersObjectIds()
	{
		IntSet set = new HashIntSet();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters");
			rset = statement.executeQuery();
			while(rset.next())
			{
				set.add(rset.getInt(1));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return set;
	}

	public String getLastIPByName(final String n)
	{
		String ip = null;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT last_ip FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				ip = rset.getString(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return ip != null ? ip : "";
	}

	public String getLastHWIDByName(final String n)
	{
		String hwid = null;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT last_hwid FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				hwid = rset.getString(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return hwid != null ? hwid : "";
	}
}