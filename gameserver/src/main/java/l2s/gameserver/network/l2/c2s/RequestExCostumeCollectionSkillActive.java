package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeCollectionSkillActive extends L2GameClientPacket {
	private int size;

	@Override
	protected boolean readImpl() {
		size = _buf.remaining();
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

		activeChar.sendMessage(getClass().getSimpleName() + ": size=" + size);
	}
}
