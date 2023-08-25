package l2s.gameserver.taskmanager.tasks;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.dao.PvpRankingDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nexvill
**/
public class WeeklyPvpRankingTask extends AutomaticTask
{
	private static final Logger _log = LoggerFactory.getLogger(WeeklyPvpRankingTask.class);

	private static final SchedulingPattern PATTERN = new SchedulingPattern("30 6 * * 2");

	public WeeklyPvpRankingTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		_log.info("Weekly Pvp Ranking Task: launched.");
		PvpRankingDAO.getInstance().updateWeek();
		_log.info("Weekly Pvp Ranking Task: completed.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}
}