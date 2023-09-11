package handler.bbs.custom;

import handler.bbs.ScriptsCommunityHandler;
import handler.onshiftaction.commons.RewardListInfo;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Language;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Community Board page containing Drop Calculator
 */
public class CommunityDropCalculator extends ScriptsCommunityHandler {
	private static final Logger _log = LoggerFactory.getLogger(CommunityDropCalculator.class);

	private static final IntObjectMap<Map<String, Object>> QUICK_VARS = new CHashIntObjectMap<Map<String, Object>>();

	@Override
	public void onInit() {
		super.onInit();
		CalculateRewardChances.init();
	}

	@Override
	public String[] getBypassCommands() {
		return new String[]{"_dropCalc", "_dropItemsByName", "_dropMonstersByItem", "_dropMonstersByName", "_dropMonsterDetailsByItem", "_dropMonsterDetailsByName"};
	}

	@Override
	public void doBypassCommand(Player player, String bypass) {
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		player.setSessionVar("add_fav", null);

		if (!BBSConfig.ALLOW_DROP_CALCULATOR) {
			String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/dropcalc/off.htm", player);
			ShowBoardPacket.separateAndSend(html, player);
			return;
		}

		/*if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
		{
			onWrongCondition(player);
			return;
		}*/

		switch (cmd) {
			case "dropCalc":
				showMainPage(player);
				break;
			case "dropItemsByName":
				if (!st.hasMoreTokens()) {
					showMainPage(player);
					return;
				}
				String itemName = "";
				while (st.countTokens() > 1)
					itemName += " " + st.nextToken();
				int itemsPage = 1;
				try {
					itemsPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
				} catch (Exception e) {
					player.sendMessage("Error occured, try again later!");
					doBypassCommand(player, "_dropCalc");
					return;
				}
				showDropItemsByNamePage(player, itemName.trim(), itemsPage);
				break;
			case "dropMonstersByItem":

				int itemId = 1;
				int monstersPage = 1;
				try {
					itemId = Integer.parseInt(st.nextToken());
					monstersPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
				} catch (Exception e) {
					player.sendMessage("Error occured, try again later!");
					doBypassCommand(player, "_dropCalc");
					return;
				}
				showDropMonstersByItem(player, itemId, monstersPage);
				break;
			case "dropMonsterDetailsByItem":
				int monsterId = 1;
				int nextTokn = 0;
				try {
					monsterId = Integer.parseInt(st.nextToken());
					if (st.hasMoreTokens()) {
						nextTokn = Integer.parseInt(st.nextToken());
						manageButton(player, nextTokn, monsterId);
					}
				} catch (Exception e) {
					player.sendMessage("Error occured, try again later!");
					doBypassCommand(player, "_dropCalc");
					return;
				}
				showdropMonsterDetailsByItem(player, monsterId);
				break;
			case "dropMonstersByName":
				if (!st.hasMoreTokens()) {
					showMainPage(player);
					return;
				}
				StringBuilder monsterName = new StringBuilder();
				while (st.countTokens() > 1)
					monsterName.append(" ").append(st.nextToken());
				int monsterPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken().trim()) : 1;
				showDropMonstersByName(player, monsterName.toString().trim(), monsterPage);
				break;
			case "dropMonsterDetailsByName":
				int chosenMobId = 1;
				try {
					chosenMobId = Integer.parseInt(st.nextToken());
					if (st.hasMoreTokens()) {
						int nexttkn = Integer.parseInt(st.nextToken());
						manageButton(player, nexttkn, chosenMobId);
					}
				} catch (Exception e) {
					player.sendMessage("Error occured, try again later!");
					doBypassCommand(player, "_dropCalc");
					return;
				}
				showDropMonsterDetailsByName(player, chosenMobId);
				break;
			default:
				break;
		}
	}

	private static void showMainPage(Player player) {
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/dropcalc/main.htm", player);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static void showDropItemsByNamePage(Player player, String itemName, int page) {
		addQuickVar(player, "DCItemName", itemName);
		addQuickVar(player, "DCItemsPage", page);
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/dropcalc/items_by_name.htm", player);
		html = replaceItemsByNamePage(player, html, itemName, page);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceItemsByNamePage(Player player, String html, String itemName, int page) {
		String newHtml = html;

		List<ItemTemplate> itemsByName = CalculateRewardChances.getItemsByNameContainingString(player, itemName, true);
		itemsByName.sort(new ItemComparator(player, itemName));

		int itemIndex = 0;

		for (int i = 0; i < 6; i++) {
			itemIndex = i + (page - 1) * 6;
			ItemTemplate item = itemsByName.size() > itemIndex ? itemsByName.get(itemIndex) : null;

			newHtml = newHtml.replace("%itemIcon" + i + '%', item != null ? getItemIcon(item) : "<br>");
			newHtml = newHtml.replace("%itemName" + i + '%', item != null ? getName(item.getName(player)) : "<br>");
			newHtml = newHtml.replace("%itemGrade" + i + '%', item != null ? getItemGradeIcon(item) : "<br>");
			newHtml = newHtml.replace("%dropLists" + i + '%', item != null ? String.valueOf(CalculateRewardChances.getDroplistsCountByItemId(item.getItemId(), true)) : "<br>");
			newHtml = newHtml.replace("%spoilLists" + i + '%', item != null ? String.valueOf(CalculateRewardChances.getDroplistsCountByItemId(item.getItemId(), false)) : "<br>");
			newHtml = newHtml.replace("%showMonsters" + i + '%', item != null ? "<button value=\"Show Monsters\" action=\"bypass _dropMonstersByItem_%itemChosenId" + i + "%\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
			newHtml = newHtml.replace("%itemChosenId" + i + '%', item != null ? String.valueOf(item.getItemId()) : "<br>");
		}

		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"Previous\" action=\"bypass _dropItemsByName_" + itemName + "_" + (page - 1) + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", itemsByName.size() > itemIndex + 1 ? "<button value=\"Next\" action=\"bypass _dropItemsByName_" + itemName + "_" + (page + 1) + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchItem%", itemName);
		newHtml = newHtml.replace("%page%", String.valueOf(page));

		return newHtml;
	}

	private static void showDropMonstersByItem(Player player, int itemId, int page) {
		addQuickVar(player, "DCItemId", itemId);
		addQuickVar(player, "DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/dropcalc/monsters_by_item.htm", player);
		html = replaceMonstersByItemPage(player, html, itemId, page);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceMonstersByItemPage(Player player, String html, int itemId, int page) {
		String newHtml = html;

		List<CalculateRewardChances.NpcTemplateDrops> templates = CalculateRewardChances.getNpcsByDropOrSpoil(itemId);
		templates.sort(new ItemChanceComparator(player, itemId));

		int npcIndex = 0;

		for (int i = 0; i < 8; i++) {
			npcIndex = i + (page - 1) * 8;
			CalculateRewardChances.NpcTemplateDrops drops = templates.size() > npcIndex ? templates.get(npcIndex) : null;
			NpcTemplate npc = templates.size() > npcIndex ? templates.get(npcIndex).template : null;

			newHtml = newHtml.replace("%monsterName" + i + '%', npc != null ? ("&@" + npc.getId() + ";") : "<br>");
			newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.level) : "<br>");
			newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? String.valueOf(npc.aggroRange > 0) : "<br>");
			newHtml = newHtml.replace("%monsterType" + i + '%', npc != null ? drops.dropNoSpoil ? "Drop" : "Spoil" : "<br>");
			CalculateRewardChances.DropInfo[] dropInfo = CalculateRewardChances.getDropInfo(player, npc, itemId);
			if (dropInfo != null) {
				CalculateRewardChances.DropInfo info = drops.dropNoSpoil ? dropInfo[0] : dropInfo[1];
				if (info.minCount == Math.abs(info.maxCount))
					newHtml = newHtml.replace("%monsterCount" + i + '%', String.valueOf(info.minCount) + (info.maxCount < 0 ? "+" : ""));
				else
					newHtml = newHtml.replace("%monsterCount" + i + '%', String.valueOf(info.minCount) + "-" + String.valueOf(Math.abs(info.maxCount)) + (info.maxCount < 0 ? "+" : ""));
				newHtml = newHtml.replace("%monsterChance" + i + '%', CalculateRewardChances.getFormatedChance(info.chance));
			} else {
				newHtml = newHtml.replace("%monsterCount" + i + '%', "<br>");
				newHtml = newHtml.replace("%monsterChance" + i + '%', "<br>");
			}
			newHtml = newHtml.replace("%showDetails" + i + '%', npc != null ? "<button value=\"Show Details\" action=\"bypass _dropMonsterDetailsByItem_%monsterId" + i + "%\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
			newHtml = newHtml.replace("%monsterId" + i + '%', npc != null ? String.valueOf(npc.getId()) : "<br>");
		}

		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"Previous\" action=\"bypass _dropMonstersByItem_%itemChosenId%_" + (page - 1) + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", templates.size() > npcIndex + 1 ? "<button value=\"Next\" action=\"bypass _dropMonstersByItem_%itemChosenId%_" + (page + 1) + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchItem%", getQuickVarS(player, "DCItemName"));
		newHtml = newHtml.replace("%searchItemPage%", String.valueOf(getQuickVarI(player, "DCItemsPage")));
		newHtml = newHtml.replace("%itemChosenId%", String.valueOf(itemId));
		newHtml = newHtml.replace("%monsterPage%", String.valueOf(page));
		return newHtml;
	}

	private static void showdropMonsterDetailsByItem(Player player, int monsterId) {
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/dropcalc/monster_details_by_item.htm", player);
		html = replaceMonsterDetails(player, html, monsterId);

		// DO NOT ALLOW TO TELEPORT TO MOBS
//		if (!canTeleToMonster(player, monsterId, false))
//			html = html.replace("%goToNpc%", "<br>");
//		else
//			html = html.replace("%goToNpc%", "<button value=\"Go to Npc\" action=\"bypass _dropMonsterDetailsByItem_"+monsterId+"_3\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1.OlympiadWnd_DF_Fight1None\">");

		//CalculateRewardChances.sendUsedImages(html, player);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceMonsterDetails(Player player, String html, int monsterId) {
		String newHtml = html;

		int itemId = getQuickVarI(player, "DCItemId");
		NpcTemplate template = NpcHolder.getInstance().getTemplate(monsterId);
		if (template == null)
			return newHtml;

		newHtml = newHtml.replace("%searchName%", String.valueOf(getQuickVarS(player, "DCMonsterName")));
		newHtml = newHtml.replace("%itemChosenId%", String.valueOf(getQuickVarI(player, "DCItemId")));
		newHtml = newHtml.replace("%monsterPage%", String.valueOf(getQuickVarI(player, "DCMonstersPage")));
		newHtml = newHtml.replace("%monsterId%", String.valueOf(monsterId));
		newHtml = newHtml.replace("%imageId%", String.valueOf(ImagesCache.getInstance().getImageId(monsterId + ".png") > 0 ? monsterId : 0));
		newHtml = newHtml.replace("%monsterName%", ("&@" + template.getId() + ";"));
		newHtml = newHtml.replace("%monsterLevel%", String.valueOf(template.level));
		newHtml = newHtml.replace("%monsterAggro%", String.valueOf(template.aggroRange > 0));
		if (itemId > 0) {
			CalculateRewardChances.DropInfo[] dropInfo = CalculateRewardChances.getDropInfo(player, template, itemId);
			if (dropInfo != null) {
				newHtml = newHtml.replace("%monsterDropSpecific%", CalculateRewardChances.getFormatedChance(dropInfo[0].chance));
				newHtml = newHtml.replace("%monsterSpoilSpecific%", CalculateRewardChances.getFormatedChance(dropInfo[1].chance));
			} else {
				newHtml = newHtml.replace("%monsterDropSpecific%", "");
				newHtml = newHtml.replace("%monsterSpoilSpecific%", "");
			}
		}
		newHtml = newHtml.replace("%monsterDropAll%", String.valueOf(CalculateRewardChances.getDrops(template, true, false).size()));
		newHtml = newHtml.replace("%monsterSpoilAll%", String.valueOf(CalculateRewardChances.getDrops(template, false, true).size()));
		newHtml = newHtml.replace("%spawnCount%", String.valueOf(CalculateRewardChances.getSpawnedCount(monsterId)));
		newHtml = newHtml.replace("%minions%", String.valueOf(template.getMinionData().size()));
		newHtml = newHtml.replace("%expReward%", String.valueOf(template.rewardExp));
		newHtml = newHtml.replace("%maxHp%", String.format(Locale.ROOT, "%.1f%n", template.getBaseHpMax(template.level)));
		newHtml = newHtml.replace("%maxMP%", String.format(Locale.ROOT, "%.1f%n", template.getBaseMpMax(template.level)));
		newHtml = newHtml.replace("%pAtk%", String.valueOf((int) template.getBasePAtk()));
		newHtml = newHtml.replace("%mAtk%", String.valueOf((int) template.getBaseMAtk()));
		newHtml = newHtml.replace("%pDef%", String.valueOf((int) template.getBasePDef()));
		newHtml = newHtml.replace("%mDef%", String.valueOf((int) template.getBaseMDef()));
		newHtml = newHtml.replace("%atkSpd%", String.valueOf((int) template.getBasePAtkSpd()));
		newHtml = newHtml.replace("%castSpd%", String.valueOf((int) template.getBaseMAtkSpd()));
		newHtml = newHtml.replace("%runSpd%", String.valueOf((int) template.getBaseRunSpd()));

		return newHtml;
	}

	private static void showDropMonstersByName(Player player, String monsterName, int page) {
		addQuickVar(player, "DCMonsterName", monsterName);
		addQuickVar(player, "DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/dropcalc/monsters_by_name.htm", player);
		html = replaceMonstersByName(player, html, monsterName, page);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceMonstersByName(Player player, String html, String monsterName, int page) {
		String newHtml = html;
		List<NpcTemplate> npcTemplates = CalculateRewardChances.getNpcsContainingString(player, monsterName);
		npcTemplates.sort(new MonsterComparator(player, monsterName));

		int npcIndex = 0;

		for (int i = 0; i < 8; i++) {
			npcIndex = i + (page - 1) * 8;
			NpcTemplate npc = npcTemplates.size() > npcIndex ? npcTemplates.get(npcIndex) : null;

			newHtml = newHtml.replace("%monsterName" + i + '%', npc != null ? ("&@" + npc.getId() + ";") : "<br>");
			newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.level) : "<br>");
			newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? String.valueOf(npc.aggroRange > 0) : "<br>");
			newHtml = newHtml.replace("%monsterDrops" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, true, false).size()) : "<br>");
			newHtml = newHtml.replace("%monsterSpoils" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, false, true).size()) : "<br>");
			newHtml = newHtml.replace("%showDetails" + i + '%', npc != null ? "< button value =\"Show Details\" action=\"bypass _dropMonsterDetailsByName_" + npc.getId() + "\" width=120 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		}

		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"Previous\" action=\"bypass _dropMonstersByName_ %searchName% _" + (page - 1) + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", npcTemplates.size() > npcIndex + 1 ? "<button value=\"Next\" action=\"bypass _dropMonstersByName_ %searchName% _" + (page + 1) + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchName%", monsterName);
		newHtml = newHtml.replace("%page%", String.valueOf(page));
		return newHtml;
	}

	private static void showDropMonsterDetailsByName(Player player, int monsterId) {
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/dropcalc/monster_details_by_name.htm", player);
		html = replaceMonsterDetails(player, html, monsterId);
//		if (!canTeleToMonster(player, monsterId, false))
//			html = html.replace("%goToNpc%", "<br>");
//		else
//			html = html.replace("%goToNpc%", "<button value=\"Go to Npc\" action=\"bypass _dropMonsterDetailsByName_" + monsterId + "_3\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1" + ".OlympiadWnd_DF_Fight1None\">");

		//CalculateRewardChances.sendUsedImages(html, player);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static void manageButton(Player player, int buttonId, int monsterId) {
		switch (buttonId) {
			case 1:// Show Monster on Map
				final List<Location> locs = CalculateRewardChances.getRandomSpawnsByNpc(monsterId);
				if (locs == null || locs.isEmpty())
					return;

				player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
				player.sendPacket(new SayPacket2(player.getObjectId(), ChatType.COMMANDCHANNEL_ALL, 0, "", "Open Map to see Locations"));

				for (Location loc : locs)
					player.sendPacket(new RadarControlPacket(0, 1, loc));
				break;
			case 2:// Show Drops
				List<NpcInstance> npc = GameObjectsStorage.getNpcs(true, monsterId);
				if (!npc.isEmpty())
					RewardListInfo.showInfo(player, npc.get(0), null, 1);
				break;
//			case 3:// Teleport To Monster
//				if (!canTeleToMonster(player, monsterId, true))
//				{
//					return;
//				}
//				List<NpcInstance> aliveInstance = GameObjectsStorage.getAllByNpcId(monsterId, true);
//				if (!aliveInstance.isEmpty())
//					player.teleToLocation(aliveInstance.get(0).getLoc());
//				else
//					player.sendMessage("Monster isn't alive!");
//				break;
			default:
				break;
		}
	}

//	private static boolean canTeleToMonster(Player player, int monsterId, boolean sendMessage)
//	{
//		if (!player.isInZonePeace())
//		{
//			if (sendMessage)
//				player.sendMessage("You can do it only in safe zone!");
//			return false;
//		}
//
//		if (Olympiad.isRegistered(player) || player.isInOlympiadMode())
//		{
//			if (sendMessage)
//				player.sendMessage("You cannot do it while being registered in Olympiad Battle!");
//			return false;
//		}
//
//		if (Arrays.binarySearch(Config.DROP_CALCULATOR_DISABLED_TELEPORT, monsterId) >= 0)
//		{
//			if (sendMessage)
//				player.sendMessage("You cannot teleport to this Npc!");
//			return false;
//		}
//
//		return true;
//	}

	private static CharSequence getItemIcon(ItemTemplate template) {
		return "<img src=\"" + template.getIcon() + "\" width=32 height=32>";
	}

	private static CharSequence getItemGradeIcon(ItemTemplate template) {
		if (template.getGrade() == ItemGrade.NONE)
			return "";
		if(template.getGrade().extGrade() != template.getGrade())
			return "<img src=\"Symbol.Grade_" + template.getGrade() + "\" width=28 height=14>";
		return "<img src=\"Symbol.Grade_" + template.getGrade() + "\" width=14 height=14>";
	}

	private static CharSequence getName(String name) {
		if (name.length() > 24)
			return "</font><font color=31B404>" + name;
		return name;
	}

	private static String getDropCount(Player player, NpcTemplate monster, int itemId, boolean drop) {
		long[] counts = CalculateRewardChances.getDropCounts(player, monster, drop, itemId);
		String formattedCounts = "[" + counts[0] + "..." + counts[1] + ']';
		if (formattedCounts.length() > 20)
			formattedCounts = "</font><font color=31B404>" + formattedCounts;
		return formattedCounts;
	}

	private static class ItemComparator implements Comparator<ItemTemplate>, Serializable {
		private static final long serialVersionUID = -6389059445439769861L;
		private final Player player;
		private final String search;

		private ItemComparator(Player player, String search) {
			this.player = player;
			this.search = search;
		}

		@Override
		public int compare(ItemTemplate o1, ItemTemplate o2) {
			if (o1.equals(o2))
				return 0;
			if (o1.getName(player).equalsIgnoreCase(search))
				return -1;
			if (o2.getName(player).equalsIgnoreCase(search))
				return 1;

			return Integer.compare(CalculateRewardChances.getDroplistsCountByItemId(o2.getItemId(), true), CalculateRewardChances.getDroplistsCountByItemId(o1.getItemId(), true));
		}
	}

	private static class ItemChanceComparator implements Comparator<CalculateRewardChances.NpcTemplateDrops>, Serializable {
		private static final long serialVersionUID = 6323413829869254438L;
		private final int itemId;
		private final Player player;

		private ItemChanceComparator(Player player, int itemId) {
			this.itemId = itemId;
			this.player = player;
		}

		@Override
		public int compare(CalculateRewardChances.NpcTemplateDrops o1, CalculateRewardChances.NpcTemplateDrops o2) {
			CalculateRewardChances.DropInfo[] dropInfo1 = CalculateRewardChances.getDropInfo(player, o1.template, itemId);
			CalculateRewardChances.DropInfo[] dropInfo2 = CalculateRewardChances.getDropInfo(player, o2.template, itemId);
			if (dropInfo1 == null || dropInfo2 == null)
				return 0;

			CalculateRewardChances.DropInfo info1 = o1.dropNoSpoil ? dropInfo1[0] : dropInfo1[1];
			CalculateRewardChances.DropInfo info2 = o2.dropNoSpoil ? dropInfo2[0] : dropInfo2[1];
			if (info2.chance == info1.chance) {
				if (Math.abs(info2.maxCount) == Math.abs(info1.maxCount)) {
					if (info2.minCount == info1.minCount)
						return o2.template.getName(player).compareTo(o1.template.getName(player));
					return Long.compare(info2.minCount, info1.minCount);
				}
				return Long.compare(Math.abs(info2.maxCount), Math.abs(info1.maxCount));
			}
			return Double.compare(info2.chance, info1.chance);
		}
	}

	private static class MonsterComparator implements Comparator<NpcTemplate>, Serializable {
		private static final long serialVersionUID = 2116090903265145828L;
		private final Player player;
		private final String search;

		private MonsterComparator(Player player, String search) {
			this.player = player;
			this.search = search;
		}

		@Override
		public int compare(NpcTemplate o1, NpcTemplate o2) {
			if (o1.equals(o2))
				return 0;
			if (o1.getName(player).equalsIgnoreCase(search))
				return 1;
			if (o2.getName(player).equalsIgnoreCase(search))
				return -1;

			return o2.getName(player).compareTo(o2.getName(player));
		}
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
	}

	public static void addQuickVar(Player player, String name, Object value) {
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null) {
			quickVars = new ConcurrentHashMap<String, Object>();
			QUICK_VARS.put(player.getObjectId(), quickVars);
		}
		quickVars.put(name, value);
	}

	public static String getQuickVarS(Player player, String name, String... defaultValue) {
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null || !quickVars.containsKey(name)) {
			if (defaultValue.length > 0)
				return defaultValue[0];
			return null;
		}
		return (String) quickVars.get(name);
	}

	private static int getQuickVarI(Player player, String name, int... defaultValue) {
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null || !quickVars.containsKey(name)) {
			if (defaultValue.length > 0)
				return defaultValue[0];
			return -1;
		}
		return ((Integer) quickVars.get(name)).intValue();
	}
}