package l2s.gameserver.network.l2.s2c.randomcraft;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExCraftInfo extends L2GameServerPacket
{
	private Player _player;

	public ExCraftInfo(Player player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_player.getCraftPoints());
		writeD(_player.getCraftGaugePoints());
		if (_player.getCraftPoints() > 0)
			writeC(1); // button color (think change if craft points > 0)
		else
			writeC(0);
	}
}