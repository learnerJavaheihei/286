package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import org.dom4j.Element;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.VIPDataHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.templates.VIPTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.utils.Language;

/**
 * @author Bonux
**/
public final class VIPDataParser extends StatParser<VIPDataHolder>
{
	private static final VIPDataParser _instance = new VIPDataParser();

	public static VIPDataParser getInstance()
	{
		return _instance;
	}

	private VIPDataParser()
	{
		super(VIPDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/vip_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "vip_data.dtd";
	}

	@Override
	public boolean isDisabled()
	{
		return !Config.EX_USE_PRIME_SHOP; // VIP система работает только при включенном итем-молле.
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("default"); iterator.hasNext();)
		{
			Element element = iterator.next();

			VIPTemplate.DEFAULT_VIP_TEMPLATE.setPointsRefillPercent(element.attributeValue("points_refill_percent") == null ? 0. : Double.parseDouble(element.attributeValue("points_refill_percent")));
			VIPTemplate.DEFAULT_VIP_TEMPLATE.setPointsConsumeCount(element.attributeValue("points_consume_count") == null ? 0L : Long.parseLong(element.attributeValue("points_consume_count")));
			VIPTemplate.DEFAULT_VIP_TEMPLATE.setPointsConsumeDelay(element.attributeValue("points_consume_delay") == null ? 0 : Integer.parseInt(element.attributeValue("points_consume_delay")));
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("vip"); iterator.hasNext();)
		{
			Element element = iterator.next();

			StatsSet set = new StatsSet();

			for(Iterator<Element> subIterator = element.elementIterator("set"); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();
				set.set(subElement.attributeValue("name"), subElement.attributeValue("value"));
			}

			int vip_level = Integer.parseInt(element.attributeValue("level"));
			long points = Long.parseLong(element.attributeValue("points"));
			double points_refill_percent = element.attributeValue("points_refill_percent") == null ? VIPTemplate.DEFAULT_VIP_TEMPLATE.getPointsRefillPercent() : Double.parseDouble(element.attributeValue("points_refill_percent"));
			long points_consume_count = element.attributeValue("points_consume_count") == null ? VIPTemplate.DEFAULT_VIP_TEMPLATE.getPointsConsumeCount() : Long.parseLong(element.attributeValue("points_consume_count"));
			int points_consume_delay = element.attributeValue("points_consume_delay") == null ? (int) VIPTemplate.DEFAULT_VIP_TEMPLATE.getPointsConsumeDelay(TimeUnit.HOURS) : Integer.parseInt(element.attributeValue("points_consume_delay"));
			VIPTemplate template = new VIPTemplate(vip_level, points, points_refill_percent, points_consume_count, points_consume_delay, set);

			for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();

				if("stats".equalsIgnoreCase(subElement.getName()))
				{
					parseFor(subElement, template);
				}
				else if("triggers".equalsIgnoreCase(subElement.getName()))
				{
					parseTriggers(subElement, template);
				}
				else if("skills".equalsIgnoreCase(subElement.getName()))
				{
					for(Iterator<Element> nextIterator = subElement.elementIterator("skill"); nextIterator.hasNext();)
					{
						Element nextElement = nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));
						template.attachSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, id, level));
					}
				}
				else if("rewards".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						int itemId = Integer.parseInt(e.attributeValue("id"));
						long minItemCount = Long.parseLong(e.attributeValue("min_count"));
						long maxItemCount = Long.parseLong(e.attributeValue("max_count"));
						double itemChance = Double.parseDouble(e.attributeValue("chance"));
						template.addReward(new RewardItemData(itemId, minItemCount, maxItemCount, itemChance));
					}
				}
			}
			getHolder().addVIPTemplate(template);
		}
	}

	@Override
	protected Object getTableValue(String name, int... arg)
	{
		return null;
	}
}