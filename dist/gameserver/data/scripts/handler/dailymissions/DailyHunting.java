package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Party;//修复组队文件
import l2s.gameserver.utils.MapUtils;//修复组队文件
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Bonux
**/
public class DailyHunting extends ProgressDailyMissionHandler
{
	protected static final int[] EMPTY_MONSTER_IDS = new int[0];

	private class HandlerListeners implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			Player player = actor.getPlayer();
			if (player.isInParty())
			{
				if(player.isInParty() && player.getParty().getCommandChannel() != null)
				{
					Player playerLeader = player.getPlayerGroup().getGroupLeader();
					for(Player p : player.getPlayerGroup())
					{
						if(p.getDistance(player) <= 1500)
						{
							if(player != null && victim.isMonster())
							{
								if(getMonsterIds().length == 0 || ArrayUtils.contains(getMonsterIds(), victim.getNpcId()))
								progressMission(p, 1, true, victim.getLevel());
							}
						}	
					}
				}
				else
				{
					Player playerLeader = player.getParty().getPartyLeader();
					for(Player p : player.getParty())
					{
						if(p.getDistance(player) <= 1500)
						{
							if(player != null && victim.isMonster())
							{
								if(getMonsterIds().length == 0 || ArrayUtils.contains(getMonsterIds(), victim.getNpcId()))
								progressMission(p, 1, true, victim.getLevel());
							}
						}
					}
				}
			}
			else
			{
				if(player != null && victim.isMonster()) 
				{
					if(getMonsterIds().length == 0 || ArrayUtils.contains(getMonsterIds(), victim.getNpcId()))
					progressMission(player, 1, true, victim.getLevel());
				}
			}
		}

		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}
	}

	private final HandlerListeners _handlerListeners = new HandlerListeners();

	protected int[] getMonsterIds() {
		return EMPTY_MONSTER_IDS;
	}

	@Override
	public CharListener getListener()
	{
		return _handlerListeners;
	}
}
