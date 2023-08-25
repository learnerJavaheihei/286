package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author nexvill
**/
public final class EffectHitCountBlock extends EffectHandler
{
	private final int _count;

	public EffectHitCountBlock(EffectTemplate template)
	{
		super(template);
		_count = getParams().getInteger("count", 0);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if (_count > 0)
			effected.getFlags().getHitCountBlocked().start(this);
		effected.setHitBlocks(_count);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getHitCountBlocked().stop(this);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		return false;
	}
}