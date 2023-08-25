package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ExTodoListInzone extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		int instancesCount = 0;
		writeH(0x00);	// TODO[UNDERGROUND]: Instances count
		for(int i = 0; i < instancesCount; i++)
		{
			writeC(0x00);	// TODO[UNDERGROUND]: Tab
			writeS("");	// TODO[UNDERGROUND]: HTML name
			writeS("");	// TODO[UNDERGROUND]: Zone name
			writeH(0x00);	// TODO[UNDERGROUND]: Min level
			writeH(0x00);	// TODO[UNDERGROUND]: Max level
			writeH(0x00);	// TODO[UNDERGROUND]: Min players
			writeH(0x00);	// TODO[UNDERGROUND]: Max players
			writeC(0x00);	// TODO[UNDERGROUND]: Entry info
			writeC(0x00);	// TODO[UNDERGROUND]: UNK
		}
	}
}