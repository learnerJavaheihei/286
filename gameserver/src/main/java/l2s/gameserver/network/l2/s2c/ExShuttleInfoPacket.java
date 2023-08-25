package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.entity.boat.Shuttle;
import l2s.gameserver.templates.ShuttleTemplate.ShuttleStop;

/**
 * @author Bonux
**/
public class ExShuttleInfoPacket extends L2GameServerPacket
{
	private final Shuttle _shuttle;
	private final Collection<ShuttleStop> _stops;

	public ExShuttleInfoPacket(Shuttle shuttle)
	{
		_shuttle = shuttle;
		_stops = shuttle.getTemplate().getStops();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_shuttle.getBoatId()); // Shuttle ObjID
		writeD(_shuttle.getX()); // X
		writeD(_shuttle.getY()); // Y
		writeD(_shuttle.getZ()); // Z
		writeD(_shuttle.getHeading()); // Maybe H
		writeD(_shuttle.getBoatId()); // Shuttle ID (Arkan: 1,2; Cruma: 3)
		writeD(_stops.size()); // doors_count
		for(ShuttleStop stop : _stops)
		{
			int stopId = stop.getId();
			writeD(stopId); // Stop ID
			for (Location loc : stop.getDimensions())
			{
				writeD(loc.getX());
				writeD(loc.getY());
				writeD(loc.getZ());
			}

			if(_shuttle.getCurrentWayEvent().containsStop(stopId)) {
				writeD(_shuttle.isDocked());
				writeD(true);
			} else {
				writeD(false);
				writeD(false);
			}
		}
	}
}