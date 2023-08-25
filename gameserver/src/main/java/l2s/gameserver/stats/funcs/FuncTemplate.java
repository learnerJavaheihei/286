package l2s.gameserver.stats.funcs;

import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class FuncTemplate
{
	private static final Logger _log = LoggerFactory.getLogger(FuncTemplate.class);

	public static final FuncTemplate[] EMPTY_ARRAY = new FuncTemplate[0];

	public final Condition _applyCond;
	public final Stats _stat;
	public final int _order;
	public final double _value;
	public final StatsSet _params;

	public Class<?> _func;
	public Constructor<?> _constructor;

	private FuncTemplate(StatsSet params)
	{
		_applyCond = (Condition) params.getObject("condition", null);
		_stat = (Stats) params.getObject("stat");
		StatModifierType modifierType = (StatModifierType) params.getObject("mode", null);
		_order = params.getInteger("order", modifierType == StatModifierType.PER ? 0x30 : 0x40);
		_value = params.getDouble("value", 0);
		_params = params;

		try
		{
			_func = Class.forName("l2s.gameserver.stats.funcs.Func" + params.getString("function"));
			_constructor = _func.getConstructor(new Class<?>[] { Stats.class, // stats to update
					Integer.TYPE, // order of execution
					Object.class, // owner
					Double.TYPE, // value for function
					StatsSet.class // params
			});
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public Func getFunc(Object owner)
	{
		try
		{
			Func f = (Func) _constructor.newInstance(_stat, _order, owner, _value, _params);
			if(_applyCond != null)
				f.setCondition(_applyCond);
			return f;
		}
		catch(IllegalAccessException e)
		{
			_log.error("", e);
			return null;
		}
		catch(InstantiationException e)
		{
			_log.error("", e);
			return null;
		}
		catch(InvocationTargetException e)
		{
			_log.error("", e);
			return null;
		}
	}

	public static FuncTemplate makeTemplate(Condition applyCond, String func, Stats stat, int order, double value) {
		StatsSet params = new StatsSet();
		params.set("stat", stat);
		params.set("function", func);
		params.set("condition", applyCond);
		params.set("order", order);
		params.set("value", value);
		return new FuncTemplate(params);
	}

	public static FuncTemplate makeTemplate(StatsSet params) {
		return new FuncTemplate(params);
	}
}