package l2s.gameserver.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.templates.spawn.SpawnRange;

/**
 * @author Bonux
**/
public class LocationsList implements SpawnRange, Iterable<Location>
{
	private final List<Location> _locations;

	public LocationsList(List<Location> locations)
	{
		_locations = locations;
	}

	public LocationsList()
	{
		this(new ArrayList<Location>());
	}

	public List<Location> getLocations()
	{
		return _locations;
	}

	public void addLocation(Location loc)
	{
		_locations.add(loc);
	}

	@Override
	public Iterator<Location> iterator()
	{
		return _locations.iterator();
	}

	@Override
	public Location getRandomLoc(int geoIndex, boolean fly)
	{
		if(_locations.isEmpty())
			return null;
		return Rnd.get(_locations).getRandomLoc(geoIndex, fly);
	}
}
