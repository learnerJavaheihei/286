package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;

/**
 * @author nexvill
 */
public class ExRankingCharHistory extends L2GameServerPacket
{
	private final Player _player;

	public ExRankingCharHistory(Player player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		int daysCount = 0;
		for (int i = 1; i < 9; i++)
		{
			if (_player.getVarInt(PlayerVariables.RANKING_HISTORY_DAY + "_" + i + "_day", 0) != 0)
			{
				daysCount++;
			}
		}
		
		writeC(daysCount);
		writeC(0);
		writeC(0);
		writeC(0);
		
		if (daysCount > 0)
		{
			for (int i = 1; i <= daysCount; i++)
			{
				int date = _player.getVarInt(PlayerVariables.RANKING_HISTORY_DAY + "_" + i + "_day", 0);
				int rank = _player.getVarInt(PlayerVariables.RANKING_HISTORY_DAY + "_" + i + "_rank", 0);
				long exp1 = 0;
				long exp2 = 0;
				if (i < daysCount)
				{
					exp1 = _player.getVarLong(PlayerVariables.RANKING_HISTORY_DAY + "_" + i + "_exp", 0);
					int j = i + 1;
					exp2 = _player.getVarLong(PlayerVariables.RANKING_HISTORY_DAY + "_" + j + "_exp", 0);
				}
				long exp = exp1 - exp2;
				writeD(date);
				writeD(rank);
				writeQ(exp);
			}
		}
	}
}