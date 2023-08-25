package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CostumesHolder;
import l2s.gameserver.templates.CostumeTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class CostumesParser extends AbstractParser<CostumesHolder> {
	private static final CostumesParser INSTANCE = new CostumesParser();

	public static CostumesParser getInstance() {
		return INSTANCE;
	}

	private CostumesParser() {
		super(CostumesHolder.getInstance());
	}

	@Override
	public File getXMLPath() {
		return new File(Config.DATAPACK_ROOT, "data/costumes.xml");
	}

	@Override
	public String getDTDFileName() {
		return "costumes.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception {
		for (Iterator<Element> iterator = rootElement.elementIterator("costume"); iterator.hasNext(); ) {
			Element firstElement = iterator.next();
			int id = parseInt(firstElement, "id");
			int skillId = parseInt(firstElement, "skill_id");
			int skillLevel = parseInt(firstElement, "skill_level", 1);
			int castItemId = parseInt(firstElement, "cast_item_id");
			long castItemCount = parseLong(firstElement, "cast_item_count");
			int evolutionCostumeId = parseInt(firstElement, "evolution_costume_id", -1);
			int evolutionMod = parseInt(firstElement, "evolution_mod");
			int grade = parseInt(firstElement, "grade");
			int locationId = parseInt(firstElement, "location_id", -1);
			CostumeTemplate template = new CostumeTemplate(id, skillId, skillLevel, castItemId, castItemCount, evolutionCostumeId, evolutionMod, grade, locationId);

			Element extractElement = firstElement.element("extract");
			if (extractElement != null) {
				int extractItemId = parseInt(extractElement, "item_id");
				long extractItemCount = parseLong(extractElement, "item_count");
				CostumeTemplate.ExtractData extractData = new CostumeTemplate.ExtractData(extractItemId, extractItemCount);
				for (Iterator<Element> thirdIterator = extractElement.elementIterator("fee"); thirdIterator.hasNext(); ) {
					Element thirdElement = thirdIterator.next();
					for (Iterator<Element> fourthIterator = thirdElement.elementIterator("item"); fourthIterator.hasNext(); ) {
						Element fourthElement = fourthIterator.next();
						int itemId = parseInt(fourthElement, "id");
						long itemCount = parseLong(fourthElement, "count");
						extractData.addFee(new ItemData(itemId, itemCount));
					}
				}
				template.setExtractData(extractData);
			}

			getHolder().addCostume(template);
		}
	}
}
