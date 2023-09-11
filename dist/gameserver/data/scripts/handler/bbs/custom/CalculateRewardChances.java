package handler.bbs.custom;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
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
import org.napile.primitive.maps.IntIntMap;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntIntMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Michał on 04.12.13.
 */
public class CalculateRewardChances {
	public static class DropInfo {
		public final long minCount;
		public final long maxCount;
		public final double chance;

		public DropInfo(long minCount, long maxCount, double chance) {
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.chance = chance;
		}
	}

	private static IntIntMap CACHED_DROP_COUNTS = new CHashIntIntMap();
	private static IntIntMap CACHED_SPOIL_COUNTS = new CHashIntIntMap();

	private static Set<ItemTemplate> CACHED_DROPPABLE_TEMPLATES = Collections.emptySet();

	private static List<NpcTemplate> NPC_CACHED_TEMPLATES = new CopyOnWriteArrayList<NpcTemplate>();
	private static IntObjectMap<List<Location>> NPC_CACHED_SPAWNS = new CHashIntObjectMap<List<Location>>();

	private static IntObjectMap<List<NpcTemplateDrops>> CACHED_DROPS = new CHashIntObjectMap<List<NpcTemplateDrops>>();

	private static final NumberFormat PERCENT_FORMAT_5 = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat PERCENT_FORMAT_10 = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat PERCENT_FORMAT_15 = NumberFormat.getPercentInstance(Locale.ENGLISH);

	static {
		PERCENT_FORMAT_5.setMaximumFractionDigits(5);
		PERCENT_FORMAT_5.setMinimumFractionDigits(0);
		PERCENT_FORMAT_10.setMaximumFractionDigits(10);
		PERCENT_FORMAT_10.setMinimumFractionDigits(0);
		PERCENT_FORMAT_15.setMaximumFractionDigits(15);
		PERCENT_FORMAT_15.setMinimumFractionDigits(0);
	}

	private static final DropInfo EMPTY_DROP_INFO = new DropInfo(0, 0, 0);

	public static void init() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> recache(), 0L, 60000L);
	}

	public static List<NpcTemplate> getNpcsContainingString(Player player, CharSequence name) {
		List<NpcTemplate> templates = new ArrayList<>();

		for (NpcTemplate template : NPC_CACHED_TEMPLATES) {
			if (StringUtils.containsIgnoreCase(template.getName(player), name))
				templates.add(template);
		}
		return templates;
	}

	public static int getDroplistsCountByItemId(int itemId, boolean drop) {
		if (drop)
			return CACHED_DROP_COUNTS.get(itemId);
		else
			return CACHED_SPOIL_COUNTS.get(itemId);
	}

	/**
	 * Key: 0 - Drop, 1 - Spoil
	 *
	 * @param itemId
	 * @return
	 */
	public static List<NpcTemplateDrops> getNpcsByDropOrSpoil(int itemId) {
		List<NpcTemplateDrops> drop = CACHED_DROPS.get(itemId);
		if (drop == null)
			return Collections.emptyList();
		return drop;
	}

	public static class NpcTemplateDrops {
		public NpcTemplate template;
		public boolean dropNoSpoil;

		private NpcTemplateDrops(NpcTemplate template, boolean dropNoSpoil) {
			this.template = template;
			this.dropNoSpoil = dropNoSpoil;
		}
	}

	private static boolean[] templateContainsItemId(NpcTemplate template, int itemId) {
		boolean[] dropSpoil = {false, false};
		for (RewardList rewardEntry : template.getRewards()) {
			if (rewardListContainsItemId(rewardEntry, itemId)) {
				if (rewardEntry.getType() == RewardType.SWEEP)
					dropSpoil[1] = true;
				else
					dropSpoil[0] = true;
			}
		}
		return dropSpoil;
	}

	private static boolean rewardListContainsItemId(RewardList list, int itemId) {
		for (RewardGroup group : list)
			for (RewardData reward : group.getItems())
				if (reward.getItemId() == itemId) {
					return true;
				}
		return false;
	}

	private static boolean isDroppingAnything(NpcTemplate template) {
		for (RewardList rewardEntry : template.getRewards())
			for (RewardGroup group : rewardEntry)
				if (!group.getItems().isEmpty())
					return true;
		return false;
	}

	public static List<RewardData> getDrops(NpcTemplate template, boolean drop, boolean spoil) {
		List<RewardData> allRewards = new ArrayList<>();
		if (template == null)
			return allRewards;

		for (RewardList rewardEntry : template.getRewards()) {
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

	public static DropInfo[] getDropInfo(Player player, NpcTemplate npc, int itemId) {
		if (player == null || npc == null)
			return null;

		DropInfo[] dropInfoArray = new DropInfo[]{EMPTY_DROP_INFO, EMPTY_DROP_INFO};

		final int diff = calculateLevelDiffForDrop(npc.level, player.getLevel(), npc.isRaid);
		final double penaltyMod = Experience.penaltyModifier(diff, 9);
		if (penaltyMod <= 0)
			return dropInfoArray;

		for (RewardList list : npc.getRewards()) {
			RewardType type = list.getType();

			if (list.isEmpty())
				continue;

			for (RewardGroup g : list) {
				List<RewardData> items = new ArrayList<RewardData>();
				for (RewardData d : g.getItems()) {
					if (d.getItemId() == itemId)
						items.add(d);
				}

				if (items.isEmpty())
					continue;

				double grate = 1.0;
				double gpmod = penaltyMod;

				if (type == RewardType.RATED_GROUPED) {
					if (g.isAdena()) {
						double rateAdena = player.getRateAdena();
						if (rateAdena == 0)
							continue;

						grate = rateAdena * 1./*npc.calcStat(Stats.ADENA_RATE_MULTIPLIER, 1., player, null)*/;
					} else {
						double rateDrop = player.getRateItems()/*npc.getRewardRate(player)*/;
						if (rateDrop == 0)
							continue;

						grate = rateDrop * 1./*npc.calcStat(Stats.DROP_RATE_MULTIPLIER, 1., player, null)*/;
						gpmod *= 1./*npc.getDropChanceMod(player)*/;
					}
				} else if (type == RewardType.SWEEP) {
					grate = player.getRateSpoil() * 1./*npc.calcStat(Stats.SPOIL_RATE_MULTIPLIER, 1., player, null)*/;
					gpmod *= player.getSpoilChanceMod();
				} else if (type == RewardType.EVENT_GROUPED) {
					grate = player.getRateItems() / Config.RATE_DROP_ITEMS_BY_LVL[player.getLevel()];
					gpmod *= player.getDropChanceMod() / Config.DROP_CHANCE_MODIFIER;
				}

				if (g.notRate()) {
					gpmod = Math.min(gpmod, 1.);
					grate = 1.;
				}

				if (!player.isGM() && (gpmod == 0 || grate == 0))
					continue;

				double groupChance = g.getChance() * gpmod;
				if (groupChance > RewardList.MAX_CHANCE) {
					gpmod = (groupChance - RewardList.MAX_CHANCE) / g.getChance() + 1;
					groupChance = RewardList.MAX_CHANCE;
				} else
					gpmod = 1.;

				double groupChanceModifier = (RewardList.MAX_CHANCE - groupChance) / RewardList.MAX_CHANCE;

				// Дальше идут изжопы с шансами, для того, чтобы отображать реальный шанс выпадения предмета.
				double itemMaxChance = (double) RewardList.MAX_CHANCE / g.getItems().size() * Math.min(g.getItems().size(), Config.MAX_DROP_ITEMS_FROM_ONE_GROUP) / RewardList.MAX_CHANCE;

				int normalChancesCount = 0;
				double normalChancesSum = 0.;
				for (RewardData d : g.getItems()) {
					double ipmod = d.notRate() ? Math.min(gpmod, 1.) : gpmod;
					double irate = d.notRate() ? 1.0 : grate;
					double chance = Math.min(RewardList.MAX_CHANCE, (d.getChance() - (d.getChance() * groupChanceModifier)) * ipmod) / RewardList.MAX_CHANCE;
					if (!g.isAdena())
						chance = getMinEventsChance(irate, chance, 1);
					if (chance < itemMaxChance) {
						normalChancesCount++;
						normalChancesSum += chance;
					}
				}

				// Высчитываем максимальный шанс учитывая количество предметов в группе и лимит дропа с группы.
				itemMaxChance += ((normalChancesCount * itemMaxChance) - normalChancesSum) / (g.getItems().size() - normalChancesCount);

				for (RewardData d : items) {
					double ipmod = d.notRate() ? Math.min(gpmod, 1.) : gpmod;
					double irate = d.notRate() ? 1.0 : grate;

					long minCount = Math.round(d.getMinDrop() * (g.isAdena() ? irate : 1.));
					long maxCount = Math.round(d.getMaxDrop() * (g.isAdena() ? irate : 1.));
					if (irate > 1 && !g.isAdena())
						maxCount = -maxCount;

					// Подсчитываем базовый шанс предмета учитывая шанс группы.
					double chance = Math.min(RewardList.MAX_CHANCE, (d.getChance() - (d.getChance() * groupChanceModifier)) * ipmod) / RewardList.MAX_CHANCE;

					// Учитываем рейты для минимального события дропа (шанс, что выпадет хотябы 1 раз).
					if (!g.isAdena())
						chance = getMinEventsChance(irate, chance, 1);

					// Ставим реальный максимальный шанс учитывая количество предметов в группе и лимит дропа с группы.
					if (g.getItems().size() > Config.MAX_DROP_ITEMS_FROM_ONE_GROUP)
						chance = Math.min(itemMaxChance, chance);

					if (type == RewardType.SWEEP)
						dropInfoArray[1] = new DropInfo(minCount, maxCount, chance);
					else
						dropInfoArray[0] = new DropInfo(minCount, maxCount, chance);
				}
			}
		}
		return dropInfoArray;
	}

	private static class TypeGroupData {
		private final RewardType type;
		private final RewardGroup group;
		private final RewardData data;

		private TypeGroupData(RewardType type, RewardGroup group, RewardData data) {
			this.type = type;
			this.group = group;
			this.data = data;
		}
	}

	public static long[] getDropCounts(Player player, NpcTemplate npc, boolean dropNoSpoil, int itemId) {
		TypeGroupData info = getGroupAndData(npc, dropNoSpoil, itemId);

		if (info == null)
			return new long[]{0L, 0L};

		double mod = Experience.penaltyModifier((long) calculateLevelDiffForDrop(npc.level, player.getLevel(), npc.isRaid), 9.0);
		double baseRate = 1.0;
		double playerRate = 1.0;
		if (info.type == RewardType.SWEEP) {
			baseRate = Config.RATE_DROP_SPOIL_BY_LVL[player.getLevel()];
			playerRate = player.getRateSpoil();
		} else if (info.type == RewardType.RATED_GROUPED) {
			if (info.group.isAdena()) {
				baseRate = Config.RATE_DROP_ADENA_BY_LVL[player.getLevel()];
				playerRate = player.getRateAdena();
			} else {
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
		return new long[]{minDrop, maxDrop};
	}

	private static TypeGroupData getGroupAndData(NpcTemplate npc, boolean dropNoSpoil, int itemId) {
		for (RewardList rewardEntry : npc.getRewards()) {
			if (rewardEntry.getType() == RewardType.SWEEP && dropNoSpoil)
				continue;
			if (rewardEntry.getType() != RewardType.SWEEP && !dropNoSpoil)
				continue;

			for (RewardGroup group : rewardEntry) {
				for (RewardData reward : group.getItems()) {
					if (reward.getItemId() == itemId)
						return new TypeGroupData(rewardEntry.getType(), group, reward);
				}
			}
		}
		return null;
	}

	private static int calculateLevelDiffForDrop(int mobLevel, int charLevel, boolean boss) {
		if (!Config.DEEPBLUE_DROP_RULES)
			return 0;

		// According to official data (Prima), deep blue mobs are 9 or more levels below players
		int deepblue_maxdiff = boss ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;

		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}

	public static List<ItemTemplate> getItemsByNameContainingString(Player player, CharSequence name, boolean onlyDroppable) {
		List<ItemTemplate> templates = new ArrayList<ItemTemplate>();
		if (onlyDroppable) {
			for (ItemTemplate template : CACHED_DROPPABLE_TEMPLATES) {
				if (template != null && StringUtils.containsIgnoreCase(template.getName(player), name))
					templates.add(template);
			}
		} else {
			for (ItemTemplate template : ItemHolder.getInstance().getAllTemplates()) {
				if (template != null && StringUtils.containsIgnoreCase(template.getName(player), name))
					templates.add(template);
			}
		}
		return templates;
	}

	public static void recache() {
		/////////////////////////////////////////////////////////////////////////////////////////
		List<NpcTemplate> cachedTemplates = new CopyOnWriteArrayList<NpcTemplate>();
		IntObjectMap<List<Location>> cachedSpawns = new CHashIntObjectMap<List<Location>>();
		for (NpcTemplate template : NpcHolder.getInstance().getAll()) {
			if (isDroppingAnything(template)) {
				List<Location> spawns = new ArrayList<Location>();
				for (NpcInstance npc : GameObjectsStorage.getNpcs(false, template.getId())) {
					if (npc.getReflection().isMain() && npc.getSpawnedLoc() != null)
						spawns.add(npc.getSpawnedLoc());
				}
				if (!spawns.isEmpty()) {
					cachedTemplates.add(template);
					cachedSpawns.put(template.getId(), spawns);
				}
			}
		}
		NPC_CACHED_TEMPLATES = cachedTemplates;
		NPC_CACHED_SPAWNS = cachedSpawns;
		/////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////
		Set<ItemTemplate> cachedDroppableItems = new CopyOnWriteArraySet<ItemTemplate>();
		for (NpcTemplate template : NPC_CACHED_TEMPLATES) {
			for (RewardList rewardEntry : template.getRewards()) {
				for (RewardGroup group : rewardEntry) {
					for (RewardData data : group.getItems()) {
						if (!cachedDroppableItems.contains(data.getItem()))
							cachedDroppableItems.add(data.getItem());
					}
				}
			}
		}
		CACHED_DROPPABLE_TEMPLATES = cachedDroppableItems;
		/////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////
		IntIntMap cachedDropCounts = new CHashIntIntMap();
		IntIntMap cachedSpoilCounts = new CHashIntIntMap();
		int dropCount = 0;
		int spoilCount = 0;
		for (ItemTemplate item : CACHED_DROPPABLE_TEMPLATES) {
			int itemId = item.getItemId();

			for (NpcTemplate template : NPC_CACHED_TEMPLATES) {
				for (RewardList rewardEntry : template.getRewards()) {
					for (RewardGroup group : rewardEntry) {
						for (RewardData data : group.getItems()) {
							if (data.getItem().getItemId() == itemId) {
								if (rewardEntry.getType() == RewardType.SWEEP)
									spoilCount++;
								else
									dropCount++;
							}
						}
					}
				}
			}
			cachedDropCounts.put(itemId, dropCount);
			cachedSpoilCounts.put(itemId, spoilCount);
		}
		CACHED_DROP_COUNTS = cachedDropCounts;
		CACHED_SPOIL_COUNTS = cachedSpoilCounts;
		/////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////
		IntObjectMap<List<NpcTemplateDrops>> cachedDrops = new CHashIntObjectMap<List<NpcTemplateDrops>>();
		for (ItemTemplate item : CACHED_DROPPABLE_TEMPLATES) {
			int itemId = item.getItemId();

			List<NpcTemplateDrops> drop = cachedDrops.get(itemId);
			if (drop == null) {
				drop = new CopyOnWriteArrayList<NpcTemplateDrops>();
				cachedDrops.put(itemId, drop);
			}

			for (NpcTemplate npc : NPC_CACHED_TEMPLATES) {
				boolean[] dropSpoil = templateContainsItemId(npc, itemId);
				if (dropSpoil[0])
					drop.add(new NpcTemplateDrops(npc, true));
				if (dropSpoil[1])
					drop.add(new NpcTemplateDrops(npc, false));
			}
		}
		CACHED_DROPS = cachedDrops;
		/////////////////////////////////////////////////////////////////////////////////////////
	}


	public static int getSpawnedCount(int npcId) {
		List<Location> spawns = NPC_CACHED_SPAWNS.get(npcId);
		if (spawns == null)
			return 0;
		return spawns.size();
	}

	public static List<Location> getRandomSpawnsByNpc(int npcId) {
		List<Location> spawns = NPC_CACHED_SPAWNS.get(npcId);
		if (spawns == null)
			return Collections.emptyList();
		return spawns;
	}

	public static String getFormatedChance(double chance) {
		NumberFormat pf;
		if (chance < 0.000000000001)
			pf = PERCENT_FORMAT_15;
		else if (chance < 0.0000001)
			pf = PERCENT_FORMAT_10;
		else
			pf = PERCENT_FORMAT_5;
		return pf.format(chance);
	}

	/**
	 * Формула Бернулли: Pn(m >= k)
	 * Возвращает вероятность того, что событие с шансом 'p' произойдет за 'n' попыток минимум 'k' раз.
	 **/
	private static double getMinEventsChance(double n, double p, int k) {
		if (n == 1)
			return p;

		n = Math.min(5000., n); // Ставим лимит, а то будет давать сильную нагрузку и не совсем корректные вычисления.

		double P = 0.;
		for (int i = 0; i < k; i++)
			P += getP(i, n, p);

		return Math.min(1., 1. - P);
	}

	private static double getP(double a, double b, double p) {
		return getC((int) a, (int) b) * Math.pow(p, a) * Math.pow(1. - p, b - a);
	}

	public static double getC(int a, int b) {
		return factorial(b).divide((factorial(b - a).multiply(factorial(a)))).doubleValue();
	}

	private static final IntObjectMap<BigInteger> FACTORIAL_CACHE = new CHashIntObjectMap<BigInteger>();

	public static BigInteger factorial(int n) {
		if (n == 0)
			return BigInteger.ONE;

		BigInteger ret;
		if ((ret = FACTORIAL_CACHE.get(n)) != null)
			return ret;

		ret = BigInteger.ONE;
		for (int i = 1; i <= n; ++i) ret = ret.multiply(BigInteger.valueOf(i));
		/*TODO: Найти более оптимильный вариант, а то данный вариант иногда вызывает StackOverflow
		ret = BigInteger.valueOf(n).multiply(factorial(n - 1));*/
		FACTORIAL_CACHE.put(n, ret);
		return ret;
	}
}
