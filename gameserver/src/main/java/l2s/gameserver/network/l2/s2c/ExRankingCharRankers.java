package l2s.gameserver.network.l2.s2c;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Friend;
import l2s.gameserver.templates.StatsSet;

/**
 * @author nexvill
 */
public class ExRankingCharRankers extends L2GameServerPacket
{
	private final Player _player;
	private final int _race;
	private final int _group;
	private final int _scope;
	private final int _class;
	private final Map<Integer, StatsSet> _playerList;
	private final Map<Integer, StatsSet> _snapshotList;

	public ExRankingCharRankers(Player player, int group, int scope, int race)
	{
		_player = player;
		
		_group = group;
		_scope = scope;
		_race = race;
		_class = 0;
		
		_playerList = RankManager.getInstance().getRankList();
		_snapshotList = RankManager.getInstance().getSnapshotList();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_group);
		writeC(_scope);
		writeD(_race);
		writeD(_class);
		
		if (_playerList.size() > 0)
		{
			switch (_group)
			{
				case 0: // all
				{
					if (_scope == 0) // all
					{
						final int count = _playerList.size() > 150 ? 150 : _playerList.size();
						
						writeD(count);
						
						for (int id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							
							writeString(player.getString("name"));
							writeString(player.getString("clanName"));
							writeD(player.getInteger("level"));
							writeD(player.getInteger("classId"));
							writeD(player.getInteger("race"));
							writeD(id); // server rank
							if (_snapshotList.size() > 0)
							{
								for (int id2 : _snapshotList.keySet())
								{
									final StatsSet snapshot = _snapshotList.get(id2);
									
									if (player.getInteger("charId") == snapshot.getInteger("charId"))
									{
										writeD(id2); // server rank snapshot
										writeD(snapshot.getInteger("raceRank", 0)); // race rank snapshot
										writeD(5316850); // unk, value from JP
									}
								}
							}
							else
							{
								writeD(id);
								writeD(0);
								writeD(5316850);
							}
						}
					}
					else
					{
						boolean found = false;
						for (int id : _playerList.keySet())
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
									writeD(plr.getInteger("level"));
									writeD(plr.getInteger("classId"));
									writeD(plr.getInteger("race"));
									writeD(id2); // server rank
									writeD(id2);
									writeD(id2);
									writeD(5316850);
									
									/*if (_snapshotList.size() > 0)
									{
										for (int id3 : _snapshotList.keySet())
										{
											final StatsSet snapshot = _snapshotList.get(id3);
											if (player.getInteger("charId") == snapshot.getInteger("charId"))
											{
												writeD(id3); // server rank snapshot
												writeD(snapshot.getInteger("raceRank", 0));
												writeD(5316850);
											}
										}
									}*/
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
				case 1: // race
				{
					if (_scope == 0) // all
					{
						int count = 0;
						
						for (int i = 1; i <= _playerList.size(); i++)
						{
							final StatsSet player = _playerList.get(i);
							if (_race == player.getInteger("race"))
							{
								count++;
							}
						}
						writeD(count > 100 ? 100 : count);
						
						int i = 1;
						for (int id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							
							if (_race == player.getInteger("race"))
							{
								writeString(player.getString("name"));
								writeString(player.getString("clanName"));
								writeD(player.getInteger("level"));
								writeD(player.getInteger("classId"));
								writeD(player.getInteger("race"));
								writeD(i); // server rank
								if (_snapshotList.size() > 0)
								{
									final Map<Integer, StatsSet> snapshotRaceList = new ConcurrentHashMap<>();
									int j = 1;
									for (int id2 : _snapshotList.keySet())
									{
										final StatsSet snapshot = _snapshotList.get(id2);
										
										if (_race == snapshot.getInteger("race"))
										{
											snapshotRaceList.put(j, _snapshotList.get(id2));
											j++;
										}
									}
									for (int id2 : snapshotRaceList.keySet())
									{
										final StatsSet snapshot = snapshotRaceList.get(id2);
										
										if (player.getInteger("charId") == snapshot.getInteger("charId"))
										{
											writeD(id2); // server rank snapshot
											writeD(snapshot.getInteger("raceRank", 0)); // race rank snapshot
											writeD(5316850);
										}
									}
								}
								else
								{
									writeD(i);
									writeD(i);
									writeD(5316850);
								}
								i++;
							}
						}
					}
					else
					{
						boolean found = false;
						
						final Map<Integer, StatsSet> raceList = new ConcurrentHashMap<>();
						int i = 1;
						for (int id : _playerList.keySet())
						{
							final StatsSet set = _playerList.get(id);
							
							if (_player.getRace().ordinal() == set.getInteger("race"))
							{
								raceList.put(i, _playerList.get(id));
								i++;
							}
						}
						
						for (int id : raceList.keySet())
						{
							final StatsSet player = raceList.get(id);
							
							if (player.getInteger("charId") == _player.getObjectId())
							{
								found = true;
								final int first = id > 10 ? (id - 9) : 1;
								final int last = raceList.size() >= (id + 10) ? id + 10 : id + (raceList.size() - id);
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
									final StatsSet plr = raceList.get(id2);
									
									writeString(plr.getString("name"));
									writeString(plr.getString("clanName"));
									writeD(plr.getInteger("level"));
									writeD(plr.getInteger("classId"));
									writeD(plr.getInteger("race"));
									writeD(id2); // server rank
									writeD(id2);
									writeD(id2);
									writeD(id2);
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
				case 2: // clan
				{
					if (_player.getClan() != null)
					{
						final Map<Integer, StatsSet> clanList = new ConcurrentHashMap<>();
						int i = 1;
						for (int id : _playerList.keySet())
						{
							final StatsSet set = _playerList.get(id);
							
							if (_player.getClan().getName() == set.getString("clanName"))
							{
								clanList.put(i, _playerList.get(id));
								i++;
							}
						}
						
						writeD(clanList.size());
						
						for (int id : clanList.keySet())
						{
							final StatsSet player = clanList.get(id);
							
							writeString(player.getString("name"));
							writeString(player.getString("clanName"));
							writeD(player.getInteger("level"));
							writeD(player.getInteger("classId"));
							writeD(player.getInteger("race"));
							writeD(id); // clan rank
							if (_snapshotList.size() > 0)
							{
								for (int id2 : _snapshotList.keySet())
								{
									final StatsSet snapshot = _snapshotList.get(id2);
									
									if (player.getInteger("charId") == snapshot.getInteger("charId"))
									{
										writeD(id2); // server rank snapshot
										writeD(snapshot.getInteger("raceRank", 0)); // race rank snapshot
										writeD(5316850);
									}
								}
							}
							else
							{
								writeD(id);
								writeD(0);
								writeD(5316850);
							}
						}
					}
					else
					{
						writeD(0);
					}
					
					break;
				}
				case 3: // friend
				{
					if (_player.getFriendList().size() > 0)
					{
						final Set<Integer> friendList = ConcurrentHashMap.newKeySet();
						int count = 1;
						for (Friend friend : _player.getFriendList().values())
						{
							for (Integer id2 : _playerList.keySet())
							{
								final StatsSet temp = _playerList.get(id2);
								if (temp.getInteger("charId") == friend.getObjectId())
								{
									friendList.add(temp.getInteger("charId"));
									count++;
								}
							}
						}
						friendList.add(_player.getObjectId());
						
						writeD(count);
						
						for (int id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							if (friendList.contains(player.getInteger("charId")))
							{
								writeString(player.getString("name"));
								writeString(player.getString("clanName"));
								writeD(player.getInteger("level"));
								writeD(player.getInteger("classId"));
								writeD(player.getInteger("race"));
								writeD(id); // friend rank
								if (_snapshotList.size() > 0)
								{
									for (int id2 : _snapshotList.keySet())
									{
										final StatsSet snapshot = _snapshotList.get(id2);
										
										if (player.getInteger("charId") == snapshot.getInteger("charId"))
										{
											writeD(id2); // server rank snapshot
											writeD(snapshot.getInteger("raceRank", 0)); // race rank snapshot
											writeD(5316850);
										}
									}
								}
								else
								{
									writeD(id);
									writeD(0);
									writeD(5316850);
								}
							}
						}
					}
					else
					{
						writeD(1);
						
						writeString(_player.getName());
						if (_player.getClan() != null)
						{
							writeString(_player.getClan().getName());
						}
						else
						{
							writeString("");
						}
						writeD(_player.getBaseSubClass().getLevel()); //level
						writeD(_player.getBaseSubClass().getClassId());
						writeD(_player.getRace().ordinal());
						writeD(1); // clan rank
						if (_snapshotList.size() > 0)
						{
							for (Integer id : _snapshotList.keySet())
							{
								final StatsSet snapshot = _snapshotList.get(id);
								
								if (_player.getObjectId() == snapshot.getInteger("charId"))
								{
									writeD(id); // server rank snapshot
									writeD(snapshot.getInteger("raceRank", 0)); // race rank snapshot
									writeD(5316850);
								}
							}
						}
						else
						{
							writeD(0);
							writeD(0);
							writeD(5316850);
						}
					}
					break;
				}
			}
		}
		else
		{
			writeD(0);
		}
	}
}