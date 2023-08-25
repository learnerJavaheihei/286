package l2s.gameserver.network.l2.s2c;

public class CameraModePacket extends L2GameServerPacket
{
	public static final L2GameServerPacket EXIT = new CameraModePacket(0);
	public static final L2GameServerPacket ENTER = new CameraModePacket(1);

	private final int mode;

	/**
	 * Forces client camera mode change
	 * @param mode
	 * 0 - third person cam
	 * 1 - first person cam
	 */
	private CameraModePacket(int mode)
	{
		this.mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(mode);
	}
}