package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ExPartyMatchingRoomHistory extends L2GameServerPacket
{
	public ExPartyMatchingRoomHistory()
	{
		//
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x00); // Previously existent rooms count
		/*for(rooms count)
		{
			writeS(""); // Name
			writeS(""); // Owner
		}*/
	}
}