package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExRankingCharBuffzoneNpcInfo;
import l2s.gameserver.network.l2.s2c.ExRankingCharBuffzoneNpcPosition;
import l2s.gameserver.network.l2.s2c.ExRankingCharRankers;

/**
 * @author JoeAlisson
 */
public class RequestRankingCharRankers extends L2GameClientPacket {
	private int _group;
	private int _scope;
	private int _race;

	@Override
	protected boolean readImpl() {
		_group = readC(); // Tab Id
		_scope = readC(); // All or personal
		_race = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		activeChar.sendPacket(new ExRankingCharRankers(activeChar, _group, _scope, _race));
		
		boolean activeNpc = ServerVariables.getBool("buffNpcActive", false);
		if (activeNpc)
		{
			int x = ServerVariables.getInt("buffNpcX", -50000);
			int y = ServerVariables.getInt("buffNpcY", -50000);
			int z = ServerVariables.getInt("buffNpcZ", -50000);
			if ((x != -50000) && (y != -50000) && (z != -50000))
				activeChar.sendPacket(new ExRankingCharBuffzoneNpcPosition((byte) 1, x, y, z));
			else
			{
				ServerVariables.unset("buffNpcActive");
				ServerVariables.unset("buffNpcX");
				ServerVariables.unset("buffNpcY");
				ServerVariables.unset("buffNpcZ");
				activeChar.sendPacket(new ExRankingCharBuffzoneNpcPosition((byte) 0, 0, 0, 0));
			}
				
		}
		else
			activeChar.sendPacket(new ExRankingCharBuffzoneNpcPosition((byte) 0, 0, 0, 0));
		
		if (RankManager.getInstance().getPlayerGlobalRank(activeChar) == 1)
			activeChar.sendPacket(new ExRankingCharBuffzoneNpcInfo());
	}
}