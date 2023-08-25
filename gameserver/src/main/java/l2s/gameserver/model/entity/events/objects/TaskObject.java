package l2s.gameserver.model.entity.events.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

/**
 * @author Bonux
**/
public class TaskObject
{
	private class Task implements Runnable
	{
		private final Event _event;

		public Task(Event event)
		{
			_event = event;
		}

		@Override
		public void run()
		{
			for(EventAction action : _actions)
				action.call(_event);
		}
	}

	private final boolean _fixedRate;
	private final int _initialDelay;
	private final int _delay;
	private final Map<Event, List<ScheduledFuture<?>>> _scheduledTasks = new HashMap<Event, List<ScheduledFuture<?>>>(0);

	private List<EventAction> _actions = Collections.emptyList();

	public TaskObject(boolean fixedRate, int initialDelay, int delay)
	{
		_fixedRate = fixedRate;
		_initialDelay = initialDelay;
		_delay = delay;
	}

	public void setActions(List<EventAction> actions)
	{
		_actions = actions;
	}

	public ScheduledFuture<?> schedule(Event event)
	{
		ScheduledFuture<?> task;
		if(_fixedRate)
			task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Task(event), _initialDelay, _delay);
		else
			task = ThreadPoolManager.getInstance().schedule(new Task(event), _delay);

		List<ScheduledFuture<?>> tasks = _scheduledTasks.get(event);
		if(tasks == null)
		{
			tasks = new ArrayList<ScheduledFuture<?>>();
			_scheduledTasks.put(event, tasks);
		}
		tasks.add(task);

		return task;
	}

	public boolean cancel(Event event)
	{
		List<ScheduledFuture<?>> tasks = _scheduledTasks.remove(event);
		if(tasks != null)
		{
			for(ScheduledFuture<?> task : tasks)
			{
				task.cancel(false);
			}
			tasks.clear();
			return true;
		}
		return false;
	}

	public boolean cancel(Event event, ScheduledFuture<?> task)
	{
		List<ScheduledFuture<?>> tasks = _scheduledTasks.remove(event);
		if(tasks != null)
		{
			if(tasks.remove(task))
			{
				task.cancel(false);
				return true;
			}
		}
		return false;
	}
}
