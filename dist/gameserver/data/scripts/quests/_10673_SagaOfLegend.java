package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;

public class _10673_SagaOfLegend extends Quest
{
	// Npcs
	private static final int ORVEN = 30857;
	
	// Monsters
	private static final int[] MOBS1 = 
	{
		20965, // Chimera Piece
		20966, // Mutated Creation
		20967, // Creature of the Past
		20968, // Forgotten Face
		20969, // Giant's Shadow
		20970, // Soldier of Ancient Times
		20971, // Warrior of Ancient Times
		20972, // Shaman of Ancient Times
		20973 // Forgotten Ancient People
	};
	
	private static final int[] MOBS2 = 
	{
		24025, // Bloody Purple
		24026, // Clipher
		24027, // Sairon
		24028, // Demon Warrior
		24029, // Veil Master
		24030, // Stone Vanul
		24031, // Death Flyer
		24032, // Seer
		24033, // Guardian Spirit
		24034, // Midnight Sairon
		24035, // Daymen
		24036, // Dolores
		24037, // Maiden Doll
		24038, // Tor Scorpion
		24039, // Pearl Horror
		24040, // Midnight Nightmare
		24041, // Bloody Mourner
		24042, // Clumsy Wimp
		24043, // Mysterious Creature
		24044, // Zaken's Treasure Chest
		24045, // Zaken's Treasure Chest
		24046, // Floating Eye Seer
		24047, // Floating Eye Seer
		24048, // Immortal Spirit
		24049, // Immortal Spirit
		24050, // Starving Spirit
		24051, // Starving Spirit
		24052, // Starving Soldier
		24053, // Starving Soldier
		24054, // Starving Warrior
		24055 // Starving Warrior
	};
	
	// Items
	private static final int MAGICAL_TABLET = 90045;
	private static final int SPELLBOOK_GOLDEN_LION = 90038;
	private static final int SPELLBOOK_PEGASUS = 90039;
	private static final int SPELLBOOK_SABER_TOOTH_COUGAR = 90040;
	private static final int SPELLBOOK_KUKURU = 90041;
	private static final int SPELLBOOK_BLACK_BEAR = 90042;
	private static final int SPELLBOOK_GRIFFIN = 91946;
	
	// Etc
	private static final String A_LIST = "A_LIST";
	private static final String B_LIST = "B_LIST";
	
	public _10673_SagaOfLegend()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(ORVEN);
		addTalkId(ORVEN);
		addKillNpcWithLog(2, NpcString.ORVENS_REQUEST.getId(), A_LIST, 700, MOBS1);
		addKillNpcWithLog(3, NpcString.ORVENS_REQUEST.getId(), B_LIST, 700, MOBS2);
		addLevelCheck("bad_level_class.htm", 76);
		addClassLevelCheck("bad_level_class.htm", ClassLevel.SECOND);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		switch (event)
		{
			case "orven_q10673_02.htm":
			{
				st.setCond(1);
				break;
			}
			case "orven_q10673_04.htm":
			{
				st.setCond(2);
				break;
			}
			case "orven_q10673_06.htm":
			{
				st.setCond(3);
				break;
			}
			case "orven_q10673_09.htm":
			{
				st.giveItems(MAGICAL_TABLET, 10);
				switch (player.getRace())
				{
					case HUMAN:
					{
						st.giveItems(SPELLBOOK_GOLDEN_LION, 1);
						break;
					}
					case ELF:
					{
						st.giveItems(SPELLBOOK_PEGASUS, 1);
						break;
					}
					case DARKELF:
					{
						st.giveItems(SPELLBOOK_SABER_TOOTH_COUGAR, 1);
						break;
					}
					case ORC:
					{
						st.giveItems(SPELLBOOK_BLACK_BEAR, 1);
						break;
					}
					case DWARF:
					{
						st.giveItems(SPELLBOOK_KUKURU, 1);
						break;
					}
					case KAMAEL:
					{
						st.giveItems(SPELLBOOK_GRIFFIN, 1);
						break;
					}
				}
				st.finishQuest();
				player.sendClassChangeAlert();
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
			case ORVEN:
			{
				switch (cond)
				{
					case 0:
					{
						return "orven_q10673_01.htm";
					}
					case 1:
					{
						return "orven_q10673_03.htm";
					}
					case 2:
					{
						return "orven_q10673_05.htm";
					}
					case 3:
					{
						return "orven_q10673_07.htm";
					}
					case 4:
					{
						return "orven_q10673_08.htm";
					}
				}
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
			qs.setCond(4);
		}
		else if ((qs.getCond() == 3) && updateKill(npc,qs))
		{
			qs.unset(B_LIST);
			qs.setCond(4);
		}
		else
			{
				qs.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
