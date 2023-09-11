package ai;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

import bosses.BaiumManager;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 07.02.2019
 * Developed for L2-Scripts.com
 **/
public class BaiumAI extends AbstractBaiumAI {

	private static final int ARCHANGEL = 29021;

	public BaiumAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn() {
		super.onEvtSpawn();
		BaiumManager.setLastAttackTime();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		BaiumManager.setLastAttackTime();
		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected boolean checkIfInLairZone(Creature target){
		return BaiumManager.getZone().checkIfInZone(target);
	}

	@Override
	protected int getArchangelId() {
		return ARCHANGEL;
	}
}