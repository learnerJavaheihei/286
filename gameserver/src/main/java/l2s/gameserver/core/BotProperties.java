package l2s.gameserver.core;

import l2s.commons.configuration.ExProperties;
import l2s.gameserver.Config;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class BotProperties {
	public static final String BOT_NEXT_BUY_TIME_KEY = "@BotNextBuyTime";
	public static final String GLOBAL_CONFIG_FILE = "config/bot.properties";
	public static Pair<Integer, Integer> NOT_ALLOW_TIME;
	public static double NOT_ALLOW_TIME_PENALTY;
	public static int MAX_SEARCH_RANGE;
	public static int[] BUFF_ITEM_IDS;
	public static boolean BOT_FREE_TO_USE;
	public static boolean ONLY_VIP;

	public static void load() throws IOException
	{
		ExProperties settings = Config.load(GLOBAL_CONFIG_FILE);
		MAX_SEARCH_RANGE = settings.getProperty("MaxSearchRange", 3500);
		BUFF_ITEM_IDS = settings.getProperty("BuffItemIds", new int[0]);
		ONLY_VIP = settings.getProperty("OnlyVip", true);
	}
}