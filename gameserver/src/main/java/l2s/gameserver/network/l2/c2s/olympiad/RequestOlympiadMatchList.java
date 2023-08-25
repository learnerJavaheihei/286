package l2s.gameserver.network.l2.c2s.olympiad;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExReceiveOlympiadPacket;

/**
 * @author nexvill
**/
public class RequestOlympiadMatchList extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(!Olympiad.inCompPeriod() || Olympiad.isOlympiadEnd())
		{
			player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}
		player.sendPacket(new ExReceiveOlympiadPacket.MatchList());
	}
}
