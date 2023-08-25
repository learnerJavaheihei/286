package l2s.gameserver.data.string;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.Files;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.SkillUtils;

/**
 * Author: VISTALL
 * Date:  19:27/29.12.2010
 */
public final class SkillNameHolder extends AbstractHolder
{
	private static final Pattern LINE_PATTERN = Pattern.compile("^([0-9]+)\\t([0-9]+)\\t(.*?)$");

	private static final SkillNameHolder _instance = new SkillNameHolder();

	private final Map<Language, TIntObjectMap<String>> _skillNames = new HashMap<Language, TIntObjectMap<String>>();

	public static SkillNameHolder getInstance()
	{
		return _instance;
	}

	private SkillNameHolder()
	{
		//
	}

	public String getSkillName(Language lang, int hashCode)
	{
		TIntObjectMap<String> skillNames = _skillNames.get(lang);
		String name = skillNames.get(hashCode);
		if(name == null)
		{
			Language secondLang = lang;
			do
			{
				if(secondLang == secondLang.getSecondLanguage())
					break;

				secondLang = secondLang.getSecondLanguage();
				skillNames = _skillNames.get(secondLang);
				name = skillNames.get(hashCode);
			}
			while(name == null);

			if(name == null)
			{
				for(Language l : Language.VALUES)
				{
					skillNames = _skillNames.get(secondLang);
					if((name = skillNames.get(hashCode)) != null)
						break;
				}
			}
		}
		return name;
	}

	public String getSkillName(Player player, int hashCode)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getSkillName(lang, hashCode);
	}

	public String getSkillName(Language lang, Skill skill)
	{
		return getSkillName(lang, skill.hashCode());
	}

	public String getSkillName(Player player, Skill skill)
	{
		return getSkillName(player, skill.hashCode());
	}

	public String getSkillName(Language lang, int id, int level)
	{
		return getSkillName(lang, SkillHolder.getInstance().getHashCode(id, level));
	}

	public String getSkillName(Player player, int id, int level)
	{
		return getSkillName(player, SkillHolder.getInstance().getHashCode(id, level));
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_skillNames.put(lang, new TIntObjectHashMap<String>());

			if(!Config.AVAILABLE_LANGUAGES.contains(lang))
				continue;

			File file = new File(Config.DATAPACK_ROOT, "data/string/skillname/" + lang.getShortName() + ".txt");
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
							int level = Integer.parseInt(m.group(2));
							int hashCode = SkillHolder.getInstance().getHashCode(id, level);
							String value = m.group(3);

							_skillNames.get(lang).put(hashCode, value);
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
		for(Map.Entry<Language, TIntObjectMap<String>> entry : _skillNames.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
			info("load skill names: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _skillNames.size();
	}

	@Override
	public void clear()
	{
		_skillNames.clear();
	}
}
