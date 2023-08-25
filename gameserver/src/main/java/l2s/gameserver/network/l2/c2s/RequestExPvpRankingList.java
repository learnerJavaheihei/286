package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPvpRankingList;

/**
 * @author nexvill
 */
public class RequestExPvpRankingList extends L2GameClientPacket 
{
	int _season, _tabId, _type, _race;

	@Override
	protected boolean readImpl() 
	{
		_season = readC();
		_tabId = readC();
		_type = readC();
		_race = readD();
		return true;
	}

	@Override
	protected void runImpl() 
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExPvpRankingList(activeChar, _season, _tabId, _type, _race));
	}
}