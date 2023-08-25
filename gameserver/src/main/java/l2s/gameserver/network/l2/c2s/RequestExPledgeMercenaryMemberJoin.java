package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.components.SystemMsg;

public class RequestExPledgeMercenaryMemberJoin extends L2GameClientPacket {
	private int charObjectId;
	private boolean join;
	private int castleId;
	private int clanId;

	@Override
	protected boolean readImpl() {
		charObjectId = readD();
		join = readD() > 0;
		castleId = readD();
		clanId = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.getObjectId() != charObjectId)
			return;

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, castleId);
		if (castle == null)
			return;

		activeChar.sendPacket(SystemMsg.YOU_CANNOT_APPLY_FOR_MERCENARY_NOW);
		//TODO: Impl mercenary system.
	}
}
