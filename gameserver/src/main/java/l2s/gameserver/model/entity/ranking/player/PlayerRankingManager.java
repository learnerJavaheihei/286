package l2s.gameserver.model.entity.ranking.player;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.PlayersRankingDAO;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.player.Friend;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerRankingManager {
    private ScheduledFuture<?> _taskBuff, _taskClear;
    private NpcInstance _hiddenNpc;
    private FakePlayer fp;
    public static final SchedulingPattern START_CYCLE_DATE_PATTERN = TimeUtils.DAILY_DATE_PATTERN;
    public static final String RANKER_AUTHORITY_REUSE_VAR = "ranker_authority_in_reuse";
    private static final PlayerRankingManager INSTANCE = new PlayerRankingManager();
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerRankingManager.class);

    private static final long RECALC_RANKING_DELAY = TimeUnit.MINUTES.toMillis(1); // 1 min
    private static final long UPDATE_DB_DELAY = TimeUnit.MINUTES.toMillis(5); // 15 min
    public static final long RANKERS_AUTHORITY_REUSE_DELAY = TimeUnit.HOURS.toMillis(24); // 24 hours
    private static final long RANKERS_AUTHORITY_DECOY_LIFE_TIME = TimeUnit.HOURS.toMillis(12); // 12 hours
    private static final String CYCLE_START_TIME_VAR = "player_rank_cycle_start_time";
    private static final String CYCLE_VAR = "player_rank_cycle";
    private static final long RANKERS_AUTHORITY_USE_PRICE = 20_000_000;
    private static final Comparator<PlayerRanking> SORT_RANKING_COMPARATOR = (o1, o2) -> Long.compare(o2.getTotalExp(), o1.getTotalExp());
    private static final String RANKERS_AUTHORITY_DESPAWN_TIME_VAR = "rankers_authority_despawn_time";
    private static final String RANKERS_AUTHORITY_LOC_VAR = "rankers_authority_loc";
    private static final String RANKERS_AUTHORITY_OWNER_VAR = "rankers_authority_owner";
    private final Map<Integer, PlayerRanking> rankings = new ConcurrentHashMap<>();
    private final AtomicBoolean updating = new AtomicBoolean(false);
    private final AtomicBoolean dailyUpdating = new AtomicBoolean(false);
    private final RankingGlobalListeners globalListeners = new RankingGlobalListeners();
    private final Map<Integer, Integer> cycleDates = new HashMap<>();
    private int cycle = 0;
    private List<PlayerRanking> sortedRankings = Collections.emptyList();
    private Map<Integer, List<PlayerRanking>> sortedRankingsByClan = Collections.emptyMap();
    private Map<Race, List<PlayerRanking>> sortedRankingsByRace = Collections.emptyMap();
    private ScheduledFuture<?> checkCycleTask = null;
    private ScheduledFuture<?> updateDbTask = null;
    private ScheduledFuture<?> reCalcRanksTask = null;
    private boolean needReCalcRanks = true;
    private RankersAuthorityDecoy rankersAuthorityDecoy = null;

    public static PlayerRankingManager getInstance() {
        return INSTANCE;
    }

    public boolean isUpdating() {
        return updating.get() || dailyUpdating.get();
    }

    public int getCycle() {
        return cycle;
    }

    public int getCycleDate(int cycle) {
        return cycleDates.getOrDefault(cycle, 0);
    }

    public void load() {
        cycle = ServerVariables.getInt(CYCLE_VAR, 0);
        int cycleDate = ServerVariables.getInt(CYCLE_START_TIME_VAR, 0);
        cycleDates.put(cycle, cycleDate);

        if (cycle > 0) {
            int tempCycleData = cycleDate;
            for (int i = (cycle - 1); i >= 0; i--) {
                tempCycleData -= (int) TimeUnit.DAYS.toSeconds(1);
                cycleDates.put(i, tempCycleData);
            }
        }

        PlayersRankingDAO.getInstance().loadAll(rankings);

        reCalcRanks();

        checkCycle(true);

        CharListenerList.addGlobal(globalListeners);

        LOGGER.info(String.format("PlayerRankingManager: loaded %d ranking info(s). Cycle: %d", rankings.size(), cycle));

        if (updateDbTask == null) {
            updateDbTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::saveAllToDb, UPDATE_DB_DELAY, UPDATE_DB_DELAY);
        }

        long rankersAuthorityDespawnTime = ServerVariables.getLong(RANKERS_AUTHORITY_DESPAWN_TIME_VAR, 0);
        if (rankersAuthorityDespawnTime > System.currentTimeMillis()) {
            Location loc = Location.parseLoc(ServerVariables.getString(RANKERS_AUTHORITY_LOC_VAR, "0 0 0"));
            int ownerObjectId = ServerVariables.getInt(RANKERS_AUTHORITY_OWNER_VAR, 0);
            Player player = Player.restore(ownerObjectId, false);
            if (player != null) {
                spawnRankersAuthority0(player, loc, rankersAuthorityDespawnTime);
                player.deleteMe();
            }
        }
    }

    public synchronized void checkCycle(boolean onLoad) {
        int cycleDate = getCycleDate(cycle);
        long nextCycleDate = START_CYCLE_DATE_PATTERN.next(TimeUnit.SECONDS.toMillis(cycleDate));
        boolean startTask = onLoad;
        if (System.currentTimeMillis() > nextCycleDate) {
            if (!dailyUpdating.compareAndSet(false, true))
                return;

            cycle++;

            nextCycleDate = START_CYCLE_DATE_PATTERN.next(System.currentTimeMillis() + 10000);
            cycleDate = (int) TimeUnit.MILLISECONDS.toSeconds(nextCycleDate - TimeUnit.DAYS.toMillis(1));
            cycleDates.put(cycle, cycleDate);

            ServerVariables.set(CYCLE_VAR, cycle);
            ServerVariables.set(CYCLE_START_TIME_VAR, cycleDate);

            if (!onLoad) {
                reCalcRanks();
            }

            rankings.values().forEach(PlayerRanking::validateRankings);

            saveAllToDb();
            dailyUpdating.set(false);
            startTask = true;
        }

        if (startTask) {
            if (checkCycleTask != null)
                checkCycleTask.cancel(false);
            checkCycleTask = ThreadPoolManager.getInstance().schedule(() -> checkCycle(false), nextCycleDate - System.currentTimeMillis() + 1000);
        }
    }

    public synchronized void saveAllToDb() {
        rankings.values().forEach(PlayerRanking::save);
    }

    public void updateRank(Player player) {
        if (!canHaveRank(player))
            return;

        PlayerRanking rp;
        synchronized (rankings) {
            rp = rankings.computeIfAbsent(player.getObjectId(), (pr) -> new PlayerRanking(player));
        }

        if (rp.update(player)) {
            needReCalcRanks = true;
            if (reCalcRanksTask == null)
                reCalcRanksTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this::reCalcRanks, RECALC_RANKING_DELAY, RECALC_RANKING_DELAY);
        }
    }

    public boolean canHaveRank(Player player) {
        return (Config.EVERYBODY_HAS_ADMIN_RIGHTS || !player.isGM()) && canHaveRank(player.getLevel(), player.getClassId());
    }

    public boolean canHaveRank(int level, ClassId classId) {
        return level >= 40 && classId.getClassLevel().ordinal() >= ClassLevel.SECOND.ordinal();
    }

    private void reCalcRanks() {
        if (!needReCalcRanks)
            return;

        if (!updating.compareAndSet(false, true))
            return;

        for (int rankingObjectId : rankings.keySet()) {
            PlayerRanking playerRanking = rankings.get(rankingObjectId);
            if (playerRanking == null || playerRanking.isExpired())
                deleteRanking(rankingObjectId);
        }

        List<PlayerRanking> rankingsList = new CopyOnWriteArrayList<>(rankings.values());
        Map<Integer, List<PlayerRanking>> rankingsByClan = new ConcurrentHashMap<>();
        Map<Race, List<PlayerRanking>> rankingsByRace = new ConcurrentHashMap<>();
        rankingsList.sort(SORT_RANKING_COMPARATOR);

        for (PlayerRanking rp : rankingsList) {
            int clanId = rp.getClanId();
            if (clanId > 0)
                rankingsByClan.computeIfAbsent(clanId, i -> new ArrayList<>()).add(rp);
            rankingsByRace.computeIfAbsent(rp.getRace(), i -> new ArrayList<>()).add(rp);
        }

        rankingsByClan.values().forEach(l -> l.sort(SORT_RANKING_COMPARATOR));
        rankingsByRace.values().forEach(l -> l.sort(SORT_RANKING_COMPARATOR));

        for (PlayerRanking rp : rankingsList) {
            PlayerRankData rankData = rp.getCurrRankData();
            int prevRank = rankData.getRank();
            int rank = rankingsList.indexOf(rp) + 1;
            boolean updatedDb = prevRank != rank;
            boolean updatedTopRank = prevRank != rank && (prevRank == 1 || rank == 1);
            rankData.setRank(rank);

            prevRank = rankData.getRaceRank();
            rank = rankingsByRace.getOrDefault(rp.getRace(), Collections.emptyList()).indexOf(rp) + 1;
            updatedDb |= prevRank != rank;
            updatedTopRank |= prevRank != rank && (prevRank == 1 || rank == 1);
            rankData.setRaceRank(rank);

            if (updatedDb)
                rankData.update();

            if (updatedTopRank) {
                Player player = rp.getPlayer();
                if (player != null) {
                    player.broadcastUserInfo(true);
                    player.checkAbnormalBoard();
                }
            }
        }

        sortedRankings = rankingsList;
        sortedRankingsByClan = rankingsByClan;
        sortedRankingsByRace = rankingsByRace;

        needReCalcRanks = false;
        updating.set(false);
    }

    public PlayerRanking getRanking(int objectId) {
        return rankings.get(objectId);
    }

    public int getCurrentRank(PlayerRankingCategory category, int objectId) {
        PlayerRanking ranking = getRanking(objectId);
        if (ranking == null)
            return 0;
        return ranking.getCurrRank(category);
    }

    public boolean deleteRanking(int objectId) {
        PlayerRanking playerRanking = rankings.remove(objectId);
        if (playerRanking == null)
            return false;
        playerRanking.delete();
        return true;
    }

    public List<PlayerRanking> getRankings(Player player, PlayerRankingCategory category, boolean personal, Race race, ClassId classId) {
        if (isUpdating())
            return Collections.emptyList();

        List<PlayerRanking> result;
        switch (category) {
            case ALL: {
                result = sortedRankings;
                break;
            }
            case RACE: {
                result = race == null ? Collections.emptyList() : sortedRankingsByRace.getOrDefault(race, Collections.emptyList());
                break;
            }
            case CLAN: {
                result = sortedRankingsByClan.getOrDefault(player.getClanId(), Collections.emptyList());
                break;
            }
            case FRIEND: {
                result = new ArrayList<>();
                PlayerRanking ranking = rankings.get(player.getObjectId());
                if (ranking != null)
                    result.add(ranking);
                for (Friend friend : player.getFriendList().values()) {
                    PlayerRanking pr = rankings.get(friend.getObjectId());
                    if (pr != null)
                        result.add(pr);
                }
                result.sort(SORT_RANKING_COMPARATOR);
                break;
            }
            default: {
                LOGGER.warn(getClass().getSimpleName() + ": Not found ranking info for category: " + category);
                return Collections.emptyList();
            }
        }
        if (personal) {
            int pos = result.indexOf(getRanking(player.getObjectId()));
            if (pos >= 0)
                return result.subList(Math.max(pos - 10, 0), Math.min(pos + 11, result.size()));
            return Collections.emptyList();
        }
        return result.subList(0, Math.min(result.size(), category.getMaxSize()));
    }

    public boolean isTopRank(int objId) {
        PlayerRanking rp = getRanking(objId);
        return rp != null && rp.isTopRank();
    }

    public boolean isTopRaceRank(int objId) {
        PlayerRanking rp = getRanking(objId);
        return rp != null && rp.isTopRaceRank();
    }

    public RankersAuthorityDecoy getRankersAuthorityDecoy() {
        return rankersAuthorityDecoy;
    }

    public synchronized void spawnRankersAuthority(Player player) {
        if (!player.isInPeaceZone() || !player.getReflection().isMain()) {
            player.sendPacket(SystemMsg.RANKERS_AUTHORITY_CANNOT_BE_USED_IN_THIS_AREA);
            return;
        }

        if (player.getVarBoolean(RANKER_AUTHORITY_REUSE_VAR)) {
//            player.sendPacket(SystemMsg.WAITING_FOR_THE_RANKERS_AUTHORITY_TO_COOLDOWN);
            player.sendMessage("WAITING FOR THE RANKERS AUTHORITY TO COOLDOWN".toLowerCase());
            return;
        }

        if (!player.reduceAdena(RANKERS_AUTHORITY_USE_PRICE, true)) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }

        Location loc = player.getLoc();
        long despawnTime = System.currentTimeMillis() + RANKERS_AUTHORITY_DECOY_LIFE_TIME;
        spawnRankersAuthority0(player, loc, despawnTime);

        ServerVariables.set(RANKERS_AUTHORITY_DESPAWN_TIME_VAR, despawnTime);
        ServerVariables.set(RANKERS_AUTHORITY_LOC_VAR, loc.toXYZString());
        ServerVariables.set(RANKERS_AUTHORITY_OWNER_VAR, player.getObjectId());

        player.setVar(RANKER_AUTHORITY_REUSE_VAR, true, System.currentTimeMillis() + RANKERS_AUTHORITY_REUSE_DELAY);

        Announcements.announceToAll(new SystemMessagePacket(SystemMsg.SERVER_RANK_1_C1_HAS_CREATED_RANKERS_AUTHORITY_IN_S2).addName(player).addZoneName(loc));
    }

    private void spawnRankersAuthority0(Player player, Location loc, long despawnTime) {
        if (rankersAuthorityDecoy != null) {
            rankersAuthorityDecoy.deleteMe();
        }

        RankersAuthorityDecoy decoy = new RankersAuthorityDecoy(IdFactory.getInstance().getNextId(), player, despawnTime);
        decoy.setCurrentHp(decoy.getMaxHp(), false);
        decoy.setCurrentMp(decoy.getMaxMp());
        decoy.setHeading(player.getHeading());
        decoy.setReflection(ReflectionManager.MAIN);
        decoy.spawnMe(loc);

        // old npc
        NpcTemplate DecoyTemplate = NpcHolder.getInstance().getTemplate(18485);

        _hiddenNpc = new NpcInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, StatsSet.EMPTY);
        _hiddenNpc.setTargetable(false);
        _hiddenNpc.spawnMe(loc);

        fp = new FakePlayer(IdFactory.getInstance().getNextId(), player.getTemplate(), player, true);
        fp.setReflection(player.getReflection());
        fp.setHeading(player.getHeading());
        fp.setTargetable(false);
        fp.setTitle(player.getTitle());
        fp.spawnMe(loc);
        for (Player plr : GameObjectsStorage.getPlayers(false, false))
            plr.sendPacket(new SystemMessagePacket(SystemMsg.SERVER_RANK_1_C1_HAS_CREATED_RANKERS_AUTHORITY_IN_S2).addName(player).addZoneName(player.getLoc()));

        ServerVariables.set("buffNpcActive", true);
        ServerVariables.set("buffNpcX", fp.getX());
        ServerVariables.set("buffNpcY", fp.getY());
        ServerVariables.set("buffNpcZ", fp.getZ());

        _taskBuff = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
        {
            SkillEntry buff = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 52018, 1);
            _hiddenNpc.doCast(buff, _hiddenNpc, true);
            for (Creature crt : fp.getAroundCharacters(300, 1000))
            {
                if (crt.isPlayer() || crt.isSummon())
                    crt.startAttackStanceTask();
            }
            fp.broadcastPacket(new SocialActionPacket(fp.getObjectId(), SocialActionPacket.GREETING));
        }, 5000, 10000);

        _taskClear = ThreadPoolManager.getInstance().schedule(() ->
        {
            clearNpc();
        }, ServerVariables.getLong(RANKERS_AUTHORITY_DESPAWN_TIME_VAR, 0));

        rankersAuthorityDecoy = decoy;
    }
    private void clearNpc()
    {
        ServerVariables.unset("buffNpcActive");
        ServerVariables.unset("buffNpcX");
        ServerVariables.unset("buffNpcY");
        ServerVariables.unset("buffNpcZ");
        _hiddenNpc.deleteMe();
        fp.deleteMe();
        if (_taskClear != null)
        {
            _taskClear.cancel(true);
            _taskClear = null;
        }
        if (_taskBuff != null)
        {
            _taskBuff.cancel(true);
            _taskBuff = null;
        }
    }

    public void onDeleteRankersAuthority(RankersAuthorityDecoy decoy) {
        if (rankersAuthorityDecoy == decoy) {
            rankersAuthorityDecoy = null;
        }
    }

    private class RankingGlobalListeners implements OnPlayerEnterListener {
        @Override
        public void onPlayerEnter(Player player) {
            PlayerRanking ranking = getRanking(player.getObjectId());
            if (ranking != null)
                ranking.checkRewards();
        }
    }
    public int getPlayerGlobalRank(int player_id){
        PlayerRanking playerRanking = rankings.get(player_id);
        if (playerRanking!=null && playerRanking.getPrevRankData()!=null) {
            return playerRanking.getPrevRankData().getRank();
        }
        return 0;
    }
}