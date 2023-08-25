package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import org.dom4j.Element;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.PremiumAccountHolder;
import l2s.gameserver.templates.PremiumAccountTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.utils.Language;

/**
 * @author Bonux
**/
public final class PremiumAccountParser extends StatParser<PremiumAccountHolder>
{
	private static final PremiumAccountParser _instance = new PremiumAccountParser();

	public static PremiumAccountParser getInstance()
	{
		return _instance;
	}

	private PremiumAccountParser()
	{
		super(PremiumAccountHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/premium_accounts.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "premium_accounts.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("config"); iterator.hasNext();)
		{
			Element element = iterator.next();

			Config.PREMIUM_ACCOUNT_ENABLED = Boolean.parseBoolean(element.attributeValue("enabled"));
			Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER = Boolean.parseBoolean(element.attributeValue("based_on_gameserver"));
			Config.FREE_PA_TYPE = element.attributeValue("free_type") == null ? 0 : Integer.parseInt(element.attributeValue("free_type"));
			Config.FREE_PA_DELAY = element.attributeValue("free_delay") == null ? 0 : Integer.parseInt(element.attributeValue("free_delay"));
			Config.ENABLE_FREE_PA_NOTIFICATION = element.attributeValue("notify_free") == null ? false : Boolean.parseBoolean(element.attributeValue("notify_free"));
		}

		if(!Config.PREMIUM_ACCOUNT_ENABLED)
			return;

		for(Iterator<Element> iterator = rootElement.elementIterator("account"); iterator.hasNext();)
		{
			Element element = iterator.next();

			StatsSet set = new StatsSet();

			for(Iterator<Element> subIterator = element.elementIterator("set"); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();
				set.set(subElement.attributeValue("name"), subElement.attributeValue("value"));
			}

			int type = Integer.parseInt(element.attributeValue("type"));
			PremiumAccountTemplate template = new PremiumAccountTemplate(type, set);

			for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();

				if("name".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						Language lang = Language.getLanguage(e.getName(), null);
						if(lang != null) {
							template.addName(lang, e.getTextTrim());
						}
					}
				}
				else if("give_items_on_start".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						int itemId = Integer.parseInt(e.attributeValue("id"));
						long itemCount = Long.parseLong(e.attributeValue("count"));
						template.addGiveItemOnStart(new ItemData(itemId, itemCount));
					}
				}
				else if("take_items_on_end".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						int itemId = Integer.parseInt(e.attributeValue("id"));
						long itemCount = Long.parseLong(e.attributeValue("count"));
						template.addTakeItemOnEnd(new ItemData(itemId, itemCount));
					}
				}
				else if("fee".equalsIgnoreCase(subElement.getName()))
				{
					int delay = subElement.attributeValue("delay") == null ? -1 : Integer.parseInt(subElement.attributeValue("delay"));
					for(Element e : subElement.elements())
					{
						int itemId = Integer.parseInt(e.attributeValue("id"));
						long itemCount = Long.parseLong(e.attributeValue("count"));
						template.addFee(delay, new ItemData(itemId, itemCount));
					}
				}
				else if("stats".equalsIgnoreCase(subElement.getName()))
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
			getHolder().addPremiumAccount(template);
		}
	}

	@Override
	protected void onParsed()
	{
		if(getHolder().size() == 0)
			Config.PREMIUM_ACCOUNT_ENABLED = false;
	}

	@Override
	protected Object getTableValue(String name, int... arg)
	{
		return null;
	}
}