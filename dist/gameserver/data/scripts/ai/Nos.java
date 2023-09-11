package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

import java.util.List;

/**
 * @author Bonux
 * Монстр спавнится в инвизе. Когда персонаж пробегает мимо него, монстр выходит с инвиза и спавнит несколько себе подобных.
 **/
public class Nos extends Fighter {
	public Nos(NpcInstance actor) {
		this(actor, true);
	}

	private Nos(NpcInstance actor, boolean main) {
		super(actor);

		if (main)
			actor.getFlags().getInvisible().start();
	}

	@Override
	protected boolean thinkActive() {
		NpcInstance actor = getActor();
		if(!actor.getFlags().getInvisible().get())
			return super.thinkActive();

		List<Playable> playables = World.getAroundPlayables(actor, 150, 100);
		playables.removeIf((p) -> p.isInvisible(actor));
		if (playables.isEmpty())
			return true;

		actor.stopInvisible(true);
		setIntention(CtrlIntention.AI_INTENTION_IDLE);

		int count = Rnd.get(5) == 0 ? 3 : 1;
		for (int i = 0; i < count; i++) {
			NpcInstance npc = NpcUtils.createNpc(actor.getNpcId());
			npc.setAI(new Nos(npc, false));
			NpcUtils.spawnNpc(npc, Location.findPointToStay(actor, 100, 200), actor.getReflection());
		}
		return true;
	}
}