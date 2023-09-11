package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public class ElementalTeacherInstance extends NpcInstance
{
	public ElementalTeacherInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showMainChatWindow(Player player, boolean firstTalk, Object... replace)
	{
		showChatWindow(player, "default/ws_human001.htm", firstTalk);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -20170726)
		{
			if(!Config.ELEMENTAL_SYSTEM_ENABLED)
				return;

			if(reply == 2) // Улучшить Ожерелье Духов
			{
				MultiSellHolder.getInstance().SeparateAndSend(606, player, 0);
			}
			else if(reply == 3) // Узнать о Духах стихий
			{
				if(player.getClassLevel().ordinal() < ClassLevel.SECOND.ordinal())
				{
					showChatWindow(player, "default/ws_human002.htm", false);
				}
				else
				{
					showChatWindow(player, "default/ws_human004.htm", false);
				}
			}
			else if(reply == 5) // Получить Агатиона Духа
			{
				MultiSellHolder.getInstance().SeparateAndSend(607, player, 0);
			}
			else if(reply == 6) // Использовать Фрагмент Стихии
			{
				MultiSellHolder.getInstance().SeparateAndSend(608, player, 0);
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
}