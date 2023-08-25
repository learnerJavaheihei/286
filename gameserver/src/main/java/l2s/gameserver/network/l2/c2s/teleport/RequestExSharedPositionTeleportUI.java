package l2s.gameserver.network.l2.c2s.teleport;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.teleport.ExSharedPositionTeleportUI;

/**
 * @author nexvill
 */
public class RequestExSharedPositionTeleportUI extends L2GameClientPacket
{
	private int _allow, _tpId;
	@Override
	protected boolean readImpl()
	{
		_allow = readC();
		_tpId = readH(); // tp id
		readC(); // ??
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		
		if (_allow == 1)
			player.sendPacket(new ExSharedPositionTeleportUI(player, _tpId));
	}
}