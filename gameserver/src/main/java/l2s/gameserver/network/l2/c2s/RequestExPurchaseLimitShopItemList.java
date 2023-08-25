package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.LimitedShopHolder;
import l2s.gameserver.model.Player;

/**
 * @author nexvill
 */
public class RequestExPurchaseLimitShopItemList extends L2GameClientPacket
{
	private int _listId;

	@Override
	protected boolean readImpl()
	{
		_listId = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		LimitedShopHolder.getInstance().SeparateAndSend(_listId, activeChar);
	}
}