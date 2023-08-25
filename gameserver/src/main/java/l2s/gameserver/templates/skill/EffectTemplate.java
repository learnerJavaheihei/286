package l2s.gameserver.templates.skill;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.handler.effects.EffectHandlerHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.EffectTargetType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.templates.StatsSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public final class EffectTemplate extends StatTemplate
{
	public static final EffectTemplate[] EMPTY_ARRAY = new EffectTemplate[0];

	private static final Logger _log = LoggerFactory.getLogger(EffectTemplate.class);

	private final Skill _skill;
	private final StatsSet _paramSet;
	private final String _name;
	private final EffectUseType _useType;
	private final EffectTargetType _targetType;

	private final double _value;
	private final int _interval;
	private final int _chance;

	private EffectHandler _handler = null;
	private Condition _attachCond = null;

	public EffectTemplate(Skill skill, StatsSet set, EffectUseType useType, EffectTargetType targetType)
	{
		_skill = skill;
		_paramSet = set;
		_name = set.getString("name", "");

		boolean instant = getParams().getBool("instant", _name.startsWith("i_"));
		if(instant)
		{
			switch(useType)
			{
				case SELF:
					useType = EffectUseType.SELF_INSTANT;
					break;
				case NORMAL:
					useType = EffectUseType.NORMAL_INSTANT;
					break;
			}
		}

		_useType = useType;
		_targetType = targetType;

		_value = set.getDouble("value", 0D);
		_interval = set.getInteger("interval", Integer.MAX_VALUE);
		_chance = set.getInteger("chance", -1);
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public StatsSet getParams()
	{
		return _paramSet;
	}

	public String getName()
	{
		return _name;
	}

	public EffectHandler getHandler()
	{
		if(_handler == null)
			_handler = EffectHandlerHolder.getInstance().makeHandler(_name, this);
		return _handler;
	}

	public boolean isInstant()
	{
		return _useType.isInstant();
	}

	public EffectUseType getUseType()
	{
		return _useType;
	}

	public EffectTargetType getTargetType()
	{
		return _targetType;
	}

	public double getValue()
	{
		return _value;
	}

	public int getInterval()
	{
		return _interval;
	}

	public int getChance()
	{
		return _chance;
	}

	public void attachCond(Condition c)
	{
		_attachCond = c;
	}

	public Condition getCondition()
	{
		return _attachCond;
	}
}