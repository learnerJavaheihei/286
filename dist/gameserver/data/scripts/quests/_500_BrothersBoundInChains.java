package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.utils.ItemFunctions;

public class _500_BrothersBoundInChains extends Quest
{
	public static final int JUDGE = 30981;
	public static final int CRUMBS = 70807;
	
	public _500_BrothersBoundInChains()
	{
		super(PARTY_NONE, DAILY, false); //cannot be aborted
		
		addStartNpc(JUDGE);
		addTalkId(JUDGE);
		addQuestItem(CRUMBS);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("starting_now.htm"))
		{
			if(ItemFunctions.getItemCount(player, 2132) < 30)
				htmltext = "nogems.htm";	
			else
			{
				st.setCond(1);
				st.takeItems(2132, 30); //take gems
				ItemFunctions.addItem(player, 70806, 1, true);
				player.sendChanges();//why?
			}	
		}	
		else if(event.equalsIgnoreCase("finish_now.htm"))
		{	
			int decrease = Rnd.get(1,3);
			player.setPkKills(Math.max(player.getPkKills() - decrease, 0));
			player.sendMessage("Your PK counter has decreased by " + decrease); // TODO: Check message.
			ItemFunctions.deleteItemsEverywhere(player, 70806); //take sin eater bracelete from everywhere
			st.takeItems(CRUMBS, 35);
			st.finishQuest();						
		}
		return htmltext;
	}	
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		if(npcId == JUDGE) 
		{
			if(cond == 0)
			{
				if(ItemFunctions.getItemCount(player, 2132) < 30)
					htmltext = "nogems.htm";	
				else if(player.getPkKills() > 0 && ItemFunctions.getItemCount(player, 70806) == 0)
					htmltext = "start.htm";	
				else if(ItemFunctions.getItemCount(player, 70806) != 0) //something went wrong	
				{
					ItemFunctions.deleteItemsEverywhere(player, 70806); //delete the item first
					htmltext = "start.htm";	
				}
				else //no pk
					htmltext = "nopk.htm";	
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(CRUMBS) >= 35)
					htmltext = "finished.htm";
				else
					htmltext = "not_finish.htm";
			} 
		}
		return htmltext;
	}	
}
