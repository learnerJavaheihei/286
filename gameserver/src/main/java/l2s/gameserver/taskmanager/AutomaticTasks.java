package l2s.gameserver.taskmanager;

import l2s.gameserver.Config;
import l2s.gameserver.taskmanager.tasks.*;

/**
 * @author Bonux
**/
public class AutomaticTasks
{
	public static void init()
	{
		if(Config.ENABLE_OLYMPIAD)
			new OlympiadSaveTask();

		new DailyTask();
		new DeleteExpiredMailTask();
		new CheckItemsTask();
		new RaidBossSaveTask();
		new WeeklyTask();
		new PledgeHuntingSaveTask();
	}
}
