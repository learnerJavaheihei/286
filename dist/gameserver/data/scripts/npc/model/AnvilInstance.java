package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public class AnvilInstance extends NpcInstance
{
	public AnvilInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
		{
			if(!Config.ALLOW_AUGMENTATION)
				showChatWindow(player, "default/" + getNpcId() + "-not_allowed.htm", firstTalk);
			else
				super.showChatWindow(player, val, firstTalk, replace);
		}
		else
			super.showChatWindow(player, val, firstTalk, replace);
	}
}