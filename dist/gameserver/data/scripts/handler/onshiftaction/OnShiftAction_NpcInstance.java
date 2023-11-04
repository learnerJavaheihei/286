package handler.onshiftaction;

import handler.onshiftaction.commons.RewardListInfo;

import java.util.*;
import java.util.stream.Collectors;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import org.apache.commons.text.TextStringBuilder;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.Util;

/**
 * @author VISTALL
 * @date 2:43/19.08.2011
 */
public class OnShiftAction_NpcInstance extends ScriptOnShiftActionHandler<NpcInstance>
{
	@Override
	public Class<NpcInstance> getClazz()
	{
		return NpcInstance.class;
	}

	@Override
	public boolean call(NpcInstance npc, Player player)
	{
		return showMain(player, npc, player.isGM());
	}

	@Bypass("actions.OnActionShift:showShort")
	public void showShort(Player player, NpcInstance npc, String[] par)
	{
		showMain(player, npc, false);
	}

	private boolean showMain(Player player, NpcInstance npc, boolean full)
	{
		if(npc == null)
			return false;

		// Для мертвых мобов не показываем табличку, иначе спойлеры плачут
		if((npc.noShiftClick() || npc.isDead()) && !player.isGM())
			return false;

		if(!Config.ALLOW_NPC_SHIFTCLICK && !player.isGM())
		{
			if(Config.ALT_GAME_SHOW_DROPLIST)
			{
				droplist(player, npc, null);
				return true;
			}
			return false;
		}

		HtmlMessage msg = new HtmlMessage(npc);
		msg.setFile("scripts/actions/player.L2NpcInstance.onActionShift." + (full ? "full.htm" : "htm"));

		if(full)
		{
			msg.replace("%class%", String.valueOf(npc.getClass().getSimpleName()));
			msg.replace("%id%", String.valueOf(npc.getNpcId()));
			msg.replace("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
			msg.replace("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
			msg.replace("%pevs%", String.valueOf(npc.getPEvasionRate(null)));
			msg.replace("%pacc%", String.valueOf(npc.getPAccuracy()));
			msg.replace("%mevs%", String.valueOf(npc.getMEvasionRate(null)));
			msg.replace("%macc%", String.valueOf(npc.getMAccuracy()));
			msg.replace("%pcrt%", String.valueOf(npc.getPCriticalHit(null)));
			msg.replace("%mcrt%", String.valueOf(npc.getMCriticalHit(null, null)));
			msg.replace("%aspd%", String.valueOf(npc.getPAtkSpd()));
			msg.replace("%cspd%", String.valueOf(npc.getMAtkSpd()));
			msg.replace("%currentMP%", String.valueOf(npc.getCurrentMp()));
			msg.replace("%currentHP%", String.valueOf(npc.getCurrentHp()));
			msg.replace("%loc%", npc.getSpawn() == null ? "" : npc.getSpawn().getName());
			msg.replace("%dist%", String.valueOf(npc.getDistance3D(player)));
			msg.replace("%killed%", String.valueOf(0));//TODO [G1ta0] убрать
			msg.replace("%spReward%", String.valueOf(npc.getSpReward()));
			msg.replace("%xyz%", npc.getLoc().x + " " + npc.getLoc().y + " " + npc.getLoc().z);
			msg.replace("%ai_type%", npc.getAI().getClass().getSimpleName());
			msg.replace("%direction%", PositionUtils.getDirectionTo(npc, player).toString().toLowerCase());
			msg.replace("%respawn%", String.valueOf(npc.getSpawn() != null ? (npc.getSpawn().getRespawnPattern() == null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : npc.getSpawn().getRespawnPattern().toString()) : "0"));
			msg.replace("%factionId%", String.valueOf(npc.getFaction()));
			msg.replace("%aggro%", String.valueOf(npc.getAggroRange()));
			msg.replace("%pDef%", String.valueOf(npc.getPDef(null)));
			msg.replace("%mDef%", String.valueOf(npc.getMDef(null, null)));
			msg.replace("%pAtk%", String.valueOf(npc.getPAtk(null)));
			msg.replace("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
			msg.replace("%runSpeed%", String.valueOf(npc.getRunSpeed()));
			msg.replace("%hp_regen%", String.valueOf(npc.getHpRegen()));
			msg.replace("%mp_regen%", String.valueOf(npc.getMpRegen()));

			// Дополнительная инфа для ГМов
			if(player.isGM())
				msg.replace("%AI%", String.valueOf(npc.getAI()) + ",<br1>active: " + npc.getAI().isActive() + ",<br1>intention: " + npc.getAI().getIntention());
			else
				msg.replace("%AI%", "");

			TextStringBuilder b = new TextStringBuilder("");
			for(Event e : npc.getEvents())
				b.append(e.toString()).append(";");

			msg.replace("%event%", b.toString());
		}

		msg.replace("<?npc_name?>", nameNpc(npc));
		msg.replace("<?id?>", String.valueOf(npc.getNpcId()));
		msg.replace("<?level?>", String.valueOf(npc.getLevel()));
		msg.replace("<?max_hp?>", String.valueOf(npc.getMaxHp()));
		msg.replace("<?max_mp?>", String.valueOf(npc.getMaxMp()));
		msg.replace("<?xp_reward?>", String.valueOf(npc.getExpReward()));
		msg.replace("<?sp_reward?>", String.valueOf(npc.getSpReward()));
		msg.replace("<?aggresive?>", new CustomMessage(npc.getAggroRange() > 0 ? "YES" : "NO").toString(player));

		player.sendPacket(msg);
		return true;
	}

	@Bypass("actions.OnActionShift:droplist")
	public void droplist(Player player, NpcInstance npc, String[] par)
	{
		if(player == null || npc == null)
			return;

		if(Config.ALT_GAME_SHOW_DROPLIST || player.isGM())
		{
			if(par == null || par.length == 0)
				RewardListInfo.showInfo(player, npc, null, 1);
			else if(par.length == 1)
				RewardListInfo.showInfo(player, npc, RewardType.valueOf(par[0]), 1);
			else if(par.length > 1)
				RewardListInfo.showInfo(player, npc, RewardType.valueOf(par[0]), Integer.parseInt(par[1]));
		}
	}

	@Bypass("actions.OnActionShift:stats")
	public void stats(Player player, NpcInstance npc, String[] par)
	{
		if(npc == null)
			return;

		HtmlMessage msg = new HtmlMessage(npc);
		msg.setFile("scripts/actions/player.L2NpcInstance.stats.htm");

		msg.replace("%name%", nameNpc(npc));
		msg.replace("%level%", String.valueOf(npc.getLevel()));
		msg.replace("%factionId%", String.valueOf(npc.getFaction()));
		msg.replace("%aggro%", String.valueOf(npc.getAggroRange()));
		msg.replace("%race%", getNpcRaceById(npc.getTemplate().getRace()));
		msg.replace("%maxHp%", String.valueOf(npc.getMaxHp()));
		msg.replace("%maxMp%", String.valueOf(npc.getMaxMp()));
		msg.replace("%pDef%", String.valueOf(npc.getPDef(null)));
		msg.replace("%mDef%", String.valueOf(npc.getMDef(null, null)));
		msg.replace("%pAtk%", String.valueOf(npc.getPAtk(null)));
		msg.replace("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
		msg.replace("%paccuracy%", String.valueOf(npc.getPAccuracy()));
		msg.replace("%pevasionRate%", String.valueOf(npc.getPEvasionRate(null)));
		msg.replace("%pcriticalHit%", String.valueOf(npc.getPCriticalHit(null)));
		msg.replace("%maccuracy%", String.valueOf(npc.getMAccuracy()));
		msg.replace("%mevasionRate%", String.valueOf(npc.getMEvasionRate(null)));
		msg.replace("%mcriticalHit%", String.valueOf(npc.getMCriticalHit(null, null)));
		msg.replace("%runSpeed%", String.valueOf(npc.getRunSpeed()));
		msg.replace("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
		msg.replace("%pAtkSpd%", String.valueOf(npc.getPAtkSpd()));
		msg.replace("%mAtkSpd%", String.valueOf(npc.getMAtkSpd()));

		player.sendPacket(msg);
	}

	@Bypass("actions.OnActionShift:quests")
	public void quests(Player player, NpcInstance npc, String[] par)
	{
		if(player == null || npc == null)
			return;

		TextStringBuilder dialog = new TextStringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><br>");

		Map<QuestEventType, Set<Quest>> list = npc.getTemplate().getQuestEvents();
		for(Map.Entry<QuestEventType, Set<Quest>> entry : list.entrySet())
		{
			for(Quest q : entry.getValue())
				dialog.append(entry.getKey()).append(" ").append(q.getClass().getSimpleName()).append("<br1>");
		}

		dialog.append("</body></html>");

		HtmlMessage msg = new HtmlMessage(npc);
		msg.setHtml(dialog.toString());
		player.sendPacket(msg);
	}

	@Bypass("actions.OnActionShift:skills")
	public void skills(Player player, NpcInstance npc, String[] par)
	{
		if(player == null || npc == null)
			return;

		TextStringBuilder dialog = new TextStringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center>");

		Collection<SkillEntry> list = npc.getAllSkills();
		if(list != null && !list.isEmpty())
		{
			dialog.append("<br><font color=\"LEVEL\">Active:</font><br>");
			for(SkillEntry s : list)
			{
				if(s.getTemplate().isActive())
					dialog.append(s.getName(player)).append(" <font color=\"LEVEL\">Id: ").append(s.getId()).append(" Level: ").append(s.getLevel()).append("</font><br1>");
			}

			dialog.append("<br><font color=\"LEVEL\">Passive:</font><br>");
			for(SkillEntry s : list)
			{
				if(!s.getTemplate().isActive())
					dialog.append(s.getName(player)).append(" <font color=\"LEVEL\">Id: ").append(s.getId()).append(" Level: ").append(s.getLevel()).append("</font><br1>");
			}
		}

		dialog.append("</body></html>");

		HtmlMessage msg = new HtmlMessage(npc);
		msg.setHtml(dialog.toString());
		player.sendPacket(msg);
	}

	@Bypass("actions.OnActionShift:effects")
	public void effects(Player player, NpcInstance npc, String[] par)
	{
		if(player == null || npc == null)
			return;

		TextStringBuilder dialog = new TextStringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><br>");

		for(Abnormal e : npc.getAbnormalList())
			dialog.append(e.getSkill().getName(player)).append("<br1>");

		dialog.append("<br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h htmbypass_actions.OnActionShift:effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");

		HtmlMessage msg = new HtmlMessage(npc);
		msg.setHtml(dialog.toString());
		player.sendPacket(msg);
	}

	@Bypass("actions.OnActionShift:resists")
	public void resists(Player player, NpcInstance npc, String[] par)
	{
		if(player == null || npc == null)
			return;

		TextStringBuilder dialog = new TextStringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><table width=\"80%\">");

		boolean hasResist;

		hasResist = addResist(dialog, "Fire", npc.getStat().calc(Stats.DEFENCE_FIRE, 0, null, null));
		hasResist |= addResist(dialog, "Wind", npc.getStat().calc(Stats.DEFENCE_WIND, 0, null, null));
		hasResist |= addResist(dialog, "Water", npc.getStat().calc(Stats.DEFENCE_WATER, 0, null, null));
		hasResist |= addResist(dialog, "Earth", npc.getStat().calc(Stats.DEFENCE_EARTH, 0, null, null));
		hasResist |= addResist(dialog, "Light", npc.getStat().calc(Stats.DEFENCE_HOLY, 0, null, null));
		hasResist |= addResist(dialog, "Darkness", npc.getStat().calc(Stats.DEFENCE_UNHOLY, 0, null, null));
		hasResist |= addResist(dialog, "Bleed", npc.getStat().calc(Stats.DEFENCE_TRAIT_BLEED));
		hasResist |= addResist(dialog, "Poison", npc.getStat().calc(Stats.DEFENCE_TRAIT_POISON));
		hasResist |= addResist(dialog, "Stun", npc.getStat().calc(Stats.DEFENCE_TRAIT_SHOCK));
		hasResist |= addResist(dialog, "Root", npc.getStat().calc(Stats.DEFENCE_TRAIT_HOLD));
		hasResist |= addResist(dialog, "Sleep", npc.getStat().calc(Stats.DEFENCE_TRAIT_SLEEP));
		hasResist |= addResist(dialog, "Paralyze", npc.getStat().calc(Stats.DEFENCE_TRAIT_PARALYZE));
		hasResist |= addResist(dialog, "Mental", npc.getStat().calc(Stats.DEFENCE_TRAIT_DERANGEMENT));
		hasResist |= addResist(dialog, "Debuff", npc.getStat().calc(Stats.RESIST_ABNORMAL_DEBUFF, 0, null, null));
		hasResist |= addResist(dialog, "Cancel", npc.getStat().calc(Stats.CANCEL_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Sword", npc.getStat().calc(Stats.DEFENCE_TRAIT_SWORD));
		hasResist |= addResist(dialog, "Dual Sword", npc.getStat().calc(Stats.DEFENCE_TRAIT_DUAL));
		hasResist |= addResist(dialog, "Blunt", npc.getStat().calc(Stats.DEFENCE_TRAIT_BLUNT));
		hasResist |= addResist(dialog, "Dagger", npc.getStat().calc(Stats.DEFENCE_TRAIT_DAGGER));
		hasResist |= addResist(dialog, "Bow", npc.getStat().calc(Stats.DEFENCE_TRAIT_BOW));
		hasResist |= addResist(dialog, "Crossbow", npc.getStat().calc(Stats.DEFENCE_TRAIT_CROSSBOW));
		hasResist |= addResist(dialog, "2H Crossbow", npc.getStat().calc(Stats.DEFENCE_TRAIT_TWOHANDCROSSBOW));
		hasResist |= addResist(dialog, "Polearm", npc.getStat().calc(Stats.DEFENCE_TRAIT_POLE));
		hasResist |= addResist(dialog, "Fist", npc.getStat().calc(Stats.DEFENCE_TRAIT_FIST));

		if(!hasResist)
			dialog.append("</table>No resists</body></html>");
		else
			dialog.append("</table></body></html>");

		HtmlMessage msg = new HtmlMessage(npc);
		msg.setHtml(dialog.toString());
		player.sendPacket(msg);
	}

//	@Bypass("actions.OnActionShift:aggro")
//	public void aggro(Player player, NpcInstance npc, String[] par)
//	{
//		if(player == null || npc == null)
//			return;
//
//		TextStringBuilder dialog = new TextStringBuilder("<html><body><table width=\"80%\"><tr><td>Attacker</td><td>Damage</td><td>Hate</td></tr>");
//
//		Set<AggroList.HateInfo> set = new TreeSet<AggroList.HateInfo>(AggroList.HateComparator.getInstance());
//		set.addAll(npc.getAggroList().getCharMap().values());
//		for(AggroList.HateInfo aggroInfo : set)
//			dialog.append("<tr><td>").append(aggroInfo.attacker.getName()).append("</td><td>").append(aggroInfo.damage).append("</td><td>").append(aggroInfo.hate).append("</td></tr>");
//
//		dialog.append("</table><br><center><button value=\"");
//		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
//		dialog.append("\" action=\"bypass -h htmbypass_actions.OnActionShift:aggro\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");
//
//		HtmlMessage msg = new HtmlMessage(npc);
//		msg.setHtml(dialog.toString());
//		player.sendPacket(msg);
//	}
	@Bypass("actions.OnActionShift:aggro")
	public void aggro(Player player, NpcInstance npc, String[] par)
	{
	if(player == null || npc == null)
		return;
	int page = 1;
	int pages = 1;
	TextStringBuilder dialog = new TextStringBuilder("<html><body><title>伤害資訊</title><table><tr><td>");
	dialog.append("<table  align=center valign=top width=292 border=0 cellspacing=0 cellpadding=0>\n" +
			"<tr><td align=center width=242>"+npc.getTemplate().getName(player)+"</td>" +
			"<td align=right>" +
			"<button value=\"返回\" action=\"bypass -h htmbypass_actions.OnActionShift:showShort\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"50\" height=\"22\"/>\n" +
			"</td>" +
			"</tr>" +
			"</table>");
	dialog.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=280>");
	dialog.append("<tr><td valign=\"top\" align=\"center\">" );
	dialog.append("<center><img src=\"L2UI.SquareWhite\" width=292 height=1></center>\n" +
			"<table bgcolor=333333 width=300><tr>" );
	dialog.append("<td width=30 align='center'><center><font color=\"FFFF00\">排名</font></center></td>");
	dialog.append("<td width=80 align='center'><center><font color=\"FFFF00\">玩家名称</font></center></td>");
	dialog.append("<td width=100 align='center'><center><font color=\"FFFF00\">累计造成伤害</font></center></td>");
	dialog.append("<td width=70 align='center'><center><font color=\"FFFF00\">怨恨值</font></center></td>");
	dialog.append("</tr></table>\n" +
			"<center><img src=\"L2UI.SquareWhite\" width=292 height=1></center>");
	dialog.append("<table border=0 cellpadding=5 cellspacing=3 width=280 valign=top>");
	try{
		Set<AggroList.HateInfo> set = new TreeSet<AggroList.HateInfo>(AggroList.HateComparator.getInstance());
		set.addAll(npc.getAggroList().getCharMap().values());
		List<AggroList.HateInfo> collect = new ArrayList<>(set);
		Comparator<AggroList.HateInfo> comparator = new Comparator<AggroList.HateInfo>() {
			@Override
			public int compare(AggroList.HateInfo o1, AggroList.HateInfo o2) {
				return o2.damage - o1.damage;
			}
		};
		if (collect != null && collect.size() > 0) {
			collect.sort(comparator);
			// 总个数
			int nums = collect.size();
			// 每页的个数
			int pageSize = 10;
			// 点击时的 总页数
			pages = nums % pageSize == 0 ? nums / pageSize : nums / pageSize + 1;

			if (par.length == 2) {
				page = Integer.parseInt(par[1]);
			}
			if (par.length == 3) {
				if (par[2].equals("prev")) {
					page = Integer.parseInt(par[1]) -1;
					page = page ==0 ? 1 : page;
				}else if (par[2].equals("next")) {
					page = Integer.parseInt(par[1]) +1;
					page = Math.min(page, pages);
				}
			}

			// 当前页数
			int pageStart = page>0 ? (page - 1) * pageSize:0; //6
			int pageEnd = (nums -(page-1) * pageSize) <= pageSize ?  (nums -(page-1) * pageSize)+pageStart-1 :pageStart+pageSize-1; //

			for (int i = pageStart; i <= pageEnd; i++) {
				if (npc.getCurrentHpPercents() != 100.){
					dialog.append("<tr><td width=30 align='center'><center>")
							.append((i+1 == 1) ? "<font color=\"FFFF00\">" : ((i+1<=3)?"<font color=\"FF4500\">":"<font color=''>")) // 第一名金色 二三名红色 以下白色
							.append(i+1)
							.append("</font></center></td>")
							.append("<td width=80><center>")
							.append(collect.get(i).attacker.getName())
							.append("</center></td><td width=100><center>")
							.append(collect.get(i).damage)
							.append("</center></td><td width=70><center>")
							.append(collect.get(i).hate)
							.append("</center></td></tr>");
				}
			}
		}
	}finally {
		dialog.append("</table></td></tr></table><br><center>");
		// 分页
		dialog.append("<table valign=\"BOTTOM\">");
		dialog.append("<tr><td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:aggro page 1\" width=15 height=15 back=\"L2UI_CH3.ScrollBarLeftOnBtn\" fore=\"L2UI_CH3.ScrollBarLeftBtn\"></td>");
		dialog.append("<td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:aggro page ").append(String.valueOf(page)).append(" prev\" width=15 height=15 back=\"L2UI_CH3.prev1_down\" fore=\"L2UI_CH3.prev1\"></td>");
		dialog.append("<td>").append(page).append("</td>");
		dialog.append("<td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:aggro page ").append(String.valueOf(page)).append(" next\" width=15 height=15 back=\"L2UI_CH3.next1_down\" fore=\"L2UI_CH3.next1\"></td>");
		dialog.append("<td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:aggro page ").append(String.valueOf(pages)).append("\" width=15 height=15 back=\"L2UI_CH3.ScrollBarRightOnBtn\" fore=\"L2UI_CH3.ScrollBarRightBtn\"></td>");
		dialog.append("</tr></table></center>");
		// 刷新
		dialog.append("<center><table valign=\"BOTTOM\"><tr><td><button value=\"");
		dialog.append(player.isLangRus() ? "刷新" : "刷新");
		dialog.append("\" action=\"bypass -h htmbypass_actions.OnActionShift:aggro\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></td></tr></table></center></td></tr></table></body></html>");
		HtmlMessage msg = new HtmlMessage(npc);
		msg.setHtml(dialog.toString());
		player.sendPacket(msg);
	}

	}

	@Bypass("actions.OnActionShift:belonger")
	public void belonger(Player player, NpcInstance npc, String[] par) {
		if (player == null || npc == null)
			return;

		int page = 1;
		int pages = 1;
		TextStringBuilder dialog = new TextStringBuilder("<html><body><title>掉落歸屬資訊</title><table><tr><td>");
		dialog.append("<table  align=center valign=top width=292 border=0 cellspacing=0 cellpadding=0>\n" +
				"<tr><td align=center width=242>"+npc.getTemplate().getName(player)+"</td>" +
				"<td align=right>" +
				"<button value=\"返回\" action=\"bypass -h htmbypass_actions.OnActionShift:showShort\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"50\" height=\"22\"/>\n" +
				"</td>" +
				"</tr>" +
				"</table>");
		dialog.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=280>");
		dialog.append("<tr><td valign=\"top\" align=\"center\">\n");
		dialog.append("<center><img src=\"L2UI.SquareWhite\" width=292 height=1></center>\n" +
				"<table bgcolor=333333 width=280 height=20><tr>");
		dialog.append("<td width=30 align='center'><center><font color=\"FFFF00\">排名</font></center></td>");
		dialog.append("<td width=80 align='center'><center><font color=\"FFFF00\">队   伍</font></center></td>");
		dialog.append("<td width=100 align='center'><center><font color=\"FFFF00\">队伍总伤害</font></center></td>");
		dialog.append("<td width=70 align='center'><center><font color=\"FFFF00\">掉落归属</font></center></td>");
		dialog.append("</tr></table>\n" +
				"<center><img src=\"L2UI.SquareWhite\" width=292 height=1></center>\n" +
				"<br>");
		dialog.append("<table border=0 cellpadding=5 cellspacing=3 width=280 valign=top>");


		try {
			// map<leaderName,damage> 按照伤害 从大到小 来排行 sort start
			Map<String, Integer> map = new HashMap<>();

			Collection<AggroList.PartyDamage> partyDamages = npc.getAggroList().getPartyDamages();
			for (AggroList.PartyDamage partyDamage : partyDamages) {
				//
				if (partyDamage.party.getPartyLeader() != null && partyDamage.damage != 0) {
					map.put(partyDamage.party.getPartyLeader().getName(), partyDamage.damage);
				}
			}
			// 如果 玩家没有队伍 单独列出来
			for (Map.Entry<Creature, AggroList.HateInfo> creatureHateInfoEntry : npc.getAggroList().getCharMap().entrySet()) {
				if (!creatureHateInfoEntry.getKey().getPlayer().isInParty() && creatureHateInfoEntry.getValue().damage != 0) {
					map.put(creatureHateInfoEntry.getKey().getPlayer().getName() + "_single", creatureHateInfoEntry.getValue().damage);
				}
			}

			List<Map.Entry<String, Integer>> collect = new ArrayList<>(map.entrySet());
			Comparator<Map.Entry<String, Integer>> comparator = new Comparator<Map.Entry<String, Integer>>() {
				@Override
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			};
			if (collect != null && collect.size() > 0) {
				collect.sort(comparator);
				// sort end
				Map<String, Integer> sortMap = collect.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				// map<leaderName,damage> 按照伤害 从大到小
				// 得到第一個 也就是最高傷害的隊伍
				String maxDamageParty = null;
				maxDamageParty = collect.get(0).getKey();
				String finalMaxDamageParty = maxDamageParty;

				// 总个数
				int nums = collect.size();
				// 每页的个数
				int pageSize = 10;
				// 点击时的 总页数
				pages = nums % pageSize == 0 ? nums / pageSize : nums / pageSize + 1;

				if (par.length == 2) {
					page = Integer.parseInt(par[1]);
				}
				if (par.length == 3) {
					if (par[2].equals("prev")) {
						page = Integer.parseInt(par[1]) -1;
						page = page ==0 ? 1 : page;
					}else if (par[2].equals("next")) {
						page = Integer.parseInt(par[1]) +1;
						page = Math.min(page, pages);
					}
				}

				// 当前页数
				int pageStart = page>0 ? (page - 1) * pageSize:0; //6
				int pageEnd = (nums -(page-1) * pageSize) <= pageSize ?  (nums -(page-1) * pageSize)+pageStart-1 :pageStart+pageSize-1; //

				Party party = player.getParty();
				String partyLeaderName = "";
				if (party!=null) {
					partyLeaderName = party.getPartyLeader().getName();
				}
				for (int i = pageStart; i <= pageEnd; i++) {
					dialog.append("<tr><td width=30 align='center'><center>")
							.append((i + 1 == 1) ? "<font color=\"FFFF00\">" : ((i + 1 <= 3) ? "<font color=\"FF4500\">" : "<font color=''>")) // 第一名金色 二三名红色 以下白色
							.append(i + 1)
							.append("</font></center></td>")
							.append("<td width=80 align='center'><center>")
							// 如果名字 是当前玩家 高亮 或者是玩家的队长
							.append((collect.get(i).getKey().equals(player.getName()) || collect.get(i).getKey().equals(player.getName() + "_single") || collect.get(i).getKey().equals(partyLeaderName)) ? "<font color=\"FF0000\">" : "")
							.append((collect.get(i).getKey() != null && collect.get(i).getKey().contains("_single")) ? collect.get(i).getKey().replace("_single", "") : collect.get(i).getKey())
							.append((collect.get(i).getKey() != null && collect.get(i).getKey().contains("_single")) ? " 玩家" : (collect.get(i).getKey() == null ? "" : "的队伍"))
							.append((collect.get(i).getKey().equals(player.getName()) || collect.get(i).getKey().equals(player.getName() + "_single") || collect.get(i).getKey().equals(partyLeaderName)) ? "</font>" : "")
							.append("</center></td><td width=100 align=right valign=right><center><font color=''>")
							.append(collect.get(i).getValue())
							.append("</font></center></td><td width=70 align='center'>")
							.append("<center><font color=\"ff4500\">")
							.append(collect.get(i).getKey().equals(finalMaxDamageParty) ? "归属者" : " ")
							.append("</font></center></td></tr>");
				}
			}
		} finally {

			dialog.append("</table></td></tr></table><br><center>");
			// 分页
			dialog.append("<table valign=\"BOTTOM\">");
			dialog.append("<tr><td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:belonger page 1\" width=15 height=15 back=\"L2UI_CH3.ScrollBarLeftOnBtn\" fore=\"L2UI_CH3.ScrollBarLeftBtn\"></td>");
			dialog.append("<td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:belonger page ").append(String.valueOf(page)).append(" prev\" width=15 height=15 back=\"L2UI_CH3.prev1_down\" fore=\"L2UI_CH3.prev1\"></td>");
			dialog.append("<td>").append(page).append("</td>");
			dialog.append("<td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:belonger page ").append(String.valueOf(page)).append(" next\" width=15 height=15 back=\"L2UI_CH3.next1_down\" fore=\"L2UI_CH3.next1\"></td>");
			dialog.append("<td><button value=\" \" action=\"bypass -h htmbypass_actions.OnActionShift:belonger page ").append(String.valueOf(pages)).append("\" width=15 height=15 back=\"L2UI_CH3.ScrollBarRightOnBtn\" fore=\"L2UI_CH3.ScrollBarRightBtn\"></td>");
			dialog.append("</tr></table></center>");
			// 刷新
			dialog.append("<center><table valign=\"BOTTOM\"><tr><td><button value=\"");
			dialog.append(player.isLangRus() ? "刷新" : "刷新");
			dialog.append("\" action=\"bypass -h htmbypass_actions.OnActionShift:belonger\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></td></tr></table></center></td></tr></table></body></html>");
			HtmlMessage msg = new HtmlMessage(npc);
			msg.setHtml(dialog.toString());
			player.sendPacket(msg);
		}

	}

	private static boolean addResist(TextStringBuilder dialog, String name, double val)
	{
		if (val == 0)
			return false;

		dialog.append("<tr><td>").append(name).append("</td><td>");
		if (val == Double.POSITIVE_INFINITY)
			dialog.append("MAX");
		else if (val == Double.NEGATIVE_INFINITY)
			dialog.append("MIN");
		else
		{
			dialog.append(String.valueOf((int)val));
			dialog.append("</td></tr>");
			return true;
		}

		dialog.append("</td></tr>");
		return true;
	}

	private static String getNpcRaceById(int raceId)
	{
		switch(raceId)
		{
			case 1:
				return "Undead";
			case 2:
				return "Magic Creatures";
			case 3:
				return "Beasts";
			case 4:
				return "Animals";
			case 5:
				return "Plants";
			case 6:
				return "Humanoids";
			case 7:
				return "Spirits";
			case 8:
				return "Angels";
			case 9:
				return "Demons";
			case 10:
				return "Dragons";
			case 11:
				return "Giants";
			case 12:
				return "Bugs";
			case 13:
				return "Fairies";
			case 14:
				return "Humans";
			case 15:
				return "Elves";
			case 16:
				return "Dark Elves";
			case 17:
				return "Orcs";
			case 18:
				return "Dwarves";
			case 19:
				return "Others";
			case 20:
				return "Non-living Beings";
			case 21:
				return "Siege Weapons";
			case 22:
				return "Defending Army";
			case 23:
				return "Mercenaries";
			case 24:
				return "Unknown Creature";
			case 25:
				return "Kamael";
			default:
				return "Not defined";
		}
	}

	private static String nameNpc(NpcInstance npc)
	{
		if(npc.getNameNpcString() == NpcString.NONE)
			return HtmlUtils.htmlNpcName(npc.getNpcId());
		else
			return HtmlUtils.htmlNpcString(npc.getNameNpcString().getId(), npc.getName());
	}
}
