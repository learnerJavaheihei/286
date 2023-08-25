package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritAbsorbInfo;

/**
 * @author Bonux
**/
public class RequestExElementalSpiritAbsorbInfo extends L2GameClientPacket
{
	private int _unk, _elementId;

	@Override
	protected boolean readImpl()
	{
		_unk = readC();
		_elementId = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExElementalSpiritAbsorbInfo(activeChar, _unk, _elementId));
	}
}