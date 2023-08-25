package l2s.gameserver.network.l2.c2s;

public class RequestSEKCustom extends L2GameClientPacket
{
	private int SlotNum, Direction;

	/**
	 * format: dd
	 */
	@Override
	protected boolean readImpl()
	{
		SlotNum = readD();
		Direction = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}