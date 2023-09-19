package l2s.gameserver.utils;

import com.spreada.utils.chinese.ZHConverter;
import l2s.gameserver.handler.onshiftaction.commons.RewardListInfo;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//import handler.bbs.custom.CalculateRewardChances;
//import handler.onshiftaction.commons.RewardListInfo;
//歡迎界面購買物品
public final class MyUtilsDropCalculator
{
	private static final Logger _log = LoggerFactory.getLogger(Files.class);
	private static final IntObjectMap<Map<String, Object>> QUICK_VARS = new CHashIntObjectMap<Map<String, Object>>();
	private MyUtilsDropCalculator()
	{}
	public static void onBypassFeedback(Player player,String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		player.setSessionVar("add_fav", null);

		switch (cmd)
		{
			case "dropCalc":
				showMainPage(player);
				break;
			case "dropItemsByName":
				if(!st.hasMoreTokens())
				{
					showMainPage(player);
					return;
				}
				String itemName = "";
				while (st.countTokens() > 1)
					itemName += " " + st.nextToken();
				int itemsPage = 1;
				try
				{
					itemsPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
				}
				catch (Exception e)
				{
					player.sendMessage("Error occured, try again later!");
					onBypassFeedback(player, "_dropCalc");
					return;
				}
				itemName = itemName.trim();
				try
				{ //簡轉繁
					itemName = ZHConverter.convert(itemName, ZHConverter.TRADITIONAL);
				}
				catch(Exception e)
				{
				}
				showDropItemsByNamePage(player, itemName, itemsPage);
				break;
		case "dropMonstersByItem"://通过首页物品查询

				int itemId = 1;
				int monstersPage = 1;
				try
				{
					itemId = Integer.parseInt(st.nextToken());
					monstersPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
				}
				catch (Exception e)
				{
					player.sendMessage("Error occured, try again later!");
					onBypassFeedback(player, "_dropCalc");
					return;
				}
				showDropMonstersByItem(player, itemId, monstersPage);
				break;
		case "dropMonsterDetailsByItem"://通过物品查询显示位置和掉落查询
				int monsterId = 1;
				int nextTokn = 0;
				try
				{
					monsterId = Integer.parseInt(st.nextToken());
					if(st.hasMoreTokens())
					{
						nextTokn = Integer.parseInt(st.nextToken());
						manageButton(player, nextTokn, monsterId);
						return;
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Error occured, try again later!");
					onBypassFeedback(player, "_dropCalc");
					return;
				}
				showdropMonsterDetailsByItem(player, monsterId);
				break;
		case "dropMonstersByName"://通过首页怪物查询
				if(!st.hasMoreTokens())
				{
					showMainPage(player);
					return;
				}
				String monsterName = "";
				while (st.countTokens() > 1)
					monsterName += " " + st.nextToken();
				int monsterPage = 1;
				try
				{
					int nexttkn = Integer.parseInt(st.nextToken());
					monsterPage = nexttkn;//'st.hasMoreTokens() ? nexttkn : 1;
				}
				catch (Exception e)
				{
					player.sendMessage("Error occured, try again later!");
					onBypassFeedback(player, "_dropCalc");
					return;
				}
				monsterName = monsterName.trim();
				try
				{ //簡轉繁
					monsterName = ZHConverter.convert(monsterName, ZHConverter.TRADITIONAL);
				}
				catch(Exception e)
				{
				}
				showDropMonstersByName(player, monsterName, monsterPage);
				break;
		case "dropMonsterDetailsByName"://通过怪物查询显示位置和掉落查询
				int chosenMobId = 1;
				try
				{
					chosenMobId = Integer.parseInt(st.nextToken());
					if(st.hasMoreTokens())
					{
						int nexttkn = Integer.parseInt(st.nextToken());
						manageButton(player, nexttkn, chosenMobId);
						return;
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Error occured, try again later!");
					onBypassFeedback(player, "_dropCalc");
					return;
				}
				NpcTemplate template = NpcHolder.getInstance().getTemplate(chosenMobId);
				if (template == null) {
					player.sendMessage("查询的怪物不存在!");
					onBypassFeedback(player, "_dropCalc");
					return ;
				}
				addQuickVar(player, "DCMonsterName", template.getName());
				addQuickVar(player, "DCMonstersPage", 1);
				showDropMonsterDetailsByName(player, chosenMobId);
				break;
			default:
				break;
		}
	}	
	private static void showMainPage(Player player)
	{
		String html = HtmCache.getInstance().getHtml("drop/dropCalcMain.htm", player);//搜寻按钮的主页显示
		showPage(html, player);
	}

	private static void showDropItemsByNamePage(Player player, String itemName, int page)
	{
		addQuickVar(player, "DCItemName", itemName);
		addQuickVar(player, "DCItemsPage", page);
		String html = HtmCache.getInstance().getHtml("drop/dropItemsByName.htm", player);//搜寻物品名称
		html = replaceItemsByNamePage(html, itemName, page);
		showPage(html, player);
	}

	private static String replaceItemsByNamePage(String html, String itemName, int page)
	{
		String newHtml = html;
		List<ItemTemplate> itemsByName = CalculateRewardChances.getItemsByNameContainingString(itemName, true);
		itemsByName.sort(new ItemComparator(itemName));
		int itemIndex = 0;

		StringBuilder content = new StringBuilder();
		for (int i = 0; i < 10; i++)//整个页面最大显示多少个 原始8改9
		{
			itemIndex = i + (page - 1) * 10;//原始8改9
			ItemTemplate item = itemsByName.size() > itemIndex ? itemsByName.get(itemIndex) : null;
			if(item != null)
			{
				content.append("<table width=290 height=20 border=0 cellpadding=0 cellspacing=0 bgcolor=23201E><tr><td fixwidth=32>");
				content.append("<center>" + getItemIcon(item) + "</center></td><td fixwidth=120><center>");
				content.append("<font name=\"__SYSTEMWORLDFONT\" color=31B404>" + getName(item.getName()) + "</font></center></td><td fixwidth=44>");
				content.append("<center><button value=\"详情\" action=\"bypass -h DropCalculator _dropMonstersByItem_" + item.getItemId() + "\" width=55 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center></td></tr></table>");
			}
		}
		newHtml = newHtml.replace("<$content$>", content.toString());
		
		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"上一页\" action=\"bypass -h DropCalculator _dropItemsByName_" + itemName + "_" + (page - 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", itemsByName.size() > itemIndex + 1 ? "<button value=\"下一页\" action=\"bypass -h DropCalculator _dropItemsByName_" + itemName + "_" + (page + 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchItem%", itemName);
		newHtml = newHtml.replace("%page%", String.valueOf(page));

		//WitreFileTxt("12.txt", newHtml);
		
		return newHtml;
	}

	private static void showDropMonstersByItem(Player player, int itemId, int page)
	{
		addQuickVar(player, "DCItemId", itemId);
		addQuickVar(player, "DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtml("drop/dropMonstersByItem.htm", player);
		html = replaceMonstersByItemPage(player, html, itemId, page);
		showPage(html, player);
	}

	private static String replaceMonstersByItemPage(Player player, String html, int itemId, int page)
	{
		String newHtml = html;

		List<CalculateRewardChances.NpcTemplateDrops> templates = CalculateRewardChances.getNpcsByDropOrSpoil(itemId);
		templates.sort(new ItemChanceComparator(player, itemId));
		//Announcements.announceToAll("itemId:" + itemId +  " Size" + templates.size());
		int npcIndex = 0;
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < 10; i++)
		{
			npcIndex = i + (page - 1) * 10;
			CalculateRewardChances.NpcTemplateDrops drops = templates.size() > npcIndex ? templates.get(npcIndex) : null;
			NpcTemplate npc = templates.size() > npcIndex ? templates.get(npcIndex).template : null;
			if(npc != null)
			{
				content.append("<table width=290 height=20 border=0 cellpadding=0 cellspacing=0 bgcolor=0d1b25><tr><td fixwidth=130>");
				content.append("<center><font name=\"__SYSTEMWORLDFONT\" color=31B404>" + getName(npc.getName()) + "</font></center>");
				content.append("</td><td fixwidth=20><center><font name=\"__SYSTEMWORLDFONT\" color=31B404>" + String.valueOf(npc.level) + "</font></center>");
				if(drops.dropNoSpoil)//是回收還是掉落
				{
					content.append("</td><td fixwidth=40><center><font name=\"__SYSTEMWORLDFONT\" color=31B404>" + getDropChance(player, npc, itemId, drops.dropNoSpoil) + "</font></center></td><td fixwidth=44><center>");
				}
				else
				{
					content.append("</td><td fixwidth=40><center><font name=\"__SYSTEMWORLDFONT\" color=FF0000>" + getDropChance(player, npc, itemId, drops.dropNoSpoil) + "</font></center></td><td fixwidth=44><center>");

				}
				content.append("<button value=\"详情\" action=\"bypass -h DropCalculator _dropMonsterDetailsByItem_" + String.valueOf(npc.getId()) + "\" width=55 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center>");
				content.append("</td></tr></table>");
			}
//			newHtml = newHtml.replace("%monsterName" + i + '%', npc != null ? getName(npc.getName()) : "<br>");
//			newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.level) : "<br>");
//			newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? String.valueOf(npc.aggroRange > 0) : "<br>");
//			newHtml = newHtml.replace("%monsterType" + i + '%', npc != null ? drops.dropNoSpoil ? "掉落" : "回收" : "<br>");
//			newHtml = newHtml.replace("%monsterCount" + i + '%', npc != null ? String.valueOf(getDropCount(player, npc, itemId, drops.dropNoSpoil)) : "<br>");
//			newHtml = newHtml.replace("%monsterChance" + i + '%', npc != null ? String.valueOf(getDropChance(player, npc, itemId, drops.dropNoSpoil)) : "<br>");//这里看能否把dropNoSpoil正常掉落白色，回收红色
//			newHtml = newHtml.replace("%showDetails" + i + '%', npc != null ? "<button value=\"详情\" action=\"bypass _dropMonsterDetailsByItem_%monsterId" + i + "%\" width=55 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
//			newHtml = newHtml.replace("%monsterId" + i + '%', npc != null ? String.valueOf(npc.getId()) : "<br>");
		}
		newHtml = newHtml.replace("<$content$>", content.toString());
		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"上一页\" action=\"bypass -h DropCalculator _dropMonstersByItem_%itemChosenId%_" + (page - 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", templates.size() > npcIndex + 1 ? "<button value=\"下一页\" action=\"bypass -h DropCalculator _dropMonstersByItem_%itemChosenId%_" + (page + 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchItem%", getQuickVarS(player, "DCItemName"));
		newHtml = newHtml.replace("%searchItemPage%", String.valueOf(getQuickVarI(player, "DCItemsPage")));
		newHtml = newHtml.replace("%itemChosenId%", String.valueOf(itemId));
		newHtml = newHtml.replace("%monsterPage%", String.valueOf(page));
		//WitreFileTxt("12.txt", newHtml);
		return newHtml;
	}

	private static void showdropMonsterDetailsByItem(Player player, int monsterId)
	{
		String html = HtmCache.getInstance().getHtml("drop/dropMonsterDetailsByItem.htm", player);
		html = replaceMonsterDetails(player, html, monsterId);

		// DO NOT ALLOW TO TELEPORT TO MOBS
		// if (!canTeleToMonster(player, monsterId, false))
		// html = html.replace("%goToNpc%", "<br>");
		// else
		// html = html.replace("%goToNpc%", "<button value=\"Go to Npc\" action=\"bypass
		// _dropMonsterDetailsByItem_"+monsterId+"_3\" width=200 height=30
		// back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\"
		// fore=\"L2UI_ct1.OlympiadWnd_DF_Fight1None\">");

		// CalculateRewardChances.sendUsedImages(html, player);
		showPage(html, player);
	}

	private static String replaceMonsterDetails(Player player, String html, int monsterId) {
		String newHtml = html;
		int itemId = MyUtilsDropCalculator.getQuickVarI(player, "DCItemId", new int[0]);
		NpcTemplate template = NpcHolder.getInstance().getTemplate(monsterId);
		if (template == null) {
			return newHtml;
		}
		newHtml = newHtml.replace("%searchName%", String.valueOf(MyUtilsDropCalculator.getQuickVarS(player, "DCMonsterName", new String[0])));
		newHtml = newHtml.replace("%itemChosenId%", String.valueOf(MyUtilsDropCalculator.getQuickVarI(player, "DCItemId", new int[0])));
		newHtml = newHtml.replace("%monsterPage%", String.valueOf(MyUtilsDropCalculator.getQuickVarI(player, "DCMonstersPage", new int[0])));
		newHtml = newHtml.replace("%monsterId%", String.valueOf(monsterId));
		newHtml = newHtml.replace("%monsterName%", MyUtilsDropCalculator.getName(template.getName()));
		newHtml = newHtml.replace("%monsterLevel%", String.valueOf(template.level));
		newHtml = newHtml.replace("%monsterAggro%", template.aggroRange > 0 ? "<font color=FF0000>\u4e3b\u52d5</font>" : "\u88ab\u52d5");
		if (itemId > 0) {
			newHtml = newHtml.replace("%monsterDropSpecific%", String.valueOf(MyUtilsDropCalculator.getDropChance(player, template, itemId, true)));
			newHtml = newHtml.replace("%monsterSpoilSpecific%", String.valueOf(MyUtilsDropCalculator.getDropChance(player, template, itemId, false)));
		}
		newHtml = newHtml.replace("%monsterDropAll%", String.valueOf(CalculateRewardChances.getDrops(template, true, false).size()));
		newHtml = newHtml.replace("%monsterSpoilAll%", String.valueOf(CalculateRewardChances.getDrops(template, false, true).size()));
		newHtml = newHtml.replace("%spawnCount%", String.valueOf(CalculateRewardChances.getSpawnedCount(monsterId)));
		newHtml = newHtml.replace("%minions%", String.valueOf(template.getMinionData().size()));
		return newHtml;
	}

	private static void showDropMonstersByName(Player player, String monsterName, int page)
	{
		addQuickVar(player, "DCMonsterName", monsterName);
		addQuickVar(player, "DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtml("drop/dropMonstersByName.htm", player);
		html = replaceMonstersByName(html, monsterName, page);
		showPage(html, player);
	}

	private static String replaceMonstersByName(String html, String monsterName, int page)
	{
		String newHtml = html;
		List<NpcTemplate> npcTemplates = CalculateRewardChances.getNpcsContainingString(monsterName);
		npcTemplates = sortMonsters(npcTemplates, monsterName);

		int npcIndex = 0;
		for (int i = 0; i < 10; ++i) {
			npcIndex = i + (page - 1) * 10;
			NpcTemplate npc = null;
			if (page != -1) {
				npc = npcTemplates.size() > npcIndex ? npcTemplates.get(npcIndex) : null;
			}

			newHtml = newHtml.replace("%monsterName" + i + '%', npc != null ? getName(npc.getName()) : "<br>");
			newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.level) : "<br>");
			newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? (npc.aggroRange > 0 ? "<font color=FF0000>主动</font>":"被动") : "<br>");
			//newHtml = newHtml.replace("%monsterDrops" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, true, false).size()) : "<br>");
			//newHtml = newHtml.replace("%monsterSpoils" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, false, true).size()) : "<br>");
			newHtml = newHtml.replace("%showDetails" + i + '%', npc != null ? "<center><button value =\"详情\" action=\"bypass -h DropCalculator _dropMonsterDetailsByName_" + npc.getId() + "\" width=55 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center>"   : "<br>");
		}

		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"上一页\" action=\"bypass -h DropCalculator _dropMonstersByName_%searchName%_" + (page - 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", npcTemplates.size() > npcIndex + 1 ? "<button value=\"下一页\" action=\"bypass -h DropCalculator _dropMonstersByName_%searchName%_" + (page + 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchName%", monsterName);
		newHtml = newHtml.replace("%page%", String.valueOf(page));
		return newHtml;
	}

	private static void showDropMonsterDetailsByName(Player player, int monsterId)
	{
		String html = HtmCache.getInstance().getHtml("drop/dropMonsterDetailsByName.htm", player);
		html = replaceMonsterDetails(player, html, monsterId);
		//		if (!canTeleToMonster(player, monsterId, false))
		//			html = html.replace("%goToNpc%", "<br>");
		//		else
		//			html = html.replace("%goToNpc%", "<button value=\"Go to Npc\" action=\"bypass -h DropCalculator _dropMonsterDetailsByName_" + monsterId + "_3\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1" + ".OlympiadWnd_DF_Fight1None\">");

		//CalculateRewardChances.sendUsedImages(html, player);
		showPage(html, player);
	}

	private static void manageButton(Player player, int buttonId, int monsterId)
	{
		switch(buttonId)
		{
			case 1:// Show Monster on Map
				final List<Location> locs = CalculateRewardChances.getRandomSpawnsByNpc(monsterId);
				if(locs == null || locs.isEmpty())
					return;

				player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
				player.sendPacket(new SayPacket2(player.getObjectId(), ChatType.COMMANDCHANNEL_ALL, "", "打开地图「Alt+M」可查看怪物位置"));

				for(Location loc : locs)
					player.sendPacket(new RadarControlPacket(0, 1, loc));
				break;
			case 2:// Show Drops
				List<NpcInstance> npcs = GameObjectsStorage.getNpcs(false, monsterId);
				if(!npcs.isEmpty())
				{
					RewardListInfo.showInfo(player, npcs.get(0), null, 1);
				}
				break;
			// case 3:// Teleport To Monster
			// if (!canTeleToMonster(player, monsterId, true))
			// {
			// return;
			// }
			// List<NpcInstance> aliveInstance = GameObjectsStorage.getAllByNpcId(monsterId,
			// true);
			// if (!aliveInstance.isEmpty())
			// player.teleToLocation(aliveInstance.get(0).getLoc());
			// else
			// player.sendMessage("Monster isn't alive!");
			// break;
			default:
				break;
		}
	}

	// private static boolean canTeleToMonster(Player player, int monsterId, boolean
	// sendMessage)
	// {
	// if (!player.isInZonePeace())
	// {
	// if (sendMessage)
	// player.sendMessage("You can do it only in safe zone!");
	// return false;
	// }
	//
	// if (Olympiad.isRegistered(player) || player.isInOlympiadMode())
	// {
	// if (sendMessage)
	// player.sendMessage("You cannot do it while being registered in Olympiad
	// Battle!");
	// return false;
	// }
	//
	// if (Arrays.binarySearch(Config.DROP_CALCULATOR_DISABLED_TELEPORT, monsterId)
	// >= 0)
	// {
	// if (sendMessage)
	// player.sendMessage("You cannot teleport to this Npc!");
	// return false;
	// }
	//
	// return true;
	// }

	private static CharSequence getItemIcon(ItemTemplate template)
	{
		return "<img src=\"" + template.getIcon() + "\" width=32 height=32>";
	}

	private static CharSequence getItemGradeIcon(ItemTemplate template)
	{
		if (template.getGrade() == ItemGrade.NONE)
			return "";
		return "<img src=\"L2UI_CT1.Icon_DF_ItemGrade_" + template.getGrade() + "\" width=16 height=16>";
	}

	private static CharSequence getName(String name)
	{
		if (name.length() > 24)
			return "</font><font color=31B404>" + name;
		return name;
	}

	private static String getDropCount(Player player, NpcTemplate monster, int itemId, boolean drop)
	{
		long[] counts = CalculateRewardChances.getDropCounts(player, monster, drop, itemId);
		String formattedCounts = "[" + counts[0] + "..." + counts[1] + ']';
		if (formattedCounts.length() > 20)
			formattedCounts = "</font><font color=31B404>" + formattedCounts;
		return formattedCounts;
	}

	private static String getDropChance(Player player, NpcTemplate monster, int itemId, boolean drop)
	{
		String chance = CalculateRewardChances.getDropChance(player, monster, drop, itemId);
		return formatDropChance(chance);
	}

	public static String formatDropChance(String chance)
	{
		String realChance = chance;
		if (realChance.length() - realChance.indexOf('.') > 4)
			realChance = realChance.substring(0, realChance.indexOf('.') + 5);

		if (realChance.endsWith(".0"))
			realChance = realChance.substring(0, realChance.length() - 2);

		return realChance + '%';
	}

	private static class ItemComparator implements Comparator<ItemTemplate>, Serializable
	{
		private static final long serialVersionUID = -6389059445439769861L;
		private final String search;

		private ItemComparator(String search)
		{
			this.search = search;
		}

		@Override
		public int compare(ItemTemplate o1, ItemTemplate o2)
		{
			if (o1.equals(o2))
				return 0;
			if (o1.getName().equalsIgnoreCase(search))
				return -1;
			if (o2.getName().equalsIgnoreCase(search))
				return 1;

			return Integer.compare(CalculateRewardChances.getDroplistsCountByItemId(o2.getItemId(), true), CalculateRewardChances.getDroplistsCountByItemId(o1.getItemId(), true));
		}
	}

	private static class ItemChanceComparator
			implements Comparator<CalculateRewardChances.NpcTemplateDrops>, Serializable
	{
		private static final long serialVersionUID = 6323413829869254438L;
		private final int itemId;
		private final Player player;

		private ItemChanceComparator(Player player, int itemId)
		{
			this.itemId = itemId;
			this.player = player;
		}

		@Override
		public int compare(CalculateRewardChances.NpcTemplateDrops o1, CalculateRewardChances.NpcTemplateDrops o2)
		{
			BigDecimal maxDrop1 = BigDecimal.valueOf(CalculateRewardChances.getDropCounts(player, o1.template, o1.dropNoSpoil, itemId)[1]);
			BigDecimal maxDrop2 = BigDecimal.valueOf(CalculateRewardChances.getDropCounts(player, o2.template, o2.dropNoSpoil, itemId)[1]);
			BigDecimal chance1 = new BigDecimal(CalculateRewardChances.getDropChance(player, o1.template, o1.dropNoSpoil, itemId));
			BigDecimal chance2 = new BigDecimal(CalculateRewardChances.getDropChance(player, o2.template, o2.dropNoSpoil, itemId));

			int compare = chance2.multiply(maxDrop2).compareTo(chance1.multiply(maxDrop1));
			if (compare == 0)
				return o2.template.getName().compareTo(o1.template.getName());
			return compare;
		}
	}

	private static List<NpcTemplate> sortMonsters(List<NpcTemplate> npcTemplates, String monsterName)
	{
		Collections.sort(npcTemplates, new MonsterComparator(monsterName));
		return npcTemplates;
	}

	private static class MonsterComparator implements Comparator<NpcTemplate>, Serializable
	{
		private static final long serialVersionUID = 2116090903265145828L;
		private final String search;

		private MonsterComparator(String search)
		{
			this.search = search;
		}

		@Override
		public int compare(NpcTemplate o1, NpcTemplate o2)
		{
			if (o1.equals(o2))
				return 0;
			if (o1.getName().equalsIgnoreCase(search))
				return 1;
			if (o2.getName().equalsIgnoreCase(search))
				return -1;

			return o2.getName().compareTo(o2.getName());
		}
	}


	public static void addQuickVar(Player player, String name, Object value)
	{
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null)
		{
			quickVars = new ConcurrentHashMap<String, Object>();
			QUICK_VARS.put(player.getObjectId(), quickVars);
		}
		quickVars.put(name, value);
	}

	public static String getQuickVarS(Player player, String name, String... defaultValue)
	{
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null || !quickVars.containsKey(name))
		{
			if (defaultValue.length > 0)
				return defaultValue[0];
			return null;
		}
		return (String) quickVars.get(name);
	}

	private static int getQuickVarI(Player player, String name, int... defaultValue)
	{
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null || !quickVars.containsKey(name))
		{
			if (defaultValue.length > 0)
				return defaultValue[0];
			return -1;
		}
		return ((Integer) quickVars.get(name)).intValue();
	}

	private static void showPage(String html, Player player)
	{
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}

	private static void WitreFileTxt(String WriteFileName, String WiteTxt)
	{
		try
		{ // 防止檔案建立或讀取失敗，用catch捕捉錯誤並列印，也可以throw
			/* 寫入Txt檔案 */
			File writename = new File(WriteFileName); // 相對路徑，如果沒有則要建立一個新的output。txt檔案
			writename.createNewFile(); // 建立新檔案
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(WiteTxt); // \r\n即為換行
			out.flush(); // 把快取區內容壓入檔案
			out.close(); // 最後記得關閉檔案
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
