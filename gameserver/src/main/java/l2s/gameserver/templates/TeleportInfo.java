package l2s.gameserver.templates;

import l2s.gameserver.geometry.Location;

public class TeleportInfo
{
	private final int id;
	private final Location loc;
	private final long price;

	public TeleportInfo(int id, Location loc, long price)
	{
		this.id = id;
		this.loc = loc;
		this.price = price;
	}

	public int getId()
	{
		return id;
	}

	public Location getLoc()
	{
		return loc;
	}

	public long getPrice()
	{
		return price;
	}
}
