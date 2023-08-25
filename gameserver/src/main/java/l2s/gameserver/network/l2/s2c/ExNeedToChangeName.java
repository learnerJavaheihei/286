package l2s.gameserver.network.l2.s2c;

public class ExNeedToChangeName extends L2GameServerPacket
{
	public static final int TYPE_PLAYER = 0;
	public static final int TYPE_PLEDGE = 1;

	public static final int NONE_REASON = 0;
	public static final int NAME_ALREADY_IN_USE_OR_INCORRECT_REASON = 1;

	private int _type, _reason;
	private String _origName;

	public ExNeedToChangeName(int type, int reason, String origName)
	{
		_type = type;
		_reason = reason;
		_origName = origName;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeD(_reason);
		writeS(_origName);
	}
}