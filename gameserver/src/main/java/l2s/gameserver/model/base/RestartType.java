package l2s.gameserver.model.base;

/**
 * @author VISTALL
 * @date 13:00/27.04.2011
 */
public enum RestartType
{
	/*0*/TO_VILLAGE,
	/*1*/TO_CLANHALL,
	/*2*/TO_CASTLE,
	/*3*/TO_FORTRESS,
	/*4*/TO_FLAG,
	/*5*/FIXED,
	/*6*/AGATHION,
	/*7*/ADVENTURES_SONG;

	public static final RestartType[] VALUES = values();
}
