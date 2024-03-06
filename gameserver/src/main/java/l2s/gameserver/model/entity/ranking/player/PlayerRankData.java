package l2s.gameserver.model.entity.ranking.player;

import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.math.SafeMath;
import l2s.gameserver.dao.PlayersRankingDAO;

public class PlayerRankData implements Comparable<PlayerRankData>, JdbcEntity {
    private final int objectId;
    private final int cycle;
    private int rank;
    private int raceRank;
    private long expReceived;

    private JdbcEntityState state = JdbcEntityState.CREATED;

    public PlayerRankData(int objectId, int cycle, int rank, int raceRank, long expReceived) {
        this.objectId = objectId;
        this.cycle = cycle;
        this.rank = rank;
        this.raceRank = raceRank;
        this.expReceived = expReceived;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getCycle() {
        return cycle;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRaceRank() {
        return raceRank;
    }

    public void setRaceRank(int raceRank) {
        this.raceRank = raceRank;
    }

    public long getExpReceived() {
        return expReceived;
    }

    public void setExpReceived(long expReceived) {
        this.expReceived = Math.max(0, expReceived);
    }

    public void addExpReceived(long value) {
        this.expReceived = Math.max(0, SafeMath.addAndLimit(this.expReceived, value));
    }

    @Override
    public JdbcEntityState getJdbcState() {
        return state;
    }

    @Override
    public void save() {
        PlayersRankingDAO.getInstance().saveOrUpdate(this);
    }

    @Override
    public void update() {
        PlayersRankingDAO.getInstance().saveOrUpdate(this);
    }

    @Override
    public void delete() {
        PlayersRankingDAO.getInstance().delete(this);
    }

    @Override
    public void setJdbcState(JdbcEntityState state) {
        this.state = state;
    }


    @Override
    public int compareTo(PlayerRankData o) {
        return Integer.compare(o.getCycle(), getCycle());
    }
}
