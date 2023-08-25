package l2s.gameserver.network.l2.c2s.randomcraft;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.randomcraft.ExCraftRandomLockSlot;

/**
 * @author nexvill
 */
public class RequestExCraftRandomLockSlot extends L2GameClientPacket
{
	int _slot;
	@Override
	protected boolean readImpl()
	{
		_slot = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExCraftRandomLockSlot(activeChar, _slot));
	}
}