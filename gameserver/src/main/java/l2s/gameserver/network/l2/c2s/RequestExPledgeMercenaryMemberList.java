package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPledgeMercenaryMemberList;

public class RequestExPledgeMercenaryMemberList extends L2GameClientPacket {
	private int castleId;
	private int clanId;

	@Override
	protected boolean readImpl() {
		castleId = readD();
		clanId = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		activeChar.sendPacket(new ExPledgeMercenaryMemberList(activeChar, castleId, clanId));
	}
}
