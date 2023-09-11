package npc.model.events;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class ChristmasSquashManagerInstance extends NpcInstance
{
	private static final SkillEntry GIFT_EFFECT_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 106, 1);	// Новогодний Праздник

	private static final int FEE_ITEM_ID = 37543;	// Нектар Снежной Тыквы
	private static final long FEE_ITEM_COUNT = 100;

	public ChristmasSquashManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -1000)
		{
			if(reply == 1)
			{
				if(!ItemFunctions.deleteItem(player, FEE_ITEM_ID, FEE_ITEM_COUNT))
				{
					showChatWindow(player, "events/christmas_squash/" + getNpcId() + "-no_have_nectar.htm", false);
					return;
				}
				GIFT_EFFECT_SKILL.getEffects(player, player);
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "events/christmas_squash/";
	}
}