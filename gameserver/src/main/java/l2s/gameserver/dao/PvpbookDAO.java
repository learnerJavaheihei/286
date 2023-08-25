package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Pvpbook;
import l2s.gameserver.model.actor.instances.player.PvpbookInfo;
import l2s.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

public class PvpbookDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CharacterCostumesDAO.class);

	private static final PvpbookDAO INSTANCE = new PvpbookDAO();

	public static PvpbookDAO getInstance() {
		return INSTANCE;
	}

	private static final String RESTORE_SQL_QUERY = "SELECT * FROM character_pvpbook WHERE char_id=?";
	private static final String STORE_SQL_QUERY = "REPLACE INTO character_pvpbook (char_id,target_id,death_time,name,level,class_id,clan_name) VALUES";
	private static final String CLEANUP_SQL_QUERY = "DELETE FROM character_pvpbook WHERE char_id=?";
	private static final String DELETE_EXPIRED_SQL_QUERY = "DELETE FROM character_pvpbook WHERE (? - death_time) > ?";

	public PvpbookDAO() {
		deleteExpired();
	}

	public boolean restore(Player player) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_SQL_QUERY);
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			Pvpbook pvpbook = player.getPvpbook();
			while (rset.next()) {
				int deathTime = rset.getInt("death_time");
				if (Pvpbook.isExpired(deathTime))
					continue;

				int targetId = rset.getInt("target_id");
				Player targetPlayer = GameObjectsStorage.getPlayer(targetId);
				if (targetPlayer != null) {
					pvpbook.addInfo(targetPlayer, deathTime);
				} else {
					String name = rset.getString("name");
					int level = rset.getInt("level");
					int classId = rset.getInt("class_id");
					String clanName = rset.getString("clan_name");
					pvpbook.addInfo(targetId, deathTime, name, level, classId, clanName);
				}
			}
		} catch (Exception e) {
			LOGGER.error("CharacterVengeancesDAO.restore(Player): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
		return true;
	}

	public boolean store(Player player) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(CLEANUP_SQL_QUERY);
			statement.setInt(1, player.getObjectId());
			statement.execute();

			SqlBatch b = new SqlBatch(STORE_SQL_QUERY);
			for (PvpbookInfo pvpbookInfo : player.getPvpbook().getInfos(false)) {
				StringBuilder sb = new StringBuilder("(");
				sb.append(player.getObjectId()).append(",");
				sb.append(pvpbookInfo.getObjectId()).append(",");
				sb.append(pvpbookInfo.getDeathTime()).append(",");
				sb.append("'").append(pvpbookInfo.getName()).append("'").append(",");
				sb.append(pvpbookInfo.getLevel()).append(",");
				sb.append(pvpbookInfo.getClassId()).append(",");
				sb.append("'").append(pvpbookInfo.getClanName()).append("'").append(")");
				b.write(sb.toString());
			}
			if (!b.isEmpty())
				statement.executeUpdate(b.close());
		} catch (Exception e) {
			LOGGER.error("CharacterVengeancesDAO.store(Player): " + e, e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	private void deleteExpired() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_EXPIRED_SQL_QUERY);
			statement.setInt(1, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			statement.setInt(2, Pvpbook.EXPIRATION_DELAY);
			statement.execute();
		} catch (final Exception e) {
			LOGGER.error("CharacterVengeancesDAO:deleteExpired()", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}
}
