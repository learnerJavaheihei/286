package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

import instances.BalthusKnightAntharas;

/**
 * @author Bonux
**/
public class FellowInstance extends NpcInstance
{
	private static final int INSTANTZONE_ID = 183;

	public FellowInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -2892)
		{
			if(reply == 11)
			{
				Reflection r = player.getActiveReflection();
				if(r != null)
				{
					if(player.canReenterInstance(INSTANTZONE_ID))
						player.teleToLocation(r.getTeleportLoc(), r);
					return;
				}

				Party party = player.getParty();
				CommandChannel commandChannel = party != null ? party.getCommandChannel() : null;
				if(commandChannel == null)
				{
					showChatWindow(player, "default/" + getNpcId() + "-no_group.htm", false);
					return;
				}

				if(player.canEnterInstance(INSTANTZONE_ID))
				{
					ReflectionUtils.enterReflection(player, new BalthusKnightAntharas(), INSTANTZONE_ID);
					return;
				}
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
}