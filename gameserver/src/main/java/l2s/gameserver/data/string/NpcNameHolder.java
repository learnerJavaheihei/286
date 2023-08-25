package l2s.gameserver.data.string;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Files;
import l2s.gameserver.utils.Language;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: Bonux
 */
public final class NpcNameHolder extends AbstractHolder {
	private static final Pattern LINE_PATTERN = Pattern.compile("^([0-9]+)\\t(.*?)$");

	private static final NpcNameHolder INSTANCE = new NpcNameHolder();

	public static NpcNameHolder getInstance() {
		return INSTANCE;
	}

	private final Map<Language, Map<Integer, String>> npcNames = new HashMap<>();

	private NpcNameHolder() {
		//
	}

	public String getNpcName(Language lang, int npcId) {
		Map<Integer, String> names = npcNames.get(lang);
		String name = names.get(npcId);
		if (name == null) {
			Language secondLang = lang;
			do {
				if (secondLang == secondLang.getSecondLanguage())
					break;

				secondLang = secondLang.getSecondLanguage();
				names = npcNames.get(secondLang);
				name = names.get(npcId);
			}
			while (name == null);

			if (name == null) {
				for (Language l : Language.VALUES) {
					names = npcNames.get(secondLang);
					if ((name = names.get(npcId)) != null)
						break;
				}
			}
		}
		return name;
	}

	public String getNpcName(Player player, int npcId) {
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getNpcName(lang, npcId);
	}

	public void load() {
		for (Language lang : Language.VALUES) {
			npcNames.put(lang, new HashMap<>());

			if (!Config.AVAILABLE_LANGUAGES.contains(lang))
				continue;

			File file = new File(Config.DATAPACK_ROOT, "data/string/npcname/" + lang.getShortName() + ".txt");
			if (!file.exists()) {
				if (!lang.isCustom())
					warn("Not find file: " + file.getAbsolutePath());
			} else {
				Scanner scanner = null;
				try {
					String content = Files.readFile(file);
					scanner = new Scanner(content);
					int i = 0;
					String line;
					while (scanner.hasNextLine()) {
						i++;
						line = scanner.nextLine();
						if (line.startsWith("#"))
							continue;

						Matcher m = LINE_PATTERN.matcher(line);
						if (m.find()) {
							int id = Integer.parseInt(m.group(1));
							String value = m.group(2);

							npcNames.get(lang).put(id, value);
						} else
							error("Error on line #: " + i + "; file: " + file.getName());
					}
				} catch (Exception e) {
					error("Exception: " + e, e);
				} finally {
					try {
						scanner.close();
					} catch (Exception e) {
						//
					}
				}
			}
		}

		log();
	}

	public void reload() {
		clear();
		load();
	}

	@Override
	public void log() {
		for (Map.Entry<Language, Map<Integer, String>> entry : npcNames.entrySet()) {
			if (!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
			info("load npc names: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size() {
		return npcNames.size();
	}

	@Override
	public void clear() {
		npcNames.clear();
	}
}
