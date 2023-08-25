package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExCostumeShortcutList;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeChangeShortcut extends L2GameClientPacket {
	private int unk1, page, slot, costumeId;

	@Override
	protected boolean readImpl() {
		unk1 = readD();
		page = readD();
		slot = readD();
		costumeId = readD();
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

		if (activeChar.isInCombat()) {
			activeChar.sendPacket(SystemMsg.YOU_CANNOR_SET_AND_UNBLOCK_YOUR_FAVORITES_DURING_A_FIGHT);
			return;
		}

		if (!activeChar.getCostumeList().setShortCut(page, slot, costumeId)) {
			activeChar.sendActionFailed();
			return;
		}

		if(costumeId > 0)
			activeChar.sendPacket(new ExCostumeShortcutList(activeChar));
		else
			activeChar.sendPacket(new ExCostumeShortcutList(page, slot, costumeId));
	}
}
