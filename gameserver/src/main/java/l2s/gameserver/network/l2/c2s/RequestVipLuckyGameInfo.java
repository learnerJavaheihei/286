package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ReciveVipLuckyGameInfo;

/**
 * @author Bonux
**/
public final class RequestVipLuckyGameInfo extends L2GameClientPacket
{
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

		activeChar.sendPacket(new ReciveVipLuckyGameInfo(activeChar));
	}
}