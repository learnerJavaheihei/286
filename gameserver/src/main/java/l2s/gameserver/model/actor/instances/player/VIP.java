package l2s.gameserver.model.actor.instances.player;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.AccountVariablesDAO;
import l2s.gameserver.data.xml.holder.VIPDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ReciveVipInfo;
import l2s.gameserver.templates.VIPTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public class VIP
{
	private static final Logger _log = LoggerFactory.getLogger(DailyMissionList.class);

	private static final String VIP_POINTS_VAR = "@vip_points";
	private static final String VIP_CONSUMED_POINTS_VAR = "@vip_consumed_points";
	private static final String VIP_CONSUME_START_TIME_VAR = "@vip_consume_start_time";

	private final Player _owner;

	private long _points = 0L;
	private VIPTemplate _vipTemplate = VIPTemplate.DEFAULT_VIP_TEMPLATE;
	private long _totalConsumedPoints = 0L;
	private long _pointsConsumeStartTime = 0L;
	private ScheduledFuture<?> _consumePointsTask = null;

	public VIP(Player owner)
	{
		_owner = owner;
	}

	public void restore()
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return;

		setPoints(Long.parseLong(AccountVariablesDAO.getInstance().select(_owner.getAccountName(), VIP_POINTS_VAR, "0")));
		setTotalConsumedPoints(Long.parseLong(AccountVariablesDAO.getInstance().select(_owner.getAccountName(), VIP_CONSUMED_POINTS_VAR, "0")));
		setPointsConsumeStartTime(Long.parseLong(AccountVariablesDAO.getInstance().select(_owner.getAccountName(), VIP_CONSUME_START_TIME_VAR, "0")));
		checkTemplate(true);
	}

	public synchronized void startTask()
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return;

		stopTask();

		long consumeCount = getPointsConsumeCount();
		long delay = getPointsConsumeDelay(TimeUnit.MILLISECONDS);
		long startTime = getPointsConsumeStartTime();
		long leftTime = (delay + startTime) - System.currentTimeMillis();
		if(consumeCount > 0 && delay > 0)
		{
			if(startTime > 0)
			{
				boolean update = false;

				while(leftTime <= 0)
				{
					update = true;

					setPoints(getPoints() - consumeCount);
					setTotalConsumedPoints(getTotalConsumedPoints() + consumeCount);
					checkTemplate(false);

					startTime += delay;

					consumeCount = getPointsConsumeCount();
					if(consumeCount <= 0)
						break;

					delay = getPointsConsumeDelay(TimeUnit.MILLISECONDS);
					if(delay <= 0)
						break;

					leftTime = (delay + startTime) - System.currentTimeMillis();
				}

				if(update)
				{
					AccountVariablesDAO.getInstance().insert(_owner.getAccountName(), VIP_POINTS_VAR, String.valueOf(getPoints()));
					AccountVariablesDAO.getInstance().insert(_owner.getAccountName(), VIP_CONSUMED_POINTS_VAR, String.valueOf(getTotalConsumedPoints()));

					setPointsConsumeStartTime(startTime);
					AccountVariablesDAO.getInstance().insert(_owner.getAccountName(), VIP_CONSUME_START_TIME_VAR, String.valueOf(startTime));

					_owner.sendPacket(new ReciveVipInfo(_owner));
				}
			}
			else
			{
				startTime = System.currentTimeMillis();
				leftTime = delay;

				setPointsConsumeStartTime(startTime);
				AccountVariablesDAO.getInstance().insert(_owner.getAccountName(), VIP_CONSUME_START_TIME_VAR, String.valueOf(startTime));

				_owner.sendPacket(new ReciveVipInfo(_owner));
			}
		}

		if(consumeCount <= 0 || delay <= 0 || leftTime <= 0)
		{
			if(startTime > 0)
			{
				startTime = 0L;
				setPointsConsumeStartTime(startTime);
				AccountVariablesDAO.getInstance().delete(_owner.getAccountName(), VIP_CONSUME_START_TIME_VAR);
			}
			return;
		}

		_consumePointsTask = ThreadPoolManager.getInstance().schedule(() -> startTask(), leftTime + 1000L);
	}

	public void stopTask()
	{
		if(_consumePointsTask != null)
		{
			_consumePointsTask.cancel(false);
			_consumePointsTask = null;
		}
	}

	public long getPoints()
	{
		return _points;
	}

	private void setPoints(long value)
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return;

		_points = Math.max(0L, value);
	}

	public synchronized void addPoints(long value)
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return;

		if(value <= 0)
			return;

		setPoints(getPoints() + value);
		AccountVariablesDAO.getInstance().insert(_owner.getAccountName(), VIP_POINTS_VAR, String.valueOf(getPoints()));

		if(checkTemplate(false))
			startTask();
	}

	private synchronized boolean checkTemplate(boolean onRestore)
	{
		VIPTemplate newTemplate = VIPDataHolder.getInstance().getVIPTemplateByPoints(getPoints());
		if(!onRestore)
		{
			if(newTemplate == _vipTemplate)
				return false;

			_vipTemplate.onRemove(_owner);
		}
		_vipTemplate = newTemplate;
		_vipTemplate.onAdd(_owner);
		return true;
	}

	public VIPTemplate getTemplate()
	{
		return _vipTemplate;
	}

	public long getTotalConsumedPoints()
	{
		return _totalConsumedPoints;
	}

	public void setTotalConsumedPoints(long value)
	{
		_totalConsumedPoints = value;
	}

	public long getPointsConsumeStartTime()
	{
		return _pointsConsumeStartTime;
	}

	public void setPointsConsumeStartTime(long value)
	{
		_pointsConsumeStartTime = value;
	}

	public long getPointsConsumeLeftTime(TimeUnit timeUnit)
	{
		long delay = getPointsConsumeDelay(TimeUnit.MILLISECONDS);
		if(delay > 0)
			return timeUnit.convert((delay + getPointsConsumeStartTime()) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		return 0L;
	}

	public double getPointsRefillPercent()
	{
		return _vipTemplate.getPointsRefillPercent();
	}

	public long getPointsConsumeCount()
	{
		return _vipTemplate.getPointsConsumeCount();
	}

	public long getPointsConsumeDelay(TimeUnit timeUnit)
	{
		return _vipTemplate.getPointsConsumeDelay(timeUnit);
	}

	public int getLevel()
	{
		return _vipTemplate.getLevel();
	}

	@Override
	public String toString()
	{
		return "VIP[owner=" + _owner.getName() + "]";
	}
}
