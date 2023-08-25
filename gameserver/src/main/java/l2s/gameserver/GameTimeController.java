package l2s.gameserver;

import java.util.Calendar;

import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.listener.GameListener;
import l2s.gameserver.listener.game.OnDayNightChangeListener;
import l2s.gameserver.listener.game.OnGameHourChangeListener;
import l2s.gameserver.listener.game.OnStartListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ClientSetTimePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameTimeController
{
	private class OnStartListenerImpl implements OnStartListener
	{
		@Override
		public void onStart()
		{
			ThreadPoolManager.getInstance().execute(new GameHourlyTask(true));
		}
	}

	private class GameHourlyTask implements Runnable
	{
		private final boolean _onStart;

		public GameHourlyTask(boolean onStart)
		{
			_onStart = onStart;
		}

		@Override
		public void run()
		{
			final int hour = getGameHour();

			getInstance().getListenerEngine().onChangeHour(hour, _onStart);

			boolean dayTimeChanged = false;
			if(_onStart)
			{
				if(isNowNight(hour))
					getInstance().getListenerEngine().onNight(_onStart);
				else
					getInstance().getListenerEngine().onDay(_onStart);

				dayTimeChanged = true;
			}
			else
			{
				if(hour == NIGHT_START_HOUR)
				{
					getInstance().getListenerEngine().onNight(_onStart);
					dayTimeChanged = true;
				}
				else if(hour == DAY_START_HOUR)
				{
					getInstance().getListenerEngine().onDay(_onStart);
					dayTimeChanged = true;
				}
			}

			if(dayTimeChanged)
			{
				ClientSetTimePacket packet = new ClientSetTimePacket();
				for(Player player : GameObjectsStorage.getPlayers(false, false))
				{
					player.checkDayNightMessages();
					player.sendPacket(packet);
				}
			}
		}
	}

	private class GameTimeListenerList extends ListenerList<GameServer>
	{
		public void onChangeHour(int hour, boolean onStart)
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnGameHourChangeListener.class.isInstance(listener))
					((OnGameHourChangeListener) listener).onChangeHour(hour, onStart);
		}

		public void onDay(boolean onStart)
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnDayNightChangeListener.class.isInstance(listener))
					((OnDayNightChangeListener) listener).onDay(onStart);
		}

		public void onNight(boolean onStart)
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnDayNightChangeListener.class.isInstance(listener))
					((OnDayNightChangeListener) listener).onNight(onStart);
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(GameTimeController.class);

	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	public static final int NIGHT_START_HOUR = 0;
	public static final int DAY_START_HOUR = 6;

	private static final GameTimeController _instance = new GameTimeController();

	private long _gameStartTime;

	private GameTimeListenerList listenerEngine = new GameTimeListenerList();

	public static final GameTimeController getInstance()
	{
		return _instance;
	}

	private GameTimeController()
	{
		_gameStartTime = generateDayStartTime();

		GameServer.getInstance().addListener(new OnStartListenerImpl());

		StringBuilder msg = new StringBuilder();
		msg.append("GameTimeController: initialized.").append(" ");
		msg.append("Current time is ");
		msg.append(getGameHour()).append(":");
		if(getGameMin() < 10)
			msg.append("0");
		msg.append(getGameMin());
		msg.append(" in the ");
		if(isNowNight())
			msg.append("night");
		else
			msg.append("day");
		msg.append(".");

		_log.info(msg.toString());

		final long oneGameHourInMillis = 60 * TICKS_PER_SECOND * 1000L;

		long hourStart = 0;

		while(_gameStartTime + hourStart < System.currentTimeMillis())
			hourStart += oneGameHourInMillis;

		hourStart -= System.currentTimeMillis() - _gameStartTime;

		GameHourlyTask gameHourlyTask = new GameHourlyTask(false);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(gameHourlyTask, hourStart, oneGameHourInMillis);
	}

	/**
	 * Вычисляем смещение до начала игровых суток
	 * 
	 * @return смещение в миллисекнду до начала игровых суток (6:00AM)
	 */
	private static long generateDayStartTime()
	{
		Calendar dayStart = Calendar.getInstance();

		int HOUR_OF_DAY = dayStart.get(Calendar.HOUR_OF_DAY);

		dayStart.add(Calendar.HOUR_OF_DAY, -(HOUR_OF_DAY + 1) % 4); //1 день в игре это 4 часа реального времени
		dayStart.set(Calendar.MINUTE, 0);
		dayStart.set(Calendar.SECOND, 0);
		dayStart.set(Calendar.MILLISECOND, 0);

		return dayStart.getTimeInMillis();
	}

	public long getDayStartTime()
	{
		return _gameStartTime;
	}

	public static boolean isNowNight(int hour)
	{
		return hour >= NIGHT_START_HOUR && hour < DAY_START_HOUR;
	}

	public boolean isNowNight()
	{
		return isNowNight(getGameHour());
	}

	public int getGameTime()
	{
		return getGameTicks() / MILLIS_IN_TICK;
	}

	public int getGameHour()
	{
		return getGameTime() / 60 % 24;
	}

	public int getGameMin()
	{
		return getGameTime() % 60;
	}

	public int getGameTicks()
	{
		return (int) ((System.currentTimeMillis() - _gameStartTime) / MILLIS_IN_TICK);
	}

	public GameTimeListenerList getListenerEngine()
	{
		return listenerEngine;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return listenerEngine.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return listenerEngine.remove(listener);
	}
}