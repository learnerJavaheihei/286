package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.dao.AccountVariablesDAO;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

/**
 * @author Bonux
**/
public class GlobalRemoveVariablesAction implements EventAction
{
	public static enum VariableType
	{
		PERSONAL,
		ACCOUNT;
	}

	private final String _name;
	private final VariableType _type;

	public GlobalRemoveVariablesAction(String name, VariableType type)
	{
		_name = name;
		_type = type;
	}

	@Override
	public void call(Event event)
	{
		if(_type == VariableType.PERSONAL)
		{
			for(Player player : GameObjectsStorage.getPlayers(true, true))
			{
				player.unsetVar(_name);
			}
			CharacterVariablesDAO.getInstance().delete(_name);
		}
		else if(_type == VariableType.ACCOUNT)
			AccountVariablesDAO.getInstance().delete(_name);
	}
}
