package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestObserverEnd extends L2GameClientPacket
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