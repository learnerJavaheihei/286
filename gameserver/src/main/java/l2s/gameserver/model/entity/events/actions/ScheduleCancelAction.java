package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

/**
 * @author Bonux
**/
public class ScheduleCancelAction implements EventAction
{
	private final String _name;
	private final boolean _schedule;

	public ScheduleCancelAction(String name, boolean schedule)
	{
		_name = name;
		_schedule = schedule;
	}

	@Override
	public void call(Event event)
	{
		event.taskAction(_name, _schedule);
	}
}
