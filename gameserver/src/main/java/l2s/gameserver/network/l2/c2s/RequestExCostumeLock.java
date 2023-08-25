package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Costume;
import l2s.gameserver.model.actor.instances.player.CostumeList;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExCostumeLock;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeLock extends L2GameClientPacket {
	private int costumeId;
	private boolean locked;

	@Override
	protected boolean readImpl() {
		costumeId = readD();
		locked = readC() > 0;
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
			activeChar.sendPacket(ExCostumeLock.FAIL);
			return;
		}

		CostumeList costumeList = activeChar.getCostumeList();

		costumeList.getLock().lock();
		try {
			Costume costume = costumeList.get(costumeId);
			if (costume == null) {
				activeChar.sendActionFailed();
				return;
			}

			costume.setFlag(Costume.IS_NEW, false);
			costume.setFlag(Costume.IS_LOCKED, locked);
			activeChar.sendPacket(new ExCostumeLock(1, costume.getId(), locked));
		} finally {
			costumeList.getLock().unlock();
		}
	}
}
