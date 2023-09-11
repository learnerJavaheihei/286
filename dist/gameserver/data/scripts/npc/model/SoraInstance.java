package npc.model;

import instances.Goldberg;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author L2-scripts.com - (sSanyaDC)
 **/
public class SoraInstance extends NpcInstance {
	private static final int INSTANTZONE_ID = 207;

	public SoraInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state) {
		if (ask == -20181015) {
				if(reply == 2)
				{
					Reflection r = player.getActiveReflection();
					if(player.getParty()==null) {
						showChatWindow(player, "default/" + getNpcId() + "-1.htm", false);
						return;
					}
					if(player.getParty().getPartyLeader().getInventory().getItemByItemId(91636) == null) {
						showChatWindow(player, "default/" + getNpcId() + "-1.htm", false);
						return;
					}
					if(r != null)
					{
						if(player.canReenterInstance(INSTANTZONE_ID))
							player.teleToLocation(r.getTeleportLoc(), r);
						return;
					}
					if(player.canEnterInstance(INSTANTZONE_ID))
					{
						ReflectionUtils.enterReflection(player, new Goldberg(), INSTANTZONE_ID);
						ItemFunctions.deleteItem(player, 91636, 1, true);
						return;
					}
				}
			}

		else
			super.onMenuSelect(player, ask, reply, state);
	}
}
