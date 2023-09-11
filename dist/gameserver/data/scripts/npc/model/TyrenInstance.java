package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.ReflectionUtils;
import instances.KingIgnis;

/**
 * @author L2-scripts.com - (SanyaDC)
 **/
public class TyrenInstance extends NpcInstance {
	private static final Location TELEPORT_LOC = new Location(173512, -115528, -3761);

	public TyrenInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state) {
		if (ask == -2017072604) {
				player.teleToLocation(TELEPORT_LOC, ReflectionManager.MAIN);
		}

		else
			super.onMenuSelect(player, ask, reply, state);
	}
}
