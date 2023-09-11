package ai;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

/**
 * @author pchayka
 */

public class AntharasMinion extends Fighter
{
	public AntharasMinion(NpcInstance actor)
	{
		super(actor);
		actor.getFlags().getDebuffImmunity().start();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		//TODO: for(Player p : AntharasManager.getZone().getInsidePlayers())
			//notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 5000);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		getActor().doCast(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5097, 1), getActor(), true);
		super.onEvtDead(killer);
	}

	@Override
	protected boolean returnHome(boolean clearAggro, boolean teleport, boolean running, boolean force)
	{
		return false;
	}
}