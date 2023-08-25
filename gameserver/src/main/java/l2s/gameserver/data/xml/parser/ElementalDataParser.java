package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ElementalDataHolder;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.templates.elemental.ElementalAbsorbItem;
import l2s.gameserver.templates.elemental.ElementalLevelData;
import l2s.gameserver.templates.elemental.ElementalEvolution;
import l2s.gameserver.templates.elemental.ElementalTemplate;
import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author Bonux
**/
public final class ElementalDataParser extends AbstractParser<ElementalDataHolder>
{
	private static final ElementalDataParser _instance = new ElementalDataParser();

	public static ElementalDataParser getInstance()
	{
		return _instance;
	}

	private ElementalDataParser()
	{
		super(ElementalDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/elemental_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "elemental_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("configs"); iterator.hasNext();)
		{
			Element element = iterator.next();

			Config.ELEMENTAL_SYSTEM_ENABLED = Boolean.parseBoolean(element.attributeValue("enabled"));
			for(Iterator<Element> secondIterator = element.elementIterator(); secondIterator.hasNext();)
			{
				Element secondElement = secondIterator.next();

				if(secondElement.getName().equalsIgnoreCase("reset_points_cost"))
				{
					Config.ELEMENTAL_RESET_POINTS_ITEM_ID = Integer.parseInt(secondElement.attributeValue("item_id"));
					Config.ELEMENTAL_RESET_POINTS_ITEM_COUNT = Long.parseLong(secondElement.attributeValue("item_count"));
				}
			}
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("elemental"); iterator.hasNext();)
		{
			Element element = iterator.next();

			ElementalTemplate template = new ElementalTemplate(ElementalElement.valueOf(element.attributeValue("element").toUpperCase()));
			for(Iterator<Element> absorbItemsIterator = element.elementIterator("absorb_items"); absorbItemsIterator.hasNext();)
			{
				Element absorbItemsElement = absorbItemsIterator.next();

				for(Iterator<Element> absorbItemIterator = absorbItemsElement.elementIterator("absorb_item"); absorbItemIterator.hasNext();)
				{
					Element absorbItemElement = absorbItemIterator.next();

					int id = Integer.parseInt(absorbItemElement.attributeValue("id"));
					int power = Integer.parseInt(absorbItemElement.attributeValue("power"));
					template.addAbsorbItem(new ElementalAbsorbItem(id, 0, power));
				}
			}

			for(Iterator<Element> evolutionIterator = element.elementIterator("evolution"); evolutionIterator.hasNext();)
			{
				Element evolutionElement = evolutionIterator.next();

				int evolutionLevel = Integer.parseInt(evolutionElement.attributeValue("level"));
				ElementalEvolution evolution = new ElementalEvolution(template.getElement(), evolutionLevel);
				for(Iterator<Element> thirdIterator = evolutionElement.elementIterator(); thirdIterator.hasNext();)
				{
					Element thirdElement = thirdIterator.next();
					if(thirdElement.getName().equalsIgnoreCase("limits"))
					{
						int maxAttackPoints = Integer.parseInt(thirdElement.attributeValue("max_attack_points"));
						int maxDefencePoints = Integer.parseInt(thirdElement.attributeValue("max_defence_points"));
						int maxCritRatePoints = Integer.parseInt(thirdElement.attributeValue("max_crit_rate_points"));
						int maxCritAttackPoints = Integer.parseInt(thirdElement.attributeValue("max_crit_attack_points"));
						evolution.setLimits(maxAttackPoints, maxDefencePoints, maxCritRatePoints, maxCritAttackPoints);
					}
					else if(thirdElement.getName().equalsIgnoreCase("rise_level_cost"))
					{
						for(Iterator<Element> riseLevelCostIterator = thirdElement.elementIterator("cost_item"); riseLevelCostIterator.hasNext();)
						{
							Element riseLevelCostElement = riseLevelCostIterator.next();

							int id = Integer.parseInt(riseLevelCostElement.attributeValue("id"));
							long count = Long.parseLong(riseLevelCostElement.attributeValue("count"));
							evolution.addRiseLevelCost(new ItemData(id, count));
						}
					}
					else if(thirdElement.getName().equalsIgnoreCase("datas"))
					{
						for(Iterator<Element> dataIterator = thirdElement.elementIterator("data"); dataIterator.hasNext();)
						{
							Element dataElement = dataIterator.next();

							int level = Integer.parseInt(dataElement.attributeValue("level"));
							int attack = dataElement.attributeValue("attack") != null ? Integer.parseInt(dataElement.attributeValue("attack")) : 0;
							int defence = dataElement.attributeValue("defence") != null ? Integer.parseInt(dataElement.attributeValue("defence")) : 0;
							int critRate = dataElement.attributeValue("crit_rate") != null ? Integer.parseInt(dataElement.attributeValue("crit_rate")) : 0;
							int critAttack = dataElement.attributeValue("crit_attack") != null ? Integer.parseInt(dataElement.attributeValue("crit_attack")) : 0;
							long exp = dataElement.attributeValue("exp") != null ? Long.parseLong(dataElement.attributeValue("exp")) : 0L;
							ElementalLevelData levelData = new ElementalLevelData(level, attack, defence, critRate, critAttack, exp);
							for(Iterator<Element> extractIterator = dataElement.elementIterator("extract"); extractIterator.hasNext();)
							{
								Element extractElement = extractIterator.next();

								int extractItemId = Integer.parseInt(extractElement.attributeValue("item_id"));
								long extractItemCount = Long.parseLong(extractElement.attributeValue("item_count"));
								levelData.setExtractItem(new ItemData(extractItemId, extractItemCount));
								for(Iterator<Element> costItemIterator = extractElement.elementIterator("cost_item"); costItemIterator.hasNext();)
								{
									Element costItemElement = costItemIterator.next();

									int id = Integer.parseInt(costItemElement.attributeValue("id"));
									long count = Long.parseLong(costItemElement.attributeValue("count"));
									levelData.addExtractCost(new ItemData(id, count));
								}

							}
							evolution.addLevelData(levelData);
						}
					}
				}
				template.addEvolution(evolution);
			}
			getHolder().addTemplate(template);
		}
	}
}