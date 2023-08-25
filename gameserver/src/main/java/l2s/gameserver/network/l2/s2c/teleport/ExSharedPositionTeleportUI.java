package l2s.gameserver.network.l2.s2c.teleport;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExSharedPositionTeleportUI extends L2GameServerPacket
{
	private Player _player;
	private int _tpId;
	public ExSharedPositionTeleportUI(Player player, int tpId)
	{
		_player = player;
		_tpId = tpId;
	}

	@Override
	protected final void writeImpl()
	{
		if (ServerVariables.getString("tpId_" + _tpId + "_name") != null)
		{
			writeString(ServerVariables.getString("tpId_" + _tpId + "_name"));
			writeD(_tpId);
			writeD(_player.getVarInt(PlayerVariables.SHARED_POSITION_TELEPORTS, Config.SHARED_TELEPORTS_PER_DAY)); // exist teleports
			writeH(150); // ?? also not work with low values like 0 or 1
			writeD(ServerVariables.getInt("tpId_" + _tpId + "_x"));
			writeD(ServerVariables.getInt("tpId_" + _tpId + "_y"));
			writeD(ServerVariables.getInt("tpId_" + _tpId + "_z"));
		}
	}
}