package l2s.gameserver.stats;

import java.util.NoSuchElementException;

/**
 * @author Sdw
 */
public enum StatModifierType
{
	DIFF,
	PER;

	public static final StatModifierType[] VALUES = values();

	public static StatModifierType valueOfXml(String name)
	{
		String upperCaseName = name.toUpperCase();
		for(StatModifierType s : VALUES)
		{
			if(s.toString().equalsIgnoreCase(upperCaseName))
				return s;
		}
		throw new NoSuchElementException("Unknown name '" + name + "' for enum Stats");
	}
}
