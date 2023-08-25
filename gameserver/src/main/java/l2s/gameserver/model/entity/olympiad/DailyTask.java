package l2s.gameserver.model.entity.olympiad;

public class DailyTask implements Runnable
{
	@Override
	public void run()
	{
		Olympiad.doDailyTasks();
		Olympiad.setDayStartTime(System.currentTimeMillis());
	}
}