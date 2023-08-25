package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.ExMercenaryCastlewarCastleInfo;
import l2s.gameserver.network.l2.s2c.ExMercenaryCastlewarCastleSiegeAttackerList;
import l2s.gameserver.network.l2.s2c.ExMercenaryCastlewarCastleSiegeDefenderList;

public class RequestExMercenaryCastlewarCastleInfo extends L2GameClientPacket {
	private int castleId;

	@Override
	protected boolean readImpl() {
		castleId = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		sendInfo(activeChar, castleId);
	}

	public static void sendInfo(Player activeChar, int castleId) {
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, castleId);
		if (castle == null) {
			activeChar.sendActionFailed();
			return;
		}
		
		activeChar.sendPacket(new ExMercenaryCastlewarCastleInfo(castle, castle.getSiegeDate().getTimeInMillis()));
	}
}
