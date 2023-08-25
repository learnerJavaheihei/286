package l2s.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import org.dom4j.Element;
import org.slf4j.Logger;

/**
 * @reworked VISTALL
 */
@SuppressWarnings("serial")
public class RewardList extends ArrayList<RewardGroup>
{
	public static final int MAX_CHANCE = 1000000;
	private final RewardType _type;
	private final boolean _autoLoot;

	public RewardList(RewardType rewardType, boolean a)
	{
		super(5);
		_type = rewardType;
		_autoLoot = a;
	}

	public List<RewardItem> roll(Player player)
	{
		return roll(player, 1.0, null);
	}

	public List<RewardItem> roll(Player player, double penaltyMod)
	{
		return roll(player, penaltyMod, null);
	}

	public List<RewardItem> roll(Player player, double penaltyMod, NpcInstance npc)
	{
		List<RewardItem> temp = new ArrayList<RewardItem>();
		for(RewardGroup g : this)
			temp.addAll(g.roll(_type, player, penaltyMod, npc));
		return temp;
	}

	public boolean isAutoLoot()
	{
		return _autoLoot;
	}

	public RewardType getType()
	{
		return _type;
	}

	public static RewardList parseRewardList(Logger logger, Element element, RewardType type, String debugString)
	{
		boolean autoLoot = element.attributeValue("auto_loot") != null && Boolean.parseBoolean(element.attributeValue("auto_loot"));
		RewardList list = new RewardList(type, autoLoot);

		for(Iterator<Element> nextIterator = element.elementIterator(); nextIterator.hasNext();)
		{
			final Element nextElement = nextIterator.next();
			final String nextName = nextElement.getName();
			boolean notGroupType = type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED;
			if(nextName.equalsIgnoreCase("group"))
			{
				double enterChance = nextElement.attributeValue("chance") == null ? RewardList.MAX_CHANCE : Double.parseDouble(nextElement.attributeValue("chance")) * 10000;
				String time = nextElement.attributeValue("time");

				RewardGroup group = notGroupType ? null : new RewardGroup(enterChance, time);
				for(Iterator<Element> rewardIterator = nextElement.elementIterator(); rewardIterator.hasNext();)
				{
					Element rewardElement = rewardIterator.next();
					RewardData data = RewardData.parseReward(rewardElement);
					if(Config.DISABLE_DROP_EXCEPT_ITEM_IDS.isEmpty() || Config.DISABLE_DROP_EXCEPT_ITEM_IDS.contains(data.getItemId()))
					{
						if(notGroupType)
							logger.warn("Can't load rewardlist from group: " + debugString + "; type: " + type);
						else
							group.addData(data);
					}
				}

				if(group != null && !group.getItems().isEmpty())
					list.add(group);
			}
			else if(nextName.equalsIgnoreCase("reward"))
			{
				if(!notGroupType)
				{
					logger.warn("Reward can't be without group(and not grouped): " + debugString + "; type: " + type);
					continue;
				}

				RewardData data = RewardData.parseReward(nextElement);
				if(Config.DISABLE_DROP_EXCEPT_ITEM_IDS.isEmpty() || Config.DISABLE_DROP_EXCEPT_ITEM_IDS.contains(data.getItemId()))
				{
					RewardGroup g = new RewardGroup(RewardList.MAX_CHANCE, null);
					g.addData(data);
					list.add(g);
				}
			}
		}
		return list;
	}
}