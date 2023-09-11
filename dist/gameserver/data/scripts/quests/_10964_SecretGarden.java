package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;

public class _10964_SecretGarden extends Quest
{
	// Npcs
	private static final int BATHIS = 30332;
	private static final int RAYMOND = 30289;
	
	// Monsters
	private static final int[] MOBS = 
	{
		20145, // Harpy
		20158, // Medusa
		20176, // Wyrm
		20248, // Turak Bugbear
		20249 // Turak Bugbear Warrior
	};
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	public _10964_SecretGarden()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(BATHIS);
		addTalkId(RAYMOND);
		addKillNpcWithLog(2, NpcString.CLEAR_GORGON_FLOWER_GARDEN.getId(), A_LIST, 500, MOBS);
		addLevelCheck("bad_level.htm", 30, 34);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		switch (event)
		{
			case "bathis_q10964_02.htm":
			{
				st.setCond(1);
				break;
			}
			case "raymond_q10964_02.htm":
			{
				st.setCond(2);
				break;
			}
			case "raymond_q10964_05.htm":
			{
				st.addExpAndSp(2_500_000, 62_500);
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
					case 1:
					{
						return "raymond_q10964_01.htm";
					}
					case 2:
					{
						return "raymond_q10964_03.htm";
					}
					case 3:
					{
						return "raymond_q10964_04.htm";
					}
				}
				break;
			}
			case BATHIS:
			{
				switch (cond)
				{
					case 0:
					{
						return "bathis_q10964_01.htm";
					}
					case 1:
					{
						return "bathis_q10964_03.htm";
					}
				}
				break;
			}
		}
		return NO_QUEST_DIALOG;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if ((qs.getCond() == 2) && updateKill(npc, qs))
		{
			qs.unset(A_LIST);
			qs.setCond(3);
		}
		else
			{
				qs.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
