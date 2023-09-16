package l2s.gameserver.botscript.bypasshandler;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BotBuffManager
{
	String skillTemplate = "<table cellspacing=0 cellpadding=0 width=32 height=32 background=\"$skill\"><tr><td><table width=$w height=$w $click><tr><td height=16 width=$w></td></tr></table> </td></tr></table>";
	String click = "background=\"L2UI.item_click\"";
	String[] commond = new String[] { "create", "show", "detail", "edit", "del", "add", "remove", "add_target", "del_target" };

	@Bypass(value = "bot.buff")
	public void buffSetPage(final Player player, NpcInstance npc, String[] param)
	{
		BotConfigImp config;
		block40:
		{
			block41:
			{
				block37:
				{
					block35:
					{
						block36:
						{
							block38:
							{
								block34:
								{
									block39:
									{
										config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
										String string = param[0];
										switch(string.hashCode())
										{
											case -1352294148:
											{
												if(string.equals("create"))
													break;
												return;
											}
											case -1335224239:
											{
												if(!string.equals("detail"))
												{
													return;
												}
												break block34;
											}
											case -934610812:
											{
												if(!string.equals("remove"))
												{
													return;
												}
												break block35;
											}
											case 96417:
											{
												if(!string.equals("add"))
												{
													return;
												}
												break block36;
											}
											case 99339:
											{
												if(!string.equals("del"))
												{
													return;
												}
												break block37;
											}
											case 3108362:
											{
												if(!string.equals("edit"))
												{
													return;
												}
												break block38;
											}
											case 3529469:
											{
												if(!string.equals("show"))
												{
													return;
												}
												break block39;
											}
											case 731732037:
											{
												if(!string.equals("del_target"))
												{
													return;
												}
												break block40;
											}
											case 2071146223:
											{
												if(!string.equals("add_target"))
												{
													return;
												}
												break block41;
											}
										}
										if(param.length != 2)
											return;
										String configName = param[1];
										if(config.getBuffSets().containsKey(configName))
										{
											player.sendMessage("\u8fd9\u4e2a\u540d\u5b57\u6b63\u5728\u4f7f\u7528\u4e2d,\u8bf7\u6362\u4e00\u4e2a\u540d\u5b57");
											/*\u8fd9\u4e2a\u540d\u5b57\u6b63\u5728\u4f7f\u7528\u4e2d,\u8bf7\u6362\u4e00\u4e2a\u540d\u5b57 这个名字正在使用中，请换一个名字*/
										}
										else if(ArrayUtils.contains((Object[]) this.commond, (Object) param[1]))
										{
											player.sendMessage("\u4e0d\u53ef\u4ee5\u4f7f\u7528\u7cfb\u7edf\u5173\u952e\u5b57\u4f5c\u4e3a\u5957\u9910\u540d\u5b57");
											/*\u4e0d\u53ef\u4ee5\u4f7f\u7528\u7cfb\u7edf\u5173\u952e\u5b57\u4f5c\u4e3a\u5957\u9910\u540d\u5b57 不可以使用系统关键字作为套餐名字*/
										}
										else if(config.getBuffSets().size() >= 5)
										{
											player.sendMessage("\u5df2\u8fbe\u5230\u6700\u5927\u503c\u9650\u5236,\u4e0d\u80fd\u521b\u5efa\u66f4\u591a\u7684\u5957\u9910");
											/*\u5df2\u8fbe\u5230\u6700\u5927\u503c\u9650\u5236,\u4e0d\u80fd\u521b\u5efa\u66f4\u591a\u7684\u5957\u9910 已达到最大值限制,不能创建更多的套餐*/
										}
										else
										{
											config.getBuffSets().put(configName, new HashSet<Integer>());
											player.sendMessage("\u5957\u9910\u5df2\u521b\u5efa,\u8bf7\u70b9\u51fb\u5957\u9910\u540d\u5b57\u6309\u94ae\u8fdb\u5165\u7f16\u8f91\u9875\u9762");
											/*\u5957\u9910\u5df2\u521b\u5efa,\u8bf7\u70b9\u51fb\u5957\u9910\u540d\u5b57\u6309\u94ae\u8fdb\u5165\u7f16\u8f91\u9875\u9762 套餐已创建,请点击套餐名字按钮进入编辑页面*/
										}
										this.showBuffManagerPage(player, config);
										return;
									}
									this.showBuffManagerPage(player, config);
									return;
								}
								this.showInfoPage(player, param[1], config);
								return;
							}
							this.showEditPage(player, param[1], Integer.parseInt(param[2]), config);
							return;
						}
						config.getBuffSets().get(param[1]).add(Integer.parseInt(param[2]));
						this.showEditPage(player, param[1], Integer.parseInt(param[3]), config);
						return;
					}
					config.getBuffSets().get(param[1]).remove(Integer.parseInt(param[2]));
					this.showEditPage(player, param[1], Integer.parseInt(param[3]), config);
					return;
				}
				final String name = param[1];
				player.ask(new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("\u786e\u5b9a\u8981\u5220\u9664$name\u5957\u9910\u5417?".replace("$name", name)), new OnAnswerListener()
				/*\u786e\u5b9a\u8981\u5220\u9664$name\u5957\u9910\u5417? 确定要删除$name套餐吗?*/
				{

					public void sayYes()
					{
						config.getBuffSets().remove(name);
						player.sendMessage("\u5220\u9664\u4e86\u5957\u9910$name".replace("$name", name));
						/*\u5220\u9664\u4e86\u5957\u9910 删除了套餐*/
						BotBuffManager.checkBuffConfig(config);
						BotBuffManager.this.showBuffManagerPage(player, config);
					}

					public void sayNo()
					{
					}
				});
				return;
			}
			try
			{
				if(config.getBuffConfig().size() >= 12)
				{
					player.sendMessage("\u6700\u591a\u53ea\u80fd\u6dfb\u52a012\u4e2a\u76ee\u6807");
					/*\u6700\u591a\u53ea\u80fd\u6dfb\u52a012\u4e2a\u76ee\u6807 最多只能添加12个目标*/
				}
				else if(!config.getBuffConfig().containsKey(param[1]))
				{
					config.getBuffConfig().put(param[1], param[2]);
				}
				else
				{
					config.getBuffConfig().replace(param[1], param[2]);
				}
				this.showInfoPage(player, param[2], config);
				return;
			}
			catch(Exception e)
			{
				player.sendMessage("\u64cd\u4f5c\u9519\u8bef,\u8bf7\u8f93\u5165\u5fc5\u8981\u7684\u53c2\u6570");
			/*\u64cd\u4f5c\u9519\u8bef,\u8bf7\u8f93\u5165\u5fc5\u8981\u7684\u53c2\u6570 操作错误,请输入必要的参数*/
			}
			return;
		}
		try
		{
			if(config.getBuffConfig().containsKey(param[1]))
			{
				config.getBuffConfig().remove(param[1]);
			}
			this.showInfoPage(player, param[2], config);
			return;
		}
		catch(Exception e)
		{
			player.sendMessage("\u64cd\u4f5c\u9519\u8bef,\u8bf7\u8f93\u5165\u5fc5\u8981\u7684\u53c2\u6570");
			/*\u64cd\u4f5c\u9519\u8bef,\u8bf7\u8f93\u5165\u5fc5\u8981\u7684\u53c2\u6570 操作错误,请输入必要的参数*/
		}
	}

	private void showEditPage(Player player, String name, int page, BotConfigImp config)
	{
		StringBuilder builder = new StringBuilder();
		Set<Integer> ids = config.getBuffSets().get(name);
		builder.append("<html noscrollbar><body><center><table width=300 cellpadding=1 cellspacing=0>");
		ArrayList<SkillEntry> buffskills = new ArrayList<SkillEntry>();
		for(SkillEntry entry : player.getAllSkillsArray())
		{
			for (EffectTemplate effectTemplate : entry.getTemplate().getEffectTemplates(EffectUseType.NORMAL_INSTANT)) {
				if ("i_summon_cubic".equals(effectTemplate.getParams().get("name"))) {
					buffskills.add(entry);
				}
			}
			if(entry.getTemplate().getSkillType() != Skill.SkillType.BUFF || entry.getTemplate().getAbnormalTime() == -1)
				continue;
			buffskills.add(entry);
		}
		int start = page * 8;
		int end = Math.min(start + 8, buffskills.size());
		for(int i = start; i < end; ++i)
		{
			boolean contains = ids.contains((buffskills.get(i)).getId());
			builder.append("<tr>");
			builder.append("<td>").append("<img src=" + (buffskills.get(i)).getTemplate().getIcon() + " width=32 height=32>").append("</td>").append("<td align=center width=150>").append((buffskills.get(i)).getName(player)).append("</td>").append("<td align=center>").append(!contains ? "<font color=00FF00>" + HtmlUtils.htmlButton("\u6dfb\u52a0", new StringBuilder("bypass -h htmbypass_bot.buff add ").append(name).append(" ").append((buffskills.get(i)).getId()).append(" ").append(page).toString(), 60, 25) + "</font>" : "<font color=FF0000>" + HtmlUtils.htmlButton("\u79fb\u9664", new StringBuilder("bypass -h htmbypass_bot.buff remove ").append(name).append(" ").append((buffskills.get(i)).getId()).append(" ").append(page).toString(),60, 25) + "</font>").append("</td>");
			/*\u6dfb\u52a0 添加	\u79fb\u9664 移除*/
			builder.append("</tr>");
		}
		builder.append("</table><br>");
		int pages = buffskills.size() / 8 + (buffskills.size() % 8 != 0 ? 1 : 0);
		if(pages > 1)
		{
			builder.append("<table>");
			builder.append("<tr>");
			for(int i2 = 0; i2 < pages; ++i2)
			{
				builder.append("<td width=32>").append("<a action=\"bypass -h htmbypass_bot.buff edit " + name + " " + i2 + "\">" + (1 + i2) + "\u9875</a>").append("</td>");
				/*\u9875 页*/
			}
			builder.append("<td><button value=\"\u8fd4\u56de\" action=\"bypass -h htmbypass_bot.buff show\" width=45 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\"></td>");
			/*\u8fd4\u56de 返回*/
			builder.append("</tr>");
			builder.append("</table>");
		}
		builder.append("<br1>");
		if(!ids.isEmpty())
		{
			int currentIndex = 0;
			builder.append("<table width=280>");
			for(int id : ids)
			{
				if(currentIndex == 0)
				{
					builder.append("<tr>");
				}
				builder.append("<td>").append("<img src=\"").append(SkillHolder.getInstance().getSkill(id, 1).getIcon()).append("\" width=32 height=32>").append("</td>");
				if(++currentIndex != 8)
					continue;
				currentIndex = 0;
				builder.append("</tr>");
			}
			if(currentIndex != 0)
			{
				builder.append("</tr>");
			}
			builder.append("</table>");
		}
		builder.append("</center></body></html>");
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(builder.toString());
		player.sendPacket(msg);
//		Functions.show(builder.toString(), player);
	}

	private void showInfoPage(Player player, String name, BotConfigImp config)
	{
		String html = HtmCache.getInstance().getHtml("bot/buff_set.htm", player);
		Set<Integer> buffs = config.getBuffSets().get(name);
		html = html.replace((CharSequence) "%name%", (CharSequence) name);
		html = html.replace((CharSequence) "%count%", (CharSequence) Integer.toString(buffs.size()));
		if(buffs.isEmpty())
		{
			html = html.replace((CharSequence) "%skills%", (CharSequence) "");
			html = html.replace((CharSequence) "%targets%", (CharSequence) "");
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			int currentIndex = 0;
			builder.append("<table width=300>");
			for(int id : buffs)
			{
				if(currentIndex == 0)
				{
					builder.append("<tr>");
				}
				builder.append("<td>").append("<img src=\"").append(SkillHolder.getInstance().getSkill(id, 1).getIcon()).append("\" width=32 height=32>").append("</td>");
				if(++currentIndex != 8)
					continue;
				currentIndex = 0;
				builder.append("</tr>");
			}
			if(currentIndex != 0)
			{
				builder.append("</tr>");
			}
			builder.append("</table>");
			html = html.replace((CharSequence) "%skills%", (CharSequence) builder.toString());
			builder = new StringBuilder();
			for(Map.Entry<String,String> entry : config.getBuffConfig().entrySet())
			{
				if(!(entry.getValue()).equals(name))
					continue;
				builder.append(entry.getKey()).append("<br1>");
			}
			html = html.replace((CharSequence) "%targets%", (CharSequence) builder.toString());
		}
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
//		Functions.show(html, player);
	}

	private void showBuffManagerPage(Player player, BotConfigImp config)
	{
		String html = HtmCache.getInstance().getHtml("bot/buff_manage.htm", player);
		if(config.getBuffSets().isEmpty())
		{
			html = html.replace((CharSequence) "%list%", (CharSequence) "");
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			for(String name : config.getBuffSets().keySet())
			{
				builder.append(HtmlUtils.htmlButton(name,"bypass -h htmbypass_bot.buff detail " + name, 90));
			}
			html = html.replace((CharSequence) "%list%", (CharSequence) builder.toString());
		}
		HtmlMessage msg = new HtmlMessage(0);
		msg.setItemId(-1);
		msg.setHtml(html);
		player.sendPacket(msg);
//		Functions.show(html,player);
	}

	public static void checkBuffConfig(BotConfigImp config)
	{
		Set<String> names = config.getBuffSets().keySet();
		HashSet<String> delKey = new HashSet<String>();
		for(Map.Entry<String,String> entry : config.getBuffConfig().entrySet())
		{
			if(names.contains(entry.getValue()))
				continue;
			delKey.add(entry.getKey());
		}
		delKey.forEach(config.getBuffConfig()::remove);
	}

	static /* synthetic */ void access$0(BotBuffManager botBuffManager, Player player, BotConfigImp botConfigImp)
	{
		botBuffManager.showBuffManagerPage(player, botConfigImp);
	}
}
