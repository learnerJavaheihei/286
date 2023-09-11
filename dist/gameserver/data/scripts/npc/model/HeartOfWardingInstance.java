package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
 **/
public final class HeartOfWardingInstance extends NpcInstance {
	public HeartOfWardingInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onTeleportRequest(Player talker) {
		showChatWindow(talker, "default/heart_of_warding002.htm", false);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace) {
		if (val == 0)
			showChatWindow(player, "default/heart_of_warding001.htm", firstTalk, replace);
		else
			super.showChatWindow(player, val, firstTalk, replace);
	}
}