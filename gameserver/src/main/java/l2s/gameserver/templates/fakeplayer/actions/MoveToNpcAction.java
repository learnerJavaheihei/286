package l2s.gameserver.templates.fakeplayer.actions;

import l2s.gameserver.ai.FakeAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import org.dom4j.Element;

import java.util.List;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

/**
 * @author Bonux
 **/
public class MoveToNpcAction extends MoveAction {
	private final int _npcId;

	public MoveToNpcAction(int npcId, double chance) {
		super(chance);
		_npcId = npcId;
	}

	@Override
	public boolean performAction(FakeAI ai) {
		Player player = ai.getActor();
		NpcInstance npc = null;

		List<NpcInstance> npcs = GameObjectsStorage.getNpcs(true, _npcId);
		for (NpcInstance n : npcs) {
			if (npc == null || n.getDistance(player) < npc.getDistance(player))
				npc = n;
		}

		if (npc != null) {
			int range = player.getInteractionDistance(npc);
			if (player.isInRangeZ(npc, range + (player.isMovementDisabled() ? 32 : 16))) {
				player.doInteract(npc);
				player.getAI().setIntention(AI_INTENTION_ACTIVE);
			} else {
				Location loc = npc.getLoc();
				if (player.getDistance(loc) > 2000 || !player.getMovement().moveToLocation(loc, range, false)) {
					Location newLoc = Location.findFrontPosition(npc, player, range, range);
					if (newLoc == null)
						return false;
					player.teleToLocation(loc, 0, 0);
				}
			}
			return true;
		}
		return false;
	}

	public static MoveToNpcAction parse(Element element) {
		int npcId = Integer.parseInt(element.attributeValue("id"));
		double chance = element.attributeValue("chance") == null ? 100. : Double.parseDouble(element.attributeValue("chance"));
		return new MoveToNpcAction(npcId, chance);
	}
}