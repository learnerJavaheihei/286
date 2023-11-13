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
public class _205_TutorialBlueGemstone extends Quest
{
	private class PlayerEnterListener implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			if ((player.getRace() == Race.ORC) && (player.getLevel() < 5))
			{
				QuestState st = player.getQuestState(_205_TutorialBlueGemstone.this);
				if(st == null)
				{
					newQuestState(player);
					st = player.getQuestState(_205_TutorialBlueGemstone.this);
					st.startQuestTimer("questStart", 10000L);
				}
			}
		}
	}
	
	// Npcs
	private static final int NEWBIE_HELPER = 30575;
	private static final int VULKUS = 30573;
	
	// Monsters
	private static final int GREMLIN = 18342;
	
	// Items
	private static final int BLUE_GEMSTONE = 6353;
	private static final int SOULSHOT = 91927;
	private static final int SOE = 10650;
	private static final int HASTE_POTION = 49036;
	
	private final OnPlayerEnterListener _playerEnterListener = new PlayerEnterListener();
	
	public _205_TutorialBlueGemstone()
	{
		super(PARTY_NONE, ONETIME);
		addFirstTalkId(NEWBIE_HELPER, VULKUS);
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
/* 		else if (event.equalsIgnoreCase("vulkus_q205_03.htm"))
		{
			st.takeItems(MARK_OF_FLAME, st.getQuestItemsCount(MARK_OF_FLAME));
		} */
		else if (event.equalsIgnoreCase("teleport"))
		{
			st.giveItems(SOULSHOT, 200, false);
			st.getPlayer().teleToLocation(-45080, -113592, -213);
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
				if (!player.isMageClass())
					return "newbie_helper_q205_01a.htm";
				else
					return "newbie_helper_q205_01b.htm";
			else if (st.getCond() == 2)
			{
				st.takeItems(BLUE_GEMSTONE, st.getQuestItemsCount(BLUE_GEMSTONE));
				st.giveItems(HASTE_POTION, 5, false);
				st.giveItems(SOE, 5, false);
				st.giveItems(SOULSHOT, 200, false);
				st.playTutorialVoice("tutorial_voice_026");
				st.setCond(3);
				return "newbie_helper_q205_02.htm";
			}
			else if (st.getCond() == 3)
				return "newbie_helper_q205_02.htm";
		}
		else if (npcId == VULKUS)
		{
			if ((st == null) || (st.getCond() != 3))
				return "vulkus_q205_01.htm";
			else if (st.getCond() == 3)
				return "vulkus_q205_02.htm";
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
