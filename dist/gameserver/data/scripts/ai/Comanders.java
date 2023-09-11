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
 * @author L2-scripts.com - (SanyaDC)
 */

public class Comanders extends Fighter
{
	public Comanders(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(Rnd.chance(100))
			{
				spawnC(actor, 1);
			}
		super.onEvtDead(killer);
	}
	
	private void spawnC(NpcInstance actor, int count)
	{
		int npcId = actor.getNpcId();
		if(npcId == 21752)
			{
				NpcInstance minion = NpcUtils.spawnSingle(21754, actor.getLoc());
				}
		if(npcId == 21753)
			{
				NpcInstance minion = NpcUtils.spawnSingle(21755, actor.getLoc());
				}
	}
}