package handler.items;

import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class CallPlugin extends ScriptItemHandler//內掛
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
			return false;
		 BotControlPage.mainPage((Player)playable);
		return true;
	}
}