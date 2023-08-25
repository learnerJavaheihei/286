package l2s.gameserver.network.l2.c2s.teleport;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.teleport.ExSharedPositionSharingUI;

/**
 * @author nexvill
 */
public class RequestExSharedPositionSharingUI extends L2GameClientPacket
{

	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		
		player.sendPacket(new ExSharedPositionSharingUI(player));
	}
}