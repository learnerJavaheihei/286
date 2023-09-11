package ai.locations.forestofmirrors;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author Bonux
**/
public class Mirror extends Fighter
{
	//Monster ID's
	private static final int MIRROR_NPC_ID = 20639;	// Зекрало

	private static final int DESPAWN_TIME = 600000;

	private int _spawnStage = 0;

	public Mirror(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if(_spawnStage < 4)
		{
			final NpcInstance actor = getActor();
			ThreadPoolManager.getInstance().schedule(() ->
			{
				for(int i = 0; i < 2; i++)
				{
					NpcInstance npc = NpcUtils.spawnSingle(MIRROR_NPC_ID, actor.getLoc(), DESPAWN_TIME);
					if(npc.getAI() instanceof Mirror)
						((Mirror) npc.getAI()).setSpawnStage(_spawnStage + 1);
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 200);
				}
			}, 1000L);
		}
	}

	public void setSpawnStage(int value)
	{
		_spawnStage = value;
	}
}
