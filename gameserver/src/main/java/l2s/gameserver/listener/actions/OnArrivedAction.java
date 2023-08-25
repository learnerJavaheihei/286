package l2s.gameserver.listener.actions;

import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.model.Creature;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 16.05.2019
 * Developed for L2-Scripts.com
 **/
@FunctionalInterface
public interface OnArrivedAction {
	void onArrived(Creature actor, ILocation loc, boolean toTarget);
}
