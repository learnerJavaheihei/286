package l2s.gameserver.network.l2.s2c;

public class ExActivateAutoShortcut extends L2GameServerPacket
{
	private final int _slot;
	private final boolean _active;

	public ExActivateAutoShortcut(int slot, int page, boolean active)
	{
		_slot = page * 12 + slot;
		_active = active;
	}

	@Override
	protected void writeImpl()
	{
		writeH(_slot);
		writeC(_active);
	}
}
