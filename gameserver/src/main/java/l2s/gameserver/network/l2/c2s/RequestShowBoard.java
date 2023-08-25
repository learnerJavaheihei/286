package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class RequestShowBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;

	/**
	 * packet type id 0x5E
	 *
	 * sample
	 *
	 * 5E
	 * 01 00 00 00
	 *
	 * format:		cd
	 */
	@Override
	public boolean readImpl()
	{
		_unknown = readD();
		return true;
	}

	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(Config.BBS_ENABLED)
		{
			IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(Config.BBS_DEFAULT_PAGE);
			if(handler != null)
				handler.onBypassCommand(activeChar, Config.BBS_DEFAULT_PAGE);
		}
		else
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
	}
}
