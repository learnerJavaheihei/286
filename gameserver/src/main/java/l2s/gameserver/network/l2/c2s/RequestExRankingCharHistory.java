package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExRankingCharHistory;

/**
 * @author nexvill
 */
public class RequestExRankingCharHistory extends L2GameClientPacket {
	private int unk;

	@Override
	protected boolean readImpl() {
		unk = readC(); //atm returns 8 all the time
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		activeChar.sendPacket(new ExRankingCharHistory(activeChar, unk));
	}
}