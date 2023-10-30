package quests;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;

public class _10961_EffectiveTraining extends Quest
{
	// Npcs
	private static final int NEWBIE_GUIDE = 34110;
	private static final int LEAHEN = 34111;
	
	// Monsters
	private static final int[] MOBS = 
	{
		21981, // Grey Keltir
		21982, // Elder Grey Keltir
		21983, // Black Wolf
		21984 // Elder Black Wolf
	};
	
	// Items
	private static final int SOE_NOVICE = 10650;
	private static final int NOVICE_NECKLACE = 49039;
	private static final int NOVICE_EARRING = 49040;
	private static final int NOVICE_RING = 49041;
	private static final int ADAPTATION_TRAINING = 91917;
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-121817, 56777, -1984);
	
	public _10961_EffectiveTraining()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(NEWBIE_GUIDE);
		addTalkId(LEAHEN);
		addKillNpcWithLog(1, NpcString.CLEAR_HILLS_OF_HOPE.getId(), A_LIST, 20, MOBS);
		addLevelCheck("high_level.htm", 1, 20);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		
		switch (event)
		{
			case "newbie_guide_q10961_02.htm":
			{
				st.setCond(1);
				player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				break;
			}
			case "leahen_q10961_02.htm":
			{
				st.addExpAndSp(26_000, 1_000);
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
						return "newbie_guide_q10961_01.htm";
					}
					case 1:
					{
						return "newbie_guide_q10961_03.htm";
					}
					case 2:
					{
						return "newbie_guide_q10961_04.htm";
					}
				}
				break;
			}
			case LEAHEN:
			{
				if (cond == 2)
					return "leahen_q10961_01.htm";
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
			qs.giveItems(ADAPTATION_TRAINING, 1);
			qs.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.TALK_TO_LEAHEN, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
		}
		else
			{
				qs.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
