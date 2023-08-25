package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;

/**
 * @author VISTALL
 * @date  16:28/10.12.2010
 */
public interface SpawnableObject
{
	void spawnObject(Event event, Reflection reflection);

	void despawnObject(Event event, Reflection reflection);

	void respawnObject(Event event, Reflection reflection);

	void refreshObject(Event event, Reflection reflection);
}
