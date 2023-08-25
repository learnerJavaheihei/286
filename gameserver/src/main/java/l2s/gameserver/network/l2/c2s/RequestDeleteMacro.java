package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestDeleteMacro extends L2GameClientPacket
{
	private int _id;

	/**
	 * format:		cd
	 */
	@Override
	protected boolean readImpl()
	{
		_id = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.deleteMacro(_id);
	}
}