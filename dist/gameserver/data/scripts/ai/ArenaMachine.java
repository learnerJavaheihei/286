package ai;

import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.utils.ChatUtils;

import instances.ClanArena;

/**
 * @author Bonux
**/
public class ArenaMachine extends NpcAI
{
	private static final int WELCOME_MESSAGE_TIMER_ID = 10000;
	private static final int EXTEND_DURATION_TIMER_ID = 10001;

	public ArenaMachine(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		addTimer(WELCOME_MESSAGE_TIMER_ID, 5000);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		super.onEvtTimer(timerId, arg1, arg2);

		switch(timerId)
		{
			case WELCOME_MESSAGE_TIMER_ID:
			{
				ChatUtils.say(getActor(), NpcString.WELCOME_TO_THE_ARENA_TEST_YOUR_CLANS_STRENGTH);
				addTimer(EXTEND_DURATION_TIMER_ID, 120000);
				break;
			}
			case EXTEND_DURATION_TIMER_ID:
			{
				Reflection reflection = getActor().getReflection();
				if(reflection instanceof ClanArena)
				{
					if(((ClanArena) reflection).isStarted())
						ChatUtils.say(getActor(), NpcString.TO_EXTEND_THE_DURATION_CLICK_THE_EXTEND_DURATION_BUTTON);
				}
				addTimer(EXTEND_DURATION_TIMER_ID, 120000);
				break;
			}
		}
	}
}