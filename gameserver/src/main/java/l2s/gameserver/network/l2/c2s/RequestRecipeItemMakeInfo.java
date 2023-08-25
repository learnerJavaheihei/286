package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.RecipeItemMakeInfoPacket;
import l2s.gameserver.templates.item.RecipeTemplate;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;

	/**
	 * packet type id 0xB7
	 * format:		cd
	 */
	@Override
	protected boolean readImpl()
	{
		_id = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		RecipeTemplate recipe = RecipeHolder.getInstance().getRecipeByRecipeId(_id);
		if(recipe == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		sendPacket(new RecipeItemMakeInfoPacket(activeChar, recipe, 0xffffffff));
	}
}