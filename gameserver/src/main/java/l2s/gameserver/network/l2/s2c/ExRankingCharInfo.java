package l2s.gameserver.network.l2.s2c;

import java.util.Map;

import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.ranking.player.PlayerRanking;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingCategory;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.network.l2.ServerPacketOpcodes;
import l2s.gameserver.templates.StatsSet;

/**
 * @author nexvill
 */
public class ExRankingCharInfo extends L2GameServerPacket
{
	private final int currentRank, currentRaceRank, prevRank, prevRaceRank;

	public ExRankingCharInfo(Player player, int unk) {
		PlayerRanking rankingInfo = PlayerRankingManager.getInstance().getRanking(player.getObjectId());
		if (rankingInfo != null) {
			currentRank = rankingInfo.getCurrRank(PlayerRankingCategory.ALL);
			currentRaceRank = rankingInfo.getCurrRank(PlayerRankingCategory.RACE);
			prevRank = rankingInfo.getCurrRank(PlayerRankingCategory.ALL);
			prevRaceRank = rankingInfo.getCurrRank(PlayerRankingCategory.RACE);
		} else {
			currentRank = 0;
			currentRaceRank = 0;
			prevRank = 0;
			prevRaceRank = 0;
		}
	}

	@Override
	protected final void writeImpl() {
		writeD(currentRank); //current rank server
		writeD(currentRaceRank); //current rank race
		writeD(prevRank); //old rank server (calcs difference between old and new can be negative)
		writeD(prevRaceRank); //old rank race (calcs difference between old and new can be negative)
		writeD(0); // current class rank
		writeD(0); // old class rank
	}
}