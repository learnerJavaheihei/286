package npc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import handler.bbs.custom.BBSConfig;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.htm.HtmTemplates;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class ServerTesterInstance extends NpcInstance
{
	public ServerTesterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
		{
			if(!Config.ALLOW_AUGMENTATION)
				showChatWindow(player, "default/" + getNpcId() + "-not_allowed.htm", firstTalk);
			else
				super.showChatWindow(player, val, firstTalk, replace);
		}
		else
			super.showChatWindow(player, val, firstTalk, replace);
	}
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final String[] buypassOptions = command.split(" ");
		
		//Announcements.announceToAll("command:" + command);
		if(command.startsWith("Chat"))
		{
			//bypass -h npc_%objectId%_Chat 1
			showChatWindow(player, "special/" + getNpcId() + "-" + buypassOptions[1] + ".htm", false);
		}
		//bypass -h npc?SetLevel xx
		else if(buypassOptions[0].equals("SetLevel"))
		{
			if(buypassOptions.length == 2)
			{
				int level = Integer.parseInt(buypassOptions[1]);
				setLevel(player, level);
				//player.sendMessage("已設置你的等級為「" + level + "」。");
			}
		}
		else if(buypassOptions[0].equals("GiveSP"))
		{
			player.addExpAndSp(0, 10000000, true);
			//player.sendMessage("已給你1000萬SP。");
		}
		else if(buypassOptions[0].equals("GiveMoney"))
		{
			ItemFunctions.addItem(player, 57, 99999999, true);
			//player.sendMessage("已給你金幣99999999。");
		}
		else if(buypassOptions[0].equals("GiveMemberCoins"))
		{
			ItemFunctions.addItem(player, 29984, 1000000, true);
			//player.sendMessage("已給你金幣1萬個。");
		}
		//bypass -h npc?SetClanLevel xx   
		else if(buypassOptions[0].equals("SetClanLevel"))
		{
			if(player.getClan() == null)
			{
				player.sendMessage("您必需要有血盟。");
				return;
			}
			try
			{
				int level = Integer.parseInt(buypassOptions[1]);
				Clan clan = player.getClan();
				if(player.isClanLeader())
				{
					player.sendMessage("已幫你提高「" + clan.getName() + "」血盟等級" + level);
					int oldLevel = clan.getLevel();
					clan.setLevel(level);
					clan.updateClanInDB();
					clan.onLevelChange(oldLevel, clan.getLevel());
				}
				else
				{
					player.sendMessage("您必需是盟主。");
					return;
				}
			}
			catch (Exception e)
			{
			}
		}
		else if(buypassOptions[0].equals("LearnSkill"))
		{
			int skillCounter = player.rewardSkills(true, true, true, false);
			player.sendMessage("學習 " + skillCounter + " 技能。");
		}
		//LearnSkill
		else if(buypassOptions[0].equals("SetCareer"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String cmd = st.nextToken();
			String html = "";

			String cmd2 = st.nextToken();
			//Announcements.announceToAll("cmd2:" + cmd2);
			if("profession".equals(cmd2))
			{
				//Announcements.announceToAll("11111111111");
				HtmTemplates tpls = HtmCache.getInstance().getTemplates("merchant/40003-Career.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();

				final int feeItemId = getFeeItemIdForChangeClass(player);
				final long feeItemCount = getFeeItemCountForChangeClass(player);
				final int nextClassMinLevel = getNextClassMinLevel(player);
				//Announcements.announceToAll(" feeItemId:" + feeItemId + " feeItemCount:" + feeItemCount +" nextClassMinLevel:" + nextClassMinLevel );
				if(!st.hasMoreTokens())
				{
					//Announcements.announceToAll("22222222222");
					if(nextClassMinLevel == -1)
						content.append(tpls.get(1));
					else if(feeItemId == 0)
						content.append(tpls.get(8));
					else
					{
						if(nextClassMinLevel > player.getLevel())
							content.append(tpls.get(5).replace("<?level?>", String.valueOf(nextClassMinLevel)));
						else
						{
							List<ClassId> availClasses = getAvailClasses(player.getClassId());
							if(availClasses.isEmpty())
								content.append(tpls.get(6));
							else
							{
								ClassId classId = availClasses.get(0);

								content.append(tpls.get(2));

								if(feeItemId > 0 && feeItemCount > 0)
								{
									content.append("<br1>");
									content.append(tpls.get(3).replace("<?fee_item_count?>", String.valueOf(feeItemCount)).replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId)));
								}

								for(ClassId cls : availClasses)
								{
									content.append("<br>");

									String classHtm = tpls.get(4);
									classHtm = classHtm.replace("<?class_name?>", cls.getName(player));
									classHtm = classHtm.replace("<?class_id?>", String.valueOf(cls.getId()));
									content.append(classHtm);
								}
							}
						}
					}
				}
				else
				{
					
					//Announcements.announceToAll("33333333");
					if(nextClassMinLevel == -1 || feeItemId == 0 || nextClassMinLevel > player.getLevel())
					{
						player.sendMessage("沒有下一級可以升上去了。");
						return;
					}

					List<ClassId> availClasses = getAvailClasses(player.getClassId());
					if(availClasses.isEmpty())
					{
						player.sendMessage("沒有下一級可以升上去了。");
						return;
					}

					boolean avail = false;
					ClassId classId = ClassId.VALUES[Integer.parseInt(st.nextToken())];
					for(ClassId cls : availClasses)
					{
						if(cls == classId)
						{
							avail = true;
							break;
						}
					}

					if(!avail)
					{
						player.sendMessage("沒有下一級可以升上去了。");
						return;
					}
					player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER);
					player.setClassId(classId.getId(), false);
					player.broadcastUserInfo(true);
					//player.sendMessage("轉職成功了。");
					return;
				}
				html = html.replace("<?content?>", content.toString());
				HtmlMessage msg = new HtmlMessage(5);
				msg.setHtml(html);
				player.sendPacket(msg);
				player.sendActionFailed();		
			}
		}
	}
	
	
	private void setLevel(Player player, int level)
	{
		if(level < 1 || level > player.getMaxLevel())
		{
			player.sendMessage("你必需設置等級介於  1 - " + player.getMaxLevel());
			return;
		}
		Long exp_add = Experience.getExpForLevel(level) - player.getExp();
		player.addExpAndSp(exp_add, 0, true);
	}
	
	private static int getNextClassMinLevel(Player player)
	{
		final ClassId classId = player.getClassId();
		if(classId.isLast())
			return -1;

		return classId.getClassMinLevel(true);
	}

	private static int getFeeItemIdForChangeClass(Player player)
	{
		/* switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_3;
		} */
		return 0;
	}

	private static long getFeeItemCountForChangeClass(Player player)
	{
		/* switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_3;
		} */
		return 0L;
	}
	private static List<ClassId> getAvailClasses(ClassId playerClass)
	{
		List<ClassId> result = new ArrayList<ClassId>();
		for(ClassId _class : ClassId.values())
		{
			if(!_class.isDummy() && _class.getClassLevel().ordinal() == playerClass.getClassLevel().ordinal() + 1 && _class.childOf(playerClass))
				result.add(_class);
		}		
		return result;
	}
}