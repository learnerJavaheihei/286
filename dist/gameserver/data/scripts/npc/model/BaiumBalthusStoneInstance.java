package npc.model;

import instances.BalthusKnightBaium;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
 */
public final class BaiumBalthusStoneInstance extends NpcInstance
{
	public BaiumBalthusStoneInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == 9999)
		{
			if(reply == 1)
			{
				Reflection reflection = getReflection();
				if(!(reflection instanceof BalthusKnightBaium)) {
					//TODO: Maybe message?
					return;
				}

				BalthusKnightBaium balthusKnightBaium = (BalthusKnightBaium) reflection;
				if(!balthusKnightBaium.spawnBaiumRaid(this, player)) {
					//TODO: Maybe message?
					return;
				}
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		if(val == 0)
			showChatWindow(player, "default/baium_balthus_npc001.htm", firstTalk);
		else
			super.showChatWindow(player, val, firstTalk, arg);
	}
}