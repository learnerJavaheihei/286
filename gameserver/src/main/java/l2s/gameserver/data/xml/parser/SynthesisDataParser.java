package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.templates.item.data.ItemData;
import org.dom4j.Element;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SynthesisDataHolder;
import l2s.gameserver.templates.item.support.SynthesisData;

/**
 * @author Bonux
**/
public class SynthesisDataParser extends AbstractParser<SynthesisDataHolder>
{
	private static SynthesisDataParser _instance = new SynthesisDataParser();

	public static SynthesisDataParser getInstance()
	{
		return _instance;
	}

	private SynthesisDataParser()
	{
		super(SynthesisDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/synthesis_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "synthesis_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("synthesis"); iterator.hasNext();)
		{
			Element synthesisElement = iterator.next();

			int itemId1 = parseInt(synthesisElement, "item_1_id");
			int itemId2 = parseInt(synthesisElement, "item_2_id");
			double chance = parseDouble(synthesisElement, "chance");
			int[] successItemInfo = StringArrayUtils.stringToIntArray(parseString(synthesisElement, "success_item"), "-");
			ItemData successItemData = new ItemData(successItemInfo[0], successItemInfo.length > 1 ? successItemInfo[1] : 1);
			int[] failItemInfo = StringArrayUtils.stringToIntArray(parseString(synthesisElement, "fail_item"), "-");
			ItemData failItemData = new ItemData(failItemInfo[0], failItemInfo.length > 1 ? failItemInfo[1] : 1);
			int resultEffecttype = parseInt(synthesisElement, "result_effecttype", 0);
			int[] locationIds = StringArrayUtils.stringToIntArray(parseString(synthesisElement, "location_id", "-1"), ";");

			getHolder().addData(new SynthesisData(itemId1, itemId2, chance, successItemData, failItemData, resultEffecttype, locationIds));
		}
	}
}
