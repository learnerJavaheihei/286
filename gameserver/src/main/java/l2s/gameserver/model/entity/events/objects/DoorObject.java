package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.DoorInstance;

/**
 * @author VISTALL
 * @date  17:29/10.12.2010
 */
public class DoorObject implements SpawnableObject, InitableObject, OpenableObject
{
	private int _id;
	private DoorInstance _door;

	private boolean _weak;

	public DoorObject(int id)
	{
		_id = id;
	}

	@Override
	public void initObject(Event e)
	{
		_door = e.getReflection().getDoor(_id);
	}

	@Override
	public void spawnObject(Event event, Reflection reflection)
	{
		refreshObject(event, reflection);
	}

	@Override
	public void despawnObject(Event event, Reflection reflection)
	{
		if(reflection.isMain())
		{
			refreshObject(event, reflection);
		}
		else
		{
			//TODO [VISTALL] удалить двери
		}
	}

	@Override
	public void respawnObject(Event event, Reflection reflection)
	{
		//
	}

	@Override
	public void refreshObject(Event event, Reflection reflection)
	{
		if(!event.isInProgress())
			_door.removeEvent(event);
		else
		{
			_door.addEvent(event);
			_door.broadcastStatusUpdate();
		}

		if(_door.getCurrentHp() <= 0)
		{
			_door.decayMe();
			_door.spawnMe();
		}

		_door.setCurrentHp(_door.getMaxHp() * (isWeak() ? 0.5 : 1.), true);
		closeObject(event);
	}

	public int getId()
	{
		return _id;
	}

	public int getUpgradeValue()
	{
		return _door.getUpgradeHp();
	}

	public void setUpgradeValue(Event event, int val)
	{
		_door.setUpgradeHp(val);
		refreshObject(event, event.getReflection());
	}

	@Override
	public void openObject(Event e)
	{
		_door.openMe(null, !e.isInProgress());
	}

	@Override
	public void closeObject(Event e)
	{
		_door.closeMe(null, !e.isInProgress());
	}

	public DoorInstance getDoor()
	{
		return _door;
	}

	public boolean isWeak()
	{
		return _weak;
	}

	public void setWeak(boolean weak)
	{
		_weak = weak;
	}
}
