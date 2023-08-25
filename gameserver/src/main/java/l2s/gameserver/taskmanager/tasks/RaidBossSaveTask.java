package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.instancemanager.RaidBossSpawnManager;

/**
 * @author Bonux
**/
public class RaidBossSaveTask extends AutomaticTask
{
	public RaidBossSaveTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		RaidBossSpawnManager.getInstance().updateAllStatusDb();
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 60000L;
	}
}
