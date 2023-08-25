package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExBR_NewIConCashBtnWnd;

/**
 * @author Bonux
**/
public class RequestBR_NewIConCashBtnWnd extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new ExBR_NewIConCashBtnWnd(player));
	}
}