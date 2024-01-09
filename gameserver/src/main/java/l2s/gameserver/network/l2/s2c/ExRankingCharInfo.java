package l2s.gameserver.network.l2.s2c;

import java.util.Map;

import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.StatsSet;

/**
 * @author nexvill
 */
public class ExRankingCharInfo extends L2GameServerPacket
{
	private final Player _player;
	private final Map<Integer, StatsSet> _playerList;
	private final Map<Integer, StatsSet> _snapshotList;

	public ExRankingCharInfo(Player player)
	{
		_player = player;
		_playerList = RankManager.getInstance().getRankList();
		_snapshotList = RankManager.getInstance().getSnapshotList();
	}

	@Override
	protected final void writeImpl()
	{
		if (_playerList.size() > 0)
		{
			for (Integer id : _playerList.keySet())
			{
				final StatsSet player = _playerList.get(id);
				if (player.getInteger("charId") == _player.getObjectId())
				{
					writeD(id); // server rank
					writeD(player.getInteger("raceRank")); // race rank

					for (Integer id2 : _snapshotList.keySet())
					{
						final StatsSet snapshot = _snapshotList.get(id2);
						if (player.getInteger("charId") == snapshot.getInteger("charId"))
						{
							writeD(id2); // server rank snapshot
							writeD(snapshot.getInteger("raceRank")); // race rank snapshot
							writeD(0); // class rank
							writeD(0); // class rank snapshot
							return;
						}
					}
				}
			}
			writeD(0); // server rank
			writeD(0); // race rank
			writeD(0); // server rank snapshot
			writeD(0); // race rank snapshot
			writeD(0); // class rank
			writeD(0); // class rank snapshot
		}
		else
		{
			writeD(0); // server rank
			writeD(0); // race rank
			writeD(0); // server rank snapshot
			writeD(0); // race rank snapshot
			writeD(0); // class rank
			writeD(0); // class rank snapshot
		}
	}
}