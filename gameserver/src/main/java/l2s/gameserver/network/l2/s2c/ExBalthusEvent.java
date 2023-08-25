package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class ExBalthusEvent extends L2GameServerPacket
{
	public ExBalthusEvent(Player player)
	{
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x00);	// currentState
		writeD(0x00);	// progress
		writeD(0x00);	// Reward Item ID
		writeD(0x00);	// Available Coins count
		writeD(0x00);	// Participated
		writeD(0x00);	// Running
	}
}