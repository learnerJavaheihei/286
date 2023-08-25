package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ExPledgeBonusUpdate extends L2GameServerPacket
{
	public static enum BonusType
	{
		ATTENDANCE,
		HUNTING
	}

	private final BonusType _type;
	private final int _value;

	public ExPledgeBonusUpdate(BonusType type, int value)
	{
		_type = type;
		_value = value;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type.ordinal());	// Bonus type
		writeD(_value);	// Progress amount
	}
}