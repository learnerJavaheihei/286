package l2s.gameserver.network.l2.c2s;

import l2s.commons.ban.BanBindType;
import l2s.gameserver.instancemanager.GameBanManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.GameClient.GameClientState;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectedPacket;
import l2s.gameserver.network.l2.s2c.ExNeedToChangeName;
import l2s.gameserver.utils.AutoBan;

import org.apache.commons.lang3.StringUtils;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;

	/**
	 * Format: cdhddd
	 */
	@Override
	protected boolean readImpl()
	{
		_charSlot = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();

		if(client.getActiveChar() != null)
			return;

		if(!client.secondaryAuthed())
		{
			sendPacket(ActionFailPacket.STATIC);
			return;
		}

		int objId = client.getObjectIdForSlot(_charSlot);

		if(GameBanManager.getInstance().isBanned(BanBindType.PLAYER, objId) || AutoBan.isBanned(objId))
		{
			sendPacket(ActionFailPacket.STATIC);
			return;
		}

		Player activeChar = client.loadCharFromDisk(_charSlot);
		if(activeChar == null)
		{
			sendPacket(ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.getAccessLevel() < 0)
			activeChar.setAccessLevel(0);

		client.setState(GameClientState.IN_GAME);
		activeChar.setOnlineStatus(true); // Заглушка для МА, TODO: Перевести на другой МА или МА перевести на xml-rpc.
		activeChar.storeLastIpAndHWID(client.getIpAddr(), client.getHWID());

		String changedOldName = activeChar.getVar(Player.CHANGED_OLD_NAME);
		if(changedOldName != null && !StringUtils.isEmpty(changedOldName))
		{
			sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER, ExNeedToChangeName.NONE_REASON, changedOldName));
			return;
		}
		sendPacket(new CharacterSelectedPacket(activeChar, client.getSessionKey().playOkID1));
	}
}