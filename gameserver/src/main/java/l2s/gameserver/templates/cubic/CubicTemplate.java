package l2s.gameserver.templates.cubic;

import l2s.commons.math.random.RndSelector;

import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author Bonux
 */
public class CubicTemplate
{
	private final int _id;
	private final int _level;
	private final int _slot;
	private final int _duration;
	private final int _delay;
	private final int _maxCount;
	private final CubicUseUpType _useUp;
	private final double _power;
	private final CubicTargetType _targetType;

	private final RndSelector<CubicSkillInfo> _skills = new RndSelector<CubicSkillInfo>(true);
	private final IntObjectMap<CubicSkillInfo> _timeSkills = new HashIntObjectMap<CubicSkillInfo>();

	public CubicTemplate(int id, int level, int slot, int duration, int delay, int maxCount, CubicUseUpType useUp, double power, CubicTargetType targetType)
	{
		_id = id;
		_level = level;
		_slot = slot;
		_duration = duration;
		_delay = delay;
		_maxCount = maxCount;
		_useUp = useUp;
		_power = power;
		_targetType = targetType;
	}

	public void putSkill(CubicSkillInfo skill, int chance)
	{
		_skills.add(skill, chance);
	}

	public CubicSkillInfo getRandomSkill()
	{
		return _skills.chance();
	}

	public void putTimeSkill(CubicSkillInfo skill)
	{
		_timeSkills.put(skill.getDelay(), skill);
	}

	public CubicSkillInfo getTimeSkill(int lifeTime)
	{
		CubicSkillInfo skill = null;
		for(IntObjectPair<CubicSkillInfo> pair : _timeSkills.entrySet())
		{
			if((lifeTime % pair.getKey()) == 0)
			{
				if(skill == null || skill.getDelay() > pair.getKey())
					skill = pair.getValue();
			}
		}
		return skill;
	}

	public int getDuration()
	{
		return _duration;
	}

	public int getDelay()
	{
		return _delay;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getSlot()
	{
		return _slot;
	}

	public int getMaxCount()
	{
		return _maxCount;
	}

	public CubicUseUpType getUseUp()
	{
		return _useUp;
	}

	public double getPower()
	{
		return _power;
	}

	public CubicTargetType getTargetType()
	{
		return _targetType;
	}
}