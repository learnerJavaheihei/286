package quests;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;

public class _10981_UnbearableWolvesHowling extends Quest
{
	// Npcs
	private static final int NEWBIE_GUIDE = 30598;
	private static final int JACKSON = 30002;
	
	// Monsters
	private static final int[] MOBS = 
	{
		20120, // Wolf
		20442, // Elder Wolf
		20481, // Bearded Keltir
		20544, // Elder Keltir
	};
	
	// Items
	private static final int SOE_NOVICE = 10650;
	private static final int NOVICE_NECKLACE = 49039;
	private static final int NOVICE_EARRING = 49040;
	private static final int NOVICE_RING = 49041;
	private static final int ESCAPE_LOUD_WOLVES = 91646;
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-90050, 241763, -3560);
	
	public _10981_UnbearableWolvesHowling()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(NEWBIE_GUIDE);
		addTalkId(JACKSON);
		addKillNpcWithLog(1, NpcString.HUNT_WOLVES_AND_KELTIRS_2.getId(), A_LIST, 20, MOBS);
		addLevelCheck("high_level.htm", 1, 20);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		
		switch (event)
		{
			case "newbie_guide_q10981_02.htm":
			{
				st.setCond(1);
				break;
			}
			case "teleport":
			{
				player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				return null;
			}
			case "jackson_q10981_02.htm":
			{
				st.addExpAndSp(260_000, 6_000);
				st.giveItems(SOE_NOVICE, 10);
				st.giveItems(NOVICE_NECKLACE, 1);
				st.giveItems(NOVICE_EARRING, 2);
				st.giveItems(NOVICE_RING, 2);
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
			case NEWBIE_GUIDE:
			{
				switch (cond)
				{
					case 0:
					{
						return "newbie_guide_q10981_01.htm";
					}
					case 1:
					{
						return "newbie_guide_q10981_03.htm";
					}
					case 2:
					{
						return "newbie_guide_q10981_04.htm";
					}
				}
				break;
			}
			case JACKSON:
			{
				if (cond == 2)
					return "jackson_q10981_01.htm";
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
			qs.giveItems(ESCAPE_LOUD_WOLVES, 1);
			qs.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.TALK_TO_ARMOR_MERCHANT_JACKSON, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
		}
		else
			{
				qs.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}