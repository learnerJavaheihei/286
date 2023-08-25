package l2s.gameserver.network.l2.s2c;
/**
 * 
 * @author nexvill
 *
 */
public class ExTutorialShowId extends L2GameServerPacket 
{
	private int _id = 0;

	public ExTutorialShowId(int id)
	{
		_id = id;
	}

	@Override
	protected final void writeImpl() 
	{
		
		writeD(_id);
	}
}