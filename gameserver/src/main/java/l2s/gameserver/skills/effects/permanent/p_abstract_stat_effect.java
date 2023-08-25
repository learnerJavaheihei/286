package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 **/
public abstract class p_abstract_stat_effect extends EffectHandler
{
	private final StatModifierType _modifierType;

	public p_abstract_stat_effect(EffectTemplate template, Stats stat)
	{
		super(template);

		_modifierType = getParams().getEnum("type", StatModifierType.class, StatModifierType.DIFF);

		StatsSet params = new StatsSet();
		params.set("stat", stat);
		params.set("function", "New");
		params.set("mode", _modifierType);
		params.set("condition", template.getCondition());
		params.set("value", getValue());
		template.attachFunc(FuncTemplate.makeTemplate(params));
	}

	protected final StatModifierType getModifierType()
	{
		return _modifierType;
	}

	@Override
	public final Condition getCondition()
	{
		return null;
	}
}