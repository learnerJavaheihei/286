package l2s.gameserver.templates;

import java.util.concurrent.TimeUnit;

import l2s.gameserver.templates.StatsSet;

/**
 * @author Bonux
**/
public class VIPTemplate extends PremiumAccountTemplate
{
	public static final VIPTemplate DEFAULT_VIP_TEMPLATE = new VIPTemplate(0, 0L, 0D, 0L, 0, StatsSet.EMPTY);

	private final int _level;
	private final long _points;

	private double _pointsRefillPercent;
	private long _pointsConsumeCount;
	private int _pointsConsumeDelay;

	public VIPTemplate(int level, long points, double pointsRefillPercent, long pointsConsumeCount, int pointsConsumeDelay, StatsSet set)
	{
		super((100000000 + level), set);

		_level = level;
		_points = points;

		setPointsRefillPercent(pointsRefillPercent);
		setPointsConsumeCount(pointsConsumeCount);
		setPointsConsumeDelay(pointsConsumeDelay);
	}

	public int getLevel()
	{
		return _level;
	}

	public long getPoints()
	{
		return _points;
	}

	public double getPointsRefillPercent()
	{
		return _pointsRefillPercent;
	}

	public void setPointsRefillPercent(double value)
	{
		_pointsRefillPercent = value;
	}

	public long getPointsConsumeCount()
	{
		return _pointsConsumeCount;
	}

	public void setPointsConsumeCount(long value)
	{
		_pointsConsumeCount = value;
	}

	public long getPointsConsumeDelay(TimeUnit timeUnit)
	{
		return timeUnit.convert(_pointsConsumeDelay, TimeUnit.HOURS);
	}

	public void setPointsConsumeDelay(int value)
	{
		_pointsConsumeDelay = value;
	}
}