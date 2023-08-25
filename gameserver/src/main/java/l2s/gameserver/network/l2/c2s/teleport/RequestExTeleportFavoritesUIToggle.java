package l2s.gameserver.network.l2.c2s.teleport;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.teleport.ExTeleportFavoritesList;

/**
 * @author nexvill
 */
public class RequestExTeleportFavoritesUIToggle extends L2GameClientPacket
{
	private boolean _on;
	@Override
	protected boolean readImpl()
	{
		_on = readC() == 1 ? true : false;
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		activeChar.sendPacket(new ExTeleportFavoritesList(_on, activeChar));
	}
}