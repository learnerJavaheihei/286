package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public final class RequestExEscapeScene extends L2GameClientPacket
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

		if(!activeChar.isInMovie())
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.endScenePlayer(true);
	}
}