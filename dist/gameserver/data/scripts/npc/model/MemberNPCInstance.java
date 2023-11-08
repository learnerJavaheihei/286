package npc.model;


import handler.bbs.custom.BBSConfig;
import handler.bbs.custom.communitybuffer.BuffSkill;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.htm.HtmTemplates;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.PremiumAccountHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.PremiumAccountTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.*;
import l2s.gameserver.utils.CompensationSystem.NewServerCompensationDao;
import l2s.gameserver.utils.CompensationSystem.NewServerCompensationEntry;
import l2s.gameserver.utils.CompensationSystem.NewServerCompensationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * NPC服务
 **/
public class MemberNPCInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(MemberNPCInstance.class);

	private static String[][] itemName = {
			{ "UNK_133", "女僕(限定女)", "BranchSys3.icon1.g_co_cutie_maid" },
			{ "UNK_134", "紅色泳裝", "BranchSys3.icon1.g_co_swimsuit_event_03" },
			{ "UNK_178", "藍色步兵", "icon.armor_t2000_ul_i00" },
			{ "WHITE_ASSASSIN_SUIT", "雪白刺客", "icon.skill1802" },
			{ "DARK_ASSASSIN_SUIT", "黑暗刺客", "icon.skill1801" },
			{ "PIRATE_SUIT", "劫掠者", "icon.skill1800" },
			{ "UNK_179", "魔法師-紫紅色", "icon.armor_t2000_ul_i00" },
			{ "UNK_180", "魔術師", "icon.armor_t2000_ul_i00" },
			{ "HALLOWEEN_SUIT", "萬聖夜外型", "icon.ev_wp_halloween" },
			{ "BLUE_DYNASTY", "藍色王朝", "icon.armor_t2000_ul_i00" },
			{ "RED_ZUBEI", "紅色夏隆", "icon.armor_t2000_ul_i00" },
			{ "UNK_193", "紅色步兵衣裳", "icon.inquisitor_red" },
			{ "DARK_KNIGHT", "黑暗騎士", "icon.armor_t2000_ul_i00" },
			{ "HANBOK", "韓服", "icon.ev_kr_traditional_dress" },
			{ "TEDDY_BEAR", "熊熊衣", "icon.gomdori_chest_change" },
			{ "WHITE_KNIGHT", "雪白騎士", "icon.white_knight_suit" },
			{ "KAT_THE_CATS", "戰鬥貓", "icon.cat_the_cat_change_0" },
			{ "CAT", "貓咪娃娃", "icon.sayha2017_cat" },
			{ "PANDA", "熊貓娃娃", "icon.sayha2017_panda" },
			{ "DRAGON_BERSERKER", "狂龍戰士衣裳", "icon.bm2018_dragon_i01" },
			{ "NINJA_ASSASSIN", "忍者刺客衣裳", "icon.bm2018_ninja_i01" },
			{ "BLUE_MUSKETEER", "藍色步兵衣裳", "icon.bm2018_bluemusketeer_i01" },
			{ "VALKYRIE", "女武神衣裳", "icon.valkyrie_chest_change_0" },
			{ "WILD_WOLF", "野蠻狼衣裳", "icon.barbarian_chest_change_0" },
			{ "PIRATE", "商隊衣裳", "icon.bm2018_pirate_i01" },
			{ "PURPLE_COWBOY", "紫色牛仔衣裳", "icon.bm2018_cowboy_i01" },
			{ "HIGH_PRIEST", "牧師衣裳", "icon.healer_chest_change" },
			{ "BLOOD_NOBLESSE", "血色貴族衣裳", "icon.bm2018_rednoblesse_i01" },
			{ "WHITE_NOBLESSE", "白色貴族衣裳", "icon.bm2018_whitenoblesse_i01" }
	};
	private static Long[] Slot= {
			ItemTemplate.SLOT_R_EAR,//右耳
			ItemTemplate.SLOT_L_EAR,//左耳
			ItemTemplate.SLOT_NECK,//项练
			ItemTemplate.SLOT_R_FINGER,//右手戒
			ItemTemplate.SLOT_L_FINGER,//左手戒
			ItemTemplate.SLOT_HEAD,//头
			ItemTemplate.SLOT_R_HAND,//右手
			ItemTemplate.SLOT_L_HAND,//左手
			ItemTemplate.SLOT_GLOVES,//手套
			ItemTemplate.SLOT_CHEST,//胸
			ItemTemplate.SLOT_LEGS,//下护具
			ItemTemplate.SLOT_FEET,//脚
			ItemTemplate.SLOT_BACK,//披风
			ItemTemplate.SLOT_R_BRACELET,//右手镯
			ItemTemplate.SLOT_L_BRACELET,//左手镯
			ItemTemplate.SLOT_HAIR,//头饰
			ItemTemplate.SLOT_HAIRALL,//头饰
			ItemTemplate.SLOT_BROOCH,//胸针
			ItemTemplate.SLOT_PENDANT,//坠饰
			ItemTemplate.SLOT_BELT,//腰带
	};
	
	static final int[][] GIveEveryDayItemId = 
		{
			// 第一個是幣數量  第二個是物品及數量  第三個是等級限制 第四個是購買次數 輸入999 表示可買999 也等於無限次數了
			{31525,3},
		};
	public static int[] skillReserve = {
			11400,11401,11402,11403,11404,51275,51276,51277,51278,51279,51280,51281,51271,51272,51273,1405,51271,51252,51251,51250,51249,51248,51247,5124
	};
	private static String compensation_btn1="<td width=50 align=center background=\"L2UI_ct1.button_df_disable\" height=18><button value=\"领取\" action=\"player\" width=115 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
	private static String compensation_btn2="<td width=50 align=center background=\"L2UI_ct1.button_df_disable\" height=20><font color=DDD3B6>已领取</font></td>";
	private static String compensation_btn3="<td width=50 align=center background=\"L2UI_ct1.button_df_disable\" height=20><font color=DDD3B6>未开启</font></td>";
	private static String compensation_btn4="<td width=50 align=center background=\"L2UI_ct1.button_df_disable\" height=20><font color=DDD3B6>不可领取</font></td>";

	public MemberNPCInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}
	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		//這一區是第一次對話時該出現的對話框區域
		if(val == 0)
		{
			String html = HtmCache.getInstance().getHtml("member/" + getNpcId() + ".htm", player);
			if (!NewServerCompensationServiceImpl.OnOffset) {
				player.setTurnOnCompensationBtn(false);
			}else
				player.setTurnOnCompensationBtn(true);
			for (NewServerCompensationEntry compensationEntry : NewServerCompensationServiceImpl.filterList) {
				if (compensationEntry.getAccount().equals(player.getAccountName())) {
					if (compensationEntry.getRemain_coin()<1) {
						player.setTurnOnCompensationBtn(false);
						break;
					}
				}
			}

			if(NewServerCompensationServiceImpl.OnOffset && player.getTurnOnCompensationBtn())
			{
				String btn = "<td align=center><font color=\"7fff00\"><button value=\"领取补偿\" action=\"bypass -h npc?compensation\" width=66 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><font></td>";
				html = html.replace("<?display?>", btn);
			}
			else
			{
				html = html.replace("<?display?>", "<td width=66 height=25></td>");
			}
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.setLastNpc(this);
		}
		else
		{
			showChatWindow(player, "member/" + getNpcId() + "-" + val + ".htm", firstTalk);
		}
		
	}
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final String[] buypassOptions = command.split(" ");
		String html = "";
		//Announcements.announceToAll("command:" + command);
		if(command.startsWith("Chat"))
		{
			//bypass -h npc_%objectId%_Chat 1
			showChatWindow(player, "member/" + getNpcId() + "-" + buypassOptions[1] + ".htm", false);
		}
		//bypass -h npc?BuffMagic
		else if(buypassOptions[0].equals("BuffMagic"))
		{
			int Pages = 1;
			if(buypassOptions.length == 2)
			{
				Pages = Integer.parseInt(buypassOptions[1]);
			}
		}
		else if(buypassOptions[0].equals("GetClanGift"))
		{
			showPage(player,getNpcId()+"-2.htm");
		}
		//bypass -h npc?GiveBuffToPlayer gm deletetime
		else if(buypassOptions[0].equals("GiveBuffToPlayer"))
		{
			String name = buypassOptions[1];
			int times = Integer.parseInt(buypassOptions[2]);
			if(!checkBuffAndGive(player, name, times))
			{
				player.sendMessage("出了一些問題。");
			}
			showPage(player, getNpcId()+"-2.htm");
		}
		else if(buypassOptions[0].equals("UpgradeLoad"))
		{
			final int feeItemId = 29984;
			final long feeItemCount = 1000;
			final String[] availableColors = BBSConfig.COLOR_TITLE_SERVICE_COLORS;
			if(feeItemId == 0 || availableColors.length == 0)
			{
				player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/upgrade_load.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();
			if(buypassOptions.length == 1)
			{
				if(feeItemCount > 0)
				{
					String feeBlock = tpls.get(1);
					feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
					feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

					content.append(feeBlock);
				}
				else
					content.append(tpls.get(2));

				content.append(tpls.get(3));
			}
			else
			{
				if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
				{
					onWrongCondition(player);
					return;
				}

				// 這裡需要判定玩家目前等級
				int level = Math.max(player.getSkillLevel(46003), 0);
				if (level < 10)
				{ 
					if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));
	
						content.append(noHaveItemBlock);
					}
					else
					{
						//這裡要提升下一個等級寫法
						content.append(tpls.get(5));
						if(level > 0)
						{
							player.removeSkill(46003, true);//先刪除技能等級
						}
						
						SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 46003, level + 1);
						
						player.addSkill(skillEntry, true);
						player.sendSkillList();
						//player.getPlayer().sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skillEntry.getId(), skillEntry.getLevel()));
						player.broadcastUserInfo(true);
						Log.add("玩家 " + player.getName() + " 增加負重成功  " + level + " >> " + (level + 1), "負重");
					}
					
				}else {
					content.append(tpls.get(6));
				}			
			}
			html = html.replace("<?content?>", content.toString());
			sendHtmlMessage(player, html);
		}
		else if(buypassOptions[0].equals("pccoupon"))
		{
			player.sendPacket(ShowBoardPacket.CLOSE);
			player.sendPacket(ShowPCCafeCouponShowUI.STATIC);
			return;
		}
		else if(buypassOptions[0].equals("GetMemberCoin"))
		{
			int memberCoins = CheckMemberHaveConis(player);
			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/GetMemberCoin.htm", player);
			html = tpls.get(0);
			
			if(buypassOptions.length ==2)
			{
				int getCoins =   Integer.parseInt(buypassOptions[1]);;
				if ( getCoins > memberCoins)
				{
					String tmp = tpls.get(3);//輸入超出數額
					html = html.replace("<?content?>", tmp);
					sendHtmlMessage(player, html);
					return;
				}
				if(UpdateMemberConis(player , -getCoins))
				{
					ItemFunctions.addItem(player, 29984, getCoins, true);
				}
				memberCoins = memberCoins - getCoins;
			}
			
			if(memberCoins == 0)//顯示沒有贊助或領取完成
			{
				String tmp = tpls.get(2);
				html = html.replace("<?content?>", tmp);
				sendHtmlMessage(player, html);
				return;
			}
			String tmp = tpls.get(1);//顯示會員幣數量
			tmp  = tmp.replace("<$memberCoins$>", memberCoins+"");
			html = html.replace("<?content?>", tmp);
			sendHtmlMessage(player, html);
		}
		else if(buypassOptions[0].equals("getEveryDay"))
		{
			if (player.hasPremiumAccount())
			{
				if(getToday(player))
				{
					for (int i = 0;i<GIveEveryDayItemId.length;i++)
					{
						ItemFunctions.addItem(player, GIveEveryDayItemId[i][0], GIveEveryDayItemId[i][1], true);
					}
					updateGiveOk(player);
				}
				else {
					player.sendMessage("今日已取。");
				}
			}
		}
		else if(buypassOptions[0].equals("premiumAccount"))
		{
			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/premiumAccount.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();

			if(Config.PREMIUM_ACCOUNT_ENABLED)
			{
				if(buypassOptions.length > 1)
				{
					String cmd3 = buypassOptions[1];
					if("info".equals(cmd3))
					{
						if(buypassOptions.length < 2)
							return;

						final int schemeId = Integer.parseInt(buypassOptions[2]);

						final PremiumAccountTemplate paTemplate = PremiumAccountHolder.getInstance().getPremiumAccount(schemeId);
						if(paTemplate == null)
						{
							_log.warn(getClass().getSimpleName() + ": Error while open info about premium account scheme ID[" + schemeId + "]! Scheme is null.");
							return;
						}

						final int schemeDelay = Integer.parseInt(buypassOptions[3]);

						List<ItemData> feeItems = paTemplate.getFeeItems(schemeDelay);
						if(feeItems == null)
							return;

						String delayName = "";
						if(schemeDelay > 0)
						{
							int days = schemeDelay / 24;
							int hours = schemeDelay % 24;
							if(days > 0 && hours > 0)
							{
								delayName = tpls.get(11);
								delayName = delayName.replace("<?days?>", String.valueOf(days));
								delayName = delayName.replace("<?hours?>", String.valueOf(hours));
							}
							else if(days > 0)
							{
								delayName = tpls.get(10);
								delayName = delayName.replace("<?days?>", String.valueOf(days));
							}
							else if(hours > 0)
							{
								delayName = tpls.get(9);
								delayName = delayName.replace("<?hours?>", String.valueOf(hours));
							}
						}
						else
							delayName = tpls.get(12);

						String infoBlock = tpls.get(6);
						infoBlock = infoBlock.replace("<?scheme_id?>", String.valueOf(paTemplate.getType()));
						infoBlock = infoBlock.replace("<?scheme_delay?>", String.valueOf(schemeDelay));
						infoBlock = infoBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
						infoBlock = infoBlock.replace("<?period?>", delayName);
						infoBlock = infoBlock.replace("<?exp_rate?>", doubleToString(paTemplate.getExpRate()));
						infoBlock = infoBlock.replace("<?sp_rate?>", doubleToString(paTemplate.getSpRate()));
						infoBlock = infoBlock.replace("<?adena_drop_rate?>", doubleToString(paTemplate.getAdenaRate()));
						infoBlock = infoBlock.replace("<?items_drop_rate?>", doubleToString(paTemplate.getDropRate()));
						infoBlock = infoBlock.replace("<?spoil_rate?>", doubleToString(paTemplate.getSpoilRate()));
						infoBlock = infoBlock.replace("<?quest_drop_rate?>", doubleToString(paTemplate.getQuestDropRate()));
						infoBlock = infoBlock.replace("<?quest_reward_rate?>", doubleToString(paTemplate.getQuestRewardRate()));
						infoBlock = infoBlock.replace("<?enchant_chance?>", doubleToString(paTemplate.getEnchantChanceBonus()));
						infoBlock = infoBlock.replace("<?craft_chance?>", doubleToString(paTemplate.getCraftChanceBonus()));

						String feeItemBlock = "";
						if(!feeItems.isEmpty())
						{
							feeItemBlock = tpls.get(13);
							feeItemBlock = feeItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItems.get(0).getId()));
							feeItemBlock = feeItemBlock.replace("<?fee_count?>", Util.formatAdena(feeItems.get(0).getCount()));

							final String feeItemsBlockStr = tpls.get(14);
							StringBuilder feeItemsBlock = new StringBuilder();
							for(int i = 1; i < feeItems.size(); i++)
							{
								ItemData feeItem = feeItems.get(i);

								String tempBlock = feeItemsBlockStr;
								tempBlock = tempBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItem.getId()));
								tempBlock = tempBlock.replace("<?fee_count?>", Util.formatAdena(feeItem.getCount()));

								feeItemsBlock.append(tempBlock);
							}
							feeItemBlock = feeItemBlock.replace("<?fee_items?>", feeItemsBlock.toString());
						}
						infoBlock = infoBlock.replace("<?fees?>", feeItemBlock);

						content.append(infoBlock);
					}
					else if("buy".equals(cmd3))
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						if(!Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER && AuthServerCommunication.getInstance().isShutdown())
							content.append(tpls.get(4));
						else
						{
							if(buypassOptions.length < 2)
								return;

							final int schemeId = Integer.parseInt(buypassOptions[2]);
							final PremiumAccountTemplate paTemplate = PremiumAccountHolder.getInstance().getPremiumAccount(schemeId);
							if(paTemplate == null)
							{
								_log.warn(getClass().getSimpleName() + ": Error while open info about premium account scheme ID[" + schemeId + "]! Scheme is null.");
								return;
							}

							final int schemeDelay = Integer.parseInt(buypassOptions[3]);

							List<ItemData> feeItems = paTemplate.getFeeItems(schemeDelay);
							if(feeItems == null)
								return;

							if(player.hasPremiumAccount() && player.getPremiumAccount() != paTemplate)
							{
								int premiumAccountExpire = player.getNetConnection().getPremiumAccountExpire();
								if(premiumAccountExpire != Integer.MAX_VALUE)
								{
									String expireBlock = tpls.get(5);
									expireBlock = expireBlock.replace("<?date_expire?>", TimeUtils.toSimpleFormat(premiumAccountExpire * 1000L));
									content.append(expireBlock);
								}
								else
									content.append(tpls.get(8));
							}
							else
							{
								boolean success = true;

								if(!feeItems.isEmpty())
								{
									for(ItemData feeItem : feeItems)
									{
										if(!ItemFunctions.haveItem(player, feeItem.getId(), feeItem.getCount()))
										{
											success = false;
											break;
										}
									}

									if(success)
									{
										for(ItemData feeItem : feeItems)
											ItemFunctions.deleteItem(player, feeItem.getId(), feeItem.getCount());
									}
									else
										content.append(tpls.get(7));
								}
	
								if(success)
								{
									if(player.givePremiumAccount(paTemplate, schemeDelay))
									{
										player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
									}
									else
									{
										for(ItemData feeItem : feeItems)
											ItemFunctions.addItem(player, feeItem.getId(), feeItem.getCount());
									}

									//IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler("_cbbsservices_pa");
									//if(handler != null)
										//onBypassCommand(player, "_cbbsservices_pa");
									return;
								}
							}
						}
					}
				}
				else
				{
					if(player.hasPremiumAccount())
					{
						PremiumAccountTemplate paTemplate = player.getPremiumAccount();
						int premiumAccountExpire = player.getNetConnection().getPremiumAccountExpire();
						if(premiumAccountExpire != Integer.MAX_VALUE)
						{
							String expireBlock = tpls.get(15);
							expireBlock = expireBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
							expireBlock = expireBlock.replace("<?date_expire?>", TimeUtils.toSimpleFormat(premiumAccountExpire * 1000L));
							content.append(expireBlock);
						}
						else
						{
							String expireBlock = tpls.get(16);
							expireBlock = expireBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
							content.append(expireBlock);
						}
					}

					content.append(tpls.get(2));

					String schemeButton = tpls.get(3);
					//領取每日獎勵<?S1?>         bypass -h npc?getEveryDay <?S2?>
					if(player.hasPremiumAccount())
					{
						if(getToday(player))
						{
							schemeButton = schemeButton.replace("<?S1?>", "领取每日宝箱");
							schemeButton = schemeButton.replace("<?S2?>", "bypass -h npc?getEveryDay");
						}
						else
						{
							schemeButton = schemeButton.replace("<?S1?>", "今日已领取");
							schemeButton = schemeButton.replace("<?S2?>", "");
						}
					}
					else
					{
						schemeButton = schemeButton.replace("<?S1?>", "会员可领取每日宝箱");
						schemeButton = schemeButton.replace("<?S2?>", "");
					}
					for(PremiumAccountTemplate paTemplate : PremiumAccountHolder.getInstance().getPremiumAccounts())
					{
						int type = paTemplate.getType();
						String name = paTemplate.getName(player.getLanguage());
						for(int delay : paTemplate.getFeeDelays())
						{
							String delayName = "";
							if(delay > 0)
							{
								int days = delay / 24;
								int hours = delay % 24;
								if(days > 0 && hours > 0)
								{
									delayName = tpls.get(11);
									delayName = delayName.replace("<?days?>", String.valueOf(days));
									delayName = delayName.replace("<?hours?>", String.valueOf(hours));
								}
								else if(days > 0)
								{
									delayName = tpls.get(10);
									delayName = delayName.replace("<?days?>", String.valueOf(days));
								}
								else if(hours > 0)
								{
									delayName = tpls.get(9);
									delayName = delayName.replace("<?hours?>", String.valueOf(hours));
								}
							}
							else
								delayName = tpls.get(12);

							String tempButton = schemeButton.replace("<?scheme_name?>", name);
							tempButton = tempButton.replace("<?delay_name?>", delayName);
							tempButton = tempButton.replace("<?scheme_id?>", String.valueOf(type));
							tempButton = tempButton.replace("<?scheme_delay?>", String.valueOf(delay));
							content.append(tempButton);
						}
					}
				}
			}
			else
				content.append(tpls.get(1));

			html = html.replace("<?content?>", content.toString());
			sendHtmlMessage(player, html);
		}
		//ChangeHtml
		else if(buypassOptions[0].equals("ChangeHtml"))
		{
			showPage(player,  buypassOptions[1]);
		}
		else if(buypassOptions[0].equals("changename"))
		{			
			if("player".equals(buypassOptions[1]))
			{
				final int feeItemId = BBSConfig.CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_player_name.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length == 2)//if(!st.hasMoreTokens())
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					String newPlayerName = buypassOptions[2];//st.nextToken();
					if(newPlayerName.charAt(0) == ' ')
						newPlayerName = newPlayerName.substring(1);

					if(player.getName().equals(newPlayerName))
						content.append(tpls.get(7));
					if(!Util.isMatchingRegexp(newPlayerName, Config.CNAME_TEMPLATE))
						content.append(tpls.get(5));
					else if(CharacterDAO.getInstance().getObjectIdByName(newPlayerName) > 0)
						content.append(tpls.get(6));
					else if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(8).replace("<?player_name?>", newPlayerName));

						String oldName = player.getName();

						player.reName(newPlayerName, true);
						Log.add("Character " + oldName + " renamed to " + newPlayerName, "renames");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
			else if("pet".equals(buypassOptions[1]))
			{
				final int feeItemId = BBSConfig.CHANGE_PET_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.CHANGE_PET_NAME_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_pet_name.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length == 2)//if(!st.hasMoreTokens())
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					String newPetName = buypassOptions[2];//if(!st.hasMoreTokens())"";//st.nextToken();
					if(newPetName.charAt(0) == ' ')
						newPetName = newPetName.substring(1);

					PetInstance pet = player.getPet();
					if(pet == null)
						content.append(tpls.get(8));
					else if(feeItemCount > 0 && pet.isDefaultName())
						content.append(tpls.get(7));
					else if(pet.getName().equals(newPetName))
						content.append(tpls.get(6));
					else if(newPetName.length() < 1 || newPetName.length() > 8)
						content.append(tpls.get(5));
					else if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(9).replace("<?pet_name?>", newPetName));

						String oldName = pet.getName();

						pet.setName(newPetName);
						pet.broadcastCharInfo();
						pet.updateControlItem();
						Log.add("Pet " + oldName + " renamed to " + newPetName, "renames");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
			else if("clan".equals(buypassOptions[1]))
			{
				final int feeItemId = BBSConfig.CHANGE_CLAN_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.CHANGE_CLAN_NAME_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_clan_name.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length == 2)//if(!st.hasMoreTokens())
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
					onWrongCondition(player);
					return;
					}

					String newClanName = buypassOptions[2];//st.nextToken();
					if(newClanName.charAt(0) == ' ')
						newClanName = newClanName.substring(1);

					final Clan clan = player.getClan();
					if(clan == null)
						content.append(tpls.get(8));
					else if(!player.isClanLeader())
						content.append(tpls.get(9));
					else if(clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).getName().equals(newClanName))
						content.append(tpls.get(6));
					else if(!Util.isMatchingRegexp(newClanName, Config.CLAN_NAME_TEMPLATE))
						content.append(tpls.get(5));
					else if(ClanTable.getInstance().getClanByName(newClanName) != null)
						content.append(tpls.get(7));
					else if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(10).replace("<?clan_name?>", newClanName));

						String oldName = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).getName();

						clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).setName(newClanName, true);
						clan.updateClanInDB();
						clan.broadcastClanStatus(true, true, true);
						player.broadcastUserInfo(true);
						Log.add("Clan " + oldName + " renamed to " + newClanName, "renames");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
		}
		else if(buypassOptions[0].equals("sex"))
		{
			final int feeItemId = BBSConfig.CHANGE_SEX_SERVICE_COST_ITEM_ID;
			final long feeItemCount = BBSConfig.CHANGE_SEX_SERVICE_COST_ITEM_COUNT;
			if(feeItemId == 0)
			{
				player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_sex.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();
			if(buypassOptions.length == 1)//if(!st.hasMoreTokens())
			{
				if(feeItemCount > 0)
				{
					String feeBlock = tpls.get(1);
					feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
					feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

					content.append(feeBlock);
				}
				else
					content.append(tpls.get(2));

				content.append(tpls.get(3));
			}
			else
			{
				String cmd3 = buypassOptions[1];
				if("change".equals(cmd3))
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));
						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()));

						player.changeSex();
						player.broadcastUserInfo(true);
						player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
						Log.add("Player " + player.getName() + " changed sex to : " + player.getSex(), "changesex");
					}
				}
			}
			html = html.replace("<?content?>", content.toString());
			sendHtmlMessage(player, html);
		}
		else if(buypassOptions[0].equals("expand"))
		{
			String cmd3 = buypassOptions[1];
			if("inventory".equals(cmd3))
			{
				final int feeItemId = BBSConfig.EXPAND_INVENTORY_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.EXPAND_INVENTORY_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				// TODO: Вынести конфиг в конфиги коммунити.
				if (Config.SERVICES_EXPAND_INVENTORY_MAX-150 <= player.getExpandInventory())//修復擴充問題
				{
					player.sendMessage(player.isLangRus() ? "擴充到最大值，無法再擴充。" : "扩充到最大值，无法再扩充。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/expand_inventory.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length==2)//if(!st.hasMoreTokens())
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					int count = Integer.parseInt(buypassOptions[2]);
					if(count == 0)
						return;

					long price = feeItemCount * count;
					if(price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

						player.setExpandInventory(player.getExpandInventory() + count);
						player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
						player.sendPacket(new ExStorageMaxCountPacket(player));
						Log.add("Player " + player.getName() + " expand inventory", "expandinventory");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
			else if("warehouse".equals(cmd3))
			{
				final int feeItemId = BBSConfig.EXPAND_WAREHOUSE_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.EXPAND_WAREHOUSE_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/expand_warehouse.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length==2)
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					int count = Integer.parseInt(buypassOptions[2]);
					if(count == 0)
						return;

					long price = feeItemCount * count;
					if(price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

						player.setExpandWarehouse(player.getExpandWarehouse() + count);
						player.setVar("ExpandWarehouse", String.valueOf(player.getExpandWarehouse()), -1);
						player.sendPacket(new ExStorageMaxCountPacket(player));
						Log.add("Player " + player.getName() + " expand warehouse", "expandwarehouse");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
			else if("clanwarehouse".equals(cmd3))
			{
				final int feeItemId = BBSConfig.EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				Clan clan = player.getClan();
				if(clan == null)
				{
					player.sendMessage(player.isLangRus() ? "您不是血盟成員。" : "您不是血盟成员。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/expand_clanwarehouse.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length==2)
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					int count = Integer.parseInt(buypassOptions[2]);
					if(count == 0)
						return;

					long price = feeItemCount * count;
					if(price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

						clan.setWhBonus(player.getClan().getWhBonus() + count);
						player.sendPacket(new ExStorageMaxCountPacket(player));
						Log.add("Player " + player.getName() + " expand clan warehouse", "expandclanwarehouse");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
		}
		else if(buypassOptions[0].equals("color"))
		{
			String cmd3 = buypassOptions[1];
			if("name".equals(cmd3))
			{
				final int feeItemId = BBSConfig.COLOR_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.COLOR_NAME_SERVICE_COST_ITEM_COUNT;
				final String[] availableColors = BBSConfig.COLOR_NAME_SERVICE_COLORS;
				if(feeItemId == 0 || availableColors.length == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/color_name_change.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length == 2)
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					final String colorBlock = tpls.get(3).replace("<?player_name?>", player.getName());

					if(player.getNameColor() != Integer.decode("0xFFFFFF"))
						content.append(colorBlock.replace("<?color?>", "FFFFFF"));

					for(String color : availableColors)
					{
						String bgrColor = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
						if(player.getNameColor() != Integer.decode("0x" + bgrColor))
							content.append(colorBlock.replace("<?color?>", color));
					}
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					final String newColor = buypassOptions[2].replace(" ", "");

					if(!newColor.equalsIgnoreCase("FFFFFF"))
					{
						boolean available = false;
						for(String color : availableColors)
						{
							if(color.equalsIgnoreCase(newColor))
							{
								available = true;
								break;
							}
						}

						if(!available)
						{
							player.sendPacket(ShowBoardPacket.CLOSE);
							return;
						}
					}

					final String bgrNewColor = newColor.substring(4, 6) + newColor.substring(2, 4) + newColor.substring(0, 2);
					final int newColorInt = Integer.decode("0x" + bgrNewColor);
					if(player.getNameColor() == newColorInt)
					{
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(5).replace("<?color?>", newColor).replace("<?player_name?>", player.getName()));

						player.setNameColor(newColorInt);
						player.broadcastUserInfo(true);
						Log.add("Player " + player.getName() + " changed name color to " + newColor, "changecolor");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
			else if("title".equals(cmd3))
			{
				final int feeItemId = BBSConfig.COLOR_TITLE_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.COLOR_TITLE_SERVICE_COST_ITEM_COUNT;
				final String[] availableColors = BBSConfig.COLOR_TITLE_SERVICE_COLORS;
				if(feeItemId == 0 || availableColors.length == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/color_title_change.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(buypassOptions.length == 2)
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					final String colorBlock = tpls.get(3).replace("<?player_name?>", player.getName());

					if(player.getTitleColor() != Integer.decode("0xFFFF77"))
						content.append(colorBlock.replace("<?color?>", "77FFFF"));

					for(String color : availableColors)
					{
						String bgrColor = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
						if(player.getTitleColor() != Integer.decode("0x" + bgrColor))
							content.append(colorBlock.replace("<?color?>", color));
					}
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					final String newColor = buypassOptions[2].replace(" ", "");

					if(!newColor.equalsIgnoreCase("77FFFF"))
					{
						boolean available = false;
						for(String color : availableColors)
						{
							if(color.equalsIgnoreCase(newColor))
							{
								available = true;
								break;
							}
						}

						if(!available)
						{
							player.sendPacket(ShowBoardPacket.CLOSE);
							return;
						}
					}

					final String bgrNewColor = newColor.substring(4, 6) + newColor.substring(2, 4) + newColor.substring(0, 2);
					final int newColorInt = Integer.decode("0x" + bgrNewColor);
					if(player.getTitleColor() == newColorInt)
					{
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(5).replace("<?color?>", newColor).replace("<?player_name?>", player.getName()));

						player.setTitleColor(newColorInt);
						player.broadcastUserInfo(true);
						Log.add("Player " + player.getName() + " changed title color to " + newColor, "changecolor");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
		}
		else if(buypassOptions[0].equals("SetCareer"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String cmd = st.nextToken();
			String cmd2 = st.nextToken();
			if("profession".equals(cmd2))
			{
				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/18057-Career.htm", player);
				html = tpls.get(0);
				StringBuilder content = new StringBuilder();
				final int feeItemId = getFeeItemIdForChangeClass(player);
				final long feeItemCount = getFeeItemCountForChangeClass(player);
				final int nextClassMinLevel = getNextClassMinLevel(player);
				if(!st.hasMoreTokens())
				{
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
				sendHtmlMessage(player, html);	
			}
		}
		else if(buypassOptions[0].equals("ShowChangeClassId"))
		{
			ShowChangeClassId(player);
		}else if(buypassOptions[0].equals("ShowChangeSuite"))
		{
			ShowChangeSuite(player);
		}else if(buypassOptions[0].equals("ShowMyLevelCanChange"))
		{
			if (Hero.getInstance().isHero(player.getObjectId())) {
				player.sendMessage("英雄不能转换！");
				return;
			}
			ShowMyLevelCanChange(player);
		}else if(buypassOptions[0].equals("showChangeClassLog"))
		{
			int index = Integer.parseInt(buypassOptions[1]);
			showChangeClassLog(player, index);
		}else if(buypassOptions[0].equals("ChangeMyClassId"))
		{
			int myClassId = Integer.parseInt(buypassOptions[1]);
			if (myClassId > (ClassId.VALUES.length - 1) && myClassId != 192 && myClassId != 125 && myClassId != 193 && myClassId != 126 && myClassId != 127 && myClassId != 194 && myClassId != 130 && myClassId != 131 && myClassId != 195 && myClassId != 134)
			{
				player.sendMessage("輸入的參數錯誤");// There are no classes over 136 id.
				return;
			}
			// 如果玩家有 myClassId 的 副职业 则不能装换
			if (player.getSubClassList().containsClassId(myClassId)) {
				assert ClassId.valueOf(myClassId) != null;
				player.sendMessage("你當前存在副職業:"+ClassId.valueOf(myClassId).getName(player)+"不能進行職業轉換");// There are no classes over 136 id.
				return;
			}
			int money = GetLevelMoney(player);
			if(player.getInventory().getCountOf(29984) < money)
			{
				player.sendMessage("支付費用不足 " + money + " 個裸鑽.");// There are no classes over 136 id.
				return;
			}
			for(Servitor servitor : player.getServitors())//包结宠物
				servitor.unSummon(false);//移除招唤的

			if(player.getCubics().size() > 0)
			{
				player.deleteCubics(); //移除晶体
			}
			if(player.isTransformed())//变身
			{
				player.setTransform(null);
			}
			for(SkillEntry skillEntry : player.getAllSkills())//删除全部技能
			{
				boolean flag = false;
				if(skillEntry != null){
					for (int i = 0; i < skillReserve.length; i++) {
						if (skillReserve[i] == skillEntry.getId()) {
							flag = true;
							break;
						}
					}
					if (!flag) {
						player.removeSkill(skillEntry, true);
					}
				}
			}
			Henna[] hennas = player.getHennaList().values(false);
			for(Henna henna : hennas)//删除纹身
			{
				player.getHennaList().remove(henna);
			}
			for(long party : Slot)//脱掉所有装备
			{
				player.getInventory().unEquipItemInBodySlot(party);
			}

			//player.sendSkillList();
			ItemFunctions.deleteItem(player, 29984, money, true); //这样子删除物品有讯息出来
			int oldId = player.getActiveClassId();
			String name = "「" + player.getName() + "」通過新魔力服管理員轉換了自己的職業為: " + player.getClassId().getName(player) + " -> ";
			player.setClassId(myClassId, true);
			player.broadcastCharInfo();
			name += player.getClassId().getName(player);
			UpdateChangeClassId(player,oldId,myClassId);
			Announcements.announceToAll(name);

//			//這裡應該要把武器轉換學習的技能給完全載入給玩家。 20210409 這部份需要去實測，很重要。
//			int[] learnSkills = GetLearnedSkillList(player);
//			for (int i = 0; i < learnSkills.length; i++)
//			{
//				SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, learnSkills[i], 1);
//				player.addSkill(skillEntry, true);
//			}
		}
		else if(buypassOptions[0].equals("SetVisualSuite"))
		{
			long Times = Long.parseLong(buypassOptions[2]);
			AbnormalEffect ShowSuite = AbnormalEffect.valueOf(buypassOptions[1]);
			int index = Correct(buypassOptions[1]);
			String Name = itemName[index][1];
			if(!player.canContainEffect(ShowSuite))
			{
				player.sendMessage("似乎出現了一點問題。");
				return ;
			}
			if(buypassOptions[1].equals("UNK_133"))//换装也限制女性才可以点 以免bug
			{
				if(player.getSex().ordinal() == 0)
				{
					player.sendMessage("女性限定裝.");
					player.ResetAppearence();
					player._testEffect = AbnormalEffect.NONE;
					player._myBuyEffect = AbnormalEffect.NONE;
					ResetSuite(player);
					return;
				}
			}
			player.ResetAppearence();//先移除自身所有的外型效果
			player._testEffect = AbnormalEffect.NONE;
			player._myBuyEffect = ShowSuite;
			UpdateSuite(player,buypassOptions[1]);
			player.startAbnormalEffect(ShowSuite);
			if(player._ShowSuiteAppearence !=null)
			{
				player._ShowSuiteAppearence.cancel(false);
				player._ShowSuiteAppearence = null;
			}
			player._ShowSuiteAppearence = ThreadPoolManager.getInstance().schedule(() -> {
				player.stopAbnormalEffect(ShowSuite);
				player._testEffect = AbnormalEffect.NONE;
				player._myBuyEffect = AbnormalEffect.NONE;
				ResetSuite(player);
				player.sendMessage("你購買的「" + Name + "」商品已到期了。");
			}, Times - System.currentTimeMillis());//這一句是針對已購買的商品
		}
		else if(buypassOptions[0].equals("BuySuite"))
		{
			if(buypassOptions[1].equals("UNK_133"))
			{
				if(player.getSex().ordinal() == 0)
				{
					player.sendMessage("女性限定裝.");
					return;
				}
			}
			AbnormalEffect BuySuite = AbnormalEffect.valueOf(buypassOptions[1]);
			if(!player.canContainEffect(BuySuite))//先判断购买的类型是否存在
			{
				player.sendMessage("此類型不可購買.");
				return ;
			}
			if(CheckBuy(player, buypassOptions[1]))
			{
				player.sendMessage("已購購買,不可重複買.");
				return ;
			}
			if(player.getInventory().getCountOf(29984) < 250)
			{
				player.sendMessage("支付費用不足 250 裸鑽.");
				return ;
			}
			player.ask(new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("確定要購買「" +  buypassOptions[2] +"」？這將消耗 250 裸鑽"), new OnAnswerListener()
			{
				public void sayYes()
				{
					if(player.getInventory().getCountOf(29984) < 250) //再次判断 金币 100个
					{
						player.sendMessage("支付費用不足 250 裸鑽.");
						return;
					}
					ItemFunctions.deleteItem(player, 29984, 250, true);
					InsertIntoCharacterSuite(player, buypassOptions[1]);
				}

				public void sayNo()
				{
				}
			});
		}
		else if(buypassOptions[0].equals("SuiteAppearance"))//显示时装页面
		{
			if((player._inTryModeTime > System.currentTimeMillis()) && (player._inTryModeTime != 0))
			{
				player.sendMessage("請試穿結束後再試穿下一套。");
				return;
			}
			if(buypassOptions[1].equals("UNK_133"))
			{
				if(player.getSex().ordinal() == 0)
				{
					player.sendMessage("女性限定裝.");
					return;
				}
			}
			if(player._testEffect != AbnormalEffect.NONE)
			{
				if(player.containEffectNow(player._testEffect))
				{
					player.stopAbnormalEffect(player._testEffect);
				}
			}
			AbnormalEffect readyStatus = AbnormalEffect.valueOf(buypassOptions[1]);
			if(player.canContainEffect(readyStatus))
			{
				if(player._myBuyEffect !=AbnormalEffect.NONE )
				{
					player.stopAbnormalEffect(player._myBuyEffect);
				}
				player.startAbnormalEffect(readyStatus);
				player._inTryModeTime = System.currentTimeMillis() + 15 * 1000;
				player._testEffect = readyStatus;
				ThreadPoolManager.getInstance().schedule(() -> {
					if(player._testEffect == readyStatus)
					{
						player.stopAbnormalEffect(player._testEffect);
						player._testEffect = AbnormalEffect.NONE;
						if(player._myBuyEffect !=AbnormalEffect.NONE )
						{
							player.startAbnormalEffect(player._myBuyEffect);
						}
					}
				}, 15 * 1000);//设置15秒后移除试穿
			}
			showPage(player,getNpcId()+"-5.htm");
		}
		else if(buypassOptions[0].equals("BuyWeapenAppearance"))
		{
			int VisualId = Integer.parseInt(buypassOptions[1]);
			ItemTemplate Weapon = ItemHolder.getInstance().getTemplate(VisualId);
			ItemInstance item = player.getActiveWeaponInstance();
			if (item == null)
			{
				player.sendMessage("請裝備武器再來試穿。");
				return;
			}
			if (item.getItemType() != Weapon.getItemType())
			{
				player.sendMessage("請裝備相同武器類型再來購買。");
				return;
			}
			if(player.getInventory().getCountOf(29984) < 250)
			{
				player.sendMessage("支付費用不足 250 裸鑽.");
				return ;
			}
			if (CheckItemBuyWeapon(player.getObjectId(), VisualId))
			{
				player.sendMessage("你目前已購買，待過期後再買。");
				return;
			}

			player.ask(new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("確定要購買「" +  Weapon.getName() +"」外型？這將消耗 250 裸鑽"), new OnAnswerListener()
			{
				public void sayYes()
				{
					ItemTemplate Weapon = ItemHolder.getInstance().getTemplate(VisualId);
					ItemInstance item = player.getActiveWeaponInstance();
					if (item == null)
					{
						player.sendMessage("請裝備武器再來試穿。");
						return;
					}
					if (item.getItemType() != Weapon.getItemType())
					{
						player.sendMessage("請裝備相同武器類型再來購買。");
						return;
					}
					if(player.getInventory().getCountOf(29984) < 250)
					{
						player.sendMessage("支付費用不足 250 裸鑽.");
						return ;
					}
					if (CheckItemBuyWeapon(player.getObjectId(), VisualId))
					{
						player.sendMessage("你目前已購買，待過期後再買。");
						return;
					}
					if(item.getVisualId() > 0)//如果手上武器也是同類基武器且已做了外型的狀態下，先記得清空使用才會正常。
					{
						item.setVisualId(0);
						player.resetWeaponVisualId(item.getObjectId());
					}
					ItemFunctions.deleteItem(player, 29984, 250, true);
					InsertIntoCharacterWeapon(player.getObjectId(), item.getObjectId(), VisualId);
					item.setVisualId(VisualId);
					player.getInventory().sendModifyItem(item);
					player.broadcastUserInfo(true);
					player.sendPacket(new ExUserInfoEquipSlot(player, Inventory.PAPERDOLL_RHAND));
					//player.sendPacket(new ExUserInfoEquipSlot(player, Inventory.PAPERDOLL_LHAND));
					player.sendPacket(new ExUserInfoEquipSlot(player, Inventory.PAPERDOLL_LRHAND));
					showPage(player, buypassOptions[2]);
				}

				public void sayNo()
				{
				}
			});
		}
		else if(buypassOptions[0].equals("TestWeapenAppearance"))//这一个是test 测试试穿的
		{
			if(player._testWearWeapon)
			{
				player.sendMessage("請試穿結束後再來使用。");
				return;
			}
			ItemInstance item = player.getActiveWeaponInstance();
			if (item != null)
			{
				player.getInventory().unEquipItem(item);
			}

			Map<Integer, Integer> itemList = new HashMap<Integer, Integer>();
			int VisualId = Integer.parseInt(buypassOptions[1]);
			itemList.put(Inventory.PAPERDOLL_LRHAND, VisualId);
			//itemList.put(Inventory.PAPERDOLL_LHAND, VisualId);
			player.sendPacket(new ShopPreviewInfoPacket(itemList));
			player._testWearWeapon = true;
			ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(player, item), 10 * 1000);//10秒解除試穿
			showPage(player, buypassOptions[2]);
		}
		else if(buypassOptions[0].equals("ResetAppearence"))
		{
			player.ResetAppearence();
			player._testEffect = AbnormalEffect.NONE;
			player._myBuyEffect = AbnormalEffect.NONE;
			ResetSuite(player);
		}
		else if(buypassOptions[0].equals("ShowChangeWeapen"))
		{
			ShowChangeWeapen(player);
		}
		else if(buypassOptions[0].equals("resetWeapenAppearance"))
		{
			int objId = Integer.parseInt(buypassOptions[1]);
			int VisualId = Integer.parseInt(buypassOptions[2]);
			ItemInstance item = player.getActiveWeaponInstance();
			if (item == null)
			{
				player.sendMessage("請裝備武器在來解除。");
				return;
			}
			if (item.getObjectId() != objId)
			{
				player.sendMessage("是否拿錯把武器，請再次確認。");
				return;
			}
			item.setVisualId(0);

			player.getInventory().sendModifyItem(item);//这一个是会更新画面图示武器ICON
			//player.broadcastUserInfo(true);
			//player.sendPacket(new ExUserInfoEquipSlot(player, Inventory.PAPERDOLL_RHAND));
			//player.sendPacket(new ExUserInfoEquipSlot(player, Inventory.PAPERDOLL_LHAND));
			player.sendPacket(new ExUserInfoEquipSlot(player));
			//player.getInventory().sendEquipInfo(Inventory.PAPERDOLL_RHAND);//更新右手 人物显示武器
			//player.getInventory().sendEquipInfo(Inventory.PAPERDOLL_LHAND);//更新左手 人物显示武器
			player.broadcastUserInfo(true);
			UpdateItemVisualId(player.getObjectId(), 0, VisualId);
			ShowChangeWeapen(player);

		}
		else if(buypassOptions[0].equals("UseWeapenAppearance"))
		{
			int VisualId = Integer.parseInt(buypassOptions[1]);
			ItemTemplate Weapon = ItemHolder.getInstance().getTemplate(VisualId);
			ItemInstance item = player.getActiveWeaponInstance();
			if (item == null)
			{
				player.sendMessage("請裝備武器再來。");
				return;
			}
			if (item.getItemType() != Weapon.getItemType())
			{
				player.sendMessage("不同武器類型不可使用。");
				return;
			}
			if(item.getVisualId() > 0)
			{
				item.setVisualId(0);
				player.resetWeaponVisualId(item.getObjectId());
			}
			UpdateItemVisualId(player.getObjectId(), item.getObjectId(), VisualId);
			//InsertIntoCharacterWeapon(player.getObjectId(), item.getObjectId(), VisualId);
			item.setVisualId(VisualId);
			player.getInventory().sendModifyItem(item);

			//player.sendPacket(new ExUserInfoEquipSlot(player, Inventory.PAPERDOLL_RHAND));
			//player.sendPacket(new ExUserInfoEquipSlot(player, Inventory.PAPERDOLL_LHAND));
			player.sendPacket(new ExUserInfoEquipSlot(player));
			//.getInventory().sendEquipInfo(Inventory.PAPERDOLL_RHAND);//更新右手
			//player.getInventory().sendEquipInfo(Inventory.PAPERDOLL_LHAND);//更新左手
			player.broadcastUserInfo(true);
			ShowChangeWeapen(player);
		}
		else if(buypassOptions[0].equals("compensation"))
		{
			if (!NewServerCompensationServiceImpl.OnOffset) {
				return;
			}
			// 领取后 绑定账户设置临时关闭
			String page = HtmCache.getInstance().getHtml("member/compensationUnit.htm", player);
			NewServerCompensationEntry compensationEntry = null;
			for (NewServerCompensationEntry newServerCompensationEntry : NewServerCompensationServiceImpl.filterList) {
				if (newServerCompensationEntry.getAccount().equals(player.getAccountName())) {
					compensationEntry = newServerCompensationEntry;
					break;
				}
			}
			if (compensationEntry==null)
				return;

			page = page.replace("%remain%",String.valueOf(lcoinCountFormat(compensationEntry.getRemain_coin())));

			page = page.replace("%Lcoin1%",String.valueOf(lcoinCountFormat(compensationEntry.getFirstGiveCoin())));

			page = page.replace("%Lcoin2%",String.valueOf(lcoinCountFormat(compensationEntry.getSecondGiveCoin())));

			page = page.replace("%Lcoin3%",String.valueOf(lcoinCountFormat(compensationEntry.getThirdGiveCoin())));

			page = page.replace("%Lcoin4%",String.valueOf(lcoinCountFormat(compensationEntry.getFourthGiveCoin())));

			page = page.replace("%Lcoin5%",String.valueOf(lcoinCountFormat(compensationEntry.getFifthGiveCoin())));

			int compensationUnit = NewServerCompensationServiceImpl.compensationUnit;

			if (compensationUnit == 1) {

				if (compensationEntry.getFirstDate() < 1){
					String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFirstGiveCoin()+" first_date");
					page = page.replace("%Lcoin1_btn%",btn1);// 有 但未 **领取
				}
				else
					page = page.replace("%Lcoin1_btn%",compensation_btn2);// 有 **已领取  忽略第一期

				if (compensationEntry.getSecondGiveCoin() < 1)
					page = page.replace("%Lcoin2_btn%",compensation_btn4);// 不足 **不可领取
				else
					page = page.replace("%Lcoin2_btn%",compensation_btn3);// 充足 时间未到 **未开启

				if (compensationEntry.getThirdGiveCoin() < 1)
					page = page.replace("%Lcoin3_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin3_btn%",compensation_btn3);

				if (compensationEntry.getFourthGiveCoin() < 1)
					page = page.replace("%Lcoin4_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin4_btn%",compensation_btn3);

				if (compensationEntry.getFifthGiveCoin() < 1)
					page = page.replace("%Lcoin5_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin5_btn%",compensation_btn3);
			}
			else if (compensationUnit == 2) {
				if (compensationEntry.getFirstDate() < 1){
					String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFirstGiveCoin()+" first_date");
					page = page.replace("%Lcoin1_btn%",btn1);// 有 但未 **领取
				}
				else
					page = page.replace("%Lcoin1_btn%",compensation_btn2);// 有 **已领取  忽略第一期

				if (compensationEntry.getSecondGiveCoin() < 1){
					page = page.replace("%Lcoin2_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getSecondDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getSecondGiveCoin()+" second_date");
						page = page.replace("%Lcoin2_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin2_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}

				if (compensationEntry.getThirdGiveCoin() < 1)
					page = page.replace("%Lcoin3_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin3_btn%",compensation_btn3);

				if (compensationEntry.getFourthGiveCoin() < 1)
					page = page.replace("%Lcoin4_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin4_btn%",compensation_btn3);

				if (compensationEntry.getFifthGiveCoin() < 1)
					page = page.replace("%Lcoin5_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin5_btn%",compensation_btn3);
			}
			else if (compensationUnit == 3) {
				if (compensationEntry.getFirstDate() < 1){
					String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFirstGiveCoin()+" first_date");
					page = page.replace("%Lcoin1_btn%",btn1);// 有 但未 **领取
				}
				else
					page = page.replace("%Lcoin1_btn%",compensation_btn2);// 有 **已领取  忽略第一期

				if (compensationEntry.getSecondGiveCoin() < 1){
					page = page.replace("%Lcoin2_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getSecondDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getSecondGiveCoin()+" second_date");
						page = page.replace("%Lcoin2_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin2_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}

				if (compensationEntry.getThirdGiveCoin() < 1){
					page = page.replace("%Lcoin3_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getThirdDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getThirdGiveCoin()+" third_date");
						page = page.replace("%Lcoin3_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin3_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}

				if (compensationEntry.getFourthGiveCoin() < 1)
					page = page.replace("%Lcoin4_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin4_btn%",compensation_btn3);

				if (compensationEntry.getFifthGiveCoin() < 1)
					page = page.replace("%Lcoin5_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin5_btn%",compensation_btn3);

			}
			else if (compensationUnit == 4) {
				if (compensationEntry.getFirstDate() < 1){
					String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFirstGiveCoin()+" first_date");
					page = page.replace("%Lcoin1_btn%",btn1);// 有 但未 **领取
				}
				else
					page = page.replace("%Lcoin1_btn%",compensation_btn2);// 有 **已领取  忽略第一期

				if (compensationEntry.getSecondGiveCoin() < 1){
					page = page.replace("%Lcoin2_btn%",compensation_btn4);
				}
				else {
					if (compensationEntry.getSecondDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getSecondGiveCoin()+" second_date");
						page = page.replace("%Lcoin2_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin2_btn%",compensation_btn2);// 有 **已领取
				}

				if (compensationEntry.getThirdGiveCoin() < 1){
					page = page.replace("%Lcoin3_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getThirdDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getThirdGiveCoin()+" third_date");
						page = page.replace("%Lcoin3_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin3_btn%",compensation_btn2);// 有 **已领取
				}

				if (compensationEntry.getFourthGiveCoin() < 1){
					page = page.replace("%Lcoin4_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getFourthDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFourthGiveCoin()+" fourth_date");
						page = page.replace("%Lcoin4_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin4_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}

				if (compensationEntry.getFifthGiveCoin() < 1)
					page = page.replace("%Lcoin5_btn%",compensation_btn4);
				else
					page = page.replace("%Lcoin5_btn%",compensation_btn3);

			}
			else if (compensationUnit == 5) {
				if (compensationEntry.getFirstDate() < 1){
					String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFirstGiveCoin()+" first_date");
					page = page.replace("%Lcoin1_btn%",btn1);// 有 但未 **领取
				}
				else
					page = page.replace("%Lcoin1_btn%",compensation_btn2);// 有 **已领取  忽略第一期

				if (compensationEntry.getSecondGiveCoin() < 1){
					page = page.replace("%Lcoin2_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getSecondDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getSecondGiveCoin()+" second_date");
						page = page.replace("%Lcoin2_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin2_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}

				if (compensationEntry.getThirdGiveCoin() < 1){
					page = page.replace("%Lcoin3_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getThirdDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getThirdGiveCoin()+" third_date");
						page = page.replace("%Lcoin3_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin3_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}

				if (compensationEntry.getFourthGiveCoin() < 1){
					page = page.replace("%Lcoin4_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getFourthDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFourthGiveCoin()+" fourth_date");
						page = page.replace("%Lcoin4_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin4_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}

				if (compensationEntry.getFifthGiveCoin() < 1){
					page = page.replace("%Lcoin5_btn%",compensation_btn4);
				}
				else{
					if (compensationEntry.getFifthDate() < 1){
						String btn1 = compensation_btn1.replace("player", "bypass -h npc?getCompensationLcoin " + player.getAccountName()+" "+compensationEntry.getFifthGiveCoin()+" fifth_date");
						page = page.replace("%Lcoin5_btn%",btn1);// 有 但未 **领取
					}
					else
						page = page.replace("%Lcoin5_btn%",compensation_btn2);// 有 **已领取  忽略第一期
				}
			}

			HtmlMessage msg = new HtmlMessage(0);
			msg.setItemId(-1);
			msg.setHtml(page);
			player.sendPacket(msg);
		}
		else if(buypassOptions[0].startsWith("getCompensationLcoin"))
		{
			if (!NewServerCompensationServiceImpl.OnOffset) {
				showChatWindow(player, 0, true);
                return;
			}
			String accountName = buypassOptions[1];

			List<NewServerCompensationEntry> filterList = NewServerCompensationServiceImpl.filterList;
			NewServerCompensationEntry CompensationEntry = null;
			for (NewServerCompensationEntry newServerCompensationEntry : filterList) {
				if (newServerCompensationEntry.getAccount().equals(accountName)) {
					if (!checkCanGetCompensation(newServerCompensationEntry)) {
						player.sendMessage("当前领取状态有误,请重试!");
						return;
					}
					CompensationEntry = newServerCompensationEntry;
					break;
				}
			}
			// 领取
			if (CompensationEntry==null) {
				return;
			}

			if (player.isInventoryFull()) {
				player.sendMessage("你的包裹已满,请清理后再来领取!");
				return;
			}
			int count = Integer.parseInt(buypassOptions[2]);
			String unitName = buypassOptions[3];

			player.ask(new ConfirmDlgPacket(SystemMsg.S1, 0).addString("确认领取["+count*100+"]个 lcoin"), new OnAnswerListener() {
				@Override
				public void sayYes() {
					NewServerCompensationDao.getInstance().updateNewServerCompensation(accountName,player.getName(),count,unitName);
					ItemFunctions.addItem(player,NewServerCompensationServiceImpl.gameCoinId,(long)(count*100L));

					// 更新、添加道具、重新加载 (如果所有账号全部领取完, 则关闭该系统)
					NewServerCompensationServiceImpl.getInstance().load();

					showChatWindow(player, 0, true);
				}

				@Override
				public void sayNo() {
					showChatWindow(player, 0, true);
				}
			});

		}
	}

	private boolean checkCanGetCompensation(NewServerCompensationEntry newServerCompensationEntry) {
		int compensationUnit = NewServerCompensationServiceImpl.compensationUnit;

		if (compensationUnit == 1){
			if (newServerCompensationEntry.getRemain_coin() >= newServerCompensationEntry.getFirstGiveCoin() && newServerCompensationEntry.getFirstDate() < 1) {
				return true;
			}
		}
		else if (compensationUnit == 2){
			if (newServerCompensationEntry.getRemain_coin() >= newServerCompensationEntry.getSecondGiveCoin() && newServerCompensationEntry.getSecondDate() < 1) {
				return true;
			}
		}
		else if (compensationUnit == 3){
			if (newServerCompensationEntry.getRemain_coin() >= newServerCompensationEntry.getThirdGiveCoin() && newServerCompensationEntry.getThirdDate() < 1) {
				return true;
			}
		}
		else if (compensationUnit == 4){
			if (newServerCompensationEntry.getRemain_coin() >= newServerCompensationEntry.getFourthGiveCoin() && newServerCompensationEntry.getFourthDate() < 1) {
				return true;
			}
		}
		else if (compensationUnit == 5){
			if (newServerCompensationEntry.getRemain_coin() >= newServerCompensationEntry.getFifthGiveCoin() && newServerCompensationEntry.getFifthDate() < 1) {
				return true;
			}
		}
		return false;
	}

	private double lcoinCountFormat(int coin) {

		return ((double) coin * 100) / 10000D;
	}

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private static void ShowChangeSuite(Player player)
	{
		String html = HtmCache.getInstance().getHtml("member/41001-6.htm", player);
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		StringBuilder content = new StringBuilder();
		String name;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT  item_name,deletetime FROM _character_suite where obj_Id = ? and deletetime > ?");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, System.currentTimeMillis() / 1000);
			rset = statement.executeQuery();
			content.append("<table border=0 cellspacing=0>");
			while (rset.next())
			{
				name = rset.getString("item_name");
				long t = rset.getInt("deletetime") * 1000L;
				int index = Correct(name);
				if(index == -1)
					continue;
				content.append("<tr>");
				content.append("<td align=center width=38 height=38><img src=\"");
				content.append(itemName[index][2]);
				content.append("\" width=32 height=32></td>");
				content.append("<td fixwidth=80><font color=\"F4F4F4\">");
				content.append(itemName[index][1]);
				content.append("</font></td>");
				content.append("<td fixwidth=100 align=center><font color=LEVEL>有效期至</font><font color=00ff00><br1>");
				content.append(dateFormat.format(t));
				content.append("</font></td>");
				content.append("<td><button value=\"使用\" action=\"");
				content.append("bypass -h npc?SetVisualSuite "+ name +" " + t);
				content.append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				content.append("</tr>");
			}
			content.append("</table>");
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		html = html.replace("<$content$>", content.toString());
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}

	private static int Correct(String name)
	{
		int num = -1;
		for(int i = 0; i < itemName.length; i++)
		{
			if(itemName[i][0].equals(name))
			{
				return i;
			}
		}
		return num;
	}

	private static void ShowChangeClassId(Player player)
	{
		String html = HtmCache.getInstance().getHtml("member/41001-3.htm", player);
		int money = GetLevelMoney(player);
		html = html.replace("<$money$>", String.valueOf(money));
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	private static int GetLevelMoney(Player player)
	{
//		1 -50级=每次150玫瑰币<br1>
//		51-60级=51级收260 玫瑰币之后依次每级+10 玫瑰币<br1>
//		61-70级=61级收365 玫瑰币之后依次每级+15 玫瑰币<br1>
//		71-80级=71级收540玫瑰币之后依次每级+20 玫瑰币<br1>
//		81-85级=81级收750玫瑰币之后依次每级+50玫瑰币<br>
		int money = 150;
		int level = player.getLevel();
		if(level <= 50)
			money = 150;
		else if(level <= 60)
			money = 260 + (level - 51) * 10;
		else if(level <= 70)
			money = 365 + (level - 61) * 15;
		else if(level <= 80)
			money = 540 + (level - 71) * 20;
		else if(level <= 85)
			money = 750 + (level - 81) * 50;
		else if(level <= 90)
			money = 1000 + (level - 86) * 75;
		else
			money = 9999;//这一个区域应该不存在写一个超大值(如果超过85级才会出现的区域)
		return money;
	}
	private static void ShowMyLevelCanChange(Player player)
	{
		String html = "";
		if(player.getClassLevel() == ClassLevel.NONE)
		{
			html = HtmCache.getInstance().getHtml("member/41001-NONE.htm", player); //这一个是出生的职业，应该要出现的讯息为不支持
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			return;
		}
		html = HtmCache.getInstance().getHtml("member/41001-ClassId.htm", player); //这一个是出生的职业，应该要出现的讯息为不支持
		ClassLevel playerClassLevel = player.getClassLevel();
		int classId = player.getActiveClassId();
		StringBuilder content = new StringBuilder();
		//<Button ALIGN=LEFT ICON="NORMAL" action="bypass -h npc?ShowMyLevelCanChange">确认转换职业 (需求 <$money$> 玫瑰币)</Button><br>
		for(ClassId _class : ClassId.values())
		{
			if(_class.getClassLevel() == playerClassLevel)
			{
				if(_class.getId() != classId)
					content.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?ChangeMyClassId " + _class.getId() + "\">確認轉換職業 " + _class.getName(player) + "</Button><br>");
			}
		}
		html = html.replace("<$content$>", content.toString());

		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	public static void showChangeClassLog(Player player,int index)
	{
		int pageLimite = 16;//一頁二筆記錄  如要改成 20筆 這裡改就行了
		String number = pageLimite *index  + "," + pageLimite;
		String html = HtmCache.getInstance().getHtml("member/41001-ChangeLog.htm", player); //这一个是出生的职业，应该要出现的讯息为不支持
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		StringBuilder content = new StringBuilder();
		int Allpages =0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT count(*) as ct FROM _change_class");
			rset = statement.executeQuery();
			if (rset.next())
			{
				Allpages = rset.getInt("ct");
			}
			else
			{
				html = html.replace("<$content$>", "");
				html = html.replace("<$pages$>", "");
				HtmlMessage msg = new HtmlMessage(5);
				msg.setHtml(html);
				player.sendPacket(msg);
				player.sendActionFailed();
				statement.close();
				return;
			}

			statement.close();



			statement = con.prepareStatement("SELECT char_name,oldClassId,newClassId,changeTime FROM _change_class ORDER BY changeTime desc LIMIT "+ number);
			rset = statement.executeQuery();
			content.append("<table>");
			boolean first = true;
			while (rset.next())
			{
				if(first)
				{
					content.append("<tr><td align=center width=86><font color=33ff00>玩家</font></td><td align=center width=86><font color=33ff00>原職業</font></td><td align=center width=86><font color=33ff00>現職業</font></td></tr>");
					first=false;
				}
				content.append("<tr>");
				content.append("<td align=center>" + rset.getString("char_name") + "</td>");
				content.append("<td align=center>" + ClassIdToName(player, rset.getInt("oldClassId")) + "</td>");
				content.append("<td align=center>" + ClassIdToName(player, rset.getInt("newClassId")) + "</td>");
				/*时间记录不需要，位置不够long t = rset.getInt("changeTime") * 1000L;
				content.append("<td>" + dateFormat.format(t) + "</td>");*/
				content.append("</tr>");
			}
			content.append("</table>");
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		int minPage = 1;
		int maxPage = (int) Math.ceil((double) Allpages / pageLimite);
		int currentPage = Math.max(Math.min(maxPage, index+1), minPage);
		StringBuilder pageContent = new StringBuilder();
		pageContent.append("<center><table><tr>");
		for(int i = minPage; i <= maxPage; i++)
		{
			if(i == currentPage)
			{
				pageContent.append("<td><font color=FFFFFF>"+ i + "<font></td>");
			}
			else
			{
				//<font color=FF00FF><a action="bypass _bbsmoney:ShowMainhtml">xxx</a></font>
				pageContent.append("<td><font color=FF00FF><a action=\"bypass -h npc?showChangeClassLog " + (i - 1) + "\" >" + i + "</a></font></td>");
			}
			//<font color=FF00FF><button value=\"本人\""
		}
		pageContent.append("</tr></table></center>");

		html = html.replace("<$content$>", content.toString());
		html = html.replace("<$pages$>", pageContent.toString());
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	public static String ClassIdToName(Player player, int Id)
	{
		return new CustomMessage("l2s.gameserver.model.base.ClassId.name." + Id).toString(player);
	}
	public static void UpdateChangeClassId(Player player, int oldId, int newId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("INSERT INTO _change_class (char_name, oldClassId ,newClassId ,changeTime) VALUES (?,?,?,?)");
			statement.setString(1, player.getName());
			statement.setInt(2, oldId);
			statement.setInt(3, newId);
			statement.setInt(4, (int) (System.currentTimeMillis() / 1000));
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	private static int[] GetLearnedSkillList(Player player)//TODO
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		List<Integer> values = new ArrayList<Integer>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id from _skill_collect where obj_id =?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while (rset.next())
			{
				values.add(rset.getInt("skill_id"));
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		//int[] array = list.stream().mapToInt(i->i).toArray();
		if(values.size() > 0)
		{
			return values.stream().mapToInt(i -> i).toArray();
		}
		return new int[0];
	}
	private static void ResetSuite(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE _character_suite SET use_item=0 WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	private static void UpdateSuite(Player player ,String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE _character_suite SET use_item=0 WHERE obj_Id=? and deletetime > ?");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, (System.currentTimeMillis() / 1000));
			statement.execute();
			statement.close();

			statement = con.prepareStatement("UPDATE _character_suite SET use_item=1 WHERE obj_Id=? and deletetime > ? and item_name = ?");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, (System.currentTimeMillis() / 1000));
			statement.setString(3, name);
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	private static boolean CheckBuy(Player player, String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean buy = false;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("Select * from _character_suite where obj_Id= ? and deletetime > ? and item_name = ?");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, (System.currentTimeMillis() / 1000));
			statement.setString(3, name);
			rset = statement.executeQuery();
			if(rset.next())
			{
				buy = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return buy;
	}
	private static boolean InsertIntoCharacterSuite(Player player, String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		long times = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("INSERT INTO _character_suite (obj_Id, item_name ,deletetime) VALUES (?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, name);
			statement.setInt(3, (int) (times / 1000));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	private static boolean CheckItemBuyWeapon(int player_id,  int visualId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean buy = false;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("Select deletetime from  _character_weapon where player_id =? and visual_id =? ");
			statement.setInt(1, player_id);
			statement.setInt(2, visualId);
			rset = statement.executeQuery();
			if (rset.next())
			{
				if (rset.getInt("deletetime") >  (System.currentTimeMillis() / 1000))
				{
					buy = true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return buy;
	}
	private static boolean InsertIntoCharacterWeapon(int player_id, int objId, int visualId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		long times = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("REPLACE INTO _character_weapon (player_id, obj_id, visual_id ,deletetime) VALUES (?,?,?,?)");
			statement.setInt(1, player_id);
			statement.setInt(2, objId);
			statement.setInt(3, visualId);
			statement.setInt(4, (int) (times / 1000));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	private static class RemoveWearItemsTask implements Runnable
	{
		private Player _activeChar;
		private ItemInstance _weaponInstance;
		public RemoveWearItemsTask(Player activeChar,ItemInstance weaponInstance)
		{
			_activeChar = activeChar;
			_weaponInstance = weaponInstance;
		}

		public void run()
		{
			if(!_activeChar.isOnline())
			{
				return;
			}
			_activeChar.sendPacket(SystemMsg.YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT_);
			//_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar));

			if(_weaponInstance != null)
			{
				_activeChar.getInventory().equipItem(_weaponInstance);
				//_activeChar.getv.getActiveWeaponInstance().setVisualId(_visulaId);

			}
			//_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar, Inventory.PAPERDOLL_LRHAND));
			//.getInventory().sendEquipInfo(Inventory.PAPERDOLL_RHAND);//更新右手
			//player.getInventory().sendEquipInfo(Inventory.PAPERDOLL_LHAND);//更新左手
			//_activeChar.broadcastUserInfo(true);

			_activeChar.broadcastUserInfo(true);
			//getActor().sendPacket(new ExUserInfoEquipSlot(getActor(), slot));
			_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar));
			//_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar, Inventory.PAPERDOLL_RHAND));
			//_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar, Inventory.PAPERDOLL_LHAND));
			//_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar, Inventory.PAPERDOLL_LRHAND));

			//_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar, Inventory.PAPERDOLL_CHEST));

			//public static final int PAPERDOLL_LEGS = 11;
			//public static final int PAPERDOLL_FEET = 12;
			//public static final int PAPERDOLL_BACK = 13;


			_activeChar._testWearWeapon = false;
			//_activeChar.getInventory().sendEquipInfo(Inventory.PAPERDOLL_CHEST);//更新衣服
			//_activeChar.getInventory().sendEquipInfo(Inventory.PAPERDOLL_RHAND);//更新右手
			//_activeChar.getInventory().sendEquipInfo(Inventory.PAPERDOLL_LHAND);//更新左手
			//_activeChar.getInventory().sendEquipInfo(Inventory.PAPERDOLL_LRHAND);
		}
	}
	private static void ShowChangeWeapen(Player player)
	{
		String html = HtmCache.getInstance().getHtml("member/41001-11.htm", player);
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		StringBuilder content = new StringBuilder();
		int obj_id=0;
		int visual_id =0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT visual_id ,obj_id ,deletetime FROM _character_weapon where player_id = ? and deletetime > ?");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, System.currentTimeMillis() / 1000);
			rset = statement.executeQuery();
			content.append("<table border=0 cellspacing=0>");
			while (rset.next())
			{
				ItemTemplate item = ItemHolder.getInstance().getTemplate(rset.getInt("visual_id"));
				if(item == null)
					continue;
				long t = rset.getInt("deletetime") * 1000L;
				obj_id = rset.getInt("obj_id");
				visual_id = rset.getInt("visual_id");
				content.append("<tr>");
				content.append("<td align=center width=38 height=38><img src=\"");
				content.append(item.getIcon());
				content.append("\" width=32 height=32></td>");
				content.append("<td fixwidth=80><font color=\"F4F4F4\">");
				content.append(item.getName());
				content.append("</font></td>");
				content.append("<td fixwidth=100 align=center><font color=LEVEL>有效期至</font><font color=00ff00><br1>");
				content.append(dateFormat.format(t));
				content.append("</font></td>");
				if (obj_id > 0)//這裡需要出現移除
				{
					content.append("<td><font color=\"FF0000\"><button value=\"移除\" action=\"");
					content.append("bypass -h npc?resetWeapenAppearance " + obj_id + " " + visual_id);
					content.append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></font></td>");
				}
				else
				{
					content.append("<td><button value=\"使用\" action=\"");
					content.append("bypass -h npc?UseWeapenAppearance " + visual_id + " " + t);
					content.append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				}
				content.append("</tr>");
			}
			content.append("</table>");
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		html = html.replace("<$content$>", content.toString());
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	public static void UpdateItemVisualId( int player_id, int objId, int VisualId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("update _character_weapon set obj_id = ? WHERE player_id = ? and visual_id =? ");
			statement.setInt(1, objId);
			statement.setInt(2, player_id);
			statement.setInt(3, VisualId);
			statement.execute();
//			rset = statement.executeQuery();
//			int owner_id = 0;
//			if (rset.next())//這一區是假設物品id仍存在的情況下，
//			{
//				owner_id = rset.getInt("owner_id");//
//				Player player = GameObjectsStorage.getPlayer(owner_id);
//				if (player != null)//如果在某人的身上情況下執行清除
//				{
//					Announcements.announceToAll("物品在誰身上:" + player.getName());//不可上伺服器
//					ItemInstance item = player.getInventory().getItemByObjectId(268466462);
//					if (item != null)
//					{
//						item.setVisualId(0);
//						player.getInventory().sendModifyItem(item);//這一個是會更新畫面圖示武器ICON
//						player.getInventory().sendEquipInfo(Inventory.PAPERDOLL_RHAND);//更新右手 人物顯示武器
//						player.getInventory().sendEquipInfo(Inventory.PAPERDOLL_LHAND);//更新左手 人物顯示武器
//						if (item == player.getActiveWeaponInstance())//假設物品裝在身上情況下
//						{
//							player.getInventory().refreshEquip(item);
//							Announcements.announceToAll("裝備武器狀態更新:" + player.getName());//不可上伺服器
//						}
//						else
//						{
//							Announcements.announceToAll("應該是丟在背包:" + player.getName());//不可上伺服器
//							player.getInventory().refreshEquip(); // .refreshEquip(item);
//						}
//					}
//				}
//				else {
//					Announcements.announceToAll("不在某人身上或許是下線，這樣子直接改資料庫:"  );//不可上伺服器
//					statement.close();
//					statement = con.prepareStatement("update `items` set visual_id = 0 where `object_id` = 268466462 ");
//					statement.execute();
//				}
//			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private static void showPage(Player player, String page)
	{
		String html = HtmCache.getInstance().getHtml("member/" + page, player);
		String value = getClanBuff(player);
		//String tmp = getHtmlByBuff(buypassOptions[2], player);
		if(value.length() > 0)
		{
			html = html.replace("<$content$>", value);
		}
		else
		{
			html = html.replace("<$content$>", "目前没有东西");
		}
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();	
	}
	private static String doubleToString(double value)
	{
		int intValue = (int) value;
		if(intValue == value)
			return String.valueOf(intValue);
		return String.valueOf(value);
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
		switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_3;
		}
		return 0;
	}

	private static long getFeeItemCountForChangeClass(Player player)
	{
		switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_3;
		}
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
	
	protected boolean checkUseCondition(Player player)
	{
		if(player.getVar("jailed") != null)	// Если в тюрьме
			return false;

		if(player.isInTrainingCamp())
			return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_DEAD)	// Если мертв, или притворяется мертвым
			if(player.isAlikeDead())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_A_BATTLE)	// В состоянии битвы
			if(player.isCastingNow() || player.isInCombat() || player.isAttackingNow())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_PVP)	// В PvP
			if(player.getPvpFlag() > 0)
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_INVISIBLE)
			if(player.isInvisible(null))
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_OLLYMPIAD)	// На олимпиаде
			if(player.isInOlympiadMode() || player.isInArenaObserverMode())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_FLIGHT)	// В состоянии полета
			if(player.isFlying() || player.isInFlyingTransform())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_IN_VEHICLE)	// На корабле
			if(player.isInBoat())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_MOUNTED)	// На ездовом животном
			if(player.isMounted())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_CANNOT_MOVE)	// В состоянии обизвдижения
			if(player.isMovementDisabled())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IN_TRADE)	// В состоянии торговли
			if(player.isInStoreMode() || player.isInTrade() || player.isInOfflineMode())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_TELEPORTING)	// Во время телепортации
			if(player.isLogoutStarted() || player.isTeleporting())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_DUEL)	// На дуели
			if(player.isInDuel())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IS_PK)	// Когда PK
			if(player.isPK())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_CLAN_LEADERS_ONLY)	// Если клан лидер
			if(!player.isClanLeader())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_SIEGE)	// На осаждаемой территории
			if(player.isInSiegeZone())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_IN_PEACE_ZONE_ONLY)	// В мирной зоне
			if(!player.isInPeaceZone())
				return false;

		return true;
	}
	protected void onWrongCondition(Player player)
	{
		player.sendMessage(player.isLangRus() ? "您目前的立場不允許您使用此操作。" : "您目前的立场不允许您使用此操作");
		player.sendPacket(ShowBoardPacket.CLOSE);
	}
	private void sendHtmlMessage(Player player,String html)
	{
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();	
	}
	//23小時52分11秒
	static	String buttontimes ="<tr><td align=\"center\" width=265><table width=265><tr><td><img src=\"icon.bm_nebit_box\" width=32 height=32></td><td fixwidth=\"150\" >\r\n" + 
				"小型联盟礼物 <font color=\"8474E2\">($name$)</font><br1><font color=\"LEVEL\">剩余领取时间:$time$</font></td><td fixwidth=\"30\" ><button value=\"领取\" action=\"$action$\" width=66 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>\r\n" + 
				"</tr></table></td></tr>";
		
	private static String getClanBuff(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		String value = "";
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT  deletetime,name  FROM _player_give_clan_buff where obj_Id = ? and deletetime > ?");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, System.currentTimeMillis() / 1000);
			rset = statement.executeQuery();
			while (rset.next())
			{
				int t = rset.getInt("deletetime");
				String name = rset.getString("name");
				String cmd = "bypass -h npc?GiveBuffToPlayer " + name + " " + t;
				int between = t - (int) (System.currentTimeMillis() / 1000);
				int hour1 = between / 3600;
				int minute1 = between % 3600 / 60;
				int second1 = between % 60;
				String joinstr = buttontimes.replace("$name$", name);
				joinstr = joinstr.replace("$time$", hour1 + "小时" + minute1 + "分" + second1 + "秒");
				joinstr = joinstr.replace("$action$", cmd);
				value += joinstr;
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return value;
	}
	//player,name,times
	private static boolean checkBuffAndGive(Player player, String name , int times)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean giveBuff = true;
		BuffSkill buff ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM _player_give_clan_buff where obj_Id = ? and name = ? and deletetime=? ");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, name);
			statement.setLong(3,times);
			rset = statement.executeQuery();
			if(rset.next())
			{
				int skill_id = rset.getInt("skill_id");
				int skill_level = rset.getInt("skill_level");
				buff = BuffSkill.makeBuffSkill(skill_id, skill_level, 1, -1, false);
				player.callSkill(player, SkillEntry.makeSkillEntry(SkillEntryType.NONE, buff.getSkill()), new HashSet<Creature>(Arrays.asList(player)), false, false);
				statement.close();
				statement = con.prepareStatement("DELETE FROM _player_give_clan_buff where obj_Id = ? and name = ? and deletetime=? ");
				statement.setInt(1, player.getObjectId());
				statement.setString(2, name);
				statement.setLong(3, times);
				statement.execute();
			}
			else
			{
				giveBuff = false;
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if(!giveBuff)
		{
			return false;
		}
		return true;
	}
	private static int CheckMemberHaveConis(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int Counts = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT sum(point) as pt FROM _game_acc where account = ?");
			statement.setString(1, player.getAccountName());
			rset = statement.executeQuery();
			if (rset.next())
			{
				Counts = rset.getInt("pt");
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return Counts;
	}
	private static boolean UpdateMemberConis(Player player,int Counts)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("INSERT INTO _game_acc (point, account ,createtime) VALUES (?,?, unix_timestamp(now()))");
			statement.setInt(1, Counts);
			statement.setString(2, player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMdd");
	//activeChar.sendMessage("申訴成功:當前時間為「 " + Todays + "」 請記得抓圖給GM解封。");
	private static boolean getToday(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean go=false;
		try
		{
			int Todays = Integer.parseInt(dateFmt.format(new Date()));
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM _give_every_day_item where account = ? and createtime = ?");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, Todays);
			rset = statement.executeQuery();
			if(rset.next())
			{
				go = false;//今日已取
			}
			else
			{
				go = true;//今日沒取
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return go;
	}

	private void updateGiveOk(Player player)
	{

		Connection con = null;
		PreparedStatement statement = null;
		int Todays = Integer.parseInt(dateFmt.format(new Date()));
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO _give_every_day_item (account ,createtime) VALUES (?,?)");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, Todays);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

	}
	
	
}
