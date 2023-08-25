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
public class ExPvpRankingList extends L2GameServerPacket
{
	private final Player _player;
	private final int _race;
	private final int _season;
	private final int _group;
	private final int _scope;
	private final Map<Integer, StatsSet> _playerList;
	private final Map<Integer, StatsSet> _snapshotList;

	public ExPvpRankingList(Player player, int season, int group, int scope, int race)
	{
		_player = player;
		
		_season = season;
		_group = group;
		_scope = scope;
		_race = race;
		
		_playerList = RankManager.getInstance().getPvpRankList();
		_snapshotList = RankManager.getInstance().getOldPvpRankList();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_season);
		writeC(_group);
		writeC(_scope);
		writeD(_race);
		
		Map<Integer, StatsSet> plrList;
		if (_season == 1)
		{
			plrList = _playerList;
		}
		else
		{
			plrList = _snapshotList;
		}
		
		if (plrList.size() > 0)
		{
			switch (_group)
			{
				case 0: // all
				{
					if (_scope == 0) // all
					{
						final int count = plrList.size() > 150 ? 150 : plrList.size();
						
						writeD(count);
						
						for (int id : plrList.keySet())
						{
							final StatsSet player = plrList.get(id);
							
							writeString(player.getString("name"));
							writeString(player.getString("clanName"));
							writeD(player.getInteger("level"));
							writeD(player.getInteger("race"));
							writeD(player.getInteger("classId"));
							writeD(player.getInteger("points"));
							writeD(0); // unk
							writeD(id); // server rank
							writeD(0); // previous server rank, idk why 0
							writeD(player.getInteger("kills"));
							writeD(player.getInteger("deaths"));
						}
					}
					else
					{
						boolean found = false;
						for (int id : plrList.keySet())
						{
							final StatsSet player = plrList.get(id);
							
							if (player.getInteger("charId") == _player.getObjectId())
							{
								found = true;
								writeString(player.getString("name"));
								writeString(player.getString("clanName"));
								writeD(player.getInteger("level"));
								writeD(player.getInteger("race"));
								writeD(player.getInteger("classId"));
								writeD(player.getInteger("points"));
								writeD(0); // unk
								writeD(id); // server rank
								writeD(0); // previous server rank, idk why 0
								writeD(player.getInteger("kills"));
								writeD(player.getInteger("deaths"));
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
						
						for (int i = 1; i <= plrList.size(); i++)
						{
							final StatsSet player = plrList.get(i);
							if (_race == player.getInteger("race"))
							{
								count++;
							}
						}
						writeD(count > 100 ? 100 : count);
					
						int i = 1;
						for (int id : plrList.keySet())
						{
							final StatsSet player = plrList.get(id);
							
							if (_race == player.getInteger("race"))
							{
								writeString(player.getString("name"));
								writeString(player.getString("clanName"));
								writeD(player.getInteger("level"));
								writeD(player.getInteger("race"));
								writeD(player.getInteger("classId"));
								writeD(player.getInteger("points"));
								writeD(0); // unk
								writeD(i); // server rank
								writeD(0); // previous server rank, idk why 0
								writeD(player.getInteger("kills"));
								writeD(player.getInteger("deaths"));
								i++;
							}
						}
					}
					else
					{
						boolean found = false;
						
						final Map<Integer, StatsSet> raceList = new ConcurrentHashMap<>();
						int i = 1;
						for (int id : plrList.keySet())
						{
							final StatsSet set = plrList.get(id);
							
							if (_player.getRace().ordinal() == set.getInteger("race"))
							{
								raceList.put(i, plrList.get(id));
								i++;
							}
						}
						
						for (int id : raceList.keySet())
						{
							final StatsSet player = raceList.get(id);
							
							if (player.getInteger("charId") == _player.getObjectId())
							{
								found = true;
								writeString(player.getString("name"));
								writeString(player.getString("clanName"));
								writeD(player.getInteger("level"));
								writeD(player.getInteger("race"));
								writeD(player.getInteger("classId"));
								writeD(player.getInteger("points"));
								writeD(0); // unk
								writeD(id); // server rank
								writeD(0); // previous server rank, idk why 0
								writeD(player.getInteger("kills"));
								writeD(player.getInteger("deaths"));
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
						for (int id : plrList.keySet())
						{
							final StatsSet set = plrList.get(id);
							
							if (_player.getClan().getName() == set.getString("clanName"))
							{
								clanList.put(i, plrList.get(id));
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
							writeD(player.getInteger("race"));
							writeD(player.getInteger("classId"));
							writeD(player.getInteger("points"));
							writeD(0); // unk
							writeD(id); // server rank
							writeD(0); // previous server rank, idk why 0
							writeD(player.getInteger("kills"));
							writeD(player.getInteger("deaths"));
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
							for (Integer id2 : plrList.keySet())
							{
								final StatsSet temp = plrList.get(id2);
								if (temp.getInteger("charId") == friend.getObjectId())
								{
									friendList.add(temp.getInteger("charId"));
									count++;
								}
							}
						}
						friendList.add(_player.getObjectId());
					
						writeD(count);
					
						for (int id : plrList.keySet())
						{
							final StatsSet player = plrList.get(id);
							int i = 1;
							if (friendList.contains(player.getInteger("charId")))
							{
								writeString(player.getString("name"));
								writeString(player.getString("clanName"));
								writeD(player.getInteger("level"));
								writeD(player.getInteger("race"));
								writeD(player.getInteger("classId"));
								writeD(player.getInteger("points"));
								writeD(0); // unk
								writeD(i); // server rank
								writeD(0); // previous server rank, idk why 0
								writeD(player.getInteger("kills"));
								writeD(player.getInteger("deaths"));
								i++;
							}
						}
					}
					else
					{
						if (plrList.size() > 0)
						{
							for (int id : plrList.keySet())
							{
								final StatsSet player = plrList.get(id);
								if (_player.getObjectId() == player.getInteger("charId"))
								{
									writeD(1);
									writeString(player.getString("name"));
									writeString(player.getString("clanName"));
									writeD(player.getInteger("level"));
									writeD(player.getInteger("race"));
									writeD(player.getInteger("classId"));
									writeD(player.getInteger("points"));
									writeD(0); // unk
									writeD(1); // server rank
									writeD(0); // previous server rank, idk why 0
									writeD(player.getInteger("kills"));
									writeD(player.getInteger("deaths"));
								}
							}
						}
						else
						{
							writeD(0);
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