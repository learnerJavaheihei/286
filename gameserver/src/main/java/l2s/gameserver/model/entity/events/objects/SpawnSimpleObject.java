package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author VISTALL
 * @date 16:32/14.07.2011
 */
public class SpawnSimpleObject implements SpawnableObject
{
	protected int _npcId;
	private Location _loc;

	protected NpcInstance _npc = null;

	public SpawnSimpleObject(int npcId, Location loc)
	{
		_npcId = npcId;
		_loc = loc;
	}

	@Override
	public void spawnObject(Event event, Reflection reflection)
	{
		_npc = NpcUtils.spawnSingle(_npcId, _loc, reflection);
		if(_npc != null)
			_npc.addEvent(event);
	}

	@Override
	public void despawnObject(Event event, Reflection reflection)
	{
		if(_npc != null)
		{
			_npc.removeEvent(event);
			_npc.deleteMe();
			_npc = null;
		}
	}

	@Override
	public void respawnObject(Event event, Reflection reflection)
	{
		if(_npc != null && !_npc.isVisible())
		{
			_npc.setCurrentHpMp(_npc.getMaxHp(), _npc.getMaxMp(), true);
			_npc.setHeading(_loc.h);
			_npc.setReflection(reflection);
			_npc.spawnMe(_npc.getSpawnedLoc());
		}
	}

	@Override
	public void refreshObject(Event event, Reflection reflection)
	{

	}

	public NpcInstance getNpc()
	{
		return _npc;
	}

	public Location getLoc()
	{
		return _loc;
	}
}
