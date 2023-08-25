package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPvpRankingMyInfo;

/**
 * @author nexvill
 */
public class RequestExPvpRankingMyInfo extends L2GameClientPacket 
{

	@Override
	protected boolean readImpl() 
	{
		return true;
	}

	@Override
	protected void runImpl() 
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExPvpRankingMyInfo(activeChar));
	}
}