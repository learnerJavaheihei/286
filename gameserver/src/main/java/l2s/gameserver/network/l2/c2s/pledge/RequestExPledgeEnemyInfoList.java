package l2s.gameserver.network.l2.c2s.pledge;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.pledge.ExPledgeEnemyInfoList;
import l2s.gameserver.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eden
 */
public class RequestExPledgeEnemyInfoList extends L2GameClientPacket
{
	private int playerClan;
	
	@Override
	protected boolean readImpl()
	{
		playerClan = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final Clan clan = ClanTable.getInstance().getClan(playerClan);
		if (clan != null && clan.getAnyMember(activeChar.getObjectId()) != null) {
			activeChar.sendPacket(new ExPledgeEnemyInfoList(clan));
		}
	}
}