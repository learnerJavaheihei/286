package l2s.gameserver.config;

import l2s.commons.configuration.ExProperties;

/**
 * Flood protector configuration
 * @author fordfrog
 */
public final class FloodProtectorConfig
{
	/**
	 * Type used for identification of logging output.
	 */
	public final String FLOOD_PROTECTOR_TYPE;
	/**
	 * Flood protection interval in game ticks.
	 */
	public int FLOOD_PROTECTION_INTERVAL;
	/**
	 * Whether flooding should be logged.
	 */
	public boolean LOG_FLOODING;
	/**
	 * If specified punishment limit is exceeded, punishment is applied.
	 */
	public int PUNISHMENT_LIMIT;
	/**
	 * Punishment type. Either 'none', 'kick', 'ban' or 'jail'.
	 */
	public String PUNISHMENT_TYPE;
	/**
	 * For how long should the char/account be punished.
	 */
	public long PUNISHMENT_TIME;
	
	/**
	 * Creates new instance of FloodProtectorConfig.
	 * @param floodProtectorType {@link #FLOOD_PROTECTOR_TYPE}
	 */
	public FloodProtectorConfig(final String floodProtectorType)
	{
		FLOOD_PROTECTOR_TYPE = floodProtectorType;
	}

	public static FloodProtectorConfig load(final String type, final ExProperties properties)
	{
		FloodProtectorConfig config = new FloodProtectorConfig(type.toUpperCase());
		config.FLOOD_PROTECTION_INTERVAL = properties.getProperty(type + "_FLOOD_PROTECTION_INTERVAL", 1000);
		config.LOG_FLOODING = properties.getProperty(type + "_LOG_FLOODING", false);
		config.PUNISHMENT_LIMIT = properties.getProperty(type + "_PUNISHMENT_LIMIT", 100);
		config.PUNISHMENT_TYPE = properties.getProperty(type + "_PUNISHMENT_TYPE", "none");
		config.PUNISHMENT_TIME = properties.getProperty(type + "_PUNISHMENT_TIME", 0) * 60000;
		return config;
	}
}
