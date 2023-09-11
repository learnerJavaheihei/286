import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.BotProperties;
import l2s.gameserver.core.BotSkillStrategy;
import l2s.gameserver.listener.actor.OnMagicUseListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.actor.listener.PlayerListenerList;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

public class BotLoader implements OnInitScriptListener
{

	OnPlayerEnterListener enterListener = new OnPlayerEnterListener()
	{

		@Override
		public void onPlayerEnter(Player player)
		{

			try
			{
				Field field = BotEngine.class.getDeclaredField("configs");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")

				Map<Integer, BotConfig> configs = (Map<Integer, BotConfig>) field.get(BotEngine.getInstance());
				int obj_id = player.getObjectId();
				configs.remove(obj_id);
				BotConfigImp config = null;
				File config_file = new File("bot_config/" + obj_id + ".gson");
				if(config_file.exists())
				{
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					Type type = new TypeToken<BotConfigImp>()
					{
					}.getType();
					config = gson.fromJson(new FileReader(config_file), type);
				}
				else
				{
					config = new BotConfigImp();
				}

				configs.put(obj_id, config);
			}
			catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException
					| JsonIOException | JsonSyntaxException | FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	};

	OnMagicUseListener listener = new OnMagicUseListener()
	{
		@Override
		public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
		{
			if(actor.isPlayer() && target.isMonster())
			{
				BotConfig config = BotEngine.getInstance().getBotConfig(actor.getPlayer());
				for(BotSkillStrategy skillStrategy : config.getAttackStrategy())
				{
					if(skillStrategy.getSkillId() == skill.getId() && skillStrategy.isOneTime())
						skillStrategy.setLastTargetObjectId(target.getObjectId());
				}
			}
		}
	};

	OnPlayerExitListener exitListener = new OnPlayerExitListener()
	{
		@Override
		public void onPlayerExit(Player player)
		{
			BotConfig config = BotEngine.getInstance().getBotConfig(player);
			Gson gson = new Gson();
			String strings = gson.toJson(config);
			Writer w;
			try
			{
				w = new FileWriter("bot_config/" + player.getObjectId() + ".gson", false);
				w.write(strings);
				w.flush();
				w.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onInit()
	{
		try
		{
			BotProperties.load();
			CharListenerList.addGlobal(listener);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		BotEngine.getInstance().init();

		File file = new File("bot_config");
		if(!file.exists())
			file.mkdir();

		PlayerListenerList.addGlobal(enterListener);
		PlayerListenerList.addGlobal(exitListener);
	}
}
