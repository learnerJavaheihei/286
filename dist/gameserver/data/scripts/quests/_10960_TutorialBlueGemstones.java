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
public class _10960_TutorialBlueGemstones extends Quest
{
	private class PlayerEnterListener implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			if ((player.getRace() == Race.KAMAEL) && (player.getLevel() < 5))
			{
				QuestState st = player.getQuestState(_10960_TutorialBlueGemstones.this);
				if(st == null)
				{
					newQuestState(player);
					st = player.getQuestState(_10960_TutorialBlueGemstones.this);
					st.startQuestTimer("questStart", 10000L);
				}
			}
		}
	}
	
	// Npcs
	private static final int NEWBIE_HELPER = 34108;
	private static final int RAGNIR = 34109;
	
	// Monsters
	private static final int GREMLIN = 18342;
	
	// Items
	private static final int BLUE_GEMSTONE = 6353;
	private static final int SOULSHOT = 91927;
	private static final int SOE = 10650;
	private static final int HASTE_POTION = 49036;
	
	private final OnPlayerEnterListener _playerEnterListener = new PlayerEnterListener();
	
	public _10960_TutorialBlueGemstones()
	{
		super(PARTY_NONE, ONETIME);
		addFirstTalkId(NEWBIE_HELPER, RAGNIR);
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
		/* else if (event.equalsIgnoreCase("ragnir_q10960_03.htm"))
		{
			st.giveItems(SOULSHOT, 200, false);
		} */
		else if (event.equalsIgnoreCase("teleport"))
		{
			st.giveItems(SOULSHOT, 200, false);
			st.getPlayer().teleToLocation(-118072, 45096, 366);
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
				return "newbie_helper_q10960_01a.htm";
			else if (st.getCond() == 2)
			{
				st.takeItems(BLUE_GEMSTONE, st.getQuestItemsCount(BLUE_GEMSTONE));
				st.giveItems(HASTE_POTION, 5, false);
				st.giveItems(SOE, 5, false);
				st.giveItems(SOULSHOT, 200, false);
				st.playTutorialVoice("tutorial_voice_026");
				st.setCond(3);
				return "newbie_helper_q10960_01b.htm";
			}
			else if (st.getCond() == 3)
				return "newbie_helper_q10960_01b.htm";
		}
		else if (npcId == RAGNIR)
		{
			if ((st == null) || (st.getCond() != 3))
				return "ragnir_q10960_01.htm";
			else if (st.getCond() == 3)
				return "ragnir_q10960_02.htm";
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
