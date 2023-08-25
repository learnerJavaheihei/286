package l2s.gameserver.templates.spawn;

import l2s.gameserver.geometry.Location;

/**
 * @author Bonux
**/
public class SpawnPoint implements SpawnRange
{
	private final Location _loc;
	private final double _chance;

	public SpawnPoint(int x, int y, int z, int heading, double chance)
	{
		_loc = new Location(x, y, z, heading);
		_chance = chance;
	}

	public Location getLoc()
	{
		return _loc;
	}

	public double getChance()
	{
		return _chance;
	}

	@Override
	public Location getRandomLoc(int geoIndex, boolean fly)
	{
		return _loc.getRandomLoc(geoIndex, fly);
	}
}
