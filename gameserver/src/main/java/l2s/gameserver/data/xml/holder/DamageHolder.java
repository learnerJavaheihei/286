package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Skill;

/**
 * @author nexvill
 */
public final class DamageHolder extends AbstractHolder
{
	private final int _creatureId;
	private final String _name;
	private final Skill _skill;
	private final int _damage;
	private final int _type;
	
	public DamageHolder(int creatureId, String name, Skill skill, int damage, int type)
	{
		_creatureId = creatureId;
		_name = name;
		_skill = skill;
		_damage = damage;
		_type = type;
	}

	public int getCreatureId()
	{
		return _creatureId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	public int getType()
	{
		return _type;
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{		
	}
}
