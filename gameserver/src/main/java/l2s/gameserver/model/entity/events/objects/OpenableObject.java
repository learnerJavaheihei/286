package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.events.Event;

/**
 * @author Bonux
**/
public interface OpenableObject
{
	void openObject(Event event);

	void closeObject(Event event);
}
