package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.ThreadPoolManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 20:00/24.06.2011
 */
public abstract class AutomaticTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(AutomaticTask.class);

	public AutomaticTask()
	{
		init(true);
	}

	public abstract void doTask() throws Exception;

	public abstract long reCalcTime(boolean start);

	public void init(boolean start)
	{
		ThreadPoolManager.getInstance().schedule(this, reCalcTime(start) - System.currentTimeMillis());
	}

	@Override
	public void run()
	{
		try
		{
			doTask();
		}
		catch(Exception e)
		{
			_log.error("Exception: " + e, e);
		}
		finally
		{
			init(false);
		}
	}
}
