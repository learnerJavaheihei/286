package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.base.FenceState;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.FenceInstance;
import l2s.gameserver.utils.FenceUtils;

/**
 * @author Bonux
**/
public class FenceObject implements SpawnableObject, InitableObject, OpenableObject
{
	private final Location _loc;
	private final int _width;
	private final int _length;
	private final int _height;

	private FenceInstance _fence;

	public FenceObject(Location loc, int width, int length, int height)
	{
		_loc = loc;
		_width = width;
		_length = length;
		_height = height;
	}

	@Override
	public void initObject(Event e)
	{
		_fence = FenceUtils.initFence("Event Fence: " + e.getName() + " " + hashCode(), _loc.getX(), _loc.getY(), _loc.getZ(), _width, _length, _height, FenceState.HIDDEN);
	}

	@Override
	public void spawnObject(Event event, Reflection reflection)
	{
		if(!event.isInProgress())
			_fence.removeEvent(event);
		else
			_fence.addEvent(event);

		_fence.setReflection(reflection);
		_fence.spawnMe(_loc);
	}

	@Override
	public void despawnObject(Event event, Reflection reflection)
	{
		_fence.decayMe();
	}

	@Override
	public void respawnObject(Event event, Reflection reflection)
	{
		_fence.decayMe();
		_fence.setReflection(reflection);
		_fence.spawnMe(_loc);
	}

	@Override
	public void refreshObject(Event event, Reflection reflection)
	{
		if(!event.isInProgress())
			_fence.removeEvent(event);
		else
			_fence.addEvent(event);

		_fence.setState(FenceState.HIDDEN);
	}

	@Override
	public void openObject(Event e)
	{
		_fence.setState(FenceState.OPENED);
	}

	@Override
	public void closeObject(Event e)
	{
		_fence.setState(FenceState.CLOSED);
	}

	public FenceInstance getFence()
	{
		return _fence;
	}
}
