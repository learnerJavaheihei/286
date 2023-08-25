package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExEnchantFail;

/**
 * @author Bonux
**/
public class RequestNewEnchantClose extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setSynthesisItem1(null);
		activeChar.setSynthesisItem2(null);
		activeChar.sendPacket(ExEnchantFail.STATIC); // TODO: Check this.
	}
}