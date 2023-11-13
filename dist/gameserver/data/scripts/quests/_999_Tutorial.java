package quests;

import java.util.StringTokenizer;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.TutorialCloseHtmlPacket;

/**
 * @author Bonux
**/
/*TODO:
Добавить туториал о перемещении камеры.
*/
public final class _999_Tutorial extends Quest
{
	private class PlayerEnterListener implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			QuestState st = player.getQuestState(_999_Tutorial.this);
			if(st == null)
			{
				newQuestState(player);
				st = player.getQuestState(_999_Tutorial.this);
			}
			onTutorialEvent(ENTER_WORLD_EVENT, false, "", st);
		}
	}

	private class PlayerExitListener implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(Player player)
		{
			QuestState st = player.getQuestState(_999_Tutorial.this);
			if(st == null)
				return;

			st.stopQuestTimers();
		}
	}

	// Var's
	private static final String QUESTION_MARK_STATE = "question_mark_state";
	private static final String NEWBIE_HELPER_STATE = "newbie_helper_state";
	private static final String SHOTS_RECEIVED = "shots_received";

	// Events
	// Данные переменные не изменять, они вшиты в ядро.
	private static final String ENTER_WORLD_EVENT = "EW"; // Вход в мир.
	private static final String QUEST_TIMER_EVENT = "QT"; // Квестовый таймер.
	private static final String QUESTION_MARK_EVENT = "QM"; // Вопросытельный знак.
	private static final String TUTORIAL_LINK_EVENT = "LINK"; // Использование ссылки в туториале.
	
	// NPC's
	private static final int NEWBIE_GUIDE_HUMAN = 30598; // NPC: 说话岛初学向导
	private static final int NEWBIE_GUIDE_ELF = 30599; // NPC: 精灵村初学向导
	private static final int NEWBIE_GUIDE_DARK_ELF = 30600; // NPC: 黑暗精灵初学向导
	private static final int NEWBIE_GUIDE_DWARVEN = 30601; // NPC: 矮人初学向导
	private static final int NEWBIE_GUIDE_ORC = 30602; // NPC: 兽人初学向导
	private static final int NEWBIE_GUIDE_KAMAEL = 34110; // NPC: 鸟人初学向导
	
	private static final int[] NPC_LIST = {
			NEWBIE_GUIDE_HUMAN,
			NEWBIE_GUIDE_ELF,
			NEWBIE_GUIDE_DARK_ELF,
			NEWBIE_GUIDE_DWARVEN,
			NEWBIE_GUIDE_ORC,
			NEWBIE_GUIDE_KAMAEL
	};
	
	// Item's
	private static final int SOULSHOT = 91927; // Заряд Души
	private static final int SPIRITSHOT = 91928; // Заряд Духа

	private final OnPlayerEnterListener _playerEnterListener = new PlayerEnterListener();
	private final OnPlayerExitListener _playerExitListener = new PlayerExitListener();

	public _999_Tutorial()
	{
		super(PARTY_NONE, REPEATABLE);
		
		addFirstTalkId(NPC_LIST);

		CharListenerList.addGlobal(_playerEnterListener);
		CharListenerList.addGlobal(_playerExitListener);
	}

	@Override
	public String onTutorialEvent(final String event, final boolean quest, final String value, final QuestState st)
	{
		//st.getPlayer().sendMessage("onTutorialEvent: " + event + " " + value);
		if(event.equalsIgnoreCase(ENTER_WORLD_EVENT))
			return onEnterWorld(st);

		if(event.equalsIgnoreCase(QUESTION_MARK_EVENT))
			return onQuestionMark(Integer.parseInt(value), st);

		if(event.equalsIgnoreCase(TUTORIAL_LINK_EVENT))
			return onTutorialLink(value, st);

		final StringTokenizer tokenizer = new StringTokenizer(event, "_");
		final String cmd = tokenizer.nextToken();
		if(cmd.equalsIgnoreCase(QUEST_TIMER_EVENT))
		{
			int timerId = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
			return onQuestTimer(timerId, st);
		}

		return null;
	}

	@Override
	public String onEvent(final String event, final QuestState st, final NpcInstance npc)
	{
		final StringTokenizer tokenizer = new StringTokenizer(event, "_");
		final String cmd = tokenizer.nextToken();
		if(cmd.equalsIgnoreCase(QUEST_TIMER_EVENT))
		{
			notifyTutorialEvent(event, false, "", st);
			return null;
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(final NpcInstance npc, final Player player)
	{
		QuestState st = player.getQuestState(this);
		if(st == null)
			return null;

		String html = null;

		final int npcId = npc.getNpcId();
		switch(npcId)
		{
			case NEWBIE_GUIDE_HUMAN:
			case NEWBIE_GUIDE_ELF:
			case NEWBIE_GUIDE_DARK_ELF:
			case NEWBIE_GUIDE_DWARVEN:
			case NEWBIE_GUIDE_ORC:
			case NEWBIE_GUIDE_KAMAEL:
			{
				if (st.getInt(SHOTS_RECEIVED) == 0)
				{
					st.set(SHOTS_RECEIVED, 1);
					if(st.getPlayer().isMageClass() && st.getPlayer().getRace() != Race.ORC)
						st.giveItems(SPIRITSHOT, 100, false);
					else
						st.giveItems(SOULSHOT, 200, false);
				}
				return "";
			}
		}
		return html;
	}

	private String onEnterWorld(final QuestState st)
	{
		final int playerLevel = st.getPlayer().getLevel();
		if(playerLevel < 5)
		{
			final int nh_state = st.getInt(NEWBIE_HELPER_STATE);
			final int qm_state = st.getInt(QUESTION_MARK_STATE);
			if(nh_state == 0)
			{
				if(qm_state == 0)
				{
					st.set(QUESTION_MARK_STATE, 1);
					st.startQuestTimer(QUEST_TIMER_EVENT + "_" + 1, 10000L);
				}
				else if(qm_state == 1)
				{
					st.showQuestionMark(false, 1);
					st.playSound(SOUND_TUTORIAL);
					st.playTutorialVoice("tutorial_voice_006");
				}
				else if(qm_state == 2)
				{
					st.showQuestionMark(false, 2);
					st.playSound(SOUND_TUTORIAL);
				}
			}
			else if(nh_state == 1 || nh_state == 2)
			{
				st.showQuestionMark(false, 2);
				st.playSound(SOUND_TUTORIAL);
			}
		}
		return null;
	}

	private String onQuestTimer(final int timerId, final QuestState st)
	{
		String html = null;

		final int nh_state = st.getInt(NEWBIE_HELPER_STATE);
		final int qm_state = st.getInt(QUESTION_MARK_STATE);
		switch(timerId)
		{
			case 1:
				if(qm_state == 1)
				{
					if(nh_state == 0)
					{
						String voice = null;

						final Player player = st.getPlayer();
						switch(player.getRace())
						{
							case HUMAN:
								if(player.isMageClass())
								{
									html = "tutorial_human_mage001.htm";
									voice = "tutorial_voice_001b";
								}
								else
								{
									html = "tutorial_human_fighter001.htm";
									voice = "tutorial_voice_001a";
								}
								break;
							case ELF:
								if(player.isMageClass())
								{
									html = "tutorial_elven_mage001.htm";
									voice = "tutorial_voice_001d";
								}
								else
								{
									html = "tutorial_elven_fighter001.htm";
									voice = "tutorial_voice_001c";
								}
								break;
							case DARKELF:
								if(player.isMageClass())
								{
									html = "tutorial_delf_mage001.htm";
									voice = "tutorial_voice_001f";
								}
								else
								{
									html = "tutorial_delf_fighter001.htm";
									voice = "tutorial_voice_001e";
								}
								break;
							case ORC:
								if(player.isMageClass())
								{
									html = "tutorial_orc_mage001.htm";
									voice = "tutorial_voice_001h";
								}
								else
								{
									html = "tutorial_orc_fighter001.htm";
									voice = "tutorial_voice_001g";
								}
								break;
							case DWARF:
								html = "tutorial_dwarven_fighter001.htm";
								voice = "tutorial_voice_001i";
								break;
							case KAMAEL:
								html = "tutorial_kamael_fighter001.htm";
								voice = "tutorial_voice_001i";
								break;
						}

						st.playTutorialVoice(voice);
						st.cancelQuestTimer(QUEST_TIMER_EVENT);
					}
				}
				break;
			case 2:
				if(nh_state == 1)
					st.playTutorialVoice("tutorial_voice_009a");
				break;
		}

		return html;
	}

	private String onQuestionMark(final int markId, final QuestState st)
	{
		String html = null;

		final Player player = st.getPlayer();
		if(markId == 1)
		{
			st.set(QUESTION_MARK_STATE, 2);
			player.sendPacket(new ExShowScreenMessage(NpcString._SPEAK_WITH_THE_NEWBIE_HELPER, 5000, ScreenMessageAlign.TOP_CENTER, true));
			html = "tutorial_02.htm";
		}
		else if(markId == 2)
		{
			switch(player.getRace())
			{
				case HUMAN:
					if(player.isMageClass())
						html = "tutorial_human_mage002.htm";
					else
						html = "tutorial_human_fighter002.htm";
					break;
				case ELF:
					html = "tutorial_elven002.htm";
					break;
				case DARKELF:
					html = "tutorial_delf002.htm";
					break;
				case ORC:
					html = "tutorial_orc002.htm";
					break;
				case DWARF:
					html = "tutorial_dwarven002.htm";
					break;
				case KAMAEL:
					html = "tutorial_dwarven002.htm";
					break;
			}
		}

		return html;
	}

	private String onTutorialLink(final String value, final QuestState st)
	{
		String html = null;
		Player player = st.getPlayer();

		final StringTokenizer tokenizer = new StringTokenizer(value, "_");
		final String cmd = tokenizer.nextToken();
		if(cmd.equalsIgnoreCase("tutorial"))
		{
			final String cmd2 = tokenizer.nextToken();
			if(cmd2.equalsIgnoreCase("close"))
			{
				if(tokenizer.hasMoreTokens())
				{
					int actionId = Integer.parseInt(tokenizer.nextToken());
					switch(actionId)
					{
						case 1:
							st.showQuestionMark(false, 1);
							break;
						case 2:
							html = "tutorial_00.htm";
							break;
						case 7:
							html = "tutorial_01.htm";
							break;
					}
				}
				player.sendPacket(TutorialCloseHtmlPacket.STATIC);
			}
		}
		return html;
	}
	
	@Override
	public boolean isVisible(final Player player)
	{
		return false;
	}
}