package l2s.gameserver.network.l2.s2c.olympiad;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.StatsSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author NviX
 */
public class ExOlympiadRankingInfo extends L2GameServerPacket {
	private final Player _player;

	private final int _tabId;
	private final int _rankingType;
	private final int _unk;
	private final int _classId;
	private final int _serverId;
	private final Map<Integer, StatsSet> _playerList;
	private final Map<Integer, StatsSet> _snapshotList;

	public ExOlympiadRankingInfo(Player player, int tabId, int rankingType, int unk, int classId, int serverId) {
		_player = player;
		_tabId = tabId;
		_rankingType = rankingType;
		_unk = unk;
		_classId = classId;
		_serverId = serverId;
		_playerList = RankManager.getInstance().getOlyRankList();
		_snapshotList = RankManager.getInstance().getSnapshotOlyList();
	}

	@Override
	protected void writeImpl() {
		writeC(_tabId); // Tab id
		writeC(_rankingType); // ranking type
		writeC(_unk); // unk, shows 1 all time
		writeD(_classId); // class id (default 148) or caller class id for personal rank
		writeD(_serverId); // 0 - all servers, server id - for caller server
		writeD(933); // unk, 933 all time

		if (_playerList.size() > 0)
		{
			switch (_tabId)
			{
				case 0:
				{
					if (_rankingType == 0)
					{
						writeD(Math.min(_playerList.size(), 100));

						for (Integer id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							writeString(player.getString("name")); // name
							writeString(player.getString("clanName")); // clan name
							writeD(id); // rank

							if (_snapshotList.size() > 0)
							{
								for (Integer id2 : _snapshotList.keySet())
								{
									final StatsSet snapshot = _snapshotList.get(id2);
									if (player.getInteger("charId") == snapshot.getInteger("charId"))
									{
										writeD(id2); // previous rank
									}
								}
							}
							else
							{
								writeD(id);
							}

							writeD(Config.REQUEST_ID);// server id
							writeD(player.getInteger("level"));// level
							writeD(player.getInteger("classId"));// class id
							writeD(player.getInteger("clanLevel"));// clan level
							writeD(player.getInteger("competitions_win"));// win count
							writeD(player.getInteger("competitions_loose"));// lose count
							writeD(player.getInteger("olympiad_points"));// points
							writeD(player.getInteger("count"));// hero counts
							writeD(player.getInteger("legend_count"));// legend counts
						}
					}
					else
					{
						boolean found = false;
						for (Integer id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							if (player.getInteger("charId") == _player.getObjectId())
							{
								found = true;

								final int first = id > 10 ? (id - 9) : 1;
								final int last = _playerList.size() >= (id + 10) ? id + 10 : id + (_playerList.size() - id);
								if (first == 1)
								{
									writeD(last - (first - 1));
								}
								else
								{
									writeD(last - first);
								}

								for (int id2 = first; id2 <= last; id2++)
								{
									final StatsSet plr = _playerList.get(id2);
									writeString(plr.getString("name"));
									writeString(plr.getString("clanName"));
									writeD(id2);
									if (_snapshotList.size() > 0)
									{
										for (Integer id3 : _snapshotList.keySet())
										{
											final StatsSet snapshot = _snapshotList.get(id3);
											if (player.getInteger("charId") == snapshot.getInteger("charId"))
											{
												writeD(id3); // class rank snapshot
											}
										}
									}
									else
									{
										writeD(id2);
									}

									writeD(Config.REQUEST_ID);
									writeD(plr.getInteger("level"));
									writeD(plr.getInteger("classId"));
									writeD(plr.getInteger("clanLevel"));// clan level
									writeD(plr.getInteger("competitions_win"));// win count
									writeD(plr.getInteger("competitions_loose"));// lose count
									writeD(plr.getInteger("olympiad_points"));// points
									writeD(plr.getInteger("count"));// hero counts
									writeD(plr.getInteger("legend_count"));// legend counts
								}
							}
						}
						if (!found)
						{
							writeD(0);
						}
					}
					break;
				}
				case 1:
				{
					if (_rankingType == 0)
					{
						int count = 0;
						for (int i = 1; i <= _playerList.size(); i++)
						{
							final StatsSet player = _playerList.get(i);
							if (_classId == player.getInteger("classId"))
							{
								count++;
							}
						}
						writeD(count > 50 ? 50 : count);

						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							if (_classId == player.getInteger("classId"))
							{
								writeString(player.getString("name"));
								writeString(player.getString("clanName"));
								writeD(i); // class rank
								if (_snapshotList.size() > 0)
								{
									final Map<Integer, StatsSet> snapshotRaceList = new ConcurrentHashMap<>();
									int j = 1;
									for (Integer id2 : _snapshotList.keySet())
									{
										final StatsSet snapshot = _snapshotList.get(id2);
										if (_classId == snapshot.getInteger("classId"))
										{
											snapshotRaceList.put(j, _snapshotList.get(id2));
											j++;
										}
									}
									for (Integer id2 : snapshotRaceList.keySet())
									{
										final StatsSet snapshot = snapshotRaceList.get(id2);
										if (player.getInteger("charId") == snapshot.getInteger("charId"))
										{
											writeD(id2); // class rank snapshot
										}
									}
								}
								else
								{
									writeD(i);
								}

								writeD(Config.REQUEST_ID);
								writeD(player.getInteger("level"));
								writeD(player.getInteger("classId"));
								writeD(player.getInteger("clanLevel"));// clan level
								writeD(player.getInteger("competitions_win"));// win count
								writeD(player.getInteger("competitions_loose"));// lose count
								writeD(player.getInteger("olympiad_points"));// points
								writeD(player.getInteger("count"));// hero counts
								writeD(player.getInteger("legend_count"));// legend counts
								i++;
							}
						}
					}
					else
					{
						boolean found = false;
						final Map<Integer, StatsSet> classList = new ConcurrentHashMap<>();
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatsSet set = _playerList.get(id);
							if (_player.getBaseClassId() == set.getInteger("classId"))
							{
								classList.put(i, _playerList.get(id));
								i++;
							}
						}

						for (Integer id : classList.keySet())
						{
							final StatsSet player = classList.get(id);
							if (player.getInteger("charId") == _player.getObjectId())
							{
								found = true;
								final int first = id > 10 ? (id - 9) : 1;
								final int last = classList.size() >= (id + 10) ? id + 10 : id + (classList.size() - id);
								if (first == 1)
								{
									writeD(last - (first - 1));
								}
								else
								{
									writeD(last - first);
								}
								for (int id2 = first; id2 <= last; id2++)
								{
									final StatsSet plr = classList.get(id2);
									writeString(plr.getString("name"));
									writeString(plr.getString("clanName"));
									writeD(id2); // class rank
									writeD(id2);
									writeD(Config.REQUEST_ID);
									writeD(player.getInteger("level"));
									writeD(player.getInteger("classId"));
									writeD(player.getInteger("clanLevel"));// clan level
									writeD(player.getInteger("competitions_win"));// win count
									writeD(player.getInteger("competitions_loose"));// lose count
									writeD(player.getInteger("olympiad_points"));// points
									writeD(player.getInteger("count"));// hero counts
									writeD(player.getInteger("legend_count"));// legend counts
								}
							}
						}
						if (!found)
						{
							writeD(0);
						}
					}
					break;
				}
			}
		}
	}
}
