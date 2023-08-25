package l2s.gameserver.network.l2;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.gameserver.config.FloodProtectorConfig;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import l2s.gameserver.network.l2.GameClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flood protector implementation.
 * @author fordfrog
 */
public final class FloodProtector
{
	private static final Logger LOGGER = LoggerFactory.getLogger("floodprotector");
	
	/**
	 * Client for this instance of flood protector.
	 */
	private final GameClient _client;
	/**
	 * Configuration of this instance of flood protector.
	 */
	private final FloodProtectorConfig _config;
	/**
	 * Next game tick when new request is allowed.
	 */
	private volatile long _nextTime = System.currentTimeMillis();
	/**
	 * Request counter.
	 */
	private final AtomicInteger _count = new AtomicInteger(0);
	/**
	 * Flag determining whether exceeding request has been logged.
	 */
	private boolean _logged;
	/**
	 * Flag determining whether punishment application is in progress so that we do not apply punisment multiple times (flooding).
	 */
	private volatile boolean _punishmentInProgress;
	
	/**
	 * Creates new instance of FloodProtector.
	 * @param client the game client for which flood protection is being created
	 * @param config flood protector configuration
	 */
	public FloodProtector(final GameClient client, final FloodProtectorConfig config)
	{
		_client = client;
		_config = config;
	}
	
	/**
	 * Checks whether the request is flood protected or not.
	 * @param command command issued or short command description
	 * @return true if action is allowed, otherwise false
	 */
	public boolean tryPerformAction(final String command)
	{
		final long curTime = System.currentTimeMillis();
		
		if((_client.getActiveChar() != null) && _client.getActiveChar().getPlayerAccess().CanIgnoreFloodProtector)
		{
			return true;
		}
		
		if((curTime < _nextTime) || _punishmentInProgress)
		{
			if(_config.LOG_FLOODING && !_logged)
			{
				log("called command ", command, " ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL - (_nextTime - curTime)), " ms after previous command");
				_logged = true;
			}
			
			_count.incrementAndGet();
			
			if(!_punishmentInProgress && (_config.PUNISHMENT_LIMIT > 0) && (_count.get() >= _config.PUNISHMENT_LIMIT) && (_config.PUNISHMENT_TYPE != null))
			{
				_punishmentInProgress = true;
				if("kick".equals(_config.PUNISHMENT_TYPE))
				{
					kickPlayer();
				}
				else if("ban".equals(_config.PUNISHMENT_TYPE))
				{
					banAccount();
				}
				else if("jail".equals(_config.PUNISHMENT_TYPE))
				{
					jailChar();
				}
				_punishmentInProgress = false;
			}
			return false;
		}
		
		if(_count.get() > 0)
		{
			if(_config.LOG_FLOODING)
			{
				log("issued ", String.valueOf(_count), " extra requests within ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL), " ms");
			}
		}
		
		_nextTime = curTime + _config.FLOOD_PROTECTION_INTERVAL;
		_logged = false;
		_count.set(0);
		return true;
	}
	
	/**
	 * Kick player from game (close network connection).
	 */
	private void kickPlayer()
	{
		Player player = _client.getActiveChar();
		if(player != null)
		{
			player.kick();
			log("kicked for flooding");
		}
	}
	
	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount()
	{
		int accessLevel = 0;
		int banExpire = 0;

		if(_config.PUNISHMENT_TIME > 0)
			banExpire = (int) ((System.currentTimeMillis() + _config.PUNISHMENT_TIME) / 1000L);
		else
			accessLevel = -100;

		AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(_client.getLogin(), accessLevel, banExpire));
		Player player = _client.getActiveChar();
		if(player != null)
			player.kick();
		log("banned for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + (_config.PUNISHMENT_TIME / 60000) + " mins");
	}
	
	/**
	 * Jails char.
	 */
	private void jailChar()
	{
		Player player = _client.getActiveChar();
		if(player != null)
		{
			player.toJail((int) (_config.PUNISHMENT_TIME / 60000));
			log("jailed for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + (_config.PUNISHMENT_TIME / 60000) + " mins");
		}
	}
	
	private void log(String... lines)
	{
		final StringBuilder output = new StringBuilder(100);
		output.append(_config.FLOOD_PROTECTOR_TYPE);
		output.append(": ");
		switch(_client.getState())
		{
			case IN_GAME:
				if(_client.getActiveChar() != null)
				{
					output.append(_client.getActiveChar().getName());
					output.append("(");
					output.append(_client.getActiveChar().getObjectId());
					output.append(") ");
				}
				break;
			case AUTHED:
				if(_client.getLogin() != null)
				{
					output.append(_client.getLogin());
					output.append(" ");
				}
				break;
			case CONNECTED:
				String address = _client.getIpAddr();
				if(address != null)
				{
					output.append(address);
					output.append(" ");
				}
				break;
			default:
				throw new IllegalStateException("Missing state on switch");
		}
		
		Arrays.stream(lines).forEach(output::append);
		
		LOGGER.info(output.toString());
	}
}