package l2s.gameserver.network.l2.c2s;

public class RequestChangeBookMarkSlot extends L2GameClientPacket
{
	private int slot_old, slot_new;

	@Override
	protected boolean readImpl()
	{
		slot_old = readD();
		slot_new = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}