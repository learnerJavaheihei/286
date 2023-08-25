package l2s.gameserver.listener.game;

import l2s.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener
{
	public void onDay(boolean onStart);

	public void onNight(boolean onStart);
}
