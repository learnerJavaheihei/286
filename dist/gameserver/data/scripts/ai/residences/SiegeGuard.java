package ai.residences;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.instances.NpcInstance;


public abstract class SiegeGuard extends DefaultAI
{
	public SiegeGuard(NpcInstance actor)
	{
		super(actor);
		setMaxPursueRange(1000);
	}

	@Override
	public int getMaxPathfindFails()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	public boolean canSeeInSilentMove(Playable target)
	{
		// Осадные гварды могут видеть игроков в режиме Silent Move с вероятностью 10%
		return !target.isSilentMoving() || Rnd.chance(10);
	}

	@Override
	protected boolean isAggressive()
	{
		return true;
	}

	@Override
	protected boolean isGlobalAggro()
	{
		return true;
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;
		if(target == null || !actor.isAutoAttackable(target))
			return;
		super.onEvtAggression(target, aggro);
	}

	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;

		NpcInstance actor = getActor();
		Location sloc = actor.getSpawnedLoc();
		if(!actor.isInRange(sloc, 250))	// Проверка на расстояние до точки спауна
		{
			teleportHome();
			return true;
		}
		return false;
	}

	@Override
	public boolean canAttackCharacter(Creature target)
	{
		return getActor().isAutoAttackable(target);
	}

	@Override
	protected boolean maybeMoveToHome(boolean force)
	{
		return returnHome(true);
	}
}