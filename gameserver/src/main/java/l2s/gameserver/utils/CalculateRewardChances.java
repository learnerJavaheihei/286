package l2s.gameserver.utils;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.reward.RewardData;
import l2s.gameserver.model.reward.RewardGroup;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 搜尋系統 顯示計算獲得機率
 */
public class CalculateRewardChances
{
	public static final double CORRECT_CHANCE_TRIES = 10000.0;
	private static final Map<Integer, Integer[]> droplistsCountCache = new HashMap<>();
	private static final Map<String, String> correctedChances = new HashMap<>();

	public static List<NpcTemplate> getNpcsContainingString(CharSequence name)
	{
		List<NpcTemplate> templates = new ArrayList<>();

		for (NpcTemplate template : NPC_CACHED_TEMPLATES)
		{
			if (StringUtils.containsIgnoreCase(template.getName(), name))
				templates.add(template);
		}
		return templates;
	}

	public static int getDroplistsCountByItemId(int itemId, boolean drop)
	{
		if (droplistsCountCache.containsKey(itemId))
			if (drop)
				return droplistsCountCache.get(itemId)[0].intValue();
			else
				return droplistsCountCache.get(itemId)[1].intValue();

		int dropCount = 0;
		int spoilCount = 0;
		for (NpcTemplate template : NPC_CACHED_TEMPLATES)
		{
			for (RewardList rewardEntry : template.getRewards())
				for (RewardGroup group : rewardEntry)
					for (RewardData data : group.getItems())
						if (data.getItem().getItemId() == itemId)
							if (rewardEntry.getType() == RewardType.SWEEP)
								spoilCount++;
							else
								dropCount++;
		}

		droplistsCountCache.put(itemId, new Integer[] { dropCount, spoilCount });

		if (drop)
			return dropCount;
		else
			return spoilCount;
	}

	public static boolean isItemDroppable(int itemId)
	{
		if (!droplistsCountCache.containsKey(itemId))
			getDroplistsCountByItemId(itemId, true);

		return droplistsCountCache.get(itemId)[0].intValue() > 0 || droplistsCountCache.get(itemId)[1].intValue() > 0;
	}

	public static List<ItemTemplate> getDroppableItems()
	{
		List<ItemTemplate> items = new ArrayList<>();
		for (NpcTemplate template : NPC_CACHED_TEMPLATES)
		{
			for (RewardList rewardEntry : template.getRewards())
				for (RewardGroup group : rewardEntry)
					for (RewardData data : group.getItems())
						if (!items.contains(data.getItem()))
							items.add(data.getItem());
		}
		return items;
	}

	/**
	 * Key: 0 - Drop, 1 - Spoil
	 * 
	 * @param itemId
	 * @return
	 */
	public static List<NpcTemplateDrops> getNpcsByDropOrSpoil(int itemId)
	{
		List<NpcTemplateDrops> templates = new ArrayList<>();
		for (NpcTemplate template : NPC_CACHED_TEMPLATES)
		{
			boolean[] dropSpoil = templateContainsItemId(template, itemId);

			if (dropSpoil[0])
				templates.add(new NpcTemplateDrops(template, true));
			if (dropSpoil[1])
				templates.add(new NpcTemplateDrops(template, false));
		}
		return templates;
	}

	public static class NpcTemplateDrops
	{
		public NpcTemplate template;
		public boolean dropNoSpoil;

		private NpcTemplateDrops(NpcTemplate template, boolean dropNoSpoil)
		{
			this.template = template;
			this.dropNoSpoil = dropNoSpoil;
		}
	}

	private static boolean[] templateContainsItemId(NpcTemplate template, int itemId)
	{
		boolean[] dropSpoil = { false, false };
		for (RewardList rewardEntry : template.getRewards())
		{
			if (rewardListContainsItemId(rewardEntry, itemId))
			{
				if (rewardEntry.getType() == RewardType.SWEEP)
					dropSpoil[1] = true;
				else
					dropSpoil[0] = true;
			}
		}
		return dropSpoil;
	}

	private static boolean rewardListContainsItemId(RewardList list, int itemId)
	{
		for (RewardGroup group : list)
			for (RewardData reward : group.getItems())
				if (reward.getItemId() == itemId)
				{
					return true;
				}
		return false;
	}

	private static boolean isDroppingAnything(NpcTemplate template)
	{
		for (RewardList rewardEntry : template.getRewards())
			for (RewardGroup group : rewardEntry)
				if (!group.getItems().isEmpty())
					return true;
		return false;
	}

	public static List<RewardData> getDrops(NpcTemplate template, boolean drop, boolean spoil)
	{
		List<RewardData> allRewards = new ArrayList<>();
		if (template == null)
			return allRewards;

		for (RewardList rewardEntry : template.getRewards())
		{
			if (rewardEntry.getType() == RewardType.SWEEP && !spoil)
				continue;
			if (rewardEntry.getType() != RewardType.SWEEP && !drop)
				continue;
			for (RewardGroup group : rewardEntry)
				for (RewardData reward : group.getItems())
					allRewards.add(reward);
		}
		return allRewards;
	}

	public static String getDropChance(Player player, NpcTemplate npc, boolean dropNoSpoil, int itemId)
	{
		TypeGroupData info = getGroupAndData(npc, dropNoSpoil, itemId);

		if (info == null)
			return "0";

		double mod = Experience.penaltyModifier((long) calculateLevelDiffForDrop(npc.level, player.getLevel(), false),
				9.0);
		double baseRate = 1.0;
		double playerRate = 1.0;
		if (info.type == RewardType.SWEEP)
		{
			baseRate = Config.RATE_DROP_SPOIL_BY_LVL[player.getLevel()];
			playerRate = player.getRateSpoil();
		}
		else if (info.type == RewardType.RATED_GROUPED)
		{
			if (info.group.isAdena())
			{
				return getAdenaChance(info, mod);
			}
			if (npc.isRaid)
			{
				return getItemChance(info, mod, Config.RATE_DROP_SPOIL_BY_LVL[player.getLevel()], 1.0);
			}
			baseRate = Config.RATE_DROP_ITEMS_BY_LVL[player.getLevel()];
			playerRate = player.getRateItems();
		}

		return getItemChance(info, mod, baseRate, playerRate);
	}

	private static String getAdenaChance(TypeGroupData info, double mod)
	{
		if (mod <= 0)
			return "0";

		double groupChance = info.group.getChance();
		if (mod > 10)
		{
			groupChance = (double) RewardList.MAX_CHANCE;
		}

		double itemChance = info.data.getChance();

		groupChance /= (double) RewardList.MAX_CHANCE;
		itemChance /= (double) RewardList.MAX_CHANCE;
		double finalChance = groupChance * itemChance;
		return String.valueOf(finalChance * 100);
	}

	private static String getItemChance(TypeGroupData info, double mod, double baseRate, double playerRate)
	{
		if (mod <= 0.0)
			return "0";

		double rate;
		if (info.group.notRate())
			rate = Math.min(mod, 1.0);
		else
			rate = baseRate * playerRate * mod;

		double mult = Math.ceil(rate);

		BigDecimal totalChance = BigDecimal.valueOf(0.0);
		for (double n = 0.0; n < mult; n++)
		{
			BigDecimal groupChance = BigDecimal.valueOf(info.group.getChance() * Math.min(rate - n, 1.0));
			BigDecimal itemChance = BigDecimal.valueOf(info.data.getChance());
			groupChance = groupChance.divide(BigDecimal.valueOf((long) RewardList.MAX_CHANCE));
			itemChance = itemChance.divide(BigDecimal.valueOf((long) RewardList.MAX_CHANCE));
			totalChance = totalChance.add(groupChance.multiply(itemChance));
		}
		String totalChanceString = totalChance.multiply(BigDecimal.valueOf(100.0)).toString();

		return getCorrectedChance(totalChanceString, info.group.getChance() / 10000.0, info.data.getChance() / 10000.0,
				mult);
	}

	private static String getCorrectedChance(String totalChanceString, double groupChance, double itemChance,
			double mult)
	{
		Comparable<BigDecimal> totalChance = new BigDecimal(totalChanceString);
		if (totalChance.compareTo(BigDecimal.valueOf(5.0)) < 0)
			return totalChance.toString();

		if (correctedChances.containsKey(totalChanceString))
			return correctedChances.get(totalChanceString);

		double totalPassed = 0.0;
		double x;
		for (double i = 0.0; i < CORRECT_CHANCE_TRIES; i++)
		{
			for (x = 0.0; x < mult; x++)
			{
				if (Rnd.chance(groupChance))
					if (Rnd.chance(itemChance))
					{
						totalPassed++;
						break;
					}
			}
		}
		String finalValue = String.valueOf(totalPassed / (CORRECT_CHANCE_TRIES / 100.0));
		correctedChances.put(totalChanceString, finalValue);
		return finalValue;
	}

	private static class TypeGroupData
	{
		private final RewardType type;
		private final RewardGroup group;
		private final RewardData data;

		private TypeGroupData(RewardType type, RewardGroup group, RewardData data)
		{
			this.type = type;
			this.group = group;
			this.data = data;
		}
	}

	public static long[] getDropCounts(Player player, NpcTemplate npc, boolean dropNoSpoil, int itemId)
	{
		TypeGroupData info = getGroupAndData(npc, dropNoSpoil, itemId);

		if (info == null)
			return new long[] { 0L, 0L };

		double mod = Experience.penaltyModifier((long) calculateLevelDiffForDrop(npc.level, player.getLevel(), false),
				9.0);
		double baseRate = 1.0;
		double playerRate = 1.0;
		if (info.type == RewardType.SWEEP)
		{
			baseRate = Config.RATE_DROP_SPOIL_BY_LVL[player.getLevel()];
			playerRate = player.getRateSpoil();
		}
		else if (info.type == RewardType.RATED_GROUPED)
		{
			if (info.group.isAdena())
			{
				baseRate = Config.RATE_DROP_ADENA_BY_LVL[player.getLevel()];
				playerRate = player.getRateAdena();
			}
			else
			{
				baseRate = Config.RATE_DROP_ITEMS_BY_LVL[player.getLevel()];
				playerRate = player.getRateItems();
			}
		}
		double imult;
		if (info.data.notRate() && itemId != ItemTemplate.ITEM_ID_ADENA)
			imult = 1.0;
		else
			imult = baseRate * playerRate * mod;

		long minDrop = info.data.getMinDrop();
		if (itemId == ItemTemplate.ITEM_ID_ADENA)
			minDrop *= (long) imult;
		long maxDrop = (long) ((double) info.data.getMaxDrop() * Math.ceil(imult));
		return new long[] { minDrop, maxDrop };
	}

	private static TypeGroupData getGroupAndData(NpcTemplate npc, boolean dropNoSpoil, int itemId)
	{
		for (RewardList rewardEntry : npc.getRewards())
		{
			if (rewardEntry.getType() == RewardType.SWEEP && dropNoSpoil)
				continue;
			if (rewardEntry.getType() != RewardType.SWEEP && !dropNoSpoil)
				continue;

			for (RewardGroup group : rewardEntry)
				for (RewardData reward : group.getItems())
					if (reward.getItemId() == itemId)
					{
						return new TypeGroupData(rewardEntry.getType(), group, reward);
					}
		}
		return null;
	}

	private static int calculateLevelDiffForDrop(int mobLevel, int charLevel, boolean boss)
	{
		if (!Config.DEEPBLUE_DROP_RULES)
			return 0;

		// According to official data (Prima), deep blue mobs are 9 or more levels below
		// players
		int deepblue_maxdiff = boss ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;

		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}

	public static List<ItemTemplate> getItemsByNameContainingString(CharSequence name, boolean onlyDroppable)
	{
		ItemTemplate[] toChooseFrom = onlyDroppable ? getDroppableTemplates()
				: ItemHolder.getInstance().getAllTemplates().toArray(new ItemTemplate[0]);
		List<ItemTemplate> templates = new ArrayList<>();
		for (ItemTemplate template : toChooseFrom)
			if (template != null && StringUtils.containsIgnoreCase(template.getName(), name))
				templates.add(template);
		return templates;
	}

	private static ItemTemplate[] droppableTemplates;

	public static ItemTemplate[] getDroppableTemplates()
	{
		if (droppableTemplates == null)
		{
			List<ItemTemplate> templates = CalculateRewardChances.getDroppableItems();
			droppableTemplates = templates.toArray(new ItemTemplate[templates.size()]);
		}
		return droppableTemplates;
	}

	private static List<NpcTemplate> NPC_CACHED_TEMPLATES = new CopyOnWriteArrayList<NpcTemplate>();
	private static IntObjectMap<List<Location>> NPC_CACHED_SPAWNS = new CHashIntObjectMap<List<Location>>();

	public static void recacheNpcs()
	{
		List<NpcTemplate> cachedTemplates = new CopyOnWriteArrayList<NpcTemplate>();
		IntObjectMap<List<Location>> cachedSpawns = new CHashIntObjectMap<List<Location>>();
		for (NpcTemplate template : NpcHolder.getInstance().getAll())
		{
			if (isDroppingAnything(template))
			{
				List<Location> spawns = new ArrayList<Location>();
				for (NpcInstance npc : GameObjectsStorage.getNpcs(false, false, template.getId()))
				{
					if (npc.getReflection().isMain() && npc.getSpawnedLoc() != null)
						spawns.add(npc.getSpawnedLoc());
				}
				if (!spawns.isEmpty())
				{
					cachedTemplates.add(template);
					cachedSpawns.put(template.getId(), spawns);
				}
			}
		}
		NPC_CACHED_TEMPLATES = cachedTemplates;
		NPC_CACHED_SPAWNS = cachedSpawns;
	}

	public static int getSpawnedCount(int npcId)
	{
		List<Location> spawns = NPC_CACHED_SPAWNS.get(npcId);
		if (spawns == null)
			return 0;
		return spawns.size();
	}

	public static List<Location> getRandomSpawnsByNpc(int npcId)
	{
		List<Location> spawns = NPC_CACHED_SPAWNS.get(npcId);
		if (spawns == null)
			return Collections.emptyList();
		return spawns;
	}
}
