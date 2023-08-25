package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ExItemAnnounce extends L2GameServerPacket
{
	private final String _name;
	private final int _itemId;
	private final int _enchantLevel;

	public ExItemAnnounce(String name, int itemId, int enchantLevel)
	{
		_name = name;
		_itemId = itemId;
		_enchantLevel = enchantLevel;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00);	// Unk. Maybe fail == 1, success == 0?
		writeString(_name);
		writeD(_itemId);
		writeC(_enchantLevel);
		writeD(0x00);	// UNK
	}
}