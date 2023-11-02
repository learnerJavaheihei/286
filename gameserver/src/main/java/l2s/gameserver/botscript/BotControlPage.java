package l2s.gameserver.botscript;

import l2s.gameserver.Config;
import l2s.gameserver.core.*;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.skillclasses.Summon;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.HtmlUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BotControlPage
{
	public static void restPage(Player activeChar)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(activeChar);
		String html = HtmCache.getInstance().getHtml("bot/rest.htm", activeChar);
		html = html.replace("%followRest%", config.isFollowRest() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%idleRest%", config.isIdleRest() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%info%", config.getHpProtected() == 0 && config.getMpProtected() == 0 ? "\u4e0d\u4e3b\u52a8\u5750\u4e0b\u4f11\u606f" : "HP\u4f4e\u4e8e" + config.getHpProtected() + "%\u6216MP\u4f4e\u4e8e" + config.getMpProtected() + "\u65f6\u4f11\u606f");
		/*\u4e0d\u4e3b\u52a8\u5750\u4e0b\u4f11\u606f 不主动坐下休息	\u4f4e\u4e8e 低于	\u6216MP\u4f4e\u4e8e 或MP低于	\u65f6\u4f11\u606f 时休息*/
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		activeChar.sendPacket(msg);
	}

	public static void mainPage(Player activeChar)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(activeChar);
		String html ="";
		if (Config.ENABLE_BOTSCRIPT_RESTRICT_TIME){
			html = HtmCache.getInstance().getHtml("bot/main_restrict_time.htm", activeChar);
		}
		else
			html = HtmCache.getInstance().getHtml("bot/main.htm", activeChar);
		html = html.replace("%runtimeStats%", config.isAbort() ? "<font color=FF0000>\u25a0</font>" : "<font color=00FF00>\u25a0</font>");
		/*\u25a0 ■*/
		html = html.replace("%run%", config.isAbort() ? "start" : "stop");
		html = html.replace("%runbutton%", config.isAbort() ? "\u542f\u52a8" : "\u505c\u6b62");
		/*\u542f\u52a8 启动	\u505c\u6b62 停止*/
		html = html.replace("%autoAttack%", config.isAutoAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%pickUpItem%", config.isPickUpItem() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%pickUpFirst%", config.isPickUpFirst() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%autoSweep%", config.isAutoSweep() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%absorbBody%", config.isAbsorbBody() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%hpmpShift%", config.isHpmpShift() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%acceptRes%", config.isAcceptRes() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%usePhysicalAttack%", config.isUsePhysicalAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%followMove%", config.isFollowMove() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%followAttack%", config.isFollowAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%antidote%", config.isAntidote() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%bondage%", config.isBondage() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%partyAntidote%", config.isPartyAntidote() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%partyBondage%", config.isPartyBondage() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%partyParalysis%", config.isPartyParalysis() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%coverMember%", config.isCoverMember() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%followAttackWhenChoosed%", config.isFollowAttackWhenChoosed() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%hpmpshiftpercent%", String.valueOf(config.getHpMpShiftPercent()));
		html = html.replace("%autoSpoiledAttack%",config.is_autoSpoiledAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");

		if (Config.ENABLE_BOTSCRIPT_RESTRICT_TIME){
			html = html.replace("%scriptTime%","10\u5c0f\u65f6");

			String leftTime = BotEngine.leftTimeMap.get(String.valueOf(activeChar.getObjectId()));

			/** 新 或未使用内挂的老用户 */
			if (leftTime==null ) {
				/* 给缓存设置初始值 */
				BotEngine.leftTimeMap.put(String.valueOf(activeChar.getObjectId()),"36000");
				/* 数据库初始化挂机时间 */
				BotHangUpTimeDao.getInstance().insertScriptTime(activeChar.getObjectId(),Player.scriptTime,Player.scriptTime,0);
				leftTime = "36000";
			}
			Integer time = Integer.parseInt(leftTime);
			int hourTemplate = time / 3600;
			int minuteTemplate = (time - hourTemplate*3600 ) / 60;
			String hour = null;
			String minute = null;
			if(hourTemplate<10){
				hour = "0"+hourTemplate;
			}else {
				hour = String.valueOf(hourTemplate);
			}
			if(minuteTemplate<10){
				minute = "0"+minuteTemplate;
			}else {
				minute = String.valueOf(minuteTemplate);
			}
			html = html.replace("%scriptRemainderTime%", hour+"\u5c0f\u65f6"+minute+"\u5206\u949f");
			// 如果是第一次打开 需要去给上购买的标记 bypass -h htmbypass_bot.buyTime %isBuy% 换掉

			Player._isBuyMap.putIfAbsent(activeChar.getObjectId(), false); // 如果是空的 说明定时刷新清空了或者是 第一次打开使用 默认就是未购买 false
			Player._buyTimesByOBJ.putIfAbsent(activeChar.getObjectId(),0);
			if (html.contains("bypass -h htmbypass_bot.buyTime %isBuy%")) {
				if (BotHangUpTimeDao.getInstance().selectIsBuyByObjId(activeChar.getObjectId()) >= BotConfig.maxBuyTimes) {
					Player._isBuyMap.put(activeChar.getObjectId(),true);
				}else {
					Player._isBuyMap.put(activeChar.getObjectId(),false);
				}
			}
			html = html.replace("%isBuy%",Player._isBuyMap.get(activeChar.getObjectId())?"true":"false");
			html = html.replace("%time%",String.valueOf(BotConfig.maxBuyTimes-Player._buyTimesByOBJ.get(activeChar.getObjectId())));
		}

		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		activeChar.sendPacket(msg);
	}
	// 每次购买时间的费用 依次
	static int[] buyTimesCustom = {20,30,40,50,60,70,80};

	public static void buyTime(Player player, String[] param) {
		// 如果买过
		if (param[0].equals("true")) {
			player.sendMessage("你今天已达到最大购买次数,不能再购买了...");
			mainPage(player);
			return;
		}else if(param[0].equals("false")){
			int time = Player._buyTimesByOBJ.getOrDefault(player.getObjectId(),0);
			int count = buyTimesCustom[Math.min(Math.max(0,time),BotConfig.maxBuyTimes-1)];
			player.ask(new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("确定要购买「 两小时挂机时长吗 」？将消耗 "+count+" 幣"), new OnAnswerListener()
			{
				public void sayYes()
				{
					if (time < BotConfig.maxBuyTimes && player.getInventory().getCountOf(29520)>=count) {
						Player._buyTimesByOBJ.put(player.getObjectId(),time+1);
					}

					// 购买
					if (player.getInventory().destroyItemByItemId(29520,count)) {
						// 扣除币成功 增加时长
						String leftTime = BotEngine.leftTimeMap.get(String.valueOf(player.getObjectId()));
						int addLeftTime = Integer.parseInt(leftTime) + (2 * 60 * 60);
						BotEngine.leftTimeMap.put(String.valueOf(player.getObjectId()),String.valueOf(addLeftTime));

						BotHangUpTimeDao.getInstance().updateIsBuyStats(player.getObjectId(),Player._buyTimesByOBJ.getOrDefault(player.getObjectId(),0));
						BotHangUpTimeDao.getInstance().updateHangUpTime(player.getObjectId(),addLeftTime);

						// 更新 购买状态
						if (time == BotConfig.maxBuyTimes) {
							Player._isBuyMap.put(player.getObjectId(),true);
						}

						mainPage(player);
					}else{
						player.sendMessage("你的赞助币不足,无法购买...");
						mainPage(player);
						return;
					}
				}
				public void sayNo()
				{
					mainPage(player);
					return;
				}
			});
		}
	}


	public static void fightPage(Player activeChar)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(activeChar);
		String html = HtmCache.getInstance().getHtml("bot/fight.htm", activeChar);
		Skill[] skills = activeChar.getAllSkills().stream().filter(Objects::nonNull).map(SkillEntry::getTemplate).filter(Skill::isActive).filter(Skill::isOffensive).filter(sk -> {
			for(BotSkillStrategy skillStrategy : BotEngine.getInstance().getBotConfig(activeChar).getAttackStrategy())
			{
				if(skillStrategy.getSkillId() != sk.getId())
					continue;
				return false;
			}
			return true;
		}).sorted(Comparator.comparing(Skill::getMagicLevel)).toArray(n -> new Skill[n]);
		StringBuilder skillList = new StringBuilder();
		for(int i = 1; i <= skills.length; ++i)
		{
			Skill skill;
			if(i % 3 == 1)
			{
				skillList.append("<tr>");
			}
			skillList.append("<td fixwidth=65>").append((skill = skills[i - 1]).getName(activeChar).length() <= 5 ? skill.getName(activeChar) : skill.getName(activeChar).substring(0, 5)).append("</td>");
			skillList.append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.page skilledit " + skill.getId() + "\" width=16 height=15 back=\"Inventory_DF_Btn_Align_Down\" fore=\"Inventory_DF_Btn_Align\">").append("</td>");
			if(i % 3 != 0)
				continue;
			skillList.append("</tr>");
		}
		if(skills.length % 3 != 0)
		{
			skillList.append("</tr>");
		}
		html = html.replace("%skills%", skillList.toString());
		StringBuilder skillstrategys = new StringBuilder();
		skillstrategys.append("<table cellpadding=0 cellspacing=0>");
		int index = 0;
		for(BotSkillStrategy skillStrategy : config.getAttackStrategy())
		{
			skillstrategys.append("<tr>").append(skillStrategy.toTableTd(activeChar)).append("<td width=18>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.skillOrderUp " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Up_Down\" fore=\"L2UI_CT1.Button_DF_Up\">").append("</td>").append("<td width=18>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.skillOrderDown " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Down_Down\" fore=\"L2UI_CT1.Button_DF_Down_Over\">").append("</td>").append("<td width=15>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.skillRemove " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			++index;
		}
		skillstrategys.append("</table>");
		html = html.replace("%skillStrategy%", skillstrategys.toString());
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		activeChar.sendPacket(msg);
	}

	public static void skillPage(Player player, Integer skillId)
	{
		BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
		SkillEntry se = player.getKnownSkill(skillId.intValue());
		if(se == null)
		{
			return;
		}
		Skill skill = se.getTemplate();
		if(skill == null)
		{
			return;
		}
		String html = HtmCache.getInstance().getHtml("bot/skilledit.htm", player);

		html = html.replace("%thp1%", botConfig.getThpMin()+"%");
		html = html.replace("%thp2%", botConfig.getThpMax()+"%");
		html = html.replace("%shp1%", botConfig.getMhpMin()+"%");
		html = html.replace("%shp2%", botConfig.getMhpMax()+"%");
		html = html.replace("%smp1%", botConfig.getMmpMin()+"%");
		html = html.replace("%smp2%", botConfig.getMmpMax()+"%");
		html = html.replace("%text%", botConfig.getUseStrategy());

		html = html.replace("%skillName%", skill.getName(player));
		html = html.replace("%skillId%", Integer.toString(skill.getId()));
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
	}

	public static void pathPage(Player player)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/path.htm", player);
		html = html.replace("%findMobMaxDistance%", Integer.toString(config.getFindMobMaxDistance()));
		html = html.replace("%findMobMaxHeight%", Integer.toString(config.getFindMobMaxHeight()));
		html = html.replace("%geometry%", config.getGeometry().cnName());
		StringBuilder builder = new StringBuilder(html);
		if (config.is_autoAdjustRange()) {
			builder.replace(builder.indexOf("<$switch$>"),builder.lastIndexOf("<$switch$>"),"<font color=\"FF0000\">");
		}
		html = builder.toString();
		html = html.replace("<$switch$>","");
		html = html.replace("%switch%", config.is_autoAdjustRange()?"OFF":"ON");
		html = html.replace("%x%", Integer.toString(config.getStartX()));
		html = html.replace("%y%", Integer.toString(config.getStartY()));
		html = html.replace("%z%", Integer.toString(config.getStartZ()));
		player.sendMessage("目前最大找怪范围为："+config.getFindMobMaxDistance()+",自动调整找怪范围状态为"+(config.is_autoAdjustRange()?"开启":"关闭"));
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
	}

	public static void petPage(Player player)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/pet.htm", player);
		StringJoiner joiner = new StringJoiner(";");
		StringBuilder buffer = new StringBuilder();
		StringBuilder cubics = new StringBuilder();
		cubics.append("<tr><td></td><td></td></tr>");
		joiner.add("\u65e0");
		/*\u65e0 无*/
		AtomicInteger index = new AtomicInteger(0);

		for(SkillEntry skillEntry : player.getAllSkillsArray())
		{
			if(skillEntry.getTemplate() instanceof Summon)
			{
				joiner.add(skillEntry.getName(player));
			}
			skillEntry.getTemplate().getEffectTemplates(EffectUseType.NORMAL).forEach(effect -> {
				if(effect.getHandler().getName().equalsIgnoreCase("i_summon_cubic"))
				{
					cubics.append("<tr>");
					cubics.append("<td background=\"" + (config.getAutoCubic()[index.get()] == skillEntry.getId() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox") + "\" width=14 height=16><button value=\" \" action=\"bypass -h htmbypass_bot.configSet cubic " + index.get() + " " + skillEntry.getId() + "\" width=15 height=14 back=\"\" fore=\"\"></td>");
					cubics.append("<td fixwidth=100>$name</td>".replace("$name", skillEntry.getName(player)));
					cubics.append("</tr>");
					index.incrementAndGet();
				}
			});
		}

		html = html.replace("%summonIds%", joiner.toString());
		html = html.replace("%cubics%", cubics.toString());
		joiner = new StringJoiner(";");
		BotPetOwnerIdleAction[] arrbotPetOwnerIdleAction = BotPetOwnerIdleAction.values();
		int n = arrbotPetOwnerIdleAction.length;
		for(int i = 0; i < n; ++i)
		{
			BotPetOwnerIdleAction action = arrbotPetOwnerIdleAction[i];
			joiner.add(action.name());
		}
		html = html.replace("%idleActions%", joiner.toString());
		html = html.replace("%idleAction%", config.getBpoidleAction().name());
		html = html.replace("%targetchoose%", config.getPetTargetChoose().name());
		int petBuffIndex = 0;
		// skillType MDAM 4260 4137 4138 4139,
		// DRAIN 4261 ,
		// PDAM 4709 4708 4068 4230 6095 6096,
		// BUFF 4025 4378 ,
		// POISON 4259,HEAL 4707 ,
		// DEBUFF 4705 4706 6094,
		// ROOT 5137
		SummonInstance pet = player.getSummon();
		if (pet != null) {
			Skill.SkillType[] skillTypes = new Skill.SkillType[]{
					Skill.SkillType.MDAM,
					Skill.SkillType.DRAIN,
					Skill.SkillType.PDAM,
					Skill.SkillType.BUFF,
					Skill.SkillType.POISON,
					Skill.SkillType.DEBUFF,
					Skill.SkillType.ROOT
			};
			for (SkillEntry skillEntry : pet.getAllSkillsArray()) {

				Skill skill = skillEntry.getTemplate();
				boolean flag = false;
				for (Skill.SkillType skillType : skillTypes) {
					if (skillType.equals(skill.getSkillType())) {
						flag = true;
						break;
					}
				}
				if(!flag)
					continue;
				if(petBuffIndex == 0)
				{
					buffer.append("<tr>");
				}
				buffer.append("<td background=" + (config.getPetBuffs().contains(skill.getId()) ? "L2UI.CheckBox_checked" : "L2UI.CheckBox") + " width=14 height=16><button value=\" \" action=\"bypass -h htmbypass_bot.configSet petbuff " + skill.getId() + "\" width=15 height=14 back=\"\" fore=\"\"></td>");
				buffer.append("<td fixwidth=100>" + skill.getName(player) + "</td>");
				if(++petBuffIndex != 2)
					continue;
				buffer.append("</tr>");
				petBuffIndex = 0;
			}
			if(petBuffIndex != 0)
			{
				buffer.append("</tr>");
			}
		}
		html = html.replace("%buffs%", buffer.toString());
		html = html.replace("%summonAttack%", config.isSummonAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = config.getSummonSkillId() != 0 ? html.replace("%summonName%", SkillHolder.getInstance().getSkill(config.getSummonSkillId(), 1).getName(player)) : html.replace("%summonName%", "\u65e0");
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
	}

	public static void protectPage(Player player)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/protect.htm", player);
		StringJoiner joiner = new StringJoiner(";");
		StringJoiner joinerG = new StringJoiner(";");
		for(SkillEntry skillEntry : player.getAllSkillsArray())
		{
			if(skillEntry.getSkillType() != Skill.SkillType.HEAL && skillEntry.getSkillType() != Skill.SkillType.HEAL_PERCENT && skillEntry.getId() != 1256 && skillEntry.getId() != 1229 && skillEntry.getId() != 1553)
			/*技能ID1256 帕格立歐之心	技能ID1229 生命禮讚	技能ID1553 連鎖治癒*/
				continue;
			if(skillEntry.getTemplate().getSkillType() == Skill.SkillType.HOT)
			{
				joiner.add(skillEntry.getName(player));
				joinerG.add(skillEntry.getName(player));
				continue;
			}
			if(skillEntry.getId() == 1553)
			/*技能ID1553 連鎖治癒*/
			{
				joiner.add(skillEntry.getName(player));
				joinerG.add(skillEntry.getName(player));
				continue;
			}
			if(skillEntry.getTemplate().getTargetType() != Skill.SkillTargetType.TARGET_PARTY)
			{
				joiner.add(skillEntry.getName(player));
				continue;
			}
			joinerG.add(skillEntry.getName(player));
		}
		html = html.replace("%skills%", joiner.toString());
		html = html.replace("%skillsGroup%", joinerG.toString());
		StringJoiner members = new StringJoiner(";");
		Party party = player.getParty();
		if(party != null)
		{
			for(Player member : party)
			{
				if(member == player)
					continue;
				members.add(member.getName());
			}
		}
		html = html.replace("%members%", members.length() > 0 ? members.toString() : "");
		html = html.replace("%isUseRes%", config.isUseRes() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		StringBuilder priority = new StringBuilder();
		config.getResType().remove((Object) BotResType.DEFAULT);
		for(int index = 0; index < config.getResType().size(); ++index)
		{
			BotResType botResType = config.getResType().get(index);
			priority.append("<td>").append(botResType.getName()).append("</td>");
			if(index >= 1)
				continue;
			priority.append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.resOrder " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Right\" fore=\"L2UI_CT1.Button_DF_Right\">").append("</td>");
		}
		html = html.replace("%priority%", priority.toString());
		StringBuilder builder = new StringBuilder();
		if(config.getEvaPercent() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u4f0a]").append("MP" + config.getEvaPercent()).append("%\u65f6\u5bf9\u81ea\u5df1\u4f7f\u7528:").append("\u4f0a\u5a03\u795d\u798f").append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet evaPercent 0%\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u4f0a 伊	\u65f6\u5bf9\u81ea\u5df1\u4f7f\u7528: 时对自己使用:	\u4f0a\u5a03\u795d\u798f 伊娃祝福*/
		}
		if(config.getBalancePercent() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u5747]").append(String.valueOf(config.getBalanceSize()) + "\u4ebaHP" + config.getBalancePercent()).append("%\u65f6\u4f7f\u7528:").append("\u751f\u547d\u5747\u8861").append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet balance 0% 0%\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u5747 均	\u4ebaHP 人HP	\u65f6\u4f7f\u7528: 时使用:	\u751f\u547d\u5747\u8861 生命均衡*/
		}
		if(config.getSelfHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u81ea]HP").append(config.getSelfHpHeal()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getHealSkill1(), 1).getName(player)).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet sprotect 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u81ea 自	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPotionHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u81ea]HP").append(config.getPotionHpHeal()).append("%\u65f6\u4f7f\u7528:").append(ItemHolder.getInstance().getTemplate(config.getHpPotionId()).getName(player)).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet hppotion 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u81ea 自	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPotionMpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u81ea]MP").append(config.getPotionMpHeal()).append("%\u65f6\u4f7f\u7528:").append(ItemHolder.getInstance().getTemplate(config.getMpPotionId()).getName(player)).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet mppotion 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u81ea 自	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPartyHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u961f]HP").append(config.getPartyHpHeal()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getHealSkill2(), 1).getName(player)).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet pprotect 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u961f 队	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPartyHealSkillId() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u7fa4]").append(String.valueOf(config.getPartyHealSize()) + "\u4ebaHP" + config.getPartyHealPercent()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getPartyHealSkillId(), 1).getName(player)).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet gprotect 0% 0% 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u7fa4 群	\u4ebaHP 人HP:	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPetHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u5ba0]HP").append(config.getPetHpHeal()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getHealSkill3(), 1).getName(player)).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet petprotect 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u5ba0 宠	\u4ebaHP 人HP:	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(!config.getPartyMpHeal().isEmpty())
		{
			for(Map.Entry<Integer, Integer> entry : config.getPartyMpHeal().entrySet())
			{
				int charId = entry.getKey();
				int value = entry.getValue();
				Player member = GameObjectsStorage.getPlayer(charId);
				String name = member != null ? member.getName() : "\u4e0d\u5728\u7ebf";
				/*\u4e0d\u5728\u7ebf 不在线*/
				String color = member == null || !member.isInSameParty(player) ? "AAAAAA" : "FFFF00";
				builder.append("<tr>").append("<td width=250><font color=" + color + ">" + name + "</font>MP").append(value).append("%\u65f6\u4f7f\u7528:").append("\u56de\u590d\u672f").append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet removempp " + charId + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
				/*\u65f6\u4f7f\u7528: 时使用:	\u56de\u590d\u672f 回复术*/
			}
		}
		html = html.replace("%keepMp%", Integer.toString(config.getKeepMp()));
		html = html.replace("%mpPolicy%", config.getMpHealOrder().name());
		html = html.replace("%info%", builder.toString());
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
	}

	public static void itemUsePage(Player player)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(player);
		StringBuilder builder = new StringBuilder();
		String html = HtmCache.getInstance().getHtml("bot/useitem.htm", player);
		//<$contents$>

		builder.append("<table>");
		int index = 0;
		for(int itemId : BotProperties.BUFF_ITEM_IDS)
		{
			if(player.getInventory().getItemByItemId(itemId) == null)
				continue;
			if(index == 0)
			{
				builder.append("<tr>");
			}
			builder.append("<td align=CENTER width=150>").append("<button width=32 height=32 itemtooltip=\"" + itemId + "\" back=\"L2UI_CH3.aboutotpicon\" fore=\"L2UI_CH3.aboutotpicon\"></button\ufeff>").append("<br1>").append(HtmlUtils.htmlItemName((int) itemId)).append("<br1>").append(config.getAutoItemBuffs().contains(itemId) ? "<font color=00FF00>" + HtmlUtils.htmlButton((String) "YES", (String) new StringBuilder("bypass -h htmbypass_bot.configSet itemId ").append(itemId).toString(), (int) 30) + "</font>" : "<font color=FF0000>" + HtmlUtils.htmlButton((String) "NO", (String) new StringBuilder("bypass -h htmbypass_bot.configSet itemId ").append(itemId).toString(), (int) 30) + "</font>").append("</td>");
			if(++index != 2)
				continue;
			builder.append("</tr>");
			index = 0;
		}
		if(index != 0)
		{
			builder.append("</tr>");
		}
		builder.append("</table>");
		html = html.replace("<$contents$>", builder.toString()).replace("<table></table>","<table><tr><td></td></tr></table>");
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
	}

	public static void party(Player player, String[] param)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/party.htm", player);
		StringBuilder builder = new StringBuilder();

		String addMethod1 = param[0];
		String addMethod2 = null;
		if (param.length>=2) {
			addMethod2 = param[1];
		}

		StringBuilder builder1 = new StringBuilder();
		if (addMethod1.equals("list") || addMethod1.equals("party") ||addMethod1.equals("addList") || Objects.equals(addMethod2, "addList")) {
			// 获取周围500半径范围内的玩家
			List<Player> aroundPlayers = World.getAroundPlayers(player, 500);

			if (aroundPlayers != null && aroundPlayers.size() >0) {
				StringBuilder nameBuilder = new StringBuilder();
				for (Player aroundPlayer : aroundPlayers) {
					if (!aroundPlayer.isGM()) {
						// 不在队里  并且 是否跟我一个队
						nameBuilder.append(aroundPlayer.getName()).append(";");
					}
				}
				html = html.replace("%aroundPlayers%", nameBuilder);
			}else {
				html = html.replace("%aroundPlayers%", "");
			}
			html = html.replace("%aroundPlayers%", "");
			builder1.append(html);
			builder1.replace(builder1.indexOf("<$middleHide$>"),builder1.lastIndexOf("<$hide$>"),"");
			html = builder1.toString();
			html = html.replace("<$hide$>","");
			html = html.replace("<$middleHide$>","");
		}else if(addMethod1.equals("edit") || addMethod1.equals("addEdit")|| Objects.equals(addMethod2, "addEdit")){
			builder1.append(html);
			builder1.replace(builder1.indexOf("<$hide$>"),builder1.indexOf("<$middleHide$>"),"");
			html = builder1.toString();
			html = html.replace("<$hide$>","");
			html = html.replace("<$middleHide$>","");
		}else {
			builder1.append(html);
			builder1.replace(builder1.indexOf("<$hide$>"),builder1.indexOf("<$middleHide$>"),"");
			html = builder1.toString();
			html = html.replace("<$hide$>","");
			html = html.replace("<$middleHide$>","");
		}

		String finalAddMethod = addMethod2;
		config.getPartyMemberHolder().forEach((name, invite) -> {
			builder.append("<table background=\"L2UI_CT1.Windows.Windows_DF_TooltipBG\">");
			builder.append("<tr>");
			builder.append("<td width=115>").append(name).append("</td>");
			builder.append("<td width=65>").append(invite == false ? HtmlUtils.htmlButton("\u81ea\u52a8\u9080\u8bf7",  "bypass -h htmbypass_bot.auto_invite " + name+" "+(finalAddMethod !=null? finalAddMethod :""),  65) : HtmlUtils.htmlButton("\u53d6\u6d88\u9080\u8bf7", "bypass -h htmbypass_bot.remove_invite " + name+" "+(finalAddMethod !=null? finalAddMethod :""), 65)).append("</td>");
			/*\u81ea\u52a8\u9080\u8bf7 自动邀请 \u53d6\u6d88\u9080\u8bf7 取消邀请*/
			Player p = GameObjectsStorage.getPlayer(name);
			boolean abort = p == null || BotEngine.getInstance().getBotConfig(p).isAbort();
			builder.append("<td width=65>").append(!abort ? HtmlUtils.htmlButton( "\u505c\u6b62\u5185\u6302", "bypass -h htmbypass_bot.p_abort " + name+" "+(finalAddMethod !=null? finalAddMethod :""), 65) : HtmlUtils.htmlButton("\u542f\u52a8\u5185\u6302", "bypass -h htmbypass_bot.p_run " + name+" "+(finalAddMethod !=null? finalAddMethod :""), 65)).append("</td>");
			/*\u505c\u6b62\u5185\u6302 停止内挂	\u542f\u52a8\u5185\u6302 启动内挂*/
			builder.append("<td width=50>").append(HtmlUtils.htmlButton("\u5220\u9664", "bypass -h htmbypass_bot.remove_p " + name+" "+(finalAddMethod !=null? finalAddMethod :""),  50)).append("</td>");
			/*\u5220\u9664 删除*/
			builder.append("</tr>");
			builder.append("</table>");
		});
		html = html.replace("%loottype%", config.getLootType().name());
		html = html.replace("%leaderName%", player.getParty()!=null ? player.getParty().getPartyLeader().getName():"  ");
		html = html.replace("%list%", builder.toString());
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
	}
}