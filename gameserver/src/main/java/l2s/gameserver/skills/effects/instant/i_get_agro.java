package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class i_get_agro extends i_abstract_effect
{
	public i_get_agro(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(effected.isRaid())
			return false;
		if(effected == effector)
			return false;
		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		if(effected.isAutoAttackable(effector))
			effected.getAI().Attack(effector, false, false);
	}
}