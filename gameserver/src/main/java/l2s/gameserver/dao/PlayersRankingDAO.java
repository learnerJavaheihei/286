package l2s.gameserver.dao;

import l2s.commons.dao.JdbcDAO;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dao.JdbcEntityStats;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.model.entity.ranking.player.PlayerRankData;
import l2s.gameserver.model.entity.ranking.player.PlayerRanking;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class PlayersRankingDAO implements JdbcDAO<Integer, PlayerRankData> {
    private final static PlayersRankingDAO INSTANCE = new PlayersRankingDAO();
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayersRankingDAO.class);
    private static final String SELECT_ALL_SQL_QUERY = "SELECT c.obj_Id, c.char_name, c.clanid, c.lastAccess, cs.class_id, cs.level, (SELECT SUM(exp) as total_exp FROM character_subclasses WHERE char_obj_id = c.obj_Id) AS total_exp FROM characters c LEFT JOIN character_subclasses cs ON cs.char_obj_id = c.obj_Id AND cs.type = 0";
    private static final String SELECT_RANKS_SQL_QUERY = "SELECT `cycle`, `rank`, rank_race, exp_received FROM players_ranking WHERE char_obj_id = ?";
    private static final String REPLACE_SQL_QUERY = "REPLACE INTO players_ranking (char_obj_id,`cycle`,`rank`,rank_race,exp_received) VALUES(?,?,?,?,?)";
    private static final String UPDATE_SQL_QUERY = "UPDATE players_ranking SET `rank`=?, rank_race=?, exp_received=? WHERE char_obj_id=? AND `cycle`=?";
    private static final String DELETE_SQL_QUERY = "DELETE FROM players_ranking WHERE char_obj_id=? AND `cycle`=?";
    private final AtomicLong load = new AtomicLong();
    private final AtomicLong insert = new AtomicLong();
    private final AtomicLong update = new AtomicLong();
    private final AtomicLong delete = new AtomicLong();
    private final JdbcEntityStats stats = new JdbcEntityStats() {
        @Override
        public long getLoadCount() {
            return load.get();
        }

        @Override
        public long getInsertCount() {
            return insert.get();
        }

        @Override
        public long getUpdateCount() {
            return update.get();
        }

        @Override
        public long getDeleteCount() {
            return delete.get();
        }
    };

    public static PlayersRankingDAO getInstance() {
        return INSTANCE;
    }

    @Override
    public PlayerRankData load(Integer key) {
        throw new UnsupportedOperationException();
    }

    public void loadAll(Map<Integer, PlayerRanking> rankingInfos) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_ALL_SQL_QUERY);
            rset = statement.executeQuery();
            loop:
            while (rset.next()) {
                int objectId = rset.getInt("c.obj_Id");
//                for (int gmObjId : Config.TEMPORAL_GM_ID) {
//                    if (gmObjId == objectId)
//                        continue loop;
//                }

                if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
                    PlayerAccess playerAccess = Config.gmlist.get(objectId);
                    if (playerAccess != null && playerAccess.IsGM)
                        continue;
                }

                ClassId class_id = ClassId.valueOf(rset.getInt("cs.class_id"));
                int level = rset.getInt("cs.level");
                if (!PlayerRankingManager.getInstance().canHaveRank(level, class_id))
                    continue;

                String char_name = rset.getString("c.char_name");
                int clanid = rset.getInt("c.clanid");
                int lastAccess = rset.getInt("c.lastAccess");
                long exp = rset.getLong("total_exp");

                PlayerRanking playerRanking = new PlayerRanking(objectId, char_name, clanid, lastAccess, class_id, level, exp);
                rankingInfos.put(playerRanking.getObjectId(), playerRanking);
                load.incrementAndGet();
            }
        } catch (Exception e) {
            LOGGER.error("PlayersRankingDAO.loadAll(Map): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        rankingInfos.values().forEach(this::loadRanks);
    }

    private void loadRanks(PlayerRanking playerRanking) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_RANKS_SQL_QUERY);
            statement.setInt(1, playerRanking.getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                int cycle = rset.getInt("cycle");
                int rank = rset.getInt("rank");
                int rankRace = rset.getInt("rank_race");
                long expReceived = rset.getLong("exp_received");
                PlayerRankData rankData = new PlayerRankData(playerRanking.getObjectId(), cycle, rank, rankRace, expReceived);
                rankData.setJdbcState(JdbcEntityState.STORED);
                playerRanking.addRankData(rankData);
            }
            playerRanking.cleanUpRankDatas();
        } catch (Exception e) {
            LOGGER.error("PlayersRankingDAO.loadRanks(PlayerRanking): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    @Override
    public void save(PlayerRankData rankInfo) {
        if (save0(rankInfo)) {
            rankInfo.setJdbcState(JdbcEntityState.STORED);
        }
    }

    private boolean save0(PlayerRankData rankInfo) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(REPLACE_SQL_QUERY);
            int i = 0;
            statement.setInt(++i, rankInfo.getObjectId());
            statement.setInt(++i, rankInfo.getCycle());
            statement.setInt(++i, rankInfo.getRank());
            statement.setInt(++i, rankInfo.getRaceRank());
            statement.setLong(++i, rankInfo.getExpReceived());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("PlayersRankingDAO.save(PlayerRankData): " + e, e);
            return false;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        insert.incrementAndGet();
        return true;
    }

    @Override
    public void update(PlayerRankData rankInfo) {
        if (!rankInfo.getJdbcState().isUpdatable())
            return;

        if (update0(rankInfo)) {
            rankInfo.setJdbcState(JdbcEntityState.STORED);
            return;
        }
    }

    private boolean update0(PlayerRankData rankInfo) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL_QUERY);
            int i = 0;
            statement.setInt(++i, rankInfo.getRank());
            statement.setInt(++i, rankInfo.getRaceRank());
            statement.setLong(++i, rankInfo.getExpReceived());
            statement.setInt(++i, rankInfo.getObjectId());
            statement.setInt(++i, rankInfo.getCycle());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("PlayersRankingDAO.update0(PlayerRankData): " + e, e);
            return false;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        update.incrementAndGet();
        return true;
    }

    @Override
    public void saveOrUpdate(PlayerRankData rankInfo) {
        if (rankInfo.getJdbcState().isSavable())
            save(rankInfo);
        else if (rankInfo.getJdbcState().isUpdatable())
            update(rankInfo);
    }

    @Override
    public void delete(PlayerRankData rankInfo) {
        if (!rankInfo.getJdbcState().isDeletable())
            return;

        if (delete0(rankInfo)) {
            rankInfo.setJdbcState(JdbcEntityState.DELETED);
            return;
        }
    }

    private boolean delete0(PlayerRankData rankInfo) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY);
            int i = 0;
            statement.setInt(++i, rankInfo.getObjectId());
            statement.setInt(++i, rankInfo.getCycle());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("PlayersRankingDAO.delete0(PlayerRankData): " + e, e);
            return false;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        delete.incrementAndGet();
        return true;
    }

    @Override
    public JdbcEntityStats getStats() {
        return stats;
    }
}
