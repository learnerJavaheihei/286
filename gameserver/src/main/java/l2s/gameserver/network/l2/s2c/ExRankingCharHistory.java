package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.entity.ranking.player.PlayerRankData;
import l2s.gameserver.model.entity.ranking.player.PlayerRanking;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.network.l2.ServerPacketOpcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author nexvill
 */
public class ExRankingCharHistory extends L2GameServerPacket
{
	private final List<PlayerRankData> rankInfos = new ArrayList<>();
	private final int unk;

	public ExRankingCharHistory(Player player, int unk) {
		PlayerRanking rankingInfo = PlayerRankingManager.getInstance().getRanking(player.getObjectId());
		if (rankingInfo != null) {
			rankInfos.addAll(rankingInfo.getRankDatas());
			Collections.sort(rankInfos);
		}
		this.unk = unk;
	}

	@Override
	protected final void writeImpl() {
		writeD(rankInfos.size()); //count
		for (PlayerRankData rankInfo : rankInfos) {
			writeD(PlayerRankingManager.getInstance().getCycleDate(rankInfo.getCycle())); //date
			writeD(rankInfo.getRank()); //rank
			writeQ(rankInfo.getExpReceived()); //expReceived
		}
	}
}