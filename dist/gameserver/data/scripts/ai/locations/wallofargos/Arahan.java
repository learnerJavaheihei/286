package ai.locations.wallofargos;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.concurrent.TimeUnit;

/**
 * @author Bonux
 **/
public class Arahan extends Fighter {
	private static final long DESPAWN_TIME = TimeUnit.MINUTES.toMillis(2);

	public Arahan(NpcInstance actor) {
		super(actor);
	}

	@Override
	protected void onEvtSpawn() {
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage) {
		super.onEvtAttacked(attacker, skill, damage);
		if (!getActor().isDeleteTaskScheduled())
			getActor().startDeleteTask(DESPAWN_TIME);
	}
}
