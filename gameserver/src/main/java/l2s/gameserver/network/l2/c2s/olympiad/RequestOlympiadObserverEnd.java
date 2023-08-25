package l2s.gameserver.network.l2.c2s.olympiad;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
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

		if(activeChar.getObserverMode() == Player.OBSERVER_STARTED)
			activeChar.leaveObserverMode();
	}
}