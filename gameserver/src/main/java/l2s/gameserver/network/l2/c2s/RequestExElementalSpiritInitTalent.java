package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritSetTalent;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;

public class RequestExElementalSpiritInitTalent extends L2GameClientPacket
{
	private int _elementId;

	@Override
	protected boolean readImpl()
	{
		_elementId = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Elemental elemental = activeChar.getElementalList().get(_elementId);
		if(elemental == null || elemental.getUsedPoints() <= 0)
		{
			activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
			activeChar.sendPacket(SystemMsg.UNABLE_TO_RESET_SPIRIT_ATTRIBUTE_DURING_BATTLE);
			return;
		}

		if(Config.ELEMENTAL_RESET_POINTS_ITEM_ID > 0 && Config.ELEMENTAL_RESET_POINTS_ITEM_COUNT > 0)
		{
			if(!ItemFunctions.deleteItem(activeChar, Config.ELEMENTAL_RESET_POINTS_ITEM_ID, Config.ELEMENTAL_RESET_POINTS_ITEM_COUNT, true))
			{
				activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
				if(Config.ELEMENTAL_RESET_POINTS_ITEM_ID == ItemTemplate.ITEM_ID_ADENA)
					activeChar.sendPacket(SystemMsg.NOT_ENOUGH_ADENA_TO_PROCEED_WITH_RESET);
				else
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				return;
			}
		}

		elemental.resetPoints();

		activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, true, _elementId));
		activeChar.sendPacket(SystemMsg.RESET_THE_SELECTED_SPIRITS_CHARACTERISTICS_SUCCESSFULLY);
	}
}