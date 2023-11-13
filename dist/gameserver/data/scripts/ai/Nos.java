/* package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

import java.util.List;

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
} */

package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author SanyaDC
 */

public class Nos extends Fighter
{
	public Nos(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(Rnd.chance(10))
			{
				spawnNos(actor, 1);
			}
		super.onEvtDead(killer);	
	}
	
	private void spawnNos(NpcInstance actor, int count)
	{
				NpcInstance minion = NpcUtils.spawnSingle(20793, actor.getLoc());
		
	}
}