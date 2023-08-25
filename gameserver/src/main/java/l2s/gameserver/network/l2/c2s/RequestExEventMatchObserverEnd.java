package l2s.gameserver.network.l2.c2s;

public class RequestExEventMatchObserverEnd extends L2GameClientPacket
{
	private int unk, unk2;

	/**
	 * format: dd
	 */
	@Override
	protected boolean readImpl()
	{
		unk = readD();
		unk2 = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}