package ai.events;

import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

/**
 * @author Bonux
**/
public class ChristmasTreeAI extends NpcAI
{
	private static final int RESTORE_MP_SKILL_ID = 51155;	// Christmas Tree Blessing
	private static final int RESTORE_MP_SKILL_LEVEL = 1;
	private static final int RESTORE_MP_TIMER_ID = 400000;
	private static final int RESTORE_MP_DELAY = 60000; // Milliseconds

	public ChristmasTreeAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		addTimer(RESTORE_MP_TIMER_ID, RESTORE_MP_DELAY);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		super.onEvtTimer(timerId, arg1, arg2);

		if(timerId == RESTORE_MP_TIMER_ID)
		{
			NpcInstance actor = getActor();
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, RESTORE_MP_SKILL_ID, RESTORE_MP_SKILL_LEVEL);
			if(skillEntry != null)
				actor.doCast(skillEntry, actor, true);
			addTimer(RESTORE_MP_TIMER_ID, RESTORE_MP_DELAY);
		}
	}
}