package l2s.gameserver.network.l2.s2c;

public class ActionFailPacket extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ActionFailPacket();

	private final int _castingType;

	public ActionFailPacket()
	{
		_castingType = 0;
	}

	public ActionFailPacket(int castingType)
	{
		_castingType = castingType;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_castingType); // MagicSkillUse castingType
	}
}