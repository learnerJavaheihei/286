package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author nexvill
 */
public class RequestExResetStatusBonus extends L2GameClientPacket
{
	private static final int MONEY_L = 91663;
	@Override
	protected boolean readImpl()
	{
		readC(); // unk 0
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		int adenaCount = 200_000;
		int moneyCount = 200;
		
		if (activeChar.usedStatPoints() > 25)
		{
			adenaCount = 10_000_000;
			moneyCount = 700;
		}
		else if (activeChar.usedStatPoints() > 20)
		{
			adenaCount = 5_000_000;
			moneyCount = 600;
		}
		else if (activeChar.usedStatPoints() > 15)
		{
			adenaCount = 2_000_000;
			moneyCount = 500;
		}
		else if (activeChar.usedStatPoints() > 10)
		{
			adenaCount = 1_000_000;
			moneyCount = 400;
		}
		else if (activeChar.usedStatPoints() > 5)
		{
			adenaCount = 500_000;
			moneyCount = 300;
		}
		
		if (activeChar.getAdena() < adenaCount)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		if (activeChar.getInventory().getItemByItemId(MONEY_L).getCount() < moneyCount)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}
		
		boolean success = activeChar.getInventory().destroyItemByItemId(MONEY_L, moneyCount);
		success = success && activeChar.getInventory().destroyItemByItemId(57, adenaCount);
		
		if (success)
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S2_S1_HAS_DISAPPEARED).addLong(adenaCount).addItemName(57));
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S2_S1_HAS_DISAPPEARED).addLong(moneyCount).addItemName(MONEY_L));
			activeChar.setStatBonus(0, 0);
			activeChar.setStatBonus(1, 0);
			activeChar.setStatBonus(2, 0);
			activeChar.setStatBonus(3, 0);
			activeChar.setStatBonus(4, 0);
			activeChar.setStatBonus(5, 0);
			activeChar.setVar("elixirs_used", 0);
			activeChar.broadcastUserInfo(true);
		}
	}
}