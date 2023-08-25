package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.ObservePoint;

public class CannotMoveAnymore extends L2GameClientPacket
{
	private static abstract class Boat extends CannotMoveAnymore
	{
		protected int _boatId = -1;

		@Override
		protected boolean readImpl()
		{
			_boatId = readD();
			_loc.x = readD();
			_loc.y = readD();
			_loc.z = readD();
			_loc.h = readD();
			return true;
		}

		@Override
		protected void runImpl()
		{
			Player activeChar = getClient().getActiveChar();
			if(activeChar == null)
				return;

			l2s.gameserver.model.entity.boat.Boat boat = activeChar.getBoat();
			if(boat != null && boat.getBoatId() == _boatId)
			{
				activeChar.setInBoatPosition(_loc);
				activeChar.setHeading(_loc.h);
				activeChar.broadcastPacket(boat.inStopMovePacket(activeChar));
			}
		}
	}

	public static class AirShip extends Boat
	{
		//
	}

	public static class Vehicle extends Boat
	{
		//
	}

	public static class Shuttle extends Boat
	{
		//
	}

	protected final Location _loc = new Location();

	@Override
	protected boolean readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isInObserverMode())
		{
			ObservePoint observer = activeChar.getObservePoint();
			if(observer != null)
				observer.getMovement().stopMove();
			return;
		}

		activeChar.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, _loc, null);
	}
}