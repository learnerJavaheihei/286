package l2s.gameserver.network.l2.s2c.timerestrictfield;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExTimeRestrictFieldUserExit extends L2GameServerPacket
{
	private final int _fieldId;

	public ExTimeRestrictFieldUserExit(int fieldId)
	{
		_fieldId = fieldId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_fieldId);
	}
}