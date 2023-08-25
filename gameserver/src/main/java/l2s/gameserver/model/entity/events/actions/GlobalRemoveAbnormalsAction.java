package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.dao.EffectsDAO;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

/**
 * @author Bonux
**/
public class GlobalRemoveAbnormalsAction implements EventAction
{
	private final int _skillId;

	public GlobalRemoveAbnormalsAction(int skillId)
	{
		_skillId = skillId;
	}

	@Override
	public void call(Event event)
	{
		if(_skillId <= 0)
			return;

		for(Player player : GameObjectsStorage.getPlayers(true, true))
		{
			player.getAbnormalList().stop(_skillId);
		}
		EffectsDAO.getInstance().deleteBySkillId(_skillId);
	}
}
