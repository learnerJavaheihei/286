package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.stats.Stats;

/**
 * @author Bonux
**/
public final class p_critical_damage extends p_abstract_stat_effect
{
	public p_critical_damage(EffectTemplate template)
	{
		super(template, Stats.CRITICAL_DAMAGE);
	}
}