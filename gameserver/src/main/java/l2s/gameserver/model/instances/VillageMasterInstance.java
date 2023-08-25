package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowUpgradeSystem;
import l2s.gameserver.network.l2.s2c.ExShowUpgradeSystemNormal;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.SiegeUtils;

public final class VillageMasterInstance extends NpcInstance
{
	private static enum MasterType
	{
		NONE,
		HUMAN_1ST_CLASS_MAGE,
		HUMAN_1ST_CLASS_FIGHTER,
		ELF_1ST_CLASS,
		HUMAN_ELF_1ST_CLASS_MAGE,
		HUMAN_ELF_1ST_CLASS_FIGHTER,
		DARKELF_1ST_CLASS,
		ORC_1ST_CLASS,
		DWARVEN_1ST_CLASS,
		HUMAN_ELF_2ND_CLASS_MYSTIC,
		HUMAN_ELF_2ND_CLASS_PRIEST,
		HUMAN_ELF_2ND_CLASS_FIGHTER,
		DARKELF_2ND_CLASS,
		ORC_2ND_CLASS,
		DWARVEN_2ND_CLASS_WAREHOUSE,
		DWARVEN_2ND_CLASS_BLACKSMITH;
	}

	private final MasterType _type;

	public VillageMasterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		_type = MasterType.valueOf(getParameter("master_type", "NONE").toUpperCase());
	}

	@Override
	public String correctBypassLink(Player player, String link)
	{
		String path = "villagemaster/occupation/" + link;
		if(HtmCache.getInstance().getIfExists(path, player) != null)
			return path;
		path = "villagemaster/subclass/" + link;
		if(HtmCache.getInstance().getIfExists(path, player) != null)
			return path;
		return super.correctBypassLink(player, link);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		if(command.equals("manage_clan"))
		{
			showChatWindow(player, "pledge/pl001.htm", false);
		}
		else if(command.equals("manage_alliance"))
		{
			showChatWindow(player, "pledge/al001.htm", false);
		}
		else if(command.equals("create_clan_check"))
		{
			if(player.getLevel() <= 9)
				showChatWindow(player, "pledge/pl002.htm", false);
			else if(player.isClanLeader())
				showChatWindow(player, "pledge/pl003.htm", false);
			else if(player.getClan() != null)
				showChatWindow(player, "pledge/pl004.htm", false);
			else
				showChatWindow(player, "pledge/pl005.htm", false);
		}
		else if(command.equals("lvlup_clan_check"))
		{
			if(!player.isClanLeader())
			{
				showChatWindow(player, "pledge/pl014.htm", false);
				return;
			}
			showChatWindow(player, "pledge/pl013.htm", false);
		}
		else if(command.equals("disband_clan_check"))
		{
			if(!player.isClanLeader())
			{
				showChatWindow(player, "pledge/pl_err_master.htm", false);
				return;
			}
			showChatWindow(player, "pledge/pl007.htm", false);
		}
		else if(command.equals("restore_clan_check"))
		{
			if(!player.isClanLeader())
			{
				showChatWindow(player, "pledge/pl011.htm", false);
				return;
			}
			showChatWindow(player, "pledge/pl010.htm", false);
		}
		else if(command.equals("change_leader_check"))
		{
			showChatWindow(player, "pledge/pl_master.htm", false);
		}
		else if(command.startsWith("request_change_leader_check"))
		{
			if(!player.isClanLeader())
			{
				showChatWindow(player, "pledge/pl_err_master.htm", false);
				return;
			}
			showChatWindow(player, "pledge/pl_transfer_master.htm", false);
		}
		else if(command.startsWith("cancel_change_leader_check"))
		{
			if(!player.isClanLeader())
			{
				showChatWindow(player, "pledge/pl_err_master.htm", false);
				return;
			}
			showChatWindow(player, "pledge/pl_cancel_master.htm", false);
		}
		else if(command.startsWith("create_clan"))
		{
			if(command.length() > 12)
			{
				String val = command.substring(12);
				VillageMasterPledgeBypasses.createClan(this, player, val);
			}
		}
		else if(command.startsWith("change_leader"))
		{
			StringTokenizer tokenizer = new StringTokenizer(command);
			if(tokenizer.countTokens() != 3)
				return;

			tokenizer.nextToken();

			VillageMasterPledgeBypasses.changeLeader(this, player, Integer.parseInt(tokenizer.nextToken()), tokenizer.nextToken());
		}
		else if(command.startsWith("cancel_change_leader"))
			VillageMasterPledgeBypasses.cancelLeaderChange(this, player);
		else if(command.startsWith("check_create_ally"))
			showChatWindow(player, "pledge/al005.htm", false);
		else if(command.startsWith("create_ally"))
		{
			if(command.length() > 12)
			{
				String val = command.substring(12);
				if(VillageMasterPledgeBypasses.createAlly(player, val))
					showChatWindow(player, "pledge/al006.htm", false);
			}
		}
		else if(command.startsWith("dissolve_clan"))
			VillageMasterPledgeBypasses.dissolveClan(this, player);
		else if(command.startsWith("restore_clan"))
			VillageMasterPledgeBypasses.restoreClan(this, player);
		else if(command.startsWith("learn_clan_skills"))
			VillageMasterPledgeBypasses.showClanSkillList(this, player);
		else if(cmd.equalsIgnoreCase("changeclass"))
		{
			int classListId = getClassListId(player);

			String html = null;
			if(player.getClassId().isOfLevel(ClassLevel.THIRD))
				html = getParameter("fnYouAreFourthClass", "master_lv3_hef005.htm");
			else if(classListId == -1)
				html = getParameter("fnYouAreFirstClass", null);
			else if(classListId == 0)
				html = getParameter("fnClassMismatch", null);
			else
				html = getParameter("fnClassList" + classListId, null);

			if(html != null)
				showChatWindow(player, "villagemaster/occupation/" + html, false);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -2)
		{
			if(!Config.ENABLE_SUB_CLASSES)
			{
				super.onMenuSelect(player, ask, reply, state);
				return;
			}

			final String dialogSuffix = getSubClassDialogSuffix();
			if(dialogSuffix == null)
			{
				super.onMenuSelect(player, ask, reply, state);
				return;
			}

			if(player.containsEvent(SingleMatchEvent.class)) // Не позволяем во время PvP ивентов менять саб-класс.
				return;

			if(reply == 5)	// Меню 'Саб Класс'
			{
				if(!player.isTransformed())
					showChatWindow(player, "villagemaster/subclass/subclass_01.htm", false);
				else
					showChatWindow(player, "villagemaster/subclass/subclass_06.htm", false);
			}
			else if(reply == 10)	// Добавить подкласс
			{
				if(player.hasServitor())
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_02.htm", false);
				else
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_01.htm", false);
			}
			else if(reply == 11)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 1, 0);
			}
			else if(reply == 12)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 2, 0);
			}
			else if(reply == 13)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 3, 0);
			}
			else if(reply == 14)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 4, 0);
			}
			else if(reply == 15)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 5, 0);
			}
			else if(reply == 16)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 6, 0);
			}
			else if(reply == 17)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 7, 0);
			}
			else if(reply == 18)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 8, 0);
			}
			else if(reply == 19)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 9, 0);
			}
			else if(reply == 20)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 10, 0);
			}
			else if(reply == 30)	// Изменить подкласс
			{
				if(player.hasServitor())
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_02.htm", false);
				else if(player.isTransformed())
					showChatWindow(player, "villagemaster/subclass/subclass_06.htm", false);
				else
					VillageMasterSubClassBypasses.getSubJobList(this, player, 11, 0);
			}
			else if(reply == 40)	// Отменить текущий подкласс и активировать новый
			{
				if(player.getLevel() < 40)
				{
					showChatWindow(player, "villagemaster/subclass/master_lv3_hef_19.htm", false);
					return;
				}

				if(player.hasServitor())
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_02.htm", false);
				else
					VillageMasterSubClassBypasses.getSubJobList(this, player, 20, 0);
			}
			else if(reply == 41)
			{
				if(player.hasServitor())
				{
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_02.htm", false);
				}
				else if(state == 1)
				{
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_01b.htm", false);
				}
				else if(state == 2)
				{
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_01c.htm", false);
				}
				else if(state == 3)
				{
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_01d.htm", false);
				}
			}
			else if(reply == 42)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 1, state);
			}
			else if(reply == 43)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 2, state);
			}
			else if(reply == 44)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 3, state);
			}
			else if(reply == 45)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 4, state);
			}
			else if(reply == 46)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 5, state);
			}
			else if(reply == 47)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 6, state);
			}
			else if(reply == 48)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 7, state);
			}
			else if(reply == 49)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 8, state);
			}
			else if(reply == 50)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 9, state);
			}
			else if(reply == 51)
			{
				VillageMasterSubClassBypasses.getSubJobList(this, player, 10, state);
			}
			else if(reply >= 324 && reply <= 410)
			{
				int id = (int) (reply - (reply >= 380 ? 292 : 322));
				ClassId classId = ClassId.valueOf(id);
				if(!isMyApprentice(classId.getId()))
					return;

				if(player.getClassId() == classId)
				{
					showChatWindow(player, "villagemaster/subclass/master_lv3_" + dialogSuffix + "_16.htm", false);
					return;
				}

				if(!player.isDead())
				{
					if(!player.isQuestContinuationPossible(true))
						return;

					player.setActiveSubClass(classId.getId(), true, false);
				}
			}
			else
				super.onMenuSelect(player, ask, reply, state);
		}
		else if(ask == -2418)
		{
			if(reply == 1)
			{
				player.sendPacket(new ExShowUpgradeSystem(1));
			}
			else if(reply == 2)
			{
				player.sendPacket(new ExShowUpgradeSystemNormal(1));
			}
			else if(reply == 3)
			{
				player.sendPacket(new ExShowUpgradeSystemNormal(2));
			}
			else if(reply == 4)
			{
				showChatWindow(player, "villagemaster/upgrade_system001.htm", false);
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "villagemaster/";
	}

	@Override
	public void onChangeClassBypass(Player player, int classId)
	{
		if(!player.isQuestContinuationPossible(true))
			return;

		String html = null;

		ClassId newClassId = ClassId.valueOf(classId);
		if(player.getClassId().isOfLevel(ClassLevel.FIRST) && newClassId.isOfLevel(ClassLevel.FIRST))
			html = getParameter("fnYouAreSecondClass", null);

		if(player.getClassId().isOfLevel(ClassLevel.SECOND) && (newClassId.isOfLevel(ClassLevel.FIRST) || newClassId.isOfLevel(ClassLevel.SECOND)))
			html = getParameter("fnYouAreThirdClass", null);

		if(player.getClassId().isOfLevel(ClassLevel.THIRD))
			html = getParameter("fnYouAreSecondClass", null);

		if(html != null)
		{
			showChatWindow(player, "villagemaster/occupation/" + html, false);
			return;
		}

		if(!newClassId.childOf(player.getClassId()))
			return;

		if((newClassId.getClassLevel().ordinal() - player.getClassId().getClassLevel().ordinal()) != 1)
			return;

		boolean noHaveItems = false;
		for(int itemId : newClassId.getChangeClassItemIds())
		{
			if(ItemFunctions.getItemCount(player, itemId) == 0)
			{
				noHaveItems = true;
				break;
			}
		}

		int classListId = getClassListId(player);
		int classIdInList = 0;
		switch(newClassId)
		{
			case WARRIOR:
			case WIZARD:
			case ELVEN_KNIGHT:
			case ELVEN_WIZARD:
			case PALUS_KNIGHT:
			case DARK_WIZARD:
			case ORC_RAIDER:
			case ORC_SHAMAN:
			case SCAVENGER:
			case ARTISAN:
			case GLADIATOR:
			case PALADIN:
			case TREASURE_HUNTER:
			case SORCERER:
			case BISHOP:
			case TEMPLE_KNIGHT:
			case PLAIN_WALKER:
			case SPELLSINGER:
			case ELDER:
			case SHILLEN_KNIGHT:
			case ABYSS_WALKER:
			case SPELLHOWLER:
			case SHILLEN_ELDER:
			case DESTROYER:
			case TYRANT:
			case OVERLORD:
			case BOUNTY_HUNTER:
			case WARSMITH:
			{
				classIdInList = 1;
				break;
			}
			case KNIGHT:
			case CLERIC:
			case ELVEN_SCOUT:
			case ORACLE:
			case ASSASIN:
			case SHILLEN_ORACLE:
			case ORC_MONK:
			case WARLORD:
			case DARK_AVENGER:
			case HAWKEYE:
			case NECROMANCER:
			case PROPHET:
			case SWORDSINGER:
			case SILVER_RANGER:
			case ELEMENTAL_SUMMONER:
			case BLADEDANCER:
			case PHANTOM_RANGER:
			case PHANTOM_SUMMONER:
			case WARCRYER:
			{
				classIdInList = 2;
				break;
			}
			case ROGUE:
			case WARLOCK:
			{
				classIdInList = 3;
				break;
			}
		}

		if(player.getClassId().getClassMinLevel(true) > player.getLevel())
		{
			if(noHaveItems)
				html = getParameter("fnLowLevelNoProof" + classListId + classIdInList, null);
			else
				html = getParameter("fnLowLevel" + classListId + classIdInList, null);
		}
		else if(noHaveItems)
		{
			html = getParameter("fnNoProof" + classListId + classIdInList, null);
		}
		else
		{
			player.setClassId(newClassId.getId(), false);
			player.broadcastUserInfo(true);
			broadcastPacket(new MagicSkillUse(this, player, 5103, 1, 1000, 0));
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER);

			for(int itemId : newClassId.getChangeClassItemIds())
				ItemFunctions.deleteItem(player, itemId, 1, true);

			html = getParameter("fnAfterClassChange" + classListId + classIdInList, null);
		}

		if(html != null)
			showChatWindow(player, "villagemaster/occupation/" + html, false);
	}

	private int getClassListId(Player player)
	{
		int classListId = 0;
		switch(_type)
		{
			case HUMAN_1ST_CLASS_MAGE:
			{
				if(player.isMageClass() && player.getRace() == Race.HUMAN)
					classListId = 1;
				break;
			}
			case HUMAN_1ST_CLASS_FIGHTER:
			{
				if(!player.isMageClass() && player.getRace() == Race.HUMAN)
					classListId = 1;
				break;
			}
			case ELF_1ST_CLASS:
			{
				if(player.getRace() == Race.ELF)
				{
					if(!player.isMageClass())
						classListId = 1;
					else
						classListId = 2;
				}
				break;
			}
			case HUMAN_ELF_1ST_CLASS_MAGE:
			{
				if(player.isMageClass())
				{
					if(player.getRace() == Race.HUMAN)
						classListId = 1;
					else if(player.getRace() == Race.ELF)
						classListId = 2;
				}
				break;
			}
			case HUMAN_ELF_1ST_CLASS_FIGHTER:
			{
				if(!player.isMageClass())
				{
					if(player.getRace() == Race.HUMAN)
						classListId = 1;
					else if(player.getRace() == Race.ELF)
						classListId = 2;
				}
				break;
			}
			case DARKELF_1ST_CLASS:
			{
				if(player.getRace() == Race.DARKELF)
				{
					if(!player.isMageClass())
						classListId = 1;
					else
						classListId = 2;
				}
				break;
			}
			case ORC_1ST_CLASS:
			{
				if(player.getRace() == Race.ORC)
				{
					if(!player.isMageClass())
						classListId = 1;
					else
						classListId = 2;
				}
				break;
			}
			case DWARVEN_1ST_CLASS:
			{
				if(player.getRace() == Race.DWARF)
					classListId = 1;
				break;
			}
			case HUMAN_ELF_2ND_CLASS_MYSTIC:
			{
				if(player.isMageClass() && (player.getRace() == Race.HUMAN || player.getRace() == Race.ELF))
				{
					switch(player.getClassId())
					{
						case WIZARD:
						case SORCERER:
						case NECROMANCER:
						case WARLOCK:
						{
							classListId = 1;
							break;
						}
						case ELVEN_WIZARD:
						case SPELLSINGER:
						case ELEMENTAL_SUMMONER:
						{
							classListId = 2;
							break;
						}
						default:
						{
							classListId = -1;
							break;
						}
					}
				}
				break;
			}
			case HUMAN_ELF_2ND_CLASS_PRIEST:
			{
				if(player.isMageClass() && (player.getRace() == Race.HUMAN || player.getRace() == Race.ELF))
				{
					switch(player.getClassId())
					{
						case CLERIC:
						case BISHOP:
						case PROPHET:
						{
							classListId = 1;
							break;
						}
						case ORACLE:
						case ELDER:
						{
							classListId = 2;
							break;
						}
						default:
						{
							classListId = -1;
							break;
						}
					}
				}
				break;
			}
			case HUMAN_ELF_2ND_CLASS_FIGHTER:
			{
				if(!player.isMageClass() && (player.getRace() == Race.HUMAN || player.getRace() == Race.ELF))
				{
					switch(player.getClassId())
					{
						case WARRIOR:
						case GLADIATOR:
						case WARLORD:
						{
							classListId = 1;
							break;
						}
						case KNIGHT:
						case PALADIN:
						case DARK_AVENGER:
						{
							classListId = 2;
							break;
						}
						case ROGUE:
						case TREASURE_HUNTER:
						case HAWKEYE:
						{
							classListId = 3;
							break;
						}
						case ELVEN_KNIGHT:
						case TEMPLE_KNIGHT:
						case SWORDSINGER:
						{
							classListId = 4;
							break;
						}
						case ELVEN_SCOUT:
						case PLAIN_WALKER:
						case SILVER_RANGER:
						{
							classListId = 5;
							break;
						}
						default:
						{
							classListId = -1;
							break;
						}
					}
				}
				break;
			}
			case DARKELF_2ND_CLASS:
			{
				if(player.getRace() == Race.DARKELF)
				{
					switch(player.getClassId())
					{
						case PALUS_KNIGHT:
						case SHILLEN_KNIGHT:
						case BLADEDANCER:
						{
							classListId = 1;
							break;
						}
						case ASSASIN:
						case ABYSS_WALKER:
						case PHANTOM_RANGER:
						{
							classListId = 2;
							break;
						}
						case DARK_WIZARD:
						case SPELLHOWLER:
						case PHANTOM_SUMMONER:
						{
							classListId = 3;
							break;
						}
						case SHILLEN_ORACLE:
						case SHILLEN_ELDER:
						{
							classListId = 4;
							break;
						}
						default:
						{
							classListId = -1;
							break;
						}
					}
				}
				break;
			}
			case ORC_2ND_CLASS:
			{
				if(player.getRace() == Race.ORC)
				{
					switch(player.getClassId())
					{
						case ORC_RAIDER:
						case DESTROYER:
						{
							classListId = 1;
							break;
						}
						case ORC_MONK:
						case TYRANT:
						{
							classListId = 2;
							break;
						}
						case ORC_SHAMAN:
						case OVERLORD:
						case WARCRYER:
						{
							classListId = 3;
							break;
						}
						default:
						{
							classListId = -1;
							break;
						}
					}
				}
				break;
			}
			case DWARVEN_2ND_CLASS_WAREHOUSE:
			{
				if(player.getRace() == Race.DWARF)
				{
					switch(player.getClassId())
					{
						case SCAVENGER:
						case BOUNTY_HUNTER:
						{
							classListId = 1;
							break;
						}
						case DWARVEN_FIGHTER:
						{
							classListId = -1;
							break;
						}
					}
				}
				break;
			}
			case DWARVEN_2ND_CLASS_BLACKSMITH:
			{
				if(player.getRace() == Race.DWARF)
				{
					switch(player.getClassId())
					{
						case ARTISAN:
						case WARSMITH:
						{
							classListId = 1;
							break;
						}
						case DWARVEN_FIGHTER:
						{
							classListId = -1;
							break;
						}
					}
				}
				break;
			}
		}
		return classListId;
	}

	public void setLeader(Player leader, String newLeader)
	{
		if(!leader.isClanLeader())
		{
			leader.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}

		if(leader.containsEvent(SiegeEvent.class))
		{
			leader.sendMessage(new CustomMessage("scripts.services.Rename.SiegeNow"));
			return;
		}

		Clan clan = leader.getClan();
		SubUnit mainUnit = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
		UnitMember member = mainUnit.getUnitMember(newLeader);

		if(member == null)
		{
			showChatWindow(leader, "pledge/pl_err_man.htm", false);
			return;
		}

		if(member.isLeaderOf() != Clan.SUBUNIT_NONE)
		{
			leader.sendMessage(new CustomMessage("l2s.gameserver.model.instances.L2VillageMasterInstance.CannotAssignUnitLeader"));
			return;
		}

		setLeader(leader, clan, mainUnit, member);
	}

	public static void setLeader(Player player, Clan clan, SubUnit unit, UnitMember newLeader)
	{
		player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.L2VillageMasterInstance.ClanLeaderWillBeChangedFromS1ToS2").addString(clan.getLeaderName()).addString(newLeader.getName()));

		if(clan.getLevel() >= SiegeUtils.MIN_CLAN_SIEGE_LEVEL)
		{
			if(clan.getLeader() != null)
			{
				Player oldLeaderPlayer = clan.getLeader().getPlayer();
				if(oldLeaderPlayer != null)
					SiegeUtils.removeSiegeSkills(oldLeaderPlayer);
			}
			Player newLeaderPlayer = newLeader.getPlayer();
			if(newLeaderPlayer != null)
				SiegeUtils.addSiegeSkills(newLeaderPlayer);
		}

		unit.setLeader(newLeader, true);

		clan.broadcastClanStatus(true, true, false);
	}

	public String getSubClassDialogSuffix()
	{
		String suffix = null;
		switch(_type)
		{
			case HUMAN_ELF_2ND_CLASS_MYSTIC:
				suffix = "hew";
				break;
			case HUMAN_ELF_2ND_CLASS_PRIEST:
				suffix = "hec";
				break;
			case HUMAN_ELF_2ND_CLASS_FIGHTER:
				suffix = "hef";
				break;
			case DARKELF_2ND_CLASS:
				suffix = "de";
				break;
			case ORC_2ND_CLASS:
				suffix = "orc";
				break;
			case DWARVEN_2ND_CLASS_WAREHOUSE:
				suffix = "ware";
				break;
			case DWARVEN_2ND_CLASS_BLACKSMITH:
				suffix = "black";
				break;
		}
		return suffix;
	}

	public ClassId getNewSubClassId(int ask)
	{
		switch(_type)
		{
			case HUMAN_ELF_2ND_CLASS_MYSTIC:
			{
				switch(ask)
				{
					case 1:
						return ClassId.SORCERER;
					case 2:
						return ClassId.NECROMANCER;
					case 3:
						return ClassId.WARLOCK;
					case 4:
						return ClassId.SPELLSINGER;
					case 5:
						return ClassId.ELEMENTAL_SUMMONER;
				}
				break;
			}
			case HUMAN_ELF_2ND_CLASS_PRIEST:
			{
				switch(ask)
				{
					case 1:
						return ClassId.BISHOP;
					case 2:
						return ClassId.PROPHET;
					case 3:
						return ClassId.ELDER;
				}
				break;
			}
			case HUMAN_ELF_2ND_CLASS_FIGHTER:
			{
				switch(ask)
				{
					case 1:
						return ClassId.GLADIATOR;
					case 2:
						return ClassId.WARLORD;
					case 3:
						return ClassId.PALADIN;
					case 4:
						return ClassId.DARK_AVENGER;
					case 5:
						return ClassId.TREASURE_HUNTER;
					case 6:
						return ClassId.HAWKEYE;
					case 7:
						return ClassId.TEMPLE_KNIGHT;
					case 8:
						return ClassId.SWORDSINGER;
					case 9:
						return ClassId.PLAIN_WALKER;
					case 10:
						return ClassId.SILVER_RANGER;
				}
				break;
			}
			case DARKELF_2ND_CLASS:
			{
				switch(ask)
				{
					case 1:
						return ClassId.SHILLEN_KNIGHT;
					case 2:
						return ClassId.BLADEDANCER;
					case 3:
						return ClassId.ABYSS_WALKER;
					case 4:
						return ClassId.PHANTOM_RANGER;
					case 5:
						return ClassId.SPELLHOWLER;
					case 6:
						return ClassId.PHANTOM_SUMMONER;
					case 7:
						return ClassId.SHILLEN_ELDER;
				}
				break;
			}
			case ORC_2ND_CLASS:
			{
				switch(ask)
				{
					case 1:
						return ClassId.DESTROYER;
					case 2:
						return ClassId.TYRANT;
					case 4:
						return ClassId.WARCRYER;
				}
				break;
			}
			case DWARVEN_2ND_CLASS_WAREHOUSE:
			{
				switch(ask)
				{
					case 1:
						return ClassId.BOUNTY_HUNTER;
				}
			}
		}
		return null;
	}

	public boolean isMyApprentice(int id)
	{
		if(id < 0)
			return false;

		ClassId classId = ClassId.valueOf(id);
		switch(_type)
		{
			case HUMAN_ELF_2ND_CLASS_MYSTIC:
			{
				if(classId == ClassId.SORCERER || classId == ClassId.ARCHMAGE)
					return true;
				if(classId == ClassId.NECROMANCER || classId == ClassId.SOULTAKER)
					return true;
				if(classId == ClassId.WARLOCK || classId == ClassId.ARCANA_LORD)
					return true;
				if(classId == ClassId.SPELLSINGER || classId == ClassId.MYSTIC_MUSE)
					return true;
				if(classId == ClassId.ELEMENTAL_SUMMONER || classId == ClassId.ELEMENTAL_MASTER)
					return true;
				return false;
			}
			case HUMAN_ELF_2ND_CLASS_PRIEST:
			{
				if(classId == ClassId.BISHOP || classId == ClassId.CARDINAL)
					return true;
				if(classId == ClassId.PROPHET || classId == ClassId.HIEROPHANT)
					return true;
				if(classId == ClassId.ELDER || classId == ClassId.EVAS_SAINT)
					return true;
				return false;
			}
			case HUMAN_ELF_2ND_CLASS_FIGHTER:
			{
				if(classId == ClassId.GLADIATOR || classId == ClassId.DUELIST)
					return true;
				if(classId == ClassId.WARLORD || classId == ClassId.DREADNOUGHT)
					return true;
				if(classId == ClassId.PALADIN || classId == ClassId.PHOENIX_KNIGHT)
					return true;
				if(classId == ClassId.DARK_AVENGER || classId == ClassId.HELL_KNIGHT)
					return true;
				if(classId == ClassId.TREASURE_HUNTER || classId == ClassId.ADVENTURER)
					return true;
				if(classId == ClassId.HAWKEYE || classId == ClassId.SAGITTARIUS)
					return true;
				if(classId == ClassId.TEMPLE_KNIGHT || classId == ClassId.EVAS_TEMPLAR)
					return true;
				if(classId == ClassId.SWORDSINGER || classId == ClassId.SWORD_MUSE)
					return true;
				if(classId == ClassId.PLAIN_WALKER || classId == ClassId.WIND_RIDER)
					return true;
				if(classId == ClassId.SILVER_RANGER || classId == ClassId.MOONLIGHT_SENTINEL)
					return true;
				return false;
			}
			case DARKELF_2ND_CLASS:
			{
				if(classId == ClassId.SHILLEN_KNIGHT || classId == ClassId.SHILLIEN_TEMPLAR)
					return true;
				if(classId == ClassId.BLADEDANCER || classId == ClassId.SPECTRAL_DANCER)
					return true;
				if(classId == ClassId.ABYSS_WALKER || classId == ClassId.GHOST_HUNTER)
					return true;
				if(classId == ClassId.PHANTOM_RANGER || classId == ClassId.GHOST_SENTINEL)
					return true;
				if(classId == ClassId.SPELLHOWLER || classId == ClassId.STORM_SCREAMER)
					return true;
				if(classId == ClassId.PHANTOM_SUMMONER || classId == ClassId.SPECTRAL_MASTER)
					return true;
				if(classId == ClassId.SHILLEN_ELDER || classId == ClassId.SHILLIEN_SAINT)
					return true;
				return false;
			}
			case ORC_2ND_CLASS:
			{
				if(classId == ClassId.DESTROYER || classId == ClassId.TITAN)
					return true;
				if(classId == ClassId.TYRANT || classId == ClassId.GRAND_KHAVATARI)
					return true;
				if(classId == ClassId.WARCRYER || classId == ClassId.DOOMCRYER)
					return true;
				return false;
			}
			case DWARVEN_2ND_CLASS_WAREHOUSE:
			{
				if(classId == ClassId.BOUNTY_HUNTER || classId == ClassId.FORTUNE_SEEKER)
					return true;
				return false;
			}
			case DWARVEN_2ND_CLASS_BLACKSMITH:
			{
				if(classId == ClassId.WARSMITH || classId == ClassId.MAESTRO)
					return true;
				return false;
			}
		}
		return false;
	}
}