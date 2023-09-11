package ai;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;


/**
 * SanyaDC
 */
public class ArtifactSpawn extends Fighter
{


	public ArtifactSpawn(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean canAttackCharacter(Creature target)
	{
		return false;
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
}