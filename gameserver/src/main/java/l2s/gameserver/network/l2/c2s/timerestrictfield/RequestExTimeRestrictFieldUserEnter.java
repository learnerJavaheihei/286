package l2s.gameserver.network.l2.c2s.timerestrictfield;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.timerestrictfield.ExTimeRestrictFieldUserEnter;

/**
 * @author nexvill
 */
public class RequestExTimeRestrictFieldUserEnter extends L2GameClientPacket
{
	private int _zoneId;
	
	@Override
	protected boolean readImpl()
	{
		_zoneId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if ((activeChar.getActiveReflection() != null) || !activeChar.isInPeaceZone())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THAT_IN_A_NONPEACE_ZONE_LOCATION);
			return;
		}
		
		if (activeChar.isInOlympiadMode() || Olympiad.isRegistered(activeChar))
		{
			activeChar.sendPacket(SystemMsg.SPECIAL_INSTANCE_ZONES_CANNOT_BE_USED_WHILE_WAITING_FOR_THE_OLYMPIAD);
			return;
		}
		
		activeChar.sendPacket(new ExTimeRestrictFieldUserEnter(activeChar, _zoneId));
	}
}