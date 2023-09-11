package l2s.gameserver.botscript;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import l2s.gameserver.botscript.bypasshandler.BotBuffManager;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.IBotConfigDAO;
import l2s.gameserver.model.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;

public class BotConfigDAO implements IBotConfigDAO
{
	private static final BotConfigDAO instance = new BotConfigDAO();

	@Override
	public void update(Player player)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(player);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String strings = gson.toJson((Object) config);
		try
		{
			Files.write(Paths.get("data/bot_settings/" + player.getObjectId() + ".dat", new String[0]), strings.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void restore(Player player)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<BotConfigImp>()
		{
		}.getType();
		try
		{
			File file = new File("data/bot_settings/" + player.getObjectId() + ".dat");
			BotConfig config = null;
			if(file.exists())
			{
				config = (BotConfig) gson.fromJson(new JsonReader((Reader) new FileReader(file)), type);
			}
			if(config == null)
			{
				config = new BotConfigImp();
			}
			BotBuffManager.checkBuffConfig((BotConfigImp)config);
			BotEngine.getInstance().getConfigs().put(player.getObjectId(), config);
		}
		catch(JsonIOException | JsonSyntaxException | FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static BotConfigDAO getInstance()
	{
		return instance;
	}
}