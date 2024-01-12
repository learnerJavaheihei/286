package quests;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;

public class _10966_ATripBegins extends Quest
{
	// Npcs
	private static final int BATHIS = 30332;
	private static final int BELLA = 30256;
	
	// Monsters
	private static final int[] MOBS = 
	{
		20050, // Poisonous Thornleg
		20051, // Skeleton Bowman
		20054, // Ruin Spartoi
		20060, // Raging Spartoi
		20062, // Tumran Bugbear
		20064 // Tumran Bugbear Warrior
	};
	
	// Items
	/* private static final int IMPROVED_SOE = 49087; */
	private static final int TALISMAN_OF_ADEN = 91745;
	/* private static final int SCROLL_ENCHANT_TALISMAN_ADEN = 91756; */
	private static final int ADVENTURER_BRACELET = 91934;
	
	// Etc
	private static final String A_LIST = "A_LIST";
	
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-47344, 114188, -3672);
	
	public _10966_ATripBegins()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(BATHIS);
		addTalkId(BELLA, BATHIS);
		addKillNpcWithLog(2, NpcString.CLEAR_RUINS_OF_AGONY.getId(), A_LIST, 15, MOBS);
		addLevelCheck("bad_level.htm", 20, 25);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		
		switch (event)
		{
			case "bathis_q10966_02.htm":
			{
				st.setCond(1);
				break;
			}
			case "bella_q10966_02.htm":
			{
				st.setCond(2);
				player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				break;
			}
			case "bathis_q10966_05.htm":
			{
				st.addExpAndSp(50_000, 1_500);
				/* st.giveItems(IMPROVED_SOE, 10); */
				st.giveItems(TALISMAN_OF_ADEN, 1);
				/* st.giveItems(SCROLL_ENCHANT_TALISMAN_ADEN, 1); */
				st.giveItems(ADVENTURER_BRACELET, 1);
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
			case BELLA:
			{
				switch (cond)
				{
					case 1:
					{
						return "bella_q10966_01.htm";
					}
					case 2:
					{
						return "bella_q10966_03.htm";
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
						return "bathis_q10966_01.htm";
					}
					case 1:
					{
						return "bathis_q10966_03.htm";
					}
					case 3:
					{
						return "bathis_q10966_04.htm";
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
