package l2s.gameserver.network.l2.c2s;

public class RequestSSQStatus extends L2GameClientPacket
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