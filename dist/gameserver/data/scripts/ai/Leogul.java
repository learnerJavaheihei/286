package ai;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/*
 * @author L2-scripts.com - (SanyaDC)
 */
public class Leogul extends Fighter
{
	private static final int[] HELPER  = { 20654, 20656, 20657 };
	private static final int TIME_TO_LIVE = 15000;
	private static final int SPAWN_HELPER_TIME_ID = 150001;

	private boolean _minionsSpawned = false;

	public Leogul(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		if(timerId == SPAWN_HELPER_TIME_ID)
		{
			NpcInstance actor = getActor();
			for(int n : HELPER)
			{
				NpcInstance minion = NpcUtils.spawnSingle(n, Location.findPointToStay(actor, 150, 200));
			}
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		if(!_minionsSpawned)
		{
			_minionsSpawned = true;
			addTimer(SPAWN_HELPER_TIME_ID, TIME_TO_LIVE);
		}
		super.onEvtAttacked(attacker, skill, damage);
	}
}