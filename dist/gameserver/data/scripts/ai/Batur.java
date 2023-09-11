package ai;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.instances.NpcInstance;
import bosses.BaiumManager;

/*
 * @author L2-scripts.com - (SanyaDC)
 */
public class Batur extends Fighter
{
	private static final int TIME_TO_LIVE = 60000;
	private final long TIME_TO_DIE = System.currentTimeMillis() + TIME_TO_LIVE;

	public Batur(NpcInstance actor)
	{
		super(actor);		
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
	
		NpcInstance actor = getActor();
		if(actor != null && System.currentTimeMillis() >= TIME_TO_DIE)
		{
			actor.deleteMe();			
		}

		super.onEvtAttacked(attacker, skill, damage);
	}

	
}