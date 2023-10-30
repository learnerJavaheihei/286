package quests;

import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.TutorialShowHtmlPacket;

/**
 * @author nexvill
 */
public class _206_TutorialBlueGemstone extends Quest
{
	private class PlayerEnterListener implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			if ((player.getRace() == Race.DWARF) && (player.getLevel() < 5))
			{
				QuestState st = player.getQuestState(_206_TutorialBlueGemstone.this);
				if(st == null)
				{
					newQuestState(player);
					st = player.getQuestState(_206_TutorialBlueGemstone.this);
					st.startQuestTimer("questStart", 10000L);
				}
			}
		}
	}
	
	// Npcs
	private static final int NEWBIE_HELPER = 30530;
	private static final int LAFERON = 30528;
	
	// Monsters
	private static final int GREMLIN = 18342;
	
	// Items
	private static final int BLUE_GEMSTONE = 6353;
	private static final int SOULSHOT = 91927;
	private static final int SOE = 10650;
	private static final int HASTE_POTION = 49036;
	private static final int MINING_LICENCE = 1496;
	
	private final OnPlayerEnterListener _playerEnterListener = new PlayerEnterListener();
	
	public _206_TutorialBlueGemstone()
	{
		super(PARTY_NONE, ONETIME);
		addFirstTalkId(NEWBIE_HELPER, LAFERON);
		addKillId(GREMLIN);
		
		CharListenerList.addGlobal(_playerEnterListener);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if (event.equalsIgnoreCase("questStart"))
		{
			st.setCond(1);
			return "";
		}
		else if (event.equalsIgnoreCase("laferon_q206_03.htm"))
		{
			st.takeItems(MINING_LICENCE, st.getQuestItemsCount(MINING_LICENCE));
		}
		else if (event.equalsIgnoreCase("teleport"))
		{
			st.giveItems(SOULSHOT, 200, false);
			st.getPlayer().teleToLocation(115608, -177992, -916);
			st.finishQuest();
			return "";
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState st = player.getQuestState(this);
		int npcId = npc.getNpcId();
		if (npcId == NEWBIE_HELPER)
		{
			if ((st == null) || ((st.getCond() != 2) && (st.getCond() != 3)))
				return "newbie_helper_q206_01a.htm";
			else if (st.getCond() == 2)
			{
				st.takeItems(BLUE_GEMSTONE, st.getQuestItemsCount(BLUE_GEMSTONE));
				st.giveItems(HASTE_POTION, 5, false);
				st.giveItems(SOE, 5, false);
				st.giveItems(MINING_LICENCE, 1, false);
				st.giveItems(SOULSHOT, 200, false);
				st.playTutorialVoice("tutorial_voice_026");
				st.setCond(3);
				return "newbie_helper_q206_01b.htm";
			}
			else if (st.getCond() == 3)
				return "newbie_helper_q206_01b.htm";
		}
		else if (npcId == LAFERON)
		{
			if ((st == null) || (st.getCond() != 3))
				return "laferon_q206_01.htm";
			else if (st.getCond() == 3)
				return "laferon_q206_02.htm";
		}
		return NO_QUEST_DIALOG;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if (qs.getCond() == 1)
		{
			qs.giveItems(BLUE_GEMSTONE, 1, false);
			qs.setCond(2, SOUND_TUTORIAL);
			qs.getPlayer().sendPacket(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.LARGE_WINDOW, "..\\L2text\\QT_001_Radar_01.htm"));
		}
		return null;
	}
}
