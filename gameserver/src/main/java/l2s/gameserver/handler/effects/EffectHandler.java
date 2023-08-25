package l2s.gameserver.handler.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncOwner;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class EffectHandler implements FuncOwner
{
	public static String getName(Class<? extends EffectHandler> cls)
	{
		return cls.getSimpleName().replaceAll("^(Effect)?(.*?)(EffectHandler)?$", "$2").toLowerCase();
	}

	private final String _name;
	private final EffectTemplate _template;

	public EffectHandler(EffectTemplate template)
	{
		_name = getName(getClass());
		_template = template;
	}

	@Override
	public final boolean isFuncEnabled()
	{
		return true;
	}

	@Override
	public final boolean overrideLimits()
	{
		return false;
	}

	public final String getName()
	{
		return _name;
	}

	public final EffectTemplate getTemplate()
	{
		return _template;
	}

	public final Skill getSkill()
	{
		return _template.getSkill();
	}

	public final StatsSet getParams()
	{
		return _template.getParams();
	}

	public final double getValue()
	{
		return _template.getValue();
	}

	public int getInterval()
	{
		return _template.getInterval();
	}

	public Func[] getStatFuncs()
	{
		return _template.getStatFuncs(this);
	}

	public Condition getCondition()
	{
		return _template.getCondition();
	}

	// TODO Избавиться
	public boolean checkBlockedAbnormalType(Abnormal abnormal, Creature effector, Creature effected, AbnormalType abnormalType)
	{
		return false;
	}

	// TODO Избавиться
	public boolean checkDebuffImmunity(Abnormal abnormal, Creature effector, Creature effected)
	{
		return false;
	}

	// TODO Избавиться
	public boolean isHidden()
	{
		return false;
	}

	// TODO Избавиться
	public boolean isSaveable()
	{
		return true;
	}

	private final boolean testCondition(Creature effector, Creature effected)
	{
		Condition cond = getCondition();
		return cond == null || cond.test(new Env(effector, effected, getSkill()));
	}

	public final boolean checkConditionImpl(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(!checkCondition(abnormal, effector, effected))
			return false;
		return testCondition(effector, effected);
	}

	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return true;
	}

	public final boolean checkActingConditionImpl(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(!checkActingCondition(abnormal, effector, effected))
			return false;
		return testCondition(effector, effected);
	}

	protected boolean checkActingCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return true;
	}

	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		//
	}

	public void onApplied(Abnormal abnormal, Creature effector, Creature effected)
	{
		//
	}

	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		return true;
	}

	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		//
	}

	public final boolean checkConditionImpl(Creature effector, Creature effected)
	{
		if(!checkCondition(effector, effected))
			return false;
		return testCondition(effector, effected);
	}

	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return checkCondition(null, effector, effected);
	}

	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		onStart(null, effector, effected);
		onActionTime(null, effector, effected);
		onExit(null, effector, effected);
	}

	public EffectHandler getImpl()
	{
		return this;
	}
}
