package l2s.gameserver.network.l2.s2c;


/**
 * @author Bonux
**/
public class ExRaidServerInfo extends L2GameServerPacket
{
	public ExRaidServerInfo()
	{
		//
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00); // UNK
		writeC(0x00); // UNK
	}
}