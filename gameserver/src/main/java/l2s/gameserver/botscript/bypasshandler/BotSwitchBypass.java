package l2s.gameserver.botscript.bypasshandler;

import l2s.gameserver.core.BotEngine;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

public class BotSwitchBypass
{
	@Bypass(value = "bot.start")
	public void start(Player player, NpcInstance npc, String[] param)
	{
		if(!player.isActive())
		{
			player.setActive();
		}
		BotEngine.getInstance().startBotTask(player);
	}

	@Bypass(value = "bot.stop")
	public void stop(Player player, NpcInstance npc, String[] param)
	{
		BotEngine.getInstance().getBotConfig(player).setAbort(true, "\u624b\u52a8\u505c\u6b62");
		BotEngine.getInstance().stopTask(player);
		/*\u624b\u52a8\u505c\u6b62 手动停止*/
	}
}