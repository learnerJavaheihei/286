package l2s.gameserver.network.l2.c2s.blessing;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.blessing.ExBlessOptionPutItem;

public class RequestExBlessOptionPutItem extends L2GameClientPacket
{
	private int _itemObjId;
	@Override
	protected boolean readImpl()
	{
		_itemObjId = readD(); // item object id
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		activeChar.sendPacket(new ExBlessOptionPutItem(activeChar, _itemObjId));
	}
}