package l2s.gameserver.data.xml.parser;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.DailyMissionsHolder;
import l2s.gameserver.templates.dailymissions.DailyRewardTemplate;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;
import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author Bonux
**/
public final class DailyMissionsParser extends AbstractParser<DailyMissionsHolder>
{
	private static final DailyMissionsParser _instance = new DailyMissionsParser();

	public static DailyMissionsParser getInstance()
	{
		return _instance;
	}

	private DailyMissionsParser()
	{
		super(DailyMissionsHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/daily_missions.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "daily_missions.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();

			int id = parseInt(element, "id");
			String handler = parseString(element, "handler");
			int value = parseInt(element, "value", 1);
			int minLevel = parseInt(element, "min_level", 1);
			int maxLevel = parseInt(element, "max_level", Integer.MAX_VALUE);
			int completedMission = parseInt(element, "completedMission", 0);

			DailyMissionTemplate mission = new DailyMissionTemplate(id, handler, value, minLevel, maxLevel, completedMission);

			for(Iterator<Element> rewardsIterator = element.elementIterator("rewards"); rewardsIterator.hasNext();)
			{
				Element rewardsElement = rewardsIterator.next();

				String classes = parseString(rewardsElement, "classes", null);
				TIntSet classIds = classes == null ? null : new TIntHashSet(StringArrayUtils.stringToIntArray(rewardsElement.attributeValue("classes"), ","));

				DailyRewardTemplate reward = new DailyRewardTemplate(classIds);

				for(Iterator<Element> rewardIterator = rewardsElement.elementIterator("reward"); rewardIterator.hasNext();)
				{
					Element rewardElement = rewardIterator.next();

					int rewardId = parseInt(rewardElement, "id");
					long rewardCount = parseLong(rewardElement, "count");

					reward.addRewardItem(new ItemData(rewardId, rewardCount));
				}
				mission.addReward(reward);
			}
			getHolder().addMission(mission);
		}
	}
}