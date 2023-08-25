package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;

/**
 * @author Bonux
**/
public interface OnElementalLevelChangeListener extends PlayerListener
{
	public void onElementalLevelChange(Player player, Elemental elemental, int oldLvl, int newLvl);
}
