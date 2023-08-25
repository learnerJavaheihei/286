package l2s.gameserver.data.string;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Files;
import l2s.gameserver.utils.Language;

/**
 * @author: Bonux
 */
public final class NpcStringHolder extends AbstractHolder
{
	private static final Pattern LINE_PATTERN = Pattern.compile("^([0-9]+)\\t(.*?)$");

	private static final NpcStringHolder _instance = new NpcStringHolder();

	private final Map<Language, TIntObjectMap<String>> _npcStrings = new HashMap<Language, TIntObjectMap<String>>();

	public static NpcStringHolder getInstance()
	{
		return _instance;
	}

	private NpcStringHolder()
	{
		//
	}

	public String getNpcString(Language lang, int npcStringId)
	{
		TIntObjectMap<String> npcStrings = _npcStrings.get(lang);
		String name = npcStrings.get(npcStringId);
		if(name == null)
		{
			Language secondLang = lang;
			do
			{
				if(secondLang == secondLang.getSecondLanguage())
					break;

				secondLang = secondLang.getSecondLanguage();
				npcStrings = _npcStrings.get(secondLang);
				name = npcStrings.get(npcStringId);
			}
			while(name == null);

			if(name == null)
			{
				for(Language l : Language.VALUES)
				{
					npcStrings = _npcStrings.get(secondLang);
					if((name = npcStrings.get(npcStringId)) != null)
						break;
				}
			}
		}
		return name;
	}

	public String getNpcString(Player player, int npcStringId)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getNpcString(lang, npcStringId);
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_npcStrings.put(lang, new TIntObjectHashMap<String>());

			if(!Config.AVAILABLE_LANGUAGES.contains(lang))
				continue;

			File file = new File(Config.DATAPACK_ROOT, "data/string/npcstring/" + lang.getShortName() + ".txt");
			if(!file.exists())
			{
				if(!lang.isCustom())
					warn("Not find file: " + file.getAbsolutePath());
			}
			else
			{
				Scanner scanner = null;
				try
				{
					String content = Files.readFile(file);
					scanner = new Scanner(content);
					int i = 0;
					String line;
					while(scanner.hasNextLine())
					{
						i++;
						line = scanner.nextLine();
						if(line.startsWith("#"))
							continue;

						Matcher m = LINE_PATTERN.matcher(line);
						if(m.find())
						{
							int id = Integer.parseInt(m.group(1));
							String value = m.group(2);

							_npcStrings.get(lang).put(id, value);
						}
						else
							error("Error on line #: " + i + "; file: " + file.getName());
					}
				}
				catch(Exception e)
				{
					error("Exception: " + e, e);
				}
				finally
				{
					try
					{
						scanner.close();
					}
					catch(Exception e)
					{
						//
					}
				}
			}
		}

		log();
	}

	public void reload()
	{
		clear();
		load();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, TIntObjectMap<String>> entry : _npcStrings.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
			info("load npc strings: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _npcStrings.size();
	}

	@Override
	public void clear()
	{
		_npcStrings.clear();
	}
}
