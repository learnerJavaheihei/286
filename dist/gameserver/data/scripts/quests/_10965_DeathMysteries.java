package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;

public class _10965_DeathMysteries extends Quest
{
	// Npcs
	private static final int RAYMOND = 30289;
	private static final int MAXIMILIAN = 30120;
	
	// Monsters
	private static final int[] MOBS = 
	{
		20176, // Wyrm
		20550, // Guardian Basilisk
		20551, // Road Scavenger
		20552, // Fettered Soul
		20553, // Windsus
		20554 // Grandis
	};
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	public _10965_DeathMysteries()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(RAYMOND);
		addTalkId(MAXIMILIAN);
		addKillNpcWithLog(2, NpcString.CLEAR_DEATH_PASS.getId(), A_LIST, 500, MOBS);
		addLevelCheck("bad_level.htm", 37, 40);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		switch (event)
		{
			case "raymond_q10965_02.htm":
			{
				st.setCond(1);
				break;
			}
			case "maximilian_q10965_02.htm":
			{
				st.setCond(2);
				break;
			}
			case "maximilian_q10965_05.htm":
			{
				st.addExpAndSp(3_200_000, 80_000);
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
			case MAXIMILIAN:
			{
				switch (cond)
				{
					case 1:
					{
						return "maximilian_q10965_01.htm";
					}
					case 2:
					{
						return "maximilian_q10965_03.htm";
					}
					case 3:
					{
						return "maximilian_q10965_04.htm";
					}
				}
				break;
			}
			case RAYMOND:
			{
				switch (cond)
				{
					case 0:
					{
						return "raymond_q10965_01.htm";
					}
					case 1:
					{
						return "raymond_q10965_03.htm";
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
