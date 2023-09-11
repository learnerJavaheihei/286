package npc.model;

import instances.Frintezza;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class ImperialTombGhostInstance extends NpcInstance
{
	private static final int HIGH_GRADE_XP_SCROLL_TICKET = 90947;	// Купон на Свиток Опыта Высокого Ранга
	//private static final int MAGICAL_TABLET = 90045;	// Магическая Табличка

	public ImperialTombGhostInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public String getHtmlFilename(int val, Player player)
	{
		String filename;
		if(val == 0)
		{
			Reflection reflection = getReflection();
			if(reflection instanceof Frintezza)
			{
				Frintezza frintezza = (Frintezza) reflection;
				if(frintezza.isRewardReceived(player))
					filename = getNpcId() + "-received.htm";
				else
					filename = getNpcId() + ".htm";
			}
			else
				filename = super.getHtmlFilename(val, player);
		}
		else
			filename = getNpcId() + "-" + val + ".htm";

		return filename;
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -7001)
		{
			Reflection reflection = getReflection();
			if(!(reflection instanceof Frintezza))
				return;

			if(reply == 1)
			{
				Frintezza frintezza = (Frintezza) reflection;
				if(frintezza.isRewardReceived(player))
				{
					showChatWindow(player, "default/" + getNpcId() + "-already_received.htm", false);
					return;
				}

				int level = player.getLevel();
				if(level >= 70 && level <= 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, Rnd.get(1, 2), true);
				}
				else if(level > 75)
				{
					ItemFunctions.addItem(player, HIGH_GRADE_XP_SCROLL_TICKET, Rnd.get(1, 4), true);
					//ItemFunctions.addItem(player, MAGICAL_TABLET, 1, true);
				}
				frintezza.setRewardReceived(player);
			}
			else if(reply == 2)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3145401, player, 0);
			}
			else if(reply == 4)
			{
				player.teleToLocation(getReflection().getReturnLoc(), ReflectionManager.MAIN);
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
}