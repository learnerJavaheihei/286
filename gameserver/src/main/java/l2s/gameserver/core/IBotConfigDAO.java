package l2s.gameserver.core;

import l2s.gameserver.model.Player;

public interface IBotConfigDAO
{
	public void restore(Player var1);

	public void update(Player var1);
}