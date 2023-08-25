package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;

/**
 * @author Bonux
**/
public class RequestEquipItem extends L2GameClientPacket
{
	private long _slot;

	@Override
	protected boolean readImpl()
	{
		readC();// server ID
		_slot = readQ();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setActive();

		int paperdollIndex = Inventory.getPaperdollIndex(_slot);
		if(paperdollIndex == -1)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemInstance item = activeChar.getInventory().getPaperdollItem(paperdollIndex);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(item.isEquipped() || !item.isEquipable())
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.useItem(item, false, true);
	}
}