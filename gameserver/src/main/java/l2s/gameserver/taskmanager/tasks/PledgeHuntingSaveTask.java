package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.tables.ClanTable;

/**
 * @author Bonux
**/
public class PledgeHuntingSaveTask extends AutomaticTask
{
	private static final long SAVE_DELAY = 10 * 60 * 1000; // 10 minutes

	public PledgeHuntingSaveTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		ClanTable.getInstance().saveClanHuntingProgress();
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + SAVE_DELAY;
	}
}
