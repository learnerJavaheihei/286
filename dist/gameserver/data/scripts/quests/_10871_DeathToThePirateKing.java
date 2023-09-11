package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public class _10871_DeathToThePirateKing extends Quest
{
	private static final int EIGIS = 34017;
	private static final int ZAKEN = 29022;
	private static final int ZAKEN_BLOOD = 90754;
	
	public _10871_DeathToThePirateKing()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(EIGIS);
		addTalkId(EIGIS);

		addKillId(ZAKEN);
		addQuestItem(ZAKEN_BLOOD);
		addLevelCheck("lvl.htm", 70);
		addQuestCompletedCheck("questnotdone.htm", 10870);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("starting_now.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("finish_now.htm"))
		{
			st.addExpAndSp(900000, 27000);
			st.giveItems(21713, 1);	//cloak
			st.takeItems(ZAKEN_BLOOD, -1);
			st.finishQuest();
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == EIGIS) 
		{
			if(cond == 0)
			{
				htmltext = "start.htm";
			}
			else if(cond == 1)
			{
				htmltext = "not_finish.htm";
			}
			else if(cond == 2)
			{
				htmltext = "finished.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getCond() == 1)
		{
			qs.setCond(2);
			qs.giveItems(ZAKEN_BLOOD, 1);
		}
		return null;
	}
}
