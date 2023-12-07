package l2s.gameserver.network.l2.s2c.pledge;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author Eden
 * @reworked by nexvill
 * FE 7A 02
 * 01 00 00 00
 * 4A 00 00 00
 * 2A 00 10 60
 * 0C 00 4F 00 72 00 61 00 6E 00 67 00 65 00 50 00 6C 00 61 00 6E 00 65 00 74 00
 * 07 00 B5 30 CD 30 A2 30 C4 30 6F 52 39 82 77 95
 */
public class ExPledgeEnemyInfoList extends L2GameServerPacket
{
	private final Clan playerClan;
	public ExPledgeEnemyInfoList(Clan playerClan)
	{
		this.playerClan = playerClan;
	}

	@Override
	protected final void writeImpl()
	{
		int count = 0;
		Map<Integer, Clan> attackedClans = new HashMap<>();
		int[] pointDiff = new int[30];
		for (ClanWar war : playerClan.getWars().valueCollection())
		{
			final Clan clan = war.getOpposingClan(playerClan);
//			if (war.isAttacker(clan))
//			{
				pointDiff[count] = war.getPointDiff(playerClan);
				attackedClans.put(count, clan);
				count++;
//			}
		}
		
		writeD(count);
		for (int i = 0; i < attackedClans.size(); i++)
		{
			writeD(pointDiff[i]); // point diff?
			final Clan clan = attackedClans.get(i);
			writeD(clan.getClanId());
			writeString(clan.getName());
			writeString(clan.getLeaderName());
		}
	}
}