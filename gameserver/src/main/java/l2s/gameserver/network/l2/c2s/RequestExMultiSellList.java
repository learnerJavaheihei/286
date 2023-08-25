package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;

public class RequestExMultiSellList extends L2GameClientPacket
{
	private int _listId;
	@Override
	protected boolean readImpl()
	{
		_listId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		MultiSellHolder.getInstance().SeparateAndSend(_listId, activeChar, 0);
	}
}