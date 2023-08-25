package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class RequestSwapAgathionSlotItems extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		System.out.println("RequestSwapAgathionSlotItems size=" + _buf.remaining());
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
	}
}
