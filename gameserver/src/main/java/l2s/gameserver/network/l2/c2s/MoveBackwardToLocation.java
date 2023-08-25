package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.ObservePoint;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.MTLPacket;

// cdddddd(d)
public class MoveBackwardToLocation extends L2GameClientPacket
{
	private Location _targetLoc = new Location();
	private Location _originLoc = new Location();

	private boolean _keyboardMovement;

	/**
	 * packet type id 0x0f
	 */
	@Override
	protected boolean readImpl()
	{
		_targetLoc.x = readD();
		_targetLoc.y = readD();
		_targetLoc.z = readD();
		_originLoc.x = readD();
		_originLoc.y = readD();
		_originLoc.z = readD();
		if(_buf.hasRemaining())
			_keyboardMovement = readD() == 0;
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_keyboardMovement && !Config.ALLOW_KEYBOARD_MOVE)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(_targetLoc.equals(_originLoc))
		{
			if(_keyboardMovement)
				activeChar.getMovement().stopMove();
			else
				activeChar.sendActionFailed();
			return;
		}

		if(ValidatePosition.validatePosition(activeChar, _originLoc.x, _originLoc.y, _originLoc.z, -1))
			return;

		activeChar.setActive();

		if(System.currentTimeMillis() - activeChar.getLastMovePacket() < Config.MOVE_PACKET_DELAY)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setLastMovePacket();

		if(activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}

		// Correcting targetZ from floor level to head level (?)
		// Client is giving floor level as targetZ but that floor level doesn't
		// match our current geodata and teleport coords as good as head level!
		// L2J uses floor, not head level as char coordinates. This is some
		// sort of incompatibility fix.
		// Validate position packets sends head level.
		_targetLoc.z += activeChar.getCollisionHeight();

		if(activeChar.isInObserverMode())
		{
			ObservePoint observer = activeChar.getObservePoint();
			if(observer != null)
				observer.getMovement().moveToLocation(_targetLoc, 0, false);
			return;
		}

		if(activeChar.isFrozen())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}

		if(activeChar.isInTrainingCamp())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
			return;
		}

		if(activeChar.getNpcDialogEndTime() > (System.currentTimeMillis() / 1000L))
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTeleMode() > 0)
		{
			if(activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);
			activeChar.sendActionFailed();
			activeChar.teleToLocation(_targetLoc);
			return;
		}

		if(activeChar.isInFlyingTransform())
			_targetLoc.z = Math.min(5950, Math.max(50, _targetLoc.z)); // В летающей трансформе нельзя летать ниже, чем 0, и выше, чем 6000

		// Can't move if character is confused, or trying to move a huge distance
		if(activeChar.getDistance(_targetLoc) > 98010000) // 9900*9900
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.getMovement().moveToLocation(_targetLoc, 0, !activeChar.getVarBoolean("no_pf"), true, _keyboardMovement);
	}
}