package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author L2-scripts.com - (SanyaDC)
 */

public class _10870_UnfinishedDevice extends Quest
{
	public final int GAROM = 34021;
	public final int AIN = 34017;
	public final int RODEMAI = 30756;
	public final int KAENA = 30720;
	//mobs
	public final int MONSTERS[] = {20659, 20660, 20661, 20662, 20663, 20664, 20665, 20667};
	public final int VANOR[] = {20681, 20682 , 20683, 20684, 20685, 20686, 24014};
		
		
	//items
	public final int TOTEM = 90750;
	public final int KROV = 90751;	
		
	//	# [MOB_ID, REQUIRED, ITEM, NEED_COUNT, CHANCE]
	public final int[][] DROPLIST =
	{
			{ 20659, KROV, 50, 100 },
			{ 20660, KROV, 50, 100 },
			{ 20661, KROV, 50, 100 },
			{ 20662, KROV, 50, 100 },
			{ 20663, KROV, 50, 100 },
			{ 20664, KROV, 50, 100 },
			{ 20665, KROV, 50, 100 },
			{ 20667, KROV, 50, 100 },			
			{ 20681, TOTEM, 50, 100 },
			{ 20682, TOTEM, 50, 100 },
			{ 20683, TOTEM, 50, 100 },
			{ 20684, TOTEM, 50, 100 },
			{ 20685, TOTEM, 50, 100 },
			{ 20686, TOTEM, 50, 100 },
			{ 24014, TOTEM, 50, 100 }							
	};
	
	public _10870_UnfinishedDevice()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(GAROM);
		addTalkId(AIN);	
		addTalkId(RODEMAI);	
		addTalkId(KAENA);			
		addQuestItem(TOTEM);
		addQuestItem(KROV);		

		for(int[] element : DROPLIST)
			addKillId(element[0]);
		addQuestItem(new int[]
		{ TOTEM, KROV });
		

		addLevelCheck("lvl.htm", 70);		
		addQuestCompletedCheck("questnotdone.htm", 10868);		
	}

	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("GAROM2.htm"))
			{
				st.setCond(1);
			}
		else if(event.equalsIgnoreCase("AIN2.htm"))
			{			
				st.setCond(2);
			}		
		else if(event.equalsIgnoreCase("RODEMAI2.htm"))
			{			
				st.setCond(3);
			}
		else if(event.equalsIgnoreCase("KAENA2.htm"))
			{			
				st.setCond(4);
			}
		else if(event.equalsIgnoreCase("KAENA4.htm"))
			{			
				st.setCond(6);
			}
		else if(event.equalsIgnoreCase("AIN4.htm"))
		{
				
				st.addExpAndSp(450000, 13500);
				st.giveItems(6845, 1);				
				st.takeItems(TOTEM, -1);
				st.takeItems(KROV, -1);				
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
			if(cond == 0)
				{												
						htmltext = "GAROM.htm";		
									
				}
					if(cond ==1){
						htmltext = "GAROM2.htm";
				}					
				 
					 }
		if(npcId == AIN) {
			if(cond ==1){
						htmltext = "AIN.htm";
				}	
			if(cond ==2){
						htmltext = "AIN2.htm";
				}	
			if(cond ==6){
						htmltext = "AIN3.htm";
				}				
			}
		if(npcId == RODEMAI) {
			if(cond ==2){
						htmltext = "RODEMAI.htm";
				}	
			if(cond ==3){
						htmltext = "RODEMAI2.htm";
				}							
			}
		if(npcId == KAENA) {
			if(cond ==3){
				htmltext = "KAENA.htm";
			}	
			if(cond ==4){
				htmltext = "KAENA2.htm";
			}	
			if(cond ==5){
				htmltext = "KAENA3.htm";
			}
			if(cond ==6){
				htmltext = "KAENA4.htm";
			}				
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getCond() == 4 )
		{
			if(npc.getNpcId() == 20659 || npc.getNpcId() == 20660 || npc.getNpcId() == 20661 || npc.getNpcId() == 20662 || npc.getNpcId() == 20663 || npc.getNpcId() == 20664 || npc.getNpcId() == 20665 || npc.getNpcId() == 20667)
				qs.rollAndGive(KROV, 1, 1, 50, 100);
			if(npc.getNpcId() == 20681 || npc.getNpcId() == 20682 || npc.getNpcId() == 20683 || npc.getNpcId() == 20684 || npc.getNpcId() == 20685 || npc.getNpcId() == 20686 || npc.getNpcId() == 24014)
				qs.rollAndGive(TOTEM, 1, 1, 50, 100);	
			if(qs.getQuestItemsCount(TOTEM) >= 50 && qs.getQuestItemsCount(KROV) >= 50)
				qs.setCond(5);
		}				
		return null;
	}	
}	