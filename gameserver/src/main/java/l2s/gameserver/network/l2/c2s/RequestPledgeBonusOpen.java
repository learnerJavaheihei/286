package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPledgeBonusOpen;
import l2s.gameserver.network.l2.s2c.ExPledgeClassicRaidInfo;

/**
 * @author Bonux
**/
public class RequestPledgeBonusOpen extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getClan() == null)
			return;

		activeChar.sendPacket(new ExPledgeBonusOpen(activeChar));
		activeChar.sendPacket(new ExPledgeClassicRaidInfo(activeChar));
	}
}