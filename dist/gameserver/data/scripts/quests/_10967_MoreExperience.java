package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;

public class _10967_MoreExperience extends Quest
{
	// Npcs
	private static final int BATHIS = 30332;
	
	// Monsters
	private static final int[] MOBS = 
	{
		20063, // Ol Mahum Shooter
		20066, // Ol Mahum Officer
		20076, // Ol Mahum Commander
		20438, // Ol Mahum General
		20439 // Ol Mahum Sergeant
	};
	
	// Items
	private static final int ADVENTURER_BROOCH = 91932;
	private static final int JEWEL_OF_ADVENTURER_FRAGMENT = 91936;
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	public _10967_MoreExperience()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(BATHIS);
		addTalkId(BATHIS);
		addKillNpcWithLog(1, NpcString.CLEAR_ABANDONED_CAMP.getId(), A_LIST, 300, MOBS);
		addLevelCheck("bad_level.htm", 25, 30);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		switch (event)
		{
			case "bathis_q10967_02.htm":
			{
				st.setCond(1);
				break;
			}
			case "bathis_q10967_05.htm":
			{
				st.addExpAndSp(2_000_000, 50_000);
				st.giveItems(ADVENTURER_BROOCH, 1);
				st.giveItems(JEWEL_OF_ADVENTURER_FRAGMENT, 1);
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
			case BATHIS:
			{
				switch (cond)
				{
					case 0:
					{
						return "bathis_q10967_01.htm";
					}
					case 1:
					{
						return "bathis_q10967_03.htm";
					}
					case 2:
					{
						return "bathis_q10967_04.htm";
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
