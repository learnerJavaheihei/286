package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.AgathionHolder;
import l2s.gameserver.templates.agathion.AgathionTemplate;
import l2s.gameserver.templates.cubic.CubicTargetType;
import l2s.gameserver.templates.cubic.CubicUseUpType;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

/**
 * @author Bonux
 */
public final class AgathionParser extends AbstractParser<AgathionHolder> {
	private static AgathionParser _instance = new AgathionParser();

	public static AgathionParser getInstance() {
		return _instance;
	}

	protected AgathionParser() {
		super(AgathionHolder.getInstance());
	}

	@Override
	public File getXMLPath() {
		return new File(Config.DATAPACK_ROOT, "data/agathions.xml");
	}

	@Override
	public String getDTDFileName() {
		return "agathions.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception {
		for (Iterator<Element> iterator = rootElement.elementIterator("agathion"); iterator.hasNext(); ) {
			Element element = iterator.next();
			int npc_id = parseInt(element, "npc_id");
			int id = parseInt(element, "id");
			int duration = parseInt(element, "duration", -1);
			int delay = parseInt(element, "delay", 0);
			int max_count = parseInt(element, "max_count", Integer.MAX_VALUE);
			CubicUseUpType use_up = CubicUseUpType.valueOf(parseString(element, "use_up", "INCREASE_DELAY").toUpperCase());
			double power = parseDouble(element, "power", 0.);
			CubicTargetType target_type = CubicTargetType.valueOf(parseString(element, "target_type", "BY_SKILL").toUpperCase());
			int[] item_ids = StringArrayUtils.stringToIntArray(parseString(element, "item_ids", ""), ";");
			int energy = parseInt(element, "energy", 0);
			int max_energy = parseInt(element, "max_energy", 0);

			AgathionTemplate template = new AgathionTemplate(npc_id, id, duration, delay, max_count, use_up, power, target_type, item_ids, energy, max_energy);
			CubicParser.parseSkills(this, template, element);
			getHolder().addAgathionTemplate(template);
		}
	}
}
