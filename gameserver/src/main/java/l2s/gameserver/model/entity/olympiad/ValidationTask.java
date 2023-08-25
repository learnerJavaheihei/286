package l2s.gameserver.model.entity.olympiad;

public class ValidationTask implements Runnable
{
	@Override
	public void run()
	{
		Olympiad._period = 0;
		Olympiad._currentCycle++;

		OlympiadDatabase.setNewOlympiadStartTime();

		Olympiad.init();
		OlympiadDatabase.save();
	}
}