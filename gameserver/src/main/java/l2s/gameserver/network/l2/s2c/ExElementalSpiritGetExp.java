package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class ExElementalSpiritGetExp extends L2GameServerPacket
{
	private final int _elementId;
	private final long _exp;

	public ExElementalSpiritGetExp(int elementId, long exp)
	{
		_elementId = elementId;
		_exp = exp;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_elementId);
		writeQ(_exp);
	}
}