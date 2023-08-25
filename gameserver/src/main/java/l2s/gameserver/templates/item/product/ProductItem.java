package l2s.gameserver.templates.item.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import l2s.commons.time.cron.SchedulingPattern;

public class ProductItem implements Comparable<ProductItem>
{
	// Базовые параметры, если продукт не имеет лимита времени продаж
	public static final long NOT_LIMITED_START_TIME = 315547200000L;
	public static final long NOT_LIMITED_END_TIME = 2127445200000L;

	public static final SchedulingPattern DEFAULT_LIMIT_REFRESH_PATTERN = new SchedulingPattern("30 6 * * *");

	private final int _id;
	private final int _category;
	private final int _price;
	private final int _silverCoinCount;
	private final int _goldCoinCount;
	private final int _minVipLevel;
	private final int _maxVipLevel;
	private final int _limit;
	private final SchedulingPattern _limitRefreshPattern;
	private final boolean _isHot;
	private final boolean _isNew;
	private final int _locationId;

	private final long _startTimeSale;
	private final long _endTimeSale;

	private final boolean _onSale;

	private final List<ProductItemComponent> _components = new ArrayList<ProductItemComponent>();

	public ProductItem(int id, int category, int price, int silverCoinCount, int goldCoinCount, int minVipLevel, int maxVipLevel, int limit, String limitRefreshPattern, boolean isHot, boolean isNew, long startTimeSale, long endTimeSale, boolean onSale, int locationId)
	{
		_id = id;
		_category = category;
		_price = price;
		_silverCoinCount = silverCoinCount;
		_goldCoinCount = goldCoinCount;
		_minVipLevel = minVipLevel;
		_maxVipLevel = maxVipLevel;
		_limit = limit;
		_limitRefreshPattern = limitRefreshPattern == null ? DEFAULT_LIMIT_REFRESH_PATTERN : (limitRefreshPattern.equals("-1") ? null : new SchedulingPattern(limitRefreshPattern));
		_isHot = isHot;
		_isNew = isNew;
		_onSale = onSale;
		_startTimeSale = startTimeSale > 0 ? startTimeSale : NOT_LIMITED_START_TIME;
		_endTimeSale = endTimeSale > 0 ? endTimeSale : NOT_LIMITED_END_TIME;
		_locationId = locationId;
	}

	public void addComponent(ProductItemComponent component)
	{
		_components.add(component);
	}

	public List<ProductItemComponent> getComponents()
	{
		return _components;
	}

	public int getId()
	{
		return _id;
	}

	public int getCategory()
	{
		return _category;
	}

	public int getPrice()
	{
		return _price;
	}

	public int getSilverCoinCount()
	{
		return _silverCoinCount;
	}

	public int getGoldCoinCount()
	{
		return _goldCoinCount;
	}

	public int getMinVipLevel()
	{
		return _minVipLevel;
	}

	public int getMaxVipLevel()
	{
		return _maxVipLevel;
	}

	public int getLimit()
	{
		return _limit;
	}

	public SchedulingPattern getLimitRefreshPattern()
	{
		return _limitRefreshPattern;
	}

	public boolean isHot()
	{
		return _isHot;
	}

	public boolean isNew()
	{
		return _isNew;
	}

	public int getLocationId()
	{
		return _locationId;
	}

	public long getStartTimeSale()
	{
		return _startTimeSale;
	}

	public long getEndTimeSale()
	{
		return _endTimeSale;
	}

	public boolean isOnSale()
	{
		return _onSale && (_limit == -1 || _limit > 0);
	}

	@Override
	public int compareTo(ProductItem o)
	{
		return getId() - o.getId();
	}
}