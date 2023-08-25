package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExChooseCostumeItem;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeUseItem extends L2GameClientPacket {
	private int itemObjectId;

	@Override
	protected boolean readImpl() {
		itemObjectId = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (Config.EX_COSTUME_DISABLE) {
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setActive();

		ItemInstance item = activeChar.getInventory().getItemByObjectId(itemObjectId);
		if (item == null) {
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(new ExChooseCostumeItem(item.getItemId())); // TODO: Нужен ли он здесь?
		activeChar.useItem(item, false, true);
	}
}
