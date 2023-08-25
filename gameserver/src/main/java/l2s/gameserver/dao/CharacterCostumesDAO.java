package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.data.xml.holder.CostumesHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Costume;
import l2s.gameserver.model.actor.instances.player.CostumeList;
import l2s.gameserver.templates.CostumeTemplate;
import l2s.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class CharacterCostumesDAO {
	private static final CharacterCostumesDAO INSTANCE = new CharacterCostumesDAO();

	public static CharacterCostumesDAO getInstance() {
		return INSTANCE;
	}

	private static final String SELECT_QUERY = "SELECT costume_id, count, flags, shortcut_id FROM character_costumes WHERE char_id=?";
	private static final String STORE_QUERY = "REPLACE INTO character_costumes (char_id, costume_id, count, flags, shortcut_id) VALUES";
	private static final String UPDATE_QUERY = "UPDATE character_costumes SET count=?, flags=? WHERE char_id=? AND costume_id=?";
	private static final String CLEANUP_QUERY = "DELETE FROM character_costumes WHERE char_id=?";

	private static final String INSERT_QUERY = "REPLACE INTO character_costumes (char_id, costume_id, count, flags, shortcut_id) VALUES (?, ?, ?, ?, ?)";
	private static final String DELETE_QUERY = "DELETE FROM character_costumes WHERE char_id=? AND costume_id=?";

	private static final Logger LOGGER = LoggerFactory.getLogger(CharacterCostumesDAO.class);

	public void restore(Player owner, Map<Integer, Costume> costumes, Map<Integer, Integer> shortcuts) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_QUERY);
			statement.setInt(1, owner.getObjectId());
			rset = statement.executeQuery();

			while (rset.next()) {
				int costumeId = rset.getInt("costume_id");
				CostumeTemplate template = CostumesHolder.getInstance().getCostume(costumeId);
				if (template == null) {
					delete(owner, costumeId);
					continue;
				}

				int count = rset.getInt("count");
				int flags = rset.getInt("flags");
				Costume costume = new Costume(template, count, flags);
				costumes.put(costume.getId(), costume);

				int shortCutId = rset.getInt("shortcut_id");
				if (shortCutId > 0) {
					shortcuts.put(shortCutId, costume.getId());
				}
			}
		} catch (Exception e) {
			LOGGER.error("CharacterCostumesDAO.select(Player,Map): " + e, e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public boolean store(Player owner) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(CLEANUP_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.execute();

			CostumeList costumeList = owner.getCostumeList();
			Collection<Costume> costumes = costumeList.values();
			if (!costumes.isEmpty()) {
				SqlBatch b = new SqlBatch(STORE_QUERY);
				for (Costume costume : costumes) {
					StringBuilder sb = new StringBuilder("(");
					sb.append(owner.getObjectId()).append(",");
					sb.append(costume.getId()).append(",");
					sb.append(costume.getCount()).append(",");
					sb.append(costume.getFlags()).append(",");
					sb.append(costumeList.getShortCutId(costume.getId())).append(")");
					b.write(sb.toString());
				}

				if (!b.isEmpty())
					statement.executeUpdate(b.close());
			}
		} catch (Exception e) {
			LOGGER.error("CharacterCostumesDAO.store(Player): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean insert(Player owner, Costume costume) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, costume.getId());
			statement.setInt(3, costume.getCount());
			statement.setInt(4, costume.getFlags());
			statement.setInt(5, 0);
			statement.execute();
		} catch (Exception e) {
			LOGGER.error("CharacterCostumesDAO.insert(Player,Costume): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean update(Player owner, Costume costume) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_QUERY);
			statement.setInt(1, costume.getCount());
			statement.setInt(2, costume.getFlags());
			statement.setInt(3, owner.getObjectId());
			statement.setInt(4, costume.getId());
			statement.execute();
		} catch (Exception e) {
			LOGGER.error("CharacterCostumesDAO.update(Player,Costume): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean delete(Player owner, int costumeId) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_QUERY);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, costumeId);
			statement.execute();
		} catch (Exception e) {
			LOGGER.error("CharacterCostumesDAO.delete(Player,int): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
}
