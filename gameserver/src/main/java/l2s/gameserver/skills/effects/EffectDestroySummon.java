package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectDestroySummon extends EffectHandler
{
	public EffectDestroySummon(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(!effected.isSummon())
			return false;
		return true;
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		((Servitor) effected).unSummon(false);
		return true;
	}
}