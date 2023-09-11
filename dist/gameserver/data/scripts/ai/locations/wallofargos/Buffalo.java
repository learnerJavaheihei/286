package ai.locations.wallofargos;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author Bonux
 **/
public class Buffalo extends Fighter {
	public Buffalo(NpcInstance actor) {
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage) {
		super.onEvtAttacked(attacker, skill, damage);
		attacker.stopDeleteTask();
	}
}
