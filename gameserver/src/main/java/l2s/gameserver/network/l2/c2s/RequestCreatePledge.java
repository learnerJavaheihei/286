package l2s.gameserver.network.l2.c2s;

public class RequestCreatePledge extends L2GameClientPacket
{
	//Format: cS
	private String _pledgename;

	@Override
	protected boolean readImpl()
	{
		_pledgename = readS(64);
		return true;
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}