package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_sp extends i_abstract_effect
{
	public i_sp(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return effected.isPlayer();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		effected.getPlayer().addExpAndSp(0, (int) getValue());
	}
}
