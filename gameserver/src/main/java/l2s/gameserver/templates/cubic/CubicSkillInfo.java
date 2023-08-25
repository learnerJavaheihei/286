package l2s.gameserver.templates.cubic;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Iterator;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;

import org.dom4j.Element;

/**
 * @author Bonux
 */
public class CubicSkillInfo
{
	private final Skill _skill;
	private final int _chance;
	private final CubicTargetType _targetType;
	private final boolean _canAttackDoor;
	private final TIntIntHashMap _chances = new TIntIntHashMap();
	private final int _delay;
	private final CubicReuseType _reuse;

	public CubicSkillInfo(Skill skill, int chance, CubicTargetType targetType, boolean canAttackDoor, int delay, CubicReuseType reuse)
	{
		_skill = skill;
		_chance = chance;
		_targetType = targetType;
		_canAttackDoor = canAttackDoor;
		_delay = delay;
		_reuse = reuse;
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public int getChance()
	{
		return _chance;
	}

	public CubicTargetType getTargetType()
	{
		return _targetType;
	}

	public boolean isCanAttackDoor()
	{
		return _canAttackDoor;
	}

	public void addChance(int value, int chance)
	{
		_chances.put(value, chance);
	}

	public int getChance(int a)
	{
		return _chances.get(a);
	}

	public TIntIntHashMap getChances()
	{
		return _chances;
	}

	public int getDelay()
	{
		return _delay;
	}

	public CubicReuseType getReuseType()
	{
		return _reuse;
	}

	public static CubicSkillInfo parse(Element element)
	{
		int id = Integer.parseInt(element.attributeValue("id"));
		int level = Integer.parseInt(element.attributeValue("level"));
		int useChance = element.attributeValue("use_chance") == null ? 100 : Integer.parseInt(element.attributeValue("use_chance"));
		CubicTargetType targetType = element.attributeValue("target_type") == null ? CubicTargetType.TARGET : CubicTargetType.valueOf(element.attributeValue("target_type").toUpperCase());
		boolean canAttackDoor = element.attributeValue("can_attack_door") == null ? false : Boolean.parseBoolean(element.attributeValue("can_attack_door"));
		int delay = element.attributeValue("delay") == null ? -1 : Integer.parseInt(element.attributeValue("delay"));
		CubicReuseType reuse = element.attributeValue("reuse") == null ? CubicReuseType.DEFAULT : CubicReuseType.valueOf(element.attributeValue("reuse").toUpperCase());

		Skill skill = SkillHolder.getInstance().getSkill(id, level);
		if(skill == null)
			return null;

		CubicSkillInfo skillInfo = new CubicSkillInfo(skill, useChance, targetType, canAttackDoor, delay, reuse);

		for(Iterator<Element> chanceIterator = element.elementIterator(); chanceIterator.hasNext();)
		{
			Element chanceElement = chanceIterator.next();
			int min_hp_percent = Integer.parseInt(chanceElement.attributeValue("min_hp_percent"));
			int max_hp_percent = Integer.parseInt(chanceElement.attributeValue("max_hp_percent"));
			int value = Integer.parseInt(chanceElement.attributeValue("value"));
			for(int i = min_hp_percent; i <= max_hp_percent; i++)
				skillInfo.addChance(i, value);
		}

		return skillInfo;
	}
}