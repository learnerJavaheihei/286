package npc.model;

import java.util.List;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

import bosses.BaiumManager;

/**
 * @author Bonux
 */
public final class BaiumGatekeeperInstance extends NpcInstance
{
	// NPC's
	private static final int BAIUM_RAID_NPC_ID = 29020;
	private static final int BAIUM_STONED_NPC_ID = 29025;

	// Locations
	private static final Location TELEPORT_POSITION = new Location(113100, 14500, 10077);

	public BaiumGatekeeperInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onTeleportRequest(Player talker)
	{
		List<NpcInstance> baiumNpcs = GameObjectsStorage.getNpcs(true, BAIUM_STONED_NPC_ID);
		List<NpcInstance> baiumBosses = GameObjectsStorage.getNpcs(true, BAIUM_RAID_NPC_ID);
		if(!baiumNpcs.isEmpty() || !baiumBosses.isEmpty())
		{
			if(baiumBosses.isEmpty())
			{
				if(BaiumManager.consumeRequiredItems(talker))
				{
					talker.setVar(BaiumManager.BAIUM_PERMISSION_VAR, true, -1);
					talker.teleToLocation(TELEPORT_POSITION);
				}
				else
					showChatWindow(talker, "default/dimension_vertex_4002.htm", false);
			}
			else
				showChatWindow(talker, "default/dimension_vertex_4003.htm", false);
		}
		else
			showChatWindow(talker, "default/dimension_vertex_4004.htm", false);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		if(val == 0)
			showChatWindow(player, "default/dimension_vertex_4001.htm", firstTalk);
		else
			super.showChatWindow(player, val, firstTalk, arg);
	}
}