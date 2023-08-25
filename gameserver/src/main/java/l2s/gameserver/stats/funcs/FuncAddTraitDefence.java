package l2s.gameserver.stats.funcs;

import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

/**
 * @author Bonux
 * Временный изжоп для нормальной работы резистов, пока не перепишем движек статтов под ПТС.
**/
public class FuncAddTraitDefence extends Func
{
	public FuncAddTraitDefence(Stats stat, int order, Object owner, double value, StatsSet params)
	{
		super(stat, order, owner, value, params);
	}

	@Override
	public void calc(Env env, StatModifierType modifierType)
	{
		if(value >= 100)
			env.value = Double.POSITIVE_INFINITY;
		env.value += value;
	}
}
