package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class ZakenRaid extends ProgressDailyMissionHandler
{
	private class HandlerListeners implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			Player player = actor.getPlayer();
			if(player != null && victim.isMonster() && victim.getNpcId() == 29022)
				progressMission(player, 1, true, victim.getLevel());
		}

		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}
	}

	private final HandlerListeners _handlerListeners = new HandlerListeners();

	@Override
	public CharListener getListener()
	{
		return _handlerListeners;
	}

	@Override
	public boolean isReusable()
	{
		return false;
	}
}
