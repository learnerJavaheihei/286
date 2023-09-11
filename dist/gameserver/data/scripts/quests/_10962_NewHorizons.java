package quests;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;

public class _10962_NewHorizons extends Quest
{
	// Npcs
	private static final int LEAHEN = 34111;
	private static final int BATHIS = 30332;
	
	// Monsters
	private static final int[] MOBS = 
	{
		21985, // Mountain Werewolf
		21986, // Mountain Fungus
		21987, // Muertos Fighter
		21988 // Muertos Captain
	};
	
	// Items
	private static final int SPIRIT_ORE = 3031;
	private static final int SOE_NOVICE = 10650;
	private static final int RICE_CAKE = 49674;
	private static final int SOE_ABANDONED_CAMP = 70053;
	private static final int SOE_WINDMILL_HILL = 91726;
	private static final int SOE_RUINS_OF_AGONY = 70051;
	private static final int HP_POTION = 91912;
	private static final int ESCAPE_BATHIS = 91651;
	// Moon heavy set
	private static final int MOON_HELMET = 7850;
	private static final int MOON_ARMOR = 7851;
	private static final int MOON_GAUNTLETS = 7852;
	private static final int MOON_BOOTS = 7853;
	// Moon light set
	private static final int MOON_SHELL = 7854;
	private static final int MOON_LEATHER_GLOVES = 7855;
	private static final int MOON_SHOES = 7856;
	// Moon robe
	private static final int MOON_CAPE = 7857;
	private static final int MOON_SILK_GLOVES = 7858;
	private static final int MOON_SANDALS = 7859;
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-107812, 47531, -1448);
	
	public _10962_NewHorizons()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(LEAHEN);
		addTalkId(BATHIS);
		addKillNpcWithLog(1, NpcString.CLEAR_HILLS_OF_GOLD.getId(), A_LIST, 30, MOBS);
		addLevelCheck("bad_level.htm", 15, 20);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		
		switch (event)
		{
			case "leahen_q10962_02.htm":
			{
				st.setCond(1);
				player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				break;
			}
			case "bathis_q10962_02.htm":
			{
				st.addExpAndSp(600_000, 13_500);
				st.giveItems(SPIRIT_ORE, 50);
				st.giveItems(SOE_NOVICE, 10);
				st.giveItems(RICE_CAKE, 1);
				st.giveItems(SOE_ABANDONED_CAMP, 3);
				st.giveItems(SOE_WINDMILL_HILL, 3);
				st.giveItems(SOE_RUINS_OF_AGONY, 3);
				st.giveItems(HP_POTION, 50);
				st.giveItems(MOON_HELMET, 1);
				st.giveItems(MOON_ARMOR, 1);
				st.giveItems(MOON_GAUNTLETS, 1);
				st.giveItems(MOON_BOOTS, 1);
				st.finishQuest();
				break;
			}
			case "bathis_q10962_03.htm":
			{
				st.addExpAndSp(600_000, 13_500);
				st.giveItems(SPIRIT_ORE, 50);
				st.giveItems(SOE_NOVICE, 10);
				st.giveItems(RICE_CAKE, 1);
				st.giveItems(SOE_ABANDONED_CAMP, 3);
				st.giveItems(SOE_WINDMILL_HILL, 3);
				st.giveItems(SOE_RUINS_OF_AGONY, 3);
				st.giveItems(HP_POTION, 50);
				st.giveItems(MOON_HELMET, 1);
				st.giveItems(MOON_SHELL, 1);
				st.giveItems(MOON_LEATHER_GLOVES, 1);
				st.giveItems(MOON_SHOES, 1);
				st.finishQuest();
				break;
			}
			case "bathis_q10962_04.htm":
			{
				st.addExpAndSp(600_000, 13_500);
				st.giveItems(SPIRIT_ORE, 50);
				st.giveItems(SOE_NOVICE, 10);
				st.giveItems(RICE_CAKE, 1);
				st.giveItems(SOE_ABANDONED_CAMP, 3);
				st.giveItems(SOE_WINDMILL_HILL, 3);
				st.giveItems(SOE_RUINS_OF_AGONY, 3);
				st.giveItems(HP_POTION, 50);
				st.giveItems(MOON_HELMET, 1);
				st.giveItems(MOON_CAPE, 1);
				st.giveItems(MOON_SILK_GLOVES, 1);
				st.giveItems(MOON_SANDALS, 1);
				st.finishQuest();
				break;
			}
		}
		
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		
		switch (npcId)
		{
			case LEAHEN:
			{
				switch (cond)
				{
					case 0:
					{
						return "leahen_q10962_01.htm";
					}
					case 1:
					{
						return "leahen_q10962_03.htm";
					}
					case 2:
					{
						return "leahen_q10962_04.htm";
					}
				}
				break;
			}
			case BATHIS:
			{
				if (cond == 2)
					return "bathis_q10962_01.htm";
				break;
			}
		}
		return NO_QUEST_DIALOG;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if ((qs.getCond() == 1) && updateKill(npc, qs))
		{
			qs.unset(A_LIST);
			qs.setCond(2);
			qs.giveItems(ESCAPE_BATHIS, 1);
			qs.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.TALK_TO_CAPTAIN_BATHIS_2, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
		}
		else
			{
				qs.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}