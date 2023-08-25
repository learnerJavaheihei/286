package l2s.gameserver.network.l2.s2c.timerestrictfield;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExTimeRestrictFieldUserAlarm extends L2GameServerPacket
{
	private final int _fieldId;
	private final int _time;

	public ExTimeRestrictFieldUserAlarm(int fieldId, int time)
	{
		_fieldId = fieldId;
		_time = time;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_fieldId);
		writeD(_time);
	}
}