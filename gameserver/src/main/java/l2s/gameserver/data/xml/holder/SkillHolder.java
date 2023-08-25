package l2s.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.SkillUtils;

import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntIntMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author Bonux
**/
public final class SkillHolder extends AbstractHolder
{
	private static final SkillHolder _instance = new SkillHolder();

	private final IntObjectMap<Skill> _skills = new HashIntObjectMap<>();
	private final IntObjectMap<Skill> _skillsByIndex = new HashIntObjectMap<Skill>();
	private final IntObjectMap<List<Skill>> _skillsById = new HashIntObjectMap<List<Skill>>();

	private final IntObjectMap<IntIntMap> _cachedHashCodes = new HashIntObjectMap<IntIntMap>();
	private final AtomicInteger _lastHashCode = new AtomicInteger(0);

	public static SkillHolder getInstance()
	{
		return _instance;
	}

	public int getHashCode(int skillId, int skillLevel)
	{
		IntIntMap hashCodes = _cachedHashCodes.get(skillId);
		if(hashCodes == null)
		{
			hashCodes = new HashIntIntMap();
			_cachedHashCodes.put(skillId, hashCodes);
		}

		int index = hashCodes.get(skillLevel);
		if(index == 0)
		{
			index = _lastHashCode.incrementAndGet();
			hashCodes.put(skillLevel, index);
		}
		return index;
	}

	public void addSkill(Skill skill)
	{
		_skills.put(skill.hashCode(), skill);

		List<Skill> skills = _skillsById.get(skill.getId());
		if(skills == null)
		{
			skills = new ArrayList<Skill>();
			_skillsById.put(skill.getId(), skills);
		}
		skills.add(skill);

		_skillsByIndex.put(SkillUtils.getSkillPTSHash(skill.getId(), skills.size()), skill);
	}

	public Skill getSkillByIndex(int id, int index)
	{
		return _skillsByIndex.get(SkillUtils.getSkillPTSHash(id, index));
	}

	public Skill getSkill(int hashCode)
	{
		return _skills.get(hashCode);
	}

	public Skill getSkill(int id, int level)
	{
		return getSkill(getHashCode(id, level));
	}

	public List<Skill> getSkills(int id)
	{
		return _skillsById.get(id);
	}

	public Collection<Skill> getSkills()
	{
		return _skills.valueCollection();
	}

	public void callInit()
	{
		for(Skill skill : getSkills())
			skill.init();
	}

	@Override
	public int size()
	{
		return _skills.size();
	}

	@Override
	public void clear()
	{
		_skills.clear();
		_skillsByIndex.clear();
		_skillsById.clear();
	}
}
