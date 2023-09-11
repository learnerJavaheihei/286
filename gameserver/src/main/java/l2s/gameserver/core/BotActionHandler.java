package l2s.gameserver.core;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BotActionHandler
{
	private static final BotActionHandler INSTANCE = new BotActionHandler();
	private Map<Integer, IBotActionHandler> data = new TreeMap<Integer, IBotActionHandler>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	public Map<Integer, IBotActionHandler> getData()
	{
		this.lock.readLock().lock();
		try
		{
			Map<Integer, IBotActionHandler> map = this.data;
			return map;
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}

	public void regHandler(int index, IBotActionHandler actionHandler)
	{
		this.lock.writeLock().lock();
		try
		{
			this.data.put(index, actionHandler);
		}
		finally
		{
			this.lock.writeLock().unlock();
		}
	}

	public static BotActionHandler getInstance()
	{
		return INSTANCE;
	}
}