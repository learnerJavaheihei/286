package l2s.gameserver.network.l2.s2c.teleport;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExSharedPositionSharingUI extends L2GameServerPacket
{
	private Player _player;
	
	public ExSharedPositionSharingUI(Player player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		if ((_player.getPreviousPvpRank() > 0) && (_player.getPreviousPvpRank() < 4))
			writeQ(0);
		else
			writeQ(Config.SHARE_POSITION_COST);
	}
}