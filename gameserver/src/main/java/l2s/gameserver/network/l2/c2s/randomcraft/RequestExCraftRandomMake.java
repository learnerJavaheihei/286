package l2s.gameserver.network.l2.c2s.randomcraft;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.randomcraft.ExCraftRandomMake;

/**
 * @author nexvill
 */
public class RequestExCraftRandomMake extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if (activeChar.getCraftPoints() < 1)
			return;
		if (!activeChar.getInventory().reduceAdena(Config.RANDOM_CRAFT_TRY_COMMISSION)) {
			return;
		}
		activeChar.sendPacket(new ExCraftRandomMake(activeChar));
	}
}