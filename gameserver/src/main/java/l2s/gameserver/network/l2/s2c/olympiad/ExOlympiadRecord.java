package l2s.gameserver.network.l2.s2c.olympiad;

import java.util.Calendar;
import java.util.Map;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.StatsSet;

/**
 * @author nexvill
 */
public class ExOlympiadRecord extends L2GameServerPacket
{
	private final Player _player;
	private final Map<Integer, StatsSet> _playerList;

	public ExOlympiadRecord(Player player)
	{
		_player = player;
		_playerList = RankManager.getInstance().getPreviousOlyList();
	}

	@Override
	protected void writeImpl() {
		int totalRank = 0;
		int totalClassRankers = 0;
		int classRank = 0;
		int points = 0;
		int wins = 0;
		int loses = 0;
		
		for (int id : _playerList.keySet())
		{
			StatsSet player = _playerList.get(id);
			
			if (player.getInteger("classId") == _player.getClassId().getId())
			{
				totalClassRankers++;
				if (player.getInteger("objId") == _player.getObjectId())
				{
					classRank = totalClassRankers;
				}
			}
			if (player.getInteger("objId") == _player.getObjectId())
			{
				totalRank = id;
				points = player.getInteger("olympiad_points");
				wins = player.getInteger("competitions_win");
				loses = player.getInteger("competitions_lost");
			}
		}
		
		if (Olympiad.getCompetitionDone(_player.getObjectId()) == 0)
			writeD(Config.OLYMPIAD_POINTS_DEFAULT);
		else
			writeD(Olympiad.getParticipantPoints(_player.getObjectId())); // this cycle points
		writeD(Olympiad.getCompetitionWin(_player.getObjectId())); // wins this month
		writeD(Olympiad.getCompetitionLoose(_player.getObjectId())); // loses this month
		
		if (Olympiad.getCompetitionDone(_player.getObjectId()) == 0)
			writeD(5);
		else
			writeD(Olympiad.getDailyGameCounts(_player.getObjectId())[0]); // available matches this day
		writeD(_player.getClassId().getId()); // player class
		writeD(totalRank); // previous cycle rank
		writeD(totalRank > 0 ? _playerList.size() : 0); // previous cycle total rankers
		writeD(totalRank > 0 ? classRank : 0); // total class rank previous cycle
		writeD(totalRank > 0 ? totalClassRankers : 0); // total class rankers previous cycle
		writeD(totalRank > 0 ? classRank : 0); // server class rank previous cycle
		writeD(totalRank > 0 ? totalClassRankers : 0); // server class rankers previous cycle
		writeD(points); // previous cycle points
		writeD(wins); // previous cycle wins
		writeD(loses); // previous cycle loses
		writeD(Olympiad.getRank(_player));
		writeD(Calendar.getInstance().get(Calendar.YEAR)); // year
		if ((Calendar.getInstance().get(Calendar.MONTH) + 1) > 12)
			writeD(1);
		else
			writeD(Calendar.getInstance().get(Calendar.MONTH) + 1); // current month
		
		writeC(1); // runs?
		writeH(Olympiad.getCurrentCycle()); // current cycle
		writeC(0); // ?
		writeH(0); // ?
		writeC(3); // server type (0: essence (3x3), 1: live, 3: classic)
	}
}
