package l2s.gameserver.botscript.bypasshandler;

import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

public class BotPage
{
	@Bypass(value = "bot.page")
	public void page(Player player, NpcInstance npc, String[] param)
	{
		if("main".equals(param[0]))
		{
			BotControlPage.mainPage(player);
		}
		else if("fight".equals(param[0]))
		{
			BotControlPage.fightPage(player);
		}
		else if("skilledit".equals(param[0]))
		{
			Integer skillId = Integer.parseInt(param[1]);
			BotControlPage.skillPage(player, skillId);
		}
		else if("path".equals(param[0]))
		{
			BotControlPage.pathPage(player);
		}
		else if("protect".equals(param[0]))
		{
			BotControlPage.protectPage(player);
		}
		else if("itemuse".equals(param[0]))
		{
			BotControlPage.itemUsePage(player);
		}
		else if("rest".equals(param[0]))
		{
			BotControlPage.restPage(player);
		}
		else if("pet".equals(param[0]))
		{
			BotControlPage.petPage(player);
		}
		else if("party".equals(param[0]))
		{
			BotControlPage.party(player,param);
		}
	}
}
