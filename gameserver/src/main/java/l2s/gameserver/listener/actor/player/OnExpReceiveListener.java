package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public interface OnExpReceiveListener extends PlayerListener
{
	public void onExpReceive(Player player, long value, boolean hunting);
}
