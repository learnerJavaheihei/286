package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public class NewbieGuideInstance extends NpcInstance
{
	private static final int NEWBIE_GUIDE_HUMAN = 30598; // NPC: Гид Новичков
	private static final int NEWBIE_GUIDE_ELF = 30599; // NPC: Гид Новичков
	private static final int NEWBIE_GUIDE_DARK_ELF = 30600; // NPC: Гид Новичков
	private static final int NEWBIE_GUIDE_DWARVEN = 30601; // NPC: Гид Новичков
	private static final int NEWBIE_GUIDE_ORC = 30602; // NPC: Гид Новичков
	private static final int NEWBIE_GUIDE_KAMAEL = 34110; // NPC: Гид Новичков

	public NewbieGuideInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
			showChatWindow(player, "default/" + getHtmlName() + "001.htm", false);
		else
			super.showChatWindow(player, val, firstTalk, replace);
	}

	private String getHtmlName()
	{
		switch (getNpcId()) {
			case NEWBIE_GUIDE_HUMAN:
				return "guide_human_cnacelot";
			case NEWBIE_GUIDE_ELF:
				return "guide_elf_roios";
			case NEWBIE_GUIDE_DARK_ELF:
				return "guide_delf_frankia";
			case NEWBIE_GUIDE_DWARVEN:
				return "guide_dwarf_gullin";
			case NEWBIE_GUIDE_ORC:
				return "guide_orc_tanai";
			case NEWBIE_GUIDE_KAMAEL:
				return "guide_kamael_city";
		}
		return null;
	}
}