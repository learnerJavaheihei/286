package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.teleport.ExShowTeleportUi;
import l2s.gameserver.templates.npc.NpcTemplate;

public class GatekeeperInstance extends MerchantInstance {
	public GatekeeperInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onTeleportRequest(Player talker) {
		talker.sendPacket(ExShowTeleportUi.STATIC);
	}
}
