package npc.model.clanarena;

import l2s.commons.collections.MultiValueSet;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

import instances.ClanArena;

/**
 * @author iqman
 * @reworked by Bonux
**/
public class SuppliesInstance extends NpcInstance
{
	public SuppliesInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	public void onBypassFeedback(Player player, String command)
	{
		if(command.startsWith("openbox"))
		{
			if(player.getLevel() >= 40)
			{
				Reflection reflection = getReflection();
				if(!(reflection instanceof ClanArena))
					return;

				ClanArena clanArena = (ClanArena) reflection;
				if(!clanArena.rewardSupplies(player))
				{
					showChatWindow(player, "default/" + getNpcId() + "-already_rewarded.htm", false);
					return;
				}

				doDie(player);
			}
			else
			{
				showChatWindow(player, "default/" + getNpcId() + "-no_reward_level.htm", false);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}