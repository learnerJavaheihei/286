package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author L2-scripts.com - (SanyaDC)
 */

public class _630_PirateTreasureHunt extends Quest
{
	public final int GAROM = 34021;
	public final int SUNDUK = 24058;
	public final int TELEPORT1 = 34038;
	
	//mobs
	public final int PLAKALWIK = 24041;
	public final int DOHLAK = 24042;
	public final int SYWESTVO = 24043;
		
		
	//items
	public final int KEY = 90815;
	public final int NAGRADASUNDUK = 90762;	
	public final int MAP = 90756;	
	public final int PIECEMAP = 90755;
	public static final String A_LIST = "a_list";
	
	
	public _630_PirateTreasureHunt()
	{
		super(PARTY_NONE, DAILY);

		addStartNpc(GAROM);
		addTalkId(SUNDUK);					
		addQuestItem(KEY);
		addKillNpcWithLog(9, A_LIST, 1, 24041);		
		addKillNpcWithLog(10, A_LIST, 1, 24042);
		addKillNpcWithLog(11, A_LIST, 1, 24043);
		addLevelCheck("lvl.htm", 70);
		
	}

	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("GAROM2.htm"))
			{
				st.setCond(1);
			}
		else if(event.equalsIgnoreCase("GAROMCHANGE2.htm"))
			{			
				st.giveItems(90756, 1);				
				st.takeItems(90755, 25);	
			}		
		else if(event.equalsIgnoreCase("GAROM4.htm"))
			{			
				st.setCond(2);
			}
		else if(event.equalsIgnoreCase("TP2.htm"))
			{	
				st.takeItems(90756, 1);
				st.giveItems(90815, 1);
				st.setCond(6);
			}
		else if(event.equalsIgnoreCase("KAENA4.htm"))
			{			
				st.setCond(6);
			}
		else if(event.equalsIgnoreCase("SUND2.htm"))
		{				
				st.addExpAndSp(30000, 1000);
				st.giveItems(90762, 1);				
				st.takeItems(KEY, 1);				
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

		if(npcId == GAROM) {
			if(cond == 0 && st.getQuestItemsCount(MAP) >= 1)
				{												
						htmltext = "GAROM.htm";										
				}
			if(cond == 0 && st.getQuestItemsCount(PIECEMAP) >= 25)
				{												
						htmltext = "GAROMCHANGE.htm";										
				}					
			if(cond == 0 && st.getQuestItemsCount(MAP) < 1 && st.getQuestItemsCount(PIECEMAP) < 25)
				{												
						htmltext = "NOITEMS.htm";										
				}
			if(cond == 1)
				{												
						htmltext = "GAROM3.htm";										
				}
			if(cond == 2)
				{												
						htmltext = "GAROM4.htm";										
				}
					 }
		if(npcId == SUNDUK) {
			if(cond ==12){
						htmltext = "SUND.htm";
				}	
									
			}		
		if(npcId == TELEPORT1) {
			if(cond ==2){
						htmltext = "TP.htm";
				}	
			if(cond ==6){
						htmltext = "TP3.htm";
				}
			if(cond ==7){
						htmltext = "TP4.htm";
				}
			if(cond ==8){
						htmltext = "TP5.htm";
				}
						
			}
			return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getCond() == 9 )
			{
				if(npc.getNpcId() == 24041)
					qs.unset(A_LIST);
					qs.addSpawn(SUNDUK);
					qs.setCond(12);
			}
		if(qs.getCond() == 10 )
			{
				if(npc.getNpcId() == 24042)
					qs.unset(A_LIST);
					qs.addSpawn(SUNDUK);
					qs.setCond(12);
			}
		if(qs.getCond() == 11 )
			{
				if(npc.getNpcId() == 24043)
					qs.unset(A_LIST);
					qs.addSpawn(SUNDUK);
					qs.setCond(12);
			}		
		return null;
	}	
}	