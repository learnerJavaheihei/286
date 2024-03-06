package l2s.gameserver.model.entity.ranking.player;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.tables.ClanTable;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerRanking {
    public static final SkillEntry FIRST_PLACE_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60003, 1);
    public static final SkillEntry SECOND_PLACE_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60004, 1);
    public static final SkillEntry THIRD_PLACE_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60005, 1);

    public static final SkillEntry HUMAN_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60006, 1);
    public static final SkillEntry ELF_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60007, 1);
    public static final SkillEntry DARK_ELF_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60008, 1);
    public static final SkillEntry ORC_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60009, 1);
    public static final SkillEntry DWARF_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60010, 1);
    public static final SkillEntry KAMAEL_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 60011, 1);
    public static final SkillEntry SYLPH_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 46033, 1);

    private static final SkillEntry HUMAN_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54204, 1);
    private static final SkillEntry KAMAEL_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54205, 1);
    private static final SkillEntry DEATH_KNIGHT_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54208, 1);
    private static final SkillEntry ORC_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54209, 1);
    private static final SkillEntry VANGUARD_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54239, 1);
    private static final SkillEntry ELF_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54210, 1);
    private static final SkillEntry DELF_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54211, 1);
    private static final SkillEntry DWARF_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54212, 1);
    private static final SkillEntry SYLPH_REWARD_EFFECT_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 54226, 1);

    private static final int HISTORY_MAX_SIZE = 7;
    private static final int EXPIRE_TIME_DELAY = (int) TimeUnit.DAYS.toSeconds(7);

    private final int objectId;
    private final TreeMap<Integer, PlayerRankData> ranksData = new TreeMap<>();
    private final Lock lock = new ReentrantLock();

    private String charName;
    private Clan clan;
    private int lastAccess;
    private ClassId classId; // base class id
    private int level; // base class level

    private long totalExp = 0L;

    public PlayerRanking(int objectId, String charName, int clanId, int lastAccess, ClassId classId, int level, long totalExp) {
        this.objectId = objectId;
        this.charName = charName;
        clan = ClanTable.getInstance().getClan(clanId);
        this.lastAccess = lastAccess;
        this.classId = classId;
        this.level = level;
        this.totalExp = totalExp;
    }

    public PlayerRanking(Player player) {
        objectId = player.getObjectId();
        charName = player.getName();
        clan = player.getClan();
        lastAccess = player.isOnline() ? (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) : (int) player.getLastAccess();
        classId = ClassId.valueOf(player.getBaseClassId());
        level = player.getBaseSubClass().getLevel();
    }

    public int getObjectId() {
        return objectId;
    }

    public Player getPlayer() {
        return GameObjectsStorage.getPlayer(objectId);
    }

    public String getCharName() {
        Player player = getPlayer();
        if (player != null)
            charName = player.getName();
        return charName;
    }

    public Clan getClan() {
        Player player = getPlayer();
        if (player != null) {
            clan = player.getClan();
        }
        return clan;
    }

    public int getClanId() {
        Clan clan = getClan();
        return clan == null ? 0 : clan.getClanId();
    }

    public String getClanName() {
        Clan clan = getClan();
        return clan == null ? "" : clan.getName();
    }

    public int getLastAccess() {
        Player player = getPlayer();
        if (player != null)
            lastAccess = player.isOnline() ? (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) : (int) player.getLastAccess();
        return lastAccess;
    }

    public boolean isExpired() {
        return (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - getLastAccess()) > EXPIRE_TIME_DELAY;
    }

    public ClassId getClassId() {
        Player player = getPlayer();
        if (player != null)
            classId = ClassId.valueOf(player.getBaseClassId());
        return classId;
    }

    public Race getRace() {
        return getClassId().getRace();
    }

    public int getLevel() {
        Player player = getPlayer();
        if (player != null)
            level = player.getBaseSubClass().getLevel();
        return level;
    }

    public long getTotalExp() {
        return totalExp;
    }

    private PlayerRankData createRankData(int cycle, int rank, int raceRank) {
        return new PlayerRankData(getObjectId(), cycle, rank, raceRank, 0L);
    }

    public Collection<PlayerRankData> getRankDatas() {
        lock.lock();
        try {
            return ranksData.values();
        } finally {
            lock.unlock();
        }
    }

    public void addRankData(PlayerRankData rankData) {
        lock.lock();
        try {
            ranksData.put(rankData.getCycle(), rankData);
        } finally {
            lock.unlock();
        }
    }

    public void cleanUpRankDatas() {
        lock.lock();
        try {
            while (ranksData.size() > HISTORY_MAX_SIZE) {
                Map.Entry<Integer, PlayerRankData> entry = ranksData.pollFirstEntry();
                if (entry != null)
                    entry.getValue().delete();
            }
        } finally {
            lock.unlock();
        }
    }

    public PlayerRankData getCurrRankData() {
        lock.lock();
        try {
            int cycle = PlayerRankingManager.getInstance().getCycle();
            PlayerRankData rankData = ranksData.get(cycle);
            if (rankData == null) {
                PlayerRankData prevRankData = getPrevRankData();
                if (prevRankData != null) {
                    rankData = createRankData(cycle, prevRankData.getRank(), prevRankData.getRaceRank());
                } else {
                    rankData = createRankData(cycle, 0, 0);
                }
                rankData.save();
                addRankData(rankData);
                cleanUpRankDatas();
            }
            return rankData;
        } finally {
            lock.unlock();
        }
    }

    public PlayerRankData getPrevRankData() {
        lock.lock();
        try {
            Map.Entry<Integer, PlayerRankData> entry = ranksData.lowerEntry(PlayerRankingManager.getInstance().getCycle());
            if (entry != null)
                return entry.getValue();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void validateRankings() {
        getCurrRankData();
        checkRewards();
    }

    public int getCurrRank(PlayerRankingCategory category) {
        if (category == PlayerRankingCategory.ALL)
            return getCurrRankData().getRank();
        if (category == PlayerRankingCategory.RACE)
            return getCurrRankData().getRaceRank();
        return 0;
    }

    public int getPrevRank(PlayerRankingCategory category) {
        PlayerRankData rankData = getPrevRankData();
        if (rankData != null) {
            if (category == PlayerRankingCategory.ALL)
                return rankData.getRank();
            if (category == PlayerRankingCategory.RACE)
                return rankData.getRaceRank();
        }
        return 0;
    }

    public boolean isTopRank() {
        return getCurrRank(PlayerRankingCategory.ALL) == 1;
    }

    public boolean isTopRaceRank() {
        return getCurrRank(PlayerRankingCategory.RACE) == 1;
    }

    // Return true if need reCalc rankings
    public boolean update(Player player) {
        long totalExp = player.getTotalExp();
        if (this.totalExp >= totalExp)
            return false;

        long diff = Math.max(0, totalExp - this.totalExp);
        if (diff > 0) {
            PlayerRankData rankData = getCurrRankData();
            rankData.addExpReceived(diff);
            rankData.update();
        }
        this.totalExp = totalExp;
        return true;
    }

    public void checkRewards() {
        Player player = getPlayer();
        if (player == null)
            return;

        SkillEntry mainRewardSkill = null;
        SkillEntry raceRewardSkill = null;
        SkillEntry raceAddRewardSkill = null;


        PlayerRankData prevRankData = getCurrRankData();
        if (prevRankData != null) {
            int rank = prevRankData.getRank();
            if (rank == 1) {
                mainRewardSkill = FIRST_PLACE_SKILL_ENTRY;
            } else if (rank >= 2 && rank <= 30) {
                mainRewardSkill = SECOND_PLACE_SKILL_ENTRY;
            } else if (rank >= 31 && rank <= 100) {
                mainRewardSkill = THIRD_PLACE_SKILL_ENTRY;
            }

            int raceRank = prevRankData.getRaceRank();
            if (raceRank == 1) {
                switch (getRace()) {
                    case HUMAN:
                        raceRewardSkill = HUMAN_EFFECT_SKILL_ENTRY;
                        break;
                    case ELF:
                        raceRewardSkill = ELF_EFFECT_SKILL_ENTRY;
                        break;
                    case DARKELF:
                        raceRewardSkill = DARK_ELF_EFFECT_SKILL_ENTRY;
                        break;
                    case ORC:
                        raceRewardSkill = ORC_EFFECT_SKILL_ENTRY;
                        break;
                    case DWARF:
                        raceRewardSkill = DWARF_EFFECT_SKILL_ENTRY;
                        break;
                    case KAMAEL:
                        raceRewardSkill = KAMAEL_EFFECT_SKILL_ENTRY;
                        break;
                }
            }

            if (raceRank >= 1 && raceRank <= 3) {
                switch (getRace()) {
                    case HUMAN:
                        raceAddRewardSkill = HUMAN_REWARD_EFFECT_SKILL_ENTRY;
                        break;
                    case ELF:
                        raceAddRewardSkill = ELF_REWARD_EFFECT_SKILL_ENTRY;
                        break;
                    case DARKELF:
                        raceAddRewardSkill = DELF_REWARD_EFFECT_SKILL_ENTRY;
                        break;
                    case ORC:
                        raceAddRewardSkill = ORC_REWARD_EFFECT_SKILL_ENTRY;
                        break;
                    case DWARF:
                        raceAddRewardSkill = DWARF_REWARD_EFFECT_SKILL_ENTRY;
                        break;
                    case KAMAEL:
                        raceAddRewardSkill = KAMAEL_REWARD_EFFECT_SKILL_ENTRY;
                        break;
                }
            }
        }

        if (mainRewardSkill != FIRST_PLACE_SKILL_ENTRY)
            player.getAbnormalList().stop(FIRST_PLACE_SKILL_ENTRY, false);
        if (mainRewardSkill != SECOND_PLACE_SKILL_ENTRY)
            player.getAbnormalList().stop(SECOND_PLACE_SKILL_ENTRY, false);
        if (mainRewardSkill != THIRD_PLACE_SKILL_ENTRY)
            player.getAbnormalList().stop(THIRD_PLACE_SKILL_ENTRY, false);

        if (mainRewardSkill != null) {
            if (!player.getAbnormalList().contains(mainRewardSkill))
                mainRewardSkill.getEffects(player, player);
        }

        if (raceRewardSkill != HUMAN_EFFECT_SKILL_ENTRY)
            player.getAbnormalList().stop(HUMAN_EFFECT_SKILL_ENTRY, false);
        if (raceRewardSkill != ELF_EFFECT_SKILL_ENTRY)
            player.getAbnormalList().stop(ELF_EFFECT_SKILL_ENTRY, false);
        if (raceRewardSkill != DARK_ELF_EFFECT_SKILL_ENTRY)
            player.getAbnormalList().stop(DARK_ELF_EFFECT_SKILL_ENTRY, false);
        if (raceRewardSkill != ORC_EFFECT_SKILL_ENTRY)
            player.getAbnormalList().stop(ORC_EFFECT_SKILL_ENTRY, false);
        if (raceRewardSkill != DWARF_EFFECT_SKILL_ENTRY)
            player.getAbnormalList().stop(DWARF_EFFECT_SKILL_ENTRY, false);
        if (raceRewardSkill != KAMAEL_EFFECT_SKILL_ENTRY)
            player.getAbnormalList().stop(KAMAEL_EFFECT_SKILL_ENTRY, false);
        if (raceRewardSkill != SYLPH_EFFECT_SKILL_ENTRY)
            player.getAbnormalList().stop(SYLPH_EFFECT_SKILL_ENTRY, false);

        if (raceRewardSkill != null) {
            if (!player.getAbnormalList().contains(raceRewardSkill))
                raceRewardSkill.getEffects(player, player);
        }

        boolean updateSkills = false;
        if (raceAddRewardSkill != HUMAN_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(HUMAN_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != ELF_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(ELF_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != DELF_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(DELF_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != DEATH_KNIGHT_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(DEATH_KNIGHT_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != VANGUARD_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(VANGUARD_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != ORC_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(ORC_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != DWARF_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(DWARF_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != KAMAEL_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(KAMAEL_REWARD_EFFECT_SKILL_ENTRY, false) != null;
        if (raceAddRewardSkill != SYLPH_REWARD_EFFECT_SKILL_ENTRY)
            updateSkills |= player.removeSkill(SYLPH_REWARD_EFFECT_SKILL_ENTRY, false) != null;

        if (raceAddRewardSkill != null) {
            updateSkills |= player.addSkill(raceAddRewardSkill, false) != raceAddRewardSkill;
        }

        if (updateSkills) {
            player.sendUserInfo();
            player.updateStats();
            player.sendSkillList();
        }
    }

    public void save() {
        ranksData.values().forEach(PlayerRankData::save);
    }

    public void delete() {
        ranksData.values().forEach(PlayerRankData::delete);
        ranksData.clear();
    }
}
