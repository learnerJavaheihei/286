package l2s.gameserver.skills;

/**
 * @author Bonux
**/
public enum EffectUseType
{
	START(true, false),
	TICK(true, false),
	NORMAL(false, false),
	NORMAL_INSTANT(true, false),
	SELF(false, true),
	SELF_INSTANT(true, true),
	END(true, false);

	public static final EffectUseType[] VALUES = values();

	private final boolean _instant;
	private final boolean _self;

	private EffectUseType(boolean instant, boolean self)
	{
		_instant = instant;
		_self = self;
	}

	public boolean isInstant()
	{
		return _instant;
	}

	public boolean isSelf()
	{
		return _self;
	}
}