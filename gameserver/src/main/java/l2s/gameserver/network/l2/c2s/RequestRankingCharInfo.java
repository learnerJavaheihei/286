package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.network.l2.s2c.ExRankingCharBuffzoneNpcInfo;
import l2s.gameserver.network.l2.s2c.ExRankingCharInfo;

import java.util.concurrent.TimeUnit;

/**
 * @author JoeAlisson
 */
public class RequestRankingCharInfo extends L2GameClientPacket 
{

	private int unk;

	@Override
	protected boolean readImpl() {
		unk = readC(); //trigger? returns always 0
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		activeChar.sendPacket(new ExRankingCharInfo(activeChar, unk));

		if (activeChar.isTopRank()) {
			int delay = (int) TimeUnit.MILLISECONDS.toSeconds(Math.max(0, activeChar.getVarExpireTime(PlayerRankingManager.RANKER_AUTHORITY_REUSE_VAR) - System.currentTimeMillis()));
			activeChar.sendPacket(new ExRankingCharBuffzoneNpcInfo(delay));
		}
	}
}