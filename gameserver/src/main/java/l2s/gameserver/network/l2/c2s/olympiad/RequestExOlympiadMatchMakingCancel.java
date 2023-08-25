package l2s.gameserver.network.l2.c2s.olympiad;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadMatchMakingResult;

/**
 * @author nexvill
 */
public class RequestExOlympiadMatchMakingCancel extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (Olympiad.unregisterParticipant(activeChar))
			activeChar.sendPacket(new ExOlympiadMatchMakingResult(false));
	}
}