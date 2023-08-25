package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_target_me extends i_abstract_effect
{
	public i_target_me(EffectTemplate template)
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
		effected.setTarget(effector);

		effected.abortCast(true, true);
		effected.abortAttack(true, true);

		effected.getAI().clearNextAction();
	}
}