package l2s.gameserver.templates;

import l2s.gameserver.geometry.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Bonux
**/
public final class ShuttleTemplate extends CreatureTemplate
{
	public static class ShuttleStop
	{
		private final int _id;
		private final List<Location> _dimensions = new ArrayList<>();

		public ShuttleStop(int id)
		{
			_id = id;
		}

		public int getId()
		{
			return _id;
		}

		public List<Location> getDimensions() {
			return _dimensions;
		}
	}

	private final int _id;
	private final TreeMap<Integer, ShuttleStop> _stops = new TreeMap<Integer, ShuttleStop>();

	public ShuttleTemplate(int id)
	{
		super(CreatureTemplate.getEmptyStatsSet());
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public Collection<ShuttleStop> getStops()
	{
		return _stops.values();
	}

	public ShuttleStop getStop(int id)
	{
		return _stops.get(id);
	}

	public void addStop(ShuttleStop door)
	{
		_stops.put(door.getId(), door);
	}
}