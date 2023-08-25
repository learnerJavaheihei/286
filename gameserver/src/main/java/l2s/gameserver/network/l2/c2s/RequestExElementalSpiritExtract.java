package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritExtractInfo;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritSetTalent;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class RequestExElementalSpiritExtract extends L2GameClientPacket
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
		if(elemental == null)
		{
			activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritExtractInfo(activeChar, _elementId));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritExtractInfo(activeChar, _elementId));
			activeChar.sendPacket(SystemMsg.UNABLE_TO_EXTRACT_DURING_BATTLE);
			return;
		}

		if(activeChar.isInventoryFull())
		{
			activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritExtractInfo(activeChar, _elementId));
			activeChar.sendPacket(SystemMsg.UNABLE_TO_EXTRACT_BECAUSE_INVENTORY_IS_FULL);
			return;
		}

		ItemData extractItem = elemental.getLevelData().getExtractItem();
		if(extractItem == null)
		{
			activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritExtractInfo(activeChar, _elementId));
			activeChar.sendActionFailed();
			return;
		}

		if(extractItem.getCount() <= 0)
		{
			activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritExtractInfo(activeChar, _elementId));
			activeChar.sendPacket(SystemMsg.NOT_ENOUGH_ATTRIBUTE_XP_FOR_EXTRACTION);
			return;
		}

		activeChar.getInventory().writeLock();
		try
		{
			for(ItemData costItem : elemental.getLevelData().getExtractCost())
			{
				if(!ItemFunctions.haveItem(activeChar, costItem.getId(), costItem.getCount()))
				{
					activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, false, _elementId));
					activeChar.sendPacket(new ExElementalSpiritExtractInfo(activeChar, _elementId));
					if(costItem.getId() == ItemTemplate.ITEM_ID_ADENA)
						activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					else
						activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
					return;
				}
			}

			elemental.setExp(0);

			for(ItemData costItem : elemental.getLevelData().getExtractCost())
				ItemFunctions.deleteItem(activeChar, costItem.getId(), costItem.getCount(), true);
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		ItemFunctions.addItem(activeChar, extractItem.getId(), extractItem.getCount(), true);

		activeChar.sendPacket(new ExElementalSpiritSetTalent(activeChar, true, _elementId));
		activeChar.sendPacket(new ExElementalSpiritExtractInfo(activeChar, _elementId));
	}
}