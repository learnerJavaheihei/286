package l2s.gameserver.model.base;

import java.util.Arrays;

/**
 * @author Bonux
**/
public enum ElementalElement
{
	NONE,
	FIRE, // TODO: Check SysStringId
	WATER, // TODO: Check SysStringId
	WIND, // TODO: Check SysStringId
	EARTH; // TODO: Check SysStringId

	/** Массив элементов без NONE **/
	public final static ElementalElement[] VALUES = Arrays.copyOfRange(values(), 1, 5);

	public int getId()
	{
		return ordinal();
	}

	public ElementalElement getDominant()
	{
		return getDominant(this);
	}

	public static ElementalElement getDominant(ElementalElement element)
	{
		switch(element)
		{
			case FIRE:
				return WATER;
			case WIND:
				return EARTH;
			case EARTH:
				return FIRE;
			case WATER:
				return WIND;
		}
		return NONE;
	}

	public ElementalElement getSubordinate()
	{
		return getSubordinate(this);
	}

	public static ElementalElement getSubordinate(ElementalElement element)
	{
		switch(element)
		{
			case WATER:
				return FIRE;
			case FIRE:
				return EARTH;
			case WIND:
				return WATER;
			case EARTH:
				return WIND;
		}
		return NONE;
	}

	public static ElementalElement getElementById(int id)
	{
		for(ElementalElement e : VALUES)
		{
			if(e.getId() == id)
				return e;
		}
		return NONE;
	}
}
