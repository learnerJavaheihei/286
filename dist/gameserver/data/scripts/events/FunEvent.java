package events;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import l2s.gameserver.utils.TimeUtils;

/**
 * @author VISTALL
 * @date 17:31/07.04.2012
 */
public class FunEvent extends Event
{
	// times
	protected final Calendar _startPattern, _stopPattern;
	protected long _startTime;

	private final MultiValueSet<String> _parameters;

	private final AtomicBoolean _isInProgress = new AtomicBoolean(false);

	public FunEvent(MultiValueSet<String> set)
	{
		super(set);

		_startPattern = getCalendar(set.getString("start_time_pattern"));
		_stopPattern = getCalendar(set.getString("stop_time_pattern"));
		_parameters = set;
	}

	private static Calendar getCalendar(String d)
	{
		Calendar calendar = Calendar.getInstance();

		try
		{
			Date date = TimeUtils.SIMPLE_FORMAT.parse(d);
			calendar.setTimeInMillis(date.getTime());
		}
		catch(ParseException e)
		{
			throw new Error(e);
		}

		return calendar;
	}

	@Override
	public void initEvent()
	{
		long dff = _stopPattern.getTimeInMillis() - _startPattern.getTimeInMillis();
		addOnTimeAction(0, new StartStopAction(StartStopAction.EVENT, true));
		addOnTimeAction((int)(dff / 1000L), new StartStopAction(StartStopAction.EVENT, false));
		super.initEvent();
	}

	@Override
	public void startEvent()
	{
		if(!_isInProgress.compareAndSet(false, true))
			return;

		super.startEvent();
	}

	@Override
	public void stopEvent(boolean force)
	{
		if(!_isInProgress.compareAndSet(true, false))
			return;

		ServerVariables.unset(getName());

		super.stopEvent(force);

		if(!force)
			reCalcNextTime(false);
	}

	@Override
	public void printInfo()
	{
		final long startEventMillis = startTimeMillis();
		if(startEventMillis == 0)
			info(getName() + " time - undefined");
		else if(!isForceScheduled() && _stopPattern.getTimeInMillis() < System.currentTimeMillis())
			info(getName() + " time - " + TimeUtils.toSimpleFormat(startEventMillis) + ", stop - undefined");
		else
			info(getName() + " time - " + TimeUtils.toSimpleFormat(startEventMillis) + ", stop - " + TimeUtils.toSimpleFormat(_stopPattern.getTimeInMillis()));
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		int startTime = ServerVariables.getInt(getName(), 0);
		if(startTime > 0)
		{
			_startTime = startTime * 1000L;
		}
		else if(_stopPattern.getTimeInMillis() > System.currentTimeMillis())
		{
			_startTime = _startPattern.getTimeInMillis();
		}
		else
		{
			_startTime = 0;
		}
		registerActions();
	}

	@Override
	public EventType getType()
	{
		return EventType.FUN_EVENT;
	}

	@Override
	protected long startTimeMillis()
	{
		return _startTime;
	}

	@Override
	public boolean isInProgress()
	{
		return _isInProgress.get();
	}

	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}

	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(EVENT))
		{
			if(!start && isForceScheduled())
				return;
		}
		super.action(name, start);
	}

	@Override
	public boolean isForceScheduled()
	{
		return ServerVariables.getInt(getName(), 0) > 0;
	}

	@Override
	public boolean forceScheduleEvent()
	{
		if(isForceScheduled())
			return false;

		if(isInProgress())
			return false;

		clearActions();
		long forceStartTime = getForceStartTime();
		ServerVariables.set(getName(), (int) (forceStartTime / 1000));
		_startTime = forceStartTime;
		registerActions();
		return true;
	}

	@Override
	public boolean forceCancelEvent()
	{
		if(!isForceScheduled())
			return false;

		ServerVariables.unset(getName());
		stopEvent(true);
		reCalcNextTime(false);
		return true;
	}
}
