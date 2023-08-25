package l2s.gameserver.network.l2.s2c.blessing;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExBlessOptionEnchant extends L2GameServerPacket
{
	private boolean _success;
	
	public ExBlessOptionEnchant(boolean success)
	{
		_success = success;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_success); // success or fail
	}
}