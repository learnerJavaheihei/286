package l2s.gameserver.stats.conditions;

import l2s.gameserver.stats.Env;

public class ConditionPlayerSoulsCount extends Condition
{
	private final int type;
	private final int soulsCount;

	public ConditionPlayerSoulsCount(int type, int soulsCount)
	{
		this.type = type;
		this.soulsCount = soulsCount;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if (!env.character.isPlayer())
			return false;
		return env.character.getPlayer().getConsumedSouls(type) >= soulsCount;
	}
}