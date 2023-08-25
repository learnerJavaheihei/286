package l2s.gameserver.geometry;

import l2s.gameserver.network.l2.components.ChatType;

/**
 * @author Bonux
**/
public interface ILocation
{
	public int getX();

	public int getY();

	public int getZ();

	public int getHeading();

	default public long getXYDeltaSq(int x, int y)
	{
		long dx = x - getX();
		long dy = y - getY();
		return dx * dx + dy * dy;
	}

	default public long getXYDeltaSq(ILocation loc)
	{
		return getXYDeltaSq(loc.getX(), loc.getY());
	}

	default public long getZDeltaSq(int z)
	{
		long dz = z - getZ();
		return dz * dz;
	}

	default public long getZDeltaSq(ILocation loc)
	{
		return getZDeltaSq(loc.getZ());
	}

	default public long getXYZDeltaSq(int x, int y, int z)
	{
		return getXYDeltaSq(x, y) + getZDeltaSq(z);
	}

	default public long getXYZDeltaSq(ILocation loc)
	{
		return getXYZDeltaSq(loc.getX(), loc.getY(), loc.getZ());
	}

	default public int getDistance(int x, int y)
	{
		return (int) Math.sqrt(getXYDeltaSq(x, y));
	}

	default public int getDistance(int x, int y, int z)
	{
		return (int) Math.sqrt(getXYZDeltaSq(x, y, z));
	}

	default public int getDistance(ILocation loc)
	{
		return getDistance(loc.getX(), loc.getY());
	}

	default public int getDistance3D(ILocation loc)
	{
		return getDistance(loc.getX(), loc.getY(), loc.getZ());
	}

	default public boolean isInRangeSq(ILocation loc, long range)
	{
		return getXYDeltaSq(loc) <= range;
	}

	default public boolean isInRange(ILocation loc, int range)
	{
		return isInRangeSq(loc, (long) range * range);
	}

	default public boolean isInRangeZ(ILocation loc, int range)
	{
		return isInRangeZSq(loc, (long) range * range);
	}

	default public boolean isInRangeZSq(ILocation loc, long range)
	{
		return getXYZDeltaSq(loc) <= range;
	}

	default public long getSqDistance(int x, int y)
	{
		return getXYDeltaSq(x, y);
	}

	default public long getSqDistance(ILocation loc)
	{
		return getXYDeltaSq(loc);
	}
}
