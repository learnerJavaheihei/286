package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;

/**
 * @author Bonux
 * Данный обьект нужен для спавна обычных NPC в дефолтный мир.
**/
public class SpawnObject implements SpawnableObject
{
	private final String _name;

	public SpawnObject(String name)
	{
		_name = name;
	}

	@Override
	public void spawnObject(Event event, Reflection reflection)
	{
		SpawnManager.getInstance().spawn(_name, false);
	}

	@Override
	public void respawnObject(Event event, Reflection reflection)
	{
		SpawnManager.getInstance().despawn(_name);
		SpawnManager.getInstance().spawn(_name, false);
	}

	@Override
	public void despawnObject(Event event, Reflection reflection)
	{
		SpawnManager.getInstance().despawn(_name);
	}

	@Override
	public void refreshObject(Event event, Reflection reflection)
	{
		//
	}
}
