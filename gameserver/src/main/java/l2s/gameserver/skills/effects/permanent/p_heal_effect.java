package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.stats.Stats;

/**
 * @author Bonux
**/
public final class p_heal_effect extends p_abstract_stat_effect
{
	public p_heal_effect(EffectTemplate template)
	{
		super(template, Stats.HEAL_EFFECT);
	}
}