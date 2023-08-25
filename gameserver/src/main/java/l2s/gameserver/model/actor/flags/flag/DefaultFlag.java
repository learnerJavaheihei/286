package l2s.gameserver.model.actor.flags.flag;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.util.concurrent.atomic.AtomicState;

/**
 * @author Bonux
 **/
public class DefaultFlag
{
	private final AtomicBoolean _state = new AtomicBoolean(false);
	private final Set<Object> _statusesOwners = new HashSet<Object>();

	public boolean get()
	{
		return _state.get() || !_statusesOwners.isEmpty();
	}

	public boolean start(Object owner)
	{
		return _statusesOwners.add(owner);
	}

	public boolean start()
	{
		return _state.compareAndSet(false, true);
	}

	public boolean stop(Object owner)
	{
		return _statusesOwners.remove(owner);
	}

	public boolean stop()
	{
		return _state.compareAndSet(true, false);
	}

	public void clear()
	{
		_state.set(false);
		_statusesOwners.clear();
	}
}