package l2s.gameserver.network.l2.c2s.timerestrictfield;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.timerestrictfield.ExTimeRestrictFieldList;

/**
 * @author nexvill
 */
public class RequestExTimeRestrictFieldList extends L2GameClientPacket
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
		
		activeChar.sendPacket(new ExTimeRestrictFieldList(activeChar));
	}
}