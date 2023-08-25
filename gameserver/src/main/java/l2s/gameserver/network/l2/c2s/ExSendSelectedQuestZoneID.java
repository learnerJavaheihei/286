package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class ExSendSelectedQuestZoneID extends L2GameClientPacket
{
	private int _questZoneId;

	@Override
	protected boolean readImpl()
	{
		_questZoneId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setQuestZoneId(_questZoneId);
	}
}