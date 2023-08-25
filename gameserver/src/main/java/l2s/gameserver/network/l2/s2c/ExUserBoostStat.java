package l2s.gameserver.network.l2.s2c;

/**
 * @author nexvill
 */
public class ExUserBoostStat extends L2GameServerPacket
{
	private int _type;
	private int _count;
	private int _bonus;

	public ExUserBoostStat(int type, int count, int bonus)
	{
		_type = type;
		_count = count;
		_bonus = bonus;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		writeC(_count);
		writeH(_bonus);
	}
}