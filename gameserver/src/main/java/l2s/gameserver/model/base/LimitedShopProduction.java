package l2s.gameserver.model.base;

import l2s.gameserver.templates.StatsSet;

/**
 * @author nexvill
 */
public class LimitedShopProduction implements Cloneable
{
	private StatsSet _info;

	public LimitedShopProduction(StatsSet set)
	{
		_info = set;
	}

	@Override
	public LimitedShopProduction clone()
	{
		LimitedShopProduction mi = new LimitedShopProduction(_info);
		return mi;
	}
	
	public StatsSet getInfo()
	{
		return _info;
	}

	@Override
	public int hashCode()
	{
		return _info.getInteger("product1Id");
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		LimitedShopProduction other = (LimitedShopProduction) obj;
		if (_info != other.getInfo())
			return false;
		return true;
	}
}