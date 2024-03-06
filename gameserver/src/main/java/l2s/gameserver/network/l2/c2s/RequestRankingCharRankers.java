package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.ranking.player.PlayerRanking;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingCategory;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExRankingCharBuffzoneNpcInfo;
import l2s.gameserver.network.l2.s2c.ExRankingCharBuffzoneNpcPosition;
import l2s.gameserver.network.l2.s2c.ExRankingCharRankers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author JoeAlisson
 */
public class RequestRankingCharRankers extends L2GameClientPacket {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestRankingCharRankers.class);

	private PlayerRankingCategory category;
	private boolean personal;
	private Race race;
	private ClassId classId;

	@Override
	protected boolean readImpl() {
		//category top 150 -> 0 server ; 1 -> race ; 2 -> clan member ; 3 Friend
		try {
			category = PlayerRankingCategory.valueOf(readC());
			personal = readC() > 0;
			race = Race.VALUES[readD()];
			classId = ClassId.valueOf(readD());
		} catch (Exception e) {
			LOGGER.error("Error while read packet: " + e, e);
			return false;
		}
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		List<PlayerRanking> list;
		if (PlayerRankingManager.getInstance().isUpdating()) {
			list = Collections.emptyList();
			activeChar.sendMessage("UPDATING RANKING DATA REFRESH THE SCREEN OR REOPEN THE RANKING UI".toLowerCase());
		} else {
			if (personal)
				race = activeChar.getRace();
			list = PlayerRankingManager.getInstance().getRankings(activeChar, category, personal, race, classId);
		}
		activeChar.sendPacket(new ExRankingCharRankers(category, personal, race, classId, list));
	}
}