package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class EffectDamageBlock extends EffectHandler
{
	private final boolean _withException;

	public EffectDamageBlock(EffectTemplate template)
	{
		super(template);
		_withException = getParams().getBool("with_exception", false);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return !effected.isRaid();
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getDamageBlocked().start(this);
		if(_withException)
		{
			if(effected == effector)
			{
				if(getSkill().equals(effector.getSkillCast(SkillCastingType.NORMAL).getSkillEntry().getTemplate()))
					effected.setDamageBlockedException(effector.getSkillCast(SkillCastingType.NORMAL).getTarget());
				else if(getSkill().equals(effector.getSkillCast(SkillCastingType.NORMAL_SECOND).getSkillEntry().getTemplate()))
					effected.setDamageBlockedException(effector.getSkillCast(SkillCastingType.NORMAL_SECOND).getTarget());
			}
			else
				effected.setDamageBlockedException(effector);
		}
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getDamageBlocked().stop(this);
		effected.setDamageBlockedException(null);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		return false;
	}
}