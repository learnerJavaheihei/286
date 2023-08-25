package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ExItemAuctionStatus extends L2GameServerPacket
{
	public ExItemAuctionStatus()
	{
		//
	}

	@Override
	protected final void writeImpl()
	{
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(0x00);
		writeC(0x00);
	}
}