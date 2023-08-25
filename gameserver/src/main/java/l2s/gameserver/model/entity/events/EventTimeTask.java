package l2s.gameserver.model.entity.events;

/**
 * @author VISTALL
 * @date  18:02/10.12.2010
 */
public class EventTimeTask implements Runnable
{
	private final Event _event;
	private final int _time;

	public EventTimeTask(Event event, int time)
	{
		_event = event;
		_time = time;
	}

	@Override
	public void run()
	{
		_event.timeActions(_time);
	}
}
