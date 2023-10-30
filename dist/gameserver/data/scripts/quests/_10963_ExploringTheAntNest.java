package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;

public class _10963_ExploringTheAntNest extends Quest
{
	// Npcs
	private static final int RAYMOND = 30289;
	
	// Monsters
	private static final int[] MOBS = 
	{
		20075, // Ant Larva
		20079, // Ant
		20080, // Ant Captain
		20081, // Ant Overseer
		20082, // Ant Recruit
		20084, // Ant Patrol
		20086, // Ant Guard
		20087, // Ant Soldier
		20088, // Ant Warrior Captain
		20089, // Noble Ant
		20090 // Noble Ant Captain
	};
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	public _10963_ExploringTheAntNest()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(RAYMOND);
		addTalkId(RAYMOND);
		addKillNpcWithLog(1, NpcString.DEFEAT_MONSTERS_IN_ANT_NEST.getId(), A_LIST, 500, MOBS);
		addLevelCheck("bad_level.htm", 34, 37);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		switch (event)
		{
			case "raymond_q10963_02.htm":
			{
				st.setCond(1);
				break;
			}
			case "raymond_q10963_05.htm":
			{
				st.addExpAndSp(300_000, 7_000);
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
			case RAYMOND:
			{
				switch (cond)
				{
					case 0:
					{
						return "raymond_q10963_01.htm";
					}
					case 1:
					{
						return "raymond_q10963_03.htm";
					}
					case 2:
					{
						return "raymond_q10963_04.htm";
					}
				}
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
		}
		else
			{
				qs.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
