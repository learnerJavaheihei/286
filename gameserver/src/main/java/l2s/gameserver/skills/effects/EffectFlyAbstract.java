package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket.FlyType;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public abstract class EffectFlyAbstract extends EffectHandler
{
	private final FlyType _flyType;
	private final double _flyCourse;
	private final int _flySpeed;
	private final int _flyDelay;
	private final int _flyAnimationSpeed;
	private final int _flyRadius;

	public EffectFlyAbstract(EffectTemplate template)
	{
		super(template);

		_flyType = getParams().getEnum("fly_type", FlyType.class, getSkill().getFlyType());
		_flyCourse = getParams().getDouble("fly_course", 0D);
		_flySpeed = getParams().getInteger("fly_speed", getSkill().getFlySpeed());
		_flyDelay = getParams().getInteger("fly_delay", getSkill().getFlyDelay());
		_flyAnimationSpeed = getParams().getInteger("fly_animation_speed", getSkill().getFlyAnimationSpeed());
		_flyRadius = getParams().getInteger("fly_radius", getSkill().getFlyRadius());
	}

	public FlyType getFlyType()
	{
		return _flyType;
	}

	public double getFlyCourse()
	{
		return _flyCourse;
	}

	public int getFlySpeed()
	{
		return _flySpeed;
	}

	public int getFlyDelay()
	{
		return _flyDelay;
	}

	public int getFlyAnimationSpeed()
	{
		return _flyAnimationSpeed;
	}

	public int getFlyRadius()
	{
		return _flyRadius;
	}
}
