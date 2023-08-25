package l2s.gameserver.network.l2.c2s.pledge;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.PledgeReceiveWarList;
import l2s.gameserver.network.l2.s2c.pledge.ExPledgeEnemyInfoList;
import l2s.gameserver.network.l2.s2c.pledge.ExPledgeV3Info;

/**
 * @author nexvill
 */
public class RequestExPledgeV3Info extends L2GameClientPacket
{
	private int page;
	@Override
	protected boolean readImpl()
	{
		page = readC(); // unk 0
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (activeChar.getClan() == null)
			return;
		
		activeChar.sendPacket(new ExPledgeV3Info(activeChar.getClan().getPoints(), activeChar.getClan().getRank(), activeChar.getClan().getAnnounce(), activeChar.getClan().isShowAnnounceOnEnter()));
		activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan(), page));
		activeChar.sendPacket(new ExPledgeEnemyInfoList(activeChar.getClan()));
	}
}