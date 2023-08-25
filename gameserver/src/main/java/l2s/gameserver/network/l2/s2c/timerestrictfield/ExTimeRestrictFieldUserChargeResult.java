package l2s.gameserver.network.l2.s2c.timerestrictfield;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExTimeRestrictFieldUserChargeResult extends L2GameServerPacket
{
	private final int _fieldId;
	private final int _remainTime;
	private final int _remainTimeBase;

	public ExTimeRestrictFieldUserChargeResult(int fieldId, int remainTime, int remainTimeBase)
	{
		_fieldId = fieldId;
		_remainTime = remainTime;
		_remainTimeBase = remainTimeBase;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_fieldId);
		writeD(_remainTime);
		writeD(_remainTimeBase);
	}
}