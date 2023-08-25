package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;

public class RequestExRequestClassChangeVerifying extends L2GameClientPacket
{
	private int _classId;

	@Override
	protected boolean readImpl()
	{
		_classId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		ClassId classId = activeChar.getClassId();
		if(classId.getId() != _classId)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendClassChangeAlert();
	}
}
