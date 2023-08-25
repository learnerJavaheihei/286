package l2s.gameserver.network.l2.c2s;

public class RequestTimeCheck extends L2GameClientPacket
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
		//System.out.println("Unk1: " + unk + ", unk2: " + unk2);
		//TODO not implemented
	}
}