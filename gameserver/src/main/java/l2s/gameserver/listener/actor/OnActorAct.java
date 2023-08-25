package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 28.04.2019
 * Developed for L2-Scripts.com
 * Слушатель для совершенно любых действий. Он нужен для того, чтобы не писать отдельные слушатели под мелкие действия.
 **/
public interface OnActorAct extends CharListener {
	void onAct(Creature actor, String act, Object... args);
}
