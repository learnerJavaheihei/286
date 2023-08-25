package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.RandomCraftInfo;

/**
 * @author nexvill
 */
public class CharacterRandomCraftDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterRandomCraftDAO.class);

	private static final CharacterRandomCraftDAO _instance = new CharacterRandomCraftDAO();

	private static final String INSERT_QUERY = "INSERT INTO character_random_craft_info (char_id,slot_id,item_id,result_id,count,enchantLevel,locked,refreshToUnlock) VALUES(?,?,?,?,?,?,?,?)";
	private static final String SELECT_QUERY = "SELECT * FROM character_random_craft_info WHERE char_id = ?";
	private static final String DELETE_QUERY = "DELETE FROM character_random_craft_info WHERE char_id=?";
	public static CharacterRandomCraftDAO getInstance()
	{
		return _instance;
	}

	public Map<Integer, RandomCraftInfo> restore(int objectId)
	{
		Map<Integer, RandomCraftInfo> result = new HashMap<Integer, RandomCraftInfo>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_QUERY);
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				int slotId = rset.getInt("slot_id");
				int itemId = rset.getInt("item_id");
				int resultId = rset.getInt("result_id");
				long count = rset.getLong("count");
				byte enchantLevel = rset.getByte("enchantLevel");
				boolean locked = rset.getBoolean("locked");
				byte refreshToUnlock = rset.getByte("refreshToUnlock");
				result.put(slotId, new RandomCraftInfo(itemId, resultId, count, enchantLevel, locked, refreshToUnlock));
			}
		}
		catch(Exception e)
		{
			_log.error("CharacterRandomCraftDAO.restore(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}
	
	public void insert(Player owner, Map<Integer, RandomCraftInfo> list)
	{
		Map<Integer, RandomCraftInfo> info = list;
		if (info.isEmpty())
			owner.generateRandomCraftList();
		for (int i = 0; i < 5; i++)
		{
			Connection con = null;
			PreparedStatement statement = null;
			RandomCraftInfo data = info.get(i);
			int itemId = data.getId();
			int resultId = data.getResultId();
			long count = data.getCount();
			byte enchantLevel = data.getEnchantLevel();
			boolean locked = data.isLocked();
			byte refreshToUnlock = data.getRefreshToUnlockCount();
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(INSERT_QUERY);
				statement.setInt(1, owner.getObjectId());
				statement.setInt(2, i);
				statement.setInt(3, itemId);
				statement.setInt(4, resultId);
				statement.setLong(5, count);
				statement.setInt(6, enchantLevel);
				statement.setBoolean(7, locked);
				statement.setInt(8, refreshToUnlock);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.warn(getClass().getSimpleName() + " could not add random craft info. owner: " + owner.getName() + ", slotId: " + i, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}
	
	public void delete(int ownerId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_QUERY);
			statement.setInt(1, ownerId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": could not delete random craft info. ownerId: " + ownerId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
