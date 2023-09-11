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
 * @author L2-scripts.com - (sSanyaDC)
 **/
public class VantutuInstance extends NpcInstance {
	private static final Location TELEPORT_LOC1 = new Location(138472, 114408, -3722);
	private static final Location TELEPORT_LOC2 = new Location(144456, 119448, -3914);
	private static final Location TELEPORT_LOC3 = new Location(152280, 115160, -5378);

	public VantutuInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state) {
		if (ask == -180912) {
			if(reply == 1) {
				if (player.getAdena() > 70000) {
					player.reduceAdena(70000);
					player.teleToLocation(TELEPORT_LOC1, ReflectionManager.MAIN);
				}
				else
					showChatWindow(player, "default/30938-no_adena.htm", false);
			}
			if(reply == 2){
				if(player.getAdena()>80000){
					player.reduceAdena(80000);
				player.teleToLocation(TELEPORT_LOC2, ReflectionManager.MAIN);
				}
				else
					showChatWindow(player, "default/30938-no_adena.htm", false);
			}
			if(reply == 3){
				if(player.getAdena()>100000) {
					player.reduceAdena(100000);
					player.teleToLocation(TELEPORT_LOC3, ReflectionManager.MAIN);
				}
				else
					showChatWindow(player, "default/30938-no_adena.htm", false);
			}

		}

		else
			super.onMenuSelect(player, ask, reply, state);
	}
}
