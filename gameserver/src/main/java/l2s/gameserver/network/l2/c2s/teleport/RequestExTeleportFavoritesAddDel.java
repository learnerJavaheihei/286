package l2s.gameserver.network.l2.c2s.teleport;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;

/**
 * @author nexvill
 */
public class RequestExTeleportFavoritesAddDel extends L2GameClientPacket
{
	private boolean _type;
	private int _teleportId;
	
	@Override
	protected boolean readImpl()
	{
		_type = readC() == 1 ? true : false;
		_teleportId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (_type)
		{
			activeChar.addTeleportFavorite(_teleportId);
		}
		else
		{
			activeChar.removeTeleportFavorite(_teleportId);
		}
	}
}