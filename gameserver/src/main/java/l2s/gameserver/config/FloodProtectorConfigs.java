package l2s.gameserver.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import l2s.commons.configuration.ExProperties;
import l2s.gameserver.Config;

/**
 * @author Bonux
 */
public final class FloodProtectorConfigs
{
	public static final String FLOOD_PROTECTOR_FILE = "config/flood_protector.properties";

	public static final List<FloodProtectorConfig> FLOOD_PROTECTORS = new ArrayList<FloodProtectorConfig>();

	public static void load()
	{
		final ExProperties floodProtectors = Config.load(FLOOD_PROTECTOR_FILE);

		String[] floodProtectorTypes = floodProtectors.getProperty("FLOOD_PROTECTORS_TYPES", "").split(";");
		for(String type : floodProtectorTypes)
		{
			if(StringUtils.isEmpty(type))
				continue;

			FloodProtectorConfig floodProtector = FloodProtectorConfig.load(type, floodProtectors);
			if(floodProtector == null)
				continue;

			FLOOD_PROTECTORS.add(floodProtector);
		}
	}
}
