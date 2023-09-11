package l2s.gameserver.botscript.bypasshandler;

import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.BotResType;
import l2s.gameserver.core.BotSkillStrategy;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Functions;

import java.util.LinkedList;

public class BotStrategyEdit
{
	@Bypass(value = "bot.setSkillCondition")
	public void botSetSkillCondition(Player player, NpcInstance npc, String[] param)
	{
		if(BotEngine.getInstance().getBotConfig(player).getAttackStrategy().size() == 5)
		{
			player.sendMessage("\u6700\u591a\u53ea\u80fd\u6dfb\u52a05\u6761\u6280\u80fd\u7b56\u7565");
			/*\u6700\u591a\u53ea\u80fd\u6dfb\u52a05\u6761\u6280\u80fd\u7b56\u7565 最多只能添加5条技能策略*/
		}
		else
		{
			int skillId = Integer.parseInt(param[0]);
			boolean targetHpCheck = Integer.parseInt(param[2].replace("%",""))-Integer.parseInt(param[1].replace("%","")) > 0;

			int thpReduce = Integer.parseInt(param[2].replace("%","")) - Integer.parseInt(param[1].replace("%",""));
			int mhpReduce = Integer.parseInt(param[4].replace("%","")) - Integer.parseInt(param[3].replace("%",""));
			int mmpReduce = Integer.parseInt(param[6].replace("%","")) - Integer.parseInt(param[5].replace("%",""));

			int hpPercent = thpReduce > 0 ? thpReduce : mhpReduce > 0 ? mhpReduce : Math.max(mmpReduce, 0);
			boolean oneTime = "只用一次".equals(param[7]);
			if(player.getKnownSkill(skillId) != null)
			{
				BotSkillStrategy skillStrategy = new BotSkillStrategy(skillId, targetHpCheck, oneTime, hpPercent);
				if (BotConfig.autoDetection.equals(param[7])) {
					skillStrategy.setIs_autoDetection(true);
				}
				if(Integer.parseInt(param[5].replace("%",""))-Integer.parseInt(param[6].replace("%","")) < 0)
				{
					skillStrategy.setBaseSelfMpCheck(true);
				}else
					skillStrategy.setBaseSelfMpCheck(false);
				BotEngine.getInstance().getBotConfig(player).getAttackStrategy().addLast(skillStrategy);
			}
		}
		BotControlPage.fightPage(player);
	}

	@Bypass(value = "bot.setSkillConditionRange")
	public void setSkillConditionRange(Player player, NpcInstance npc, String[] param)
	{
		BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/skilledit.htm", player);
		if (param.length==4) {
			int min = Integer.parseInt(param[1].replace("%",""));
			int max = Integer.parseInt(param[2].replace("%",""));
			if (min > max ){
				player.sendMessage("设置范围值错误！将不能正常使用技能");
			}
			switch (param[3]){
				case "thp":
					botConfig.setThpMin(min);
					botConfig.setThpMax(max);
					botConfig.setMhpMin(0);
					botConfig.setMhpMax(0);
					botConfig.setMmpMin(0);
					botConfig.setMmpMax(0);
					break;
				case "mhp":
					botConfig.setThpMin(0);
					botConfig.setThpMax(0);
					botConfig.setMhpMin(min);
					botConfig.setMhpMax(max);
					botConfig.setMmpMin(0);
					botConfig.setMmpMax(0);
					break;
				case "mmp":
					botConfig.setThpMin(0);
					botConfig.setThpMax(0);
					botConfig.setMhpMin(0);
					botConfig.setMhpMax(0);
					botConfig.setMmpMin(min);
					botConfig.setMmpMax(max);
					break;
			}
		}
		html = html.replace("%thp1%", botConfig.getThpMin()+"%");
		html = html.replace("%thp2%", botConfig.getThpMax()+"%");
		html = html.replace("%shp1%", botConfig.getMhpMin()+"%");
		html = html.replace("%shp2%", botConfig.getMhpMax()+"%");
		html = html.replace("%smp1%", botConfig.getMmpMin()+"%");
		html = html.replace("%smp2%", botConfig.getMmpMax()+"%");
		html = html.replace("%text%", botConfig.getUseStrategy());
		SkillEntry se = player.getKnownSkill(Integer.parseInt(param[0]));
		if (se!=null) {
			html = html.replace("%skillName%", se.getTemplate().getName());
			html = html.replace("%skillId%", se.getTemplate().getId()+"");
		}else {
			html = html.replace("%skillName%", "");
			html = html.replace("%skillId%", "-1");
		}
		Functions.show(html, player);
	}

	@Bypass(value = "bot.resOrder")
	public void resOrder(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotResType> config = BotEngine.getInstance().getBotConfig(player).getResType();
		int index = Integer.parseInt(param[0]);
		BotResType botResType = config.remove(index);
		config.add(++index, botResType);
		BotControlPage.protectPage(player);
	}

	@Bypass(value = "bot.skillOrderUp")
	public void botSkillOrderUp(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotSkillStrategy> config = BotEngine.getInstance().getBotConfig(player).getAttackStrategy();
		if(config.size() <= 1)
		{
			return;
		}
		int index = Integer.parseInt(param[0]);
		if(index != 0)
		{
			BotSkillStrategy skillStrategy = config.remove(index);
			config.add(--index, skillStrategy);
		}
		BotControlPage.fightPage(player);
	}

	@Bypass(value = "bot.skillOrderDown")
	public void botSkillOrderDown(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotSkillStrategy> config = BotEngine.getInstance().getBotConfig(player).getAttackStrategy();
		if(config.size() <= 1)
		{
			return;
		}
		int index = Integer.parseInt(param[0]);
		if(index != config.size() - 1)
		{
			BotSkillStrategy skillStrategy = config.remove(index);
			config.add(++index, skillStrategy);
		}
		BotControlPage.fightPage(player);
	}

	@Bypass(value = "bot.skillRemove")
	public void botSkillRemove(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotSkillStrategy> config = BotEngine.getInstance().getBotConfig(player).getAttackStrategy();
		int index = Integer.parseInt(param[0]);
		config.remove(index);
		BotControlPage.fightPage(player);
	}
	@Bypass(value = "bot.buyTime")
	public void buyTime(Player player, NpcInstance npc, String[] param)
	{
		BotControlPage.buyTime(player,param);
	}
}