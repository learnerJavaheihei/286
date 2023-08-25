package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ReceiveVipBotCaptchaImage extends L2GameServerPacket
{
	public ReceiveVipBotCaptchaImage()
	{
		//
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(0x00);	// TransactionID
		writeC(0x00);	// TryCount
		writeD(0x00);	// RemainTime
	}
}