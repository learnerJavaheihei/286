package l2s.gameserver.model.base;

/**
 * @author Bonux
**/
public enum ResidenceFunctionType
{
	/*0*/ NONE,
	/*1*/ RESTORE_HP,
	/*2*/ RESTORE_MP,
	/*3*/ RESTORE_CP,
	/*4*/ RESTORE_EXP,
	/*5*/ TELEPORT,
	/*6*/ BROADCAST,
	/*7*/ CURTAIN,
	/*8*/ HANGING,
	/*9*/ SUPPORT,
	/*10*/ OUTERFLAG,
	/*11*/ PLATFORM,
	/*12*/ ITEM_CREATE;

	public static final ResidenceFunctionType[] VALUES = values();
}