package l2s.gameserver.model.entity.boat;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ShuttleWayEvent;
import l2s.gameserver.network.l2.s2c.ExMTLInSuttlePacket;
import l2s.gameserver.network.l2.s2c.ExSuttleGetOffPacket;
import l2s.gameserver.network.l2.s2c.ExSuttleGetOnPacket;
import l2s.gameserver.network.l2.s2c.ExShuttleInfoPacket;
import l2s.gameserver.network.l2.s2c.ExSuttleMovePacket;
import l2s.gameserver.network.l2.s2c.ExStopMoveInShuttlePacket;
import l2s.gameserver.network.l2.s2c.ExValidateLocationInShuttlePacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.ShuttleTemplate;

/**
 * @author Bonux
 */
public class Shuttle extends Boat
{
	private static class Docked implements Runnable
	{
		private Shuttle _shuttle;

		public Docked(Shuttle shuttle)
		{
			_shuttle = shuttle;
		}

		public void run()
		{
			if(_shuttle == null)
				return;

			_shuttle.getCurrentWayEvent().stopEvent(false);
			_shuttle.getNextWayEvent().reCalcNextTime(false);
		}
	}

	private final TIntObjectHashMap<ShuttleWayEvent> _wayEvents = new TIntObjectHashMap<ShuttleWayEvent>();

	private boolean _moveBack;
	public int _currentWay;

	public Shuttle(int objectId, ShuttleTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getBoatId()
	{
		return getTemplate().getId();
	}

	@Override
	public final ShuttleTemplate getTemplate()
	{
		return (ShuttleTemplate) super.getTemplate();
	}

	@Override
	public void onSpawn()
	{
		_moveBack = false;
		_currentWay = 0;

		getCurrentWayEvent().reCalcNextTime(false);
	}

	@Override
	public void onEvtArrived()
	{
		ThreadPoolManager.getInstance().schedule(new Docked(this), 1500L);
	}

	@Override
	public L2GameServerPacket infoPacket()
	{
		return new ExShuttleInfoPacket(this);
	}

	@Override
	public L2GameServerPacket movePacket()
	{
		return new ExSuttleMovePacket(this);
	}

	@Override
	public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
	{
		return new ExMTLInSuttlePacket(player, this, src, desc);
	}

	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket inStopMovePacket(Player player)
	{
		return new ExStopMoveInShuttlePacket(player);
	}

	@Override
	public L2GameServerPacket startPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket checkLocationPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket validateLocationPacket(Player player)
	{
		return new ExValidateLocationInShuttlePacket(player);
	}

	@Override
	public L2GameServerPacket getOnPacket(Playable playable, Location location)
	{
		return new ExSuttleGetOnPacket(playable, this, location);
	}

	@Override
	public L2GameServerPacket getOffPacket(Playable playable, Location location)
	{
		return new ExSuttleGetOffPacket(playable, this, location);
	}

	@Override
	public boolean isShuttle()
	{
		return true;
	}

	@Override
	public void oustPlayers()
	{
		//
	}

	@Override
	public void trajetEnded(boolean oust)
	{
		//
	}

	@Override
	public void teleportShip(int x, int y, int z)
	{
		//
	}

	@Override
	public Location getReturnLoc()
	{
		return getCurrentWayEvent().getReturnLoc();
	}

	public void addPlayer(Player player, Location boatLoc)
	{
		// Во время движения транспорта, игрок не может зайти на него. Отправляем на стабильную точку.
		if(getMovement().isMoving())
		{
			player.teleToLocation(getReturnLoc());
			return;
		}
		super.addPlayer(player, boatLoc);
	}

	public void oustPlayer(Player player, Location loc, boolean teleport)
	{
		// Во время движения транспорта, игрок не может выйти из него. Отправляем на стабильную точку.
		if(getMovement().isMoving())
		{
			player.teleToLocation(getReturnLoc());
			return;
		}
		super.oustPlayer(player, loc, teleport);
	}

	public void addWayEvent(ShuttleWayEvent wayEvent)
	{
		_wayEvents.put((wayEvent.getId() % 100), wayEvent);
	}

	public ShuttleWayEvent getCurrentWayEvent()
	{
		return _wayEvents.get(_currentWay);
	}

	private ShuttleWayEvent getNextWayEvent()
	{
		int ways = _wayEvents.size() - 1;
		if(!_moveBack)
		{
			_currentWay++;
			if(_currentWay > ways)
			{
				_currentWay = ways - 1;
				_moveBack = true;
			}
		}
		else
		{
			_currentWay--;
			if(_currentWay < 0)
			{
				_currentWay = 1;
				_moveBack = false;
			}
		}
		return _wayEvents.get(_currentWay);
	}
}
