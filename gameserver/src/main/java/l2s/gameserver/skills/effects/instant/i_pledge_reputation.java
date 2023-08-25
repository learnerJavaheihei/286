package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_pledge_reputation extends i_abstract_effect
{
	public i_pledge_reputation(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return effected.getClan() != null;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		effected.getClan().incReputation((int) getValue(), false, "Using skill ID[" + getSkill().getId() + "] LEVEL[" + getSkill().getLevel() + "]");
	}
}
