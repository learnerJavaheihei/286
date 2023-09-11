package npc.model;

import instances.Frintezza;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author pchayka
 */
public final class FrintezzaGatekeeperInstance extends NpcInstance
{
	private static final int FRINTEZZA_IZ_ID = 205;

	public FrintezzaGatekeeperInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.equalsIgnoreCase("request_frintezza"))
		{
			if(!player.isInParty()) {
				showChatWindow(player, "default/32011-no_enter.htm", false);
				return;
			}

			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(FRINTEZZA_IZ_ID))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(FRINTEZZA_IZ_ID))
			{
				ReflectionUtils.enterReflection(player, new Frintezza(), FRINTEZZA_IZ_ID);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}