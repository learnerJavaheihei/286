package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassType;

public class _664_QuarrelsTime extends Quest
{
	
	public final int GAROM = 34021;
	public final int SOMBRI = 34029;
	public final int RISUOUS = 34031;
	public final int GALLU = 34033;
	public final int KUKONI = 34035;
	//mobs
	private static final int[] MOBS = {24050, 24052, 24054, 24025, 24026, 24027, 24029, 24030, 24032, 24034, 24028, 24031, 24033, 24035, 24036, 24037, 24038};
	private static final int[] MOBS2 = {24046, 24048, 24025, 24026, 24027, 24029, 24030, 24032, 24034, 24028, 24031, 24033, 24035, 24036, 24037, 24038};
	private static final int[] MOBS3 = {24046, 24048, 24054, 24025, 24026, 24027, 24029, 24030, 24032, 24034};
	private static final int[] MOBS4 = {24046, 24048, 24054, 24025, 24026, 24028, 24031, 24033, 24035, 24036, 24037, 24038};
	
		
	//items
	public final int ZELIE = 90758;
	public final int MASO = 90759;	
	public final int ENHOLODA = 90760;	
	public final int CHEREP = 90761;
	public final int RELIKIA = 90746;		
		
//	[MOB_ID, REQUIRED, ITEM, NEED_COUNT, CHANCE]
	/*public final int[][] DROPLIST =
	{
			{ MOBS, ZELIE, 150, 100 },
			{ MOBS2, MASO, 150, 100 },
			{ MOBS3, ENHOLODA, 150, 100 },
			{ MOBS4, CHEREP, 150, 100 },
							
	};*/
	
	public _664_QuarrelsTime()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(GAROM);
		addTalkId(SOMBRI);
		addTalkId(RISUOUS);
		addTalkId(GALLU);
		addTalkId(KUKONI);		
		addQuestItem(ZELIE);
		addQuestItem(MASO);
		addQuestItem(ENHOLODA);
		addQuestItem(CHEREP);
		addKillId(MOBS);
		addKillId(MOBS2);
		addKillId(MOBS3);
		addKillId(MOBS4);

		//for(int[] element : DROPLIST)
			//addKillId(element[0]);

		addQuestItem(new int[]
		{ ZELIE, MASO, ENHOLODA, CHEREP });


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
		if(event.equalsIgnoreCase("SOMBRI2.htm"))
		{			
				st.setCond(2);
		}
		if(event.equalsIgnoreCase("RISUOUS2.htm"))
		{			
				st.setCond(3);
		}
		if(event.equalsIgnoreCase("GALLU2.htm"))
		{			
				st.setCond(4);
		}
		if(event.equalsIgnoreCase("KUKONI2.htm"))
		{			
				st.setCond(5);
		}
		else if(event.equalsIgnoreCase("end.htm"))
		{
						 			
				st.addExpAndSp(450000, 13500);
				st.giveItems(49670, 1);
				if(st.getPlayer().getQuestState(10868) != null) //why make a series of quests if they don't work O_O
					st.giveItems(90747, 1); //maybe more at once?
				st.takeItems(90746, 1);	
				st.takeItems(ZELIE, -1);							
				st.takeItems(MASO, -1);
				st.takeItems(ENHOLODA, -1);
				st.takeItems(CHEREP, -1);
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
			if(cond ==0){
						htmltext = "GAROM.htm";
				}	
			if(cond ==1){
						htmltext = "GAROM2.htm";
				}								
			}
		if(npcId == SOMBRI) {
			if(cond ==1){
						htmltext = "SOMBRI.htm";
				}	
			if(cond ==2){
						htmltext = "SOMBRI2.htm";
				}
			if(cond ==6){
						htmltext = "SOMBRI3.htm";
				}				
							
			}
		if(npcId == RISUOUS) {
			if(cond ==1){
						htmltext = "RISUOUS.htm";
				}	
			if(cond ==3){
						htmltext = "RISUOUS2.htm";
				}
			if(cond ==7){
						htmltext = "RISUOUS3.htm";
				}				
			}
		if(npcId == GALLU) {
			if(cond ==1){
						htmltext = "GALLU.htm";
				}	
			if(cond ==4){
						htmltext = "GALLU2.htm";
				}
			if(cond ==8){
						htmltext = "GALLU3.htm";
				}				
			}
		if(npcId == KUKONI) {
			if(cond ==1){
						htmltext = "KUKONI.htm";
				}	
			if(cond ==5){
						htmltext = "KUKONI2.htm";
				}	
			if(cond ==9){
						htmltext = "KUKONI3.htm";
				}				
			}
			return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getCond() == 2 )
		{			
			if(qs.rollAndGive(ZELIE, 1, 1, 150, 100))
				qs.setCond(6);
		}
		if(qs.getCond() == 3 )
		{			
			if(qs.rollAndGive(MASO, 1, 1, 150, 100))
				qs.setCond(7);
		}	
		if(qs.getCond() == 4 )
		{			
			if(qs.rollAndGive(ENHOLODA, 1, 1, 150, 100))
				qs.setCond(8);
		}	
		if(qs.getCond() == 5 )
		{			
			if(qs.rollAndGive(CHEREP, 1, 1, 150, 100))
				qs.setCond(9);
		}		
		return null;
	}
	
}

