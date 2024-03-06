package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Friend;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.ranking.player.PlayerRanking;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingCategory;
import l2s.gameserver.network.l2.ServerPacketOpcodes;
import l2s.gameserver.templates.StatsSet;

/**
 * @author nexvill
 */
public class ExRankingCharRankers extends L2GameServerPacket
{
	private static class Ranker {
		public String name;
		public String clanNanme;
		public int level;
		public ClassId classId;
		public Race race;
		public int rank;
		public int prevRank;
		public int prevRaceRank;
	}

	private final PlayerRankingCategory category;
	private final boolean personal;
	private final Race race;
	private final ClassId classId;
	private final List<Ranker> list;

	public ExRankingCharRankers(PlayerRankingCategory category, boolean personal, Race race, ClassId classId, List<PlayerRanking> list) {
		this.category = category;
		this.personal = personal;
		this.race = race;
		this.classId = classId;
		this.list = new ArrayList<>(list.size());
		for (PlayerRanking rp : list) {
			Ranker ranker = new Ranker();
			ranker.name = rp.getCharName();
			ranker.clanNanme = rp.getClanName();
			ranker.level = rp.getLevel();
			ranker.classId = rp.getClassId();
			ranker.race = rp.getRace();
			if (category == PlayerRankingCategory.CLAN || category == PlayerRankingCategory.FRIEND) {
				ranker.rank = list.indexOf(rp) + 1; // Rank
			} else {
				ranker.rank = rp.getCurrRank(category); // Rank
			}
			ranker.prevRank = rp.getCurrRank(PlayerRankingCategory.ALL);
			ranker.prevRaceRank = rp.getCurrRank(PlayerRankingCategory.RACE);
			this.list.add(ranker);
		}
	}

	@Override
	protected final void writeImpl() {
		writeC(category.ordinal()); //category otherwise duplicates shit.
		writeC(personal);
		writeD(race.ordinal());
		writeD(classId == null ? -1 : classId.getId());
		writeD(list.size());
		for (Ranker ranker : list) {
			writeString(ranker.name); // Char name
			writeString(ranker.clanNanme); // Clan name
//			writeD(Config.REQUEST_ID);
			writeD(ranker.level); // Level
			writeD(ranker.classId.getId()); // Class Id
			writeD(ranker.race.ordinal()); // Race
			writeD(ranker.rank); // Rank
			writeD(ranker.prevRank); // Previous main rank
			writeD(ranker.prevRaceRank); //  Previous race rank
			writeD(0); // class rank
		}
	}
}