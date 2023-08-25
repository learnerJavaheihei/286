package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.InitialShortCutsHolder;
import l2s.gameserver.model.actor.instances.player.Macro;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.Race;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 15.05.2019
 * Developed for L2-Scripts.com
 **/
public final class InitialShortCutsParser extends AbstractParser<InitialShortCutsHolder> {
	private static InitialShortCutsParser _instance = new InitialShortCutsParser();

	public static InitialShortCutsParser getInstance() {
		return _instance;
	}

	protected InitialShortCutsParser() {
		super(InitialShortCutsHolder.getInstance());
	}

	@Override
	public File getXMLPath() {
		return new File(Config.DATAPACK_ROOT, "data/pc_parameters/initial_shortcuts.xml");
	}

	@Override
	public String getDTDFileName() {
		return "initial_shortcuts.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception {
		for (Iterator<Element> iterator = rootElement.elementIterator("shortcuts"); iterator.hasNext(); ) {
			Element element = iterator.next();

			String raceStr = parseString(element, "race", null);
			String typeStr = parseString(element, "type", null);

			Race race = StringUtils.isEmpty(raceStr) ? null : Race.valueOf(raceStr.toUpperCase());
			ClassType type = StringUtils.isEmpty(raceStr) ? null : ClassType.valueOf(typeStr.toUpperCase());

			for (Iterator<Element> iterator2 = element.elementIterator("page"); iterator2.hasNext(); ) {
				Element element2 = iterator2.next();

				int pageId = parseInt(element2, "id");

				for (Iterator<Element> iterator3 = element2.elementIterator("shortcut"); iterator3.hasNext(); ) {
					Element element3 = iterator3.next();

					int shortCutSlot = parseInt(element3, "slot");
					ShortCut.ShortCutType shortCutType = ShortCut.ShortCutType.valueOf(parseString(element3, "type").toUpperCase());
					int shortCutId = parseInt(element3, "id");
					int shortCutLevel = parseInt(element3, "level", 0);

					ShortCut shortCut = new ShortCut(shortCutSlot, pageId, shortCutType, shortCutId, shortCutLevel, 1);

					getHolder().addInitialShortCut(race, type, shortCut);
				}
			}
		}

		for (Iterator<Element> iterator = rootElement.elementIterator("macroses"); iterator.hasNext(); ) {
			Element element = iterator.next();

			for (Iterator<Element> iterator2 = element.elementIterator("macro"); iterator2.hasNext(); ) {
				Element element2 = iterator2.next();

				int id = parseInt(element2, "id");
				int icon = parseInt(element2, "icon");

				String name = parseString(element2, "name");
				if(StringUtils.isEmpty(name))
				{
					warn("Macro ID[" + id + "] dont have name!");
					continue;
				}

				String description = parseString(element2, "description", "");
				if(description.length() > 32)
				{
					warn("Macro ID[" + id + "] description cannot contain more than 32 characters!");
					continue;
				}

				String acronym = parseString(element2, "acronym", "");
				boolean enabled = parseBoolean(element2, "enabled", true);
				List<Macro.L2MacroCmd> commands = new ArrayList<>();

				for (Iterator<Element> iterator3 = element2.elementIterator("command"); iterator3.hasNext(); ) {
					Element element3 = iterator3.next();

					Macro.MacroCmdType cmdType = Macro.MacroCmdType.valueOf(parseString(element3, "type").toUpperCase());
					int param1 = 0;
					int param2 = 0;
					String cmd = "";
					if (cmdType == Macro.MacroCmdType.SKILL) {
						param1 = parseInt(element3, "id");
						param2 = parseInt(element3, "level");
					} else if (cmdType == Macro.MacroCmdType.DELAY) {
						param1 = parseInt(element3, "delay");
					} else if (cmdType == Macro.MacroCmdType.TEXT) {
						cmd = element3.getTextTrim();
					} else if (cmdType == Macro.MacroCmdType.SHORTCUT) {
						param1 = parseInt(element3, "page");
						param2 = parseInt(element3, "slot");
					}
					commands.add(new Macro.L2MacroCmd(commands.size(), cmdType.ordinal(), param1, param2, cmd));
				}

				getHolder().addInitialMacro(new Macro(id, icon, name, description, acronym, commands.toArray(new Macro.L2MacroCmd[commands.size()]), enabled));
			}
		}
	}
}
