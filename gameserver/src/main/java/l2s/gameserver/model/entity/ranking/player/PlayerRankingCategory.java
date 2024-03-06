package l2s.gameserver.model.entity.ranking.player;

import java.util.NoSuchElementException;

public enum PlayerRankingCategory {
    ALL(0, 150),
    RACE(1, 100),
    CLAN(2, 100),
    FRIEND(3, 100);

    public static final PlayerRankingCategory[] VALUES = values();

    private final int id;
    private final int maxSize;

    PlayerRankingCategory(int id, int maxSize) {
        this.id = id;
        this.maxSize = maxSize;
    }

    public static PlayerRankingCategory valueOf(int id) {
        for (PlayerRankingCategory category : VALUES) {
            if (category.getId() == id)
                return category;
        }
        throw new NoSuchElementException("Not found PlayerRankingCategory for ID: " + id);
    }

    public int getId() {
        return id;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
