package l2s.gameserver.network.l2.s2c;

import java.util.Map;

import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.StatsSet;

/**
 * @author nexvill
 */
public class ExPvpRankingMyInfo extends L2GameServerPacket
{
	private final Player _player;
	private final Map<Integer, StatsSet> _playerList;

	public ExPvpRankingMyInfo(Player player)
	{
		_player = player;
		_playerList = RankManager.getInstance().getPvpRankList();
	}

	@Override
	protected final void writeImpl()
	{
		if (_playerList.size() > 0)
		{
			boolean found = false;
			for (Integer id : _playerList.keySet())
			{
				final StatsSet player = _playerList.get(id);
				if (player.getInteger("charId") == _player.getObjectId())
				{
					found = true;
					writeQ(player.getInteger("points")); // pvp points
					writeD(id); // current rank
					writeD(id); // ingame shown change in rank as this value - current rank value.
					writeD(player.getInteger("kills")); // kills
					writeD(player.getInteger("deaths")); // deaths
				}
			}
			if (!found)
			{
				writeQ(0); // pvp points
				writeD(0); // current rank
				writeD(0); // ingame shown change in rank as this value - current rank value.
				writeD(0); // kills
				writeD(0); // deaths
			}
		}
		else
		{
			writeQ(0); // pvp points
			writeD(0); // current rank
			writeD(0); // ingame shown change in rank as this value - current rank value.
			writeD(0); // kills
			writeD(0); // deaths
		}
	}
}