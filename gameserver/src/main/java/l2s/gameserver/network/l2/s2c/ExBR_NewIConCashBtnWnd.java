package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class ExBR_NewIConCashBtnWnd extends L2GameServerPacket
{
	private final int _value;

	public ExBR_NewIConCashBtnWnd(Player player)
	{
		_value = player.getProductHistoryList().haveGifts() ? 0x02 : 0x00;
	}

	@Override
	protected void writeImpl()
	{
		writeH(_value);	// Has Updates
	}
}
