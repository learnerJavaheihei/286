package l2s.gameserver.network.l2.s2c.teleport;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExTeleportFavoritesList extends L2GameServerPacket
{
	private final boolean _activate;
	private int[] _teleportFavorites;

	public ExTeleportFavoritesList(boolean activate, Player player)
	{
		_activate = activate;
		_teleportFavorites = player.getTeleportFavorites().toArray();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_activate);
		writeD(_teleportFavorites.length); // teleports size
		for (int i = 0; i < _teleportFavorites.length; i++)
		{
			writeD(_teleportFavorites[i]); // teleport id
		}
	}
}