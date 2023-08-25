package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.network.l2.s2c.CharacterSelectedPacket;
import l2s.gameserver.network.l2.s2c.ExNeedToChangeName;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Bonux
**/
public class RequestExChangeName extends L2GameClientPacket
{
	private int _type;
	private String _newName;
	private int _charSlot;

	@Override
	protected boolean readImpl()
	{
		_type = readD();
		_newName = readS();
		_charSlot = readD();	// Char slot
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_type == ExNeedToChangeName.TYPE_PLAYER)
		{
			String changedOldName = activeChar.getVar(Player.CHANGED_OLD_NAME);
			if(changedOldName == null || StringUtils.isEmpty(changedOldName))
			{
				activeChar.unsetVar(Player.CHANGED_OLD_NAME);
				sendPacket(new CharacterSelectedPacket(activeChar, getClient().getSessionKey().playOkID1));
				return;
			}
			if(_newName == null || _newName.isEmpty())
			{
				sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER, ExNeedToChangeName.NAME_ALREADY_IN_USE_OR_INCORRECT_REASON, changedOldName));
				return;
			}
			if(!Util.isMatchingRegexp(_newName, Config.CNAME_TEMPLATE))
			{
				sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER, ExNeedToChangeName.NAME_ALREADY_IN_USE_OR_INCORRECT_REASON, changedOldName));
				return;
			}
			if(!activeChar.getName().equalsIgnoreCase(_newName) && CharacterDAO.getInstance().getObjectIdByName(_newName) > 0)
			{
				sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER, ExNeedToChangeName.NAME_ALREADY_IN_USE_OR_INCORRECT_REASON, changedOldName));
				return;
			}

			activeChar.setName(_newName);
			activeChar.saveNameToDB();
			activeChar.unsetVar(Player.CHANGED_OLD_NAME);
			sendPacket(new CharacterSelectedPacket(activeChar, getClient().getSessionKey().playOkID1));
		}
		else if(_type == ExNeedToChangeName.TYPE_PLEDGE)
		{
			String changedOldName = activeChar.getVar(Player.CHANGED_PLEDGE_NAME);
			if(changedOldName == null || StringUtils.isEmpty(changedOldName))
			{
				activeChar.unsetVar(Player.CHANGED_PLEDGE_NAME);
				return;
			}

			Clan clan = activeChar.getClan();
			if(clan == null)
			{
				activeChar.unsetVar(Player.CHANGED_PLEDGE_NAME);
				return;
			}

			SubUnit subUnit = null;
			for(SubUnit s : clan.getAllSubUnits())
			{
				if(s.getLeaderObjectId() == activeChar.getObjectId())
				{
					subUnit = s;
					break;
				}
			}

			if(subUnit == null)
			{
				activeChar.unsetVar(Player.CHANGED_PLEDGE_NAME);
				return;
			}

			if(!Util.isMatchingRegexp(_newName, Config.CLAN_NAME_TEMPLATE))
			{
				sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLEDGE, ExNeedToChangeName.NAME_ALREADY_IN_USE_OR_INCORRECT_REASON, changedOldName));
				return;
			}

			for(SubUnit s : clan.getAllSubUnits())
			{
				if(s.getName().equalsIgnoreCase(_newName))
				{
					sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLEDGE, ExNeedToChangeName.NAME_ALREADY_IN_USE_OR_INCORRECT_REASON, changedOldName));
					return;
				}
			}

			if(!subUnit.getName().equalsIgnoreCase(_newName) && ClanTable.getInstance().getClanByName(_newName) != null)
			{
				sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLEDGE, ExNeedToChangeName.NAME_ALREADY_IN_USE_OR_INCORRECT_REASON, changedOldName));
				return;
			}

			subUnit.setName(_newName, true);
			clan.broadcastClanStatus(true, true, false);
			activeChar.unsetVar(Player.CHANGED_PLEDGE_NAME);
		}
	}
}