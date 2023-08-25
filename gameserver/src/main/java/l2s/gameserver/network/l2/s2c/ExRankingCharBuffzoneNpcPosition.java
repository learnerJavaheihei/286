package l2s.gameserver.network.l2.s2c;

/**
 * @author nexvill
 */
public class ExRankingCharBuffzoneNpcPosition extends L2GameServerPacket 
{
	private byte _active;
	private int _locX;
	private int _locY;
	private int _locZ;
	
	public ExRankingCharBuffzoneNpcPosition(byte isActive, int x, int y, int z)
	{
		_active = isActive;
		_locX = x;
		_locY = y;
		_locZ = z;
	}

	@Override
	public void writeImpl() 
	{
		writeC(_active); // is active
		writeD(_locX); // x
		writeD(_locY); // y
		writeD(_locZ); // z
	}
}
