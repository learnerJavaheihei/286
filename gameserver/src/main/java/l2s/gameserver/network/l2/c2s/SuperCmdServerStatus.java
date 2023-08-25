package l2s.gameserver.network.l2.c2s;

/**
 * Format ch
 * c: (id) 0x39
 * h: (subid) 0x02
 */
class SuperCmdServerStatus extends L2GameClientPacket
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