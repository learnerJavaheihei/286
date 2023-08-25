package l2s.gameserver.network.l2.c2s.blessing;

import l2s.gameserver.network.l2.c2s.L2GameClientPacket;

public class RequestExBlessOptionCancel extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		//
	}
}