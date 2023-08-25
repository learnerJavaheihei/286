package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.objects.ItemObject;

/**
 * @author Bonux
**/
public class GlobalRemoveItemsAction implements EventAction
{
	private final String _name;

	public GlobalRemoveItemsAction(String name)
	{
		_name = name;
	}

	@Override
	public void call(Event event)
	{
		for(Object o : event.getObjects(_name))
		{
			// Итемы удаляются только во время старта\рестарта сервера. На активном сервере удалять все итемы крайне опасно.
			if(o instanceof ItemObject)
				ItemsDAO.getInstance().glovalRemoveItem(((ItemObject) o).getItemId(), "Remove items by event: " + event.getName());
		}
	}
}
