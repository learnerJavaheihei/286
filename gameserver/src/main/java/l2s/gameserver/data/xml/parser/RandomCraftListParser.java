package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.RandomCraftListHolder;
import l2s.gameserver.templates.RandomCraftCategory;
import l2s.gameserver.templates.RandomCraftItem;

/**
 * @author nexvill
 */
public class RandomCraftListParser extends AbstractParser<RandomCraftListHolder>
{
	private static RandomCraftListParser _instance = new RandomCraftListParser();

	public static RandomCraftListParser getInstance()
	{
		return _instance;
	}

	private RandomCraftListParser()
	{
		super(RandomCraftListHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/random_craft_list.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "random_craft_list.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int i = 0;
		for (Iterator<Element> categoryIterator = rootElement.elementIterator("category"); categoryIterator.hasNext(); )
		{
			Element categoryElement = categoryIterator.next();
			
			double probability = Double.parseDouble(categoryElement.attributeValue("probability"));
			RandomCraftCategory category = new RandomCraftCategory(probability);
			
			for (Iterator<Element> itemIterator = categoryElement.elementIterator("item"); itemIterator.hasNext();)
			{
				Element itemElement = itemIterator.next();
				
				int id = Integer.parseInt(itemElement.attributeValue("id"));
				int resultId = itemElement.attributeValue("resultId") == null ? 0 : Integer.parseInt(itemElement.attributeValue("resultId"));
				long count = Long.parseLong(itemElement.attributeValue("count"));
				byte enchantLevel = itemElement.attributeValue("enchantLevel") == null ? 0 : Byte.parseByte(itemElement.attributeValue("enchantLevel"));
				double chance = Double.parseDouble(itemElement.attributeValue("chance"));
				
				category.addItem(new RandomCraftItem(id, resultId, count, enchantLevel, chance));
			}
			
			getHolder().addRandomCraftCategory(i, category);
			i++;
		}
	}
}
