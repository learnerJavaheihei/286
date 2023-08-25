package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritAbsorb;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritAbsorbInfo;
import l2s.gameserver.templates.elemental.ElementalAbsorbItem;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class RequestExElementalSpiritAbsorb extends L2GameClientPacket
{
	private int _elementId;
	private List<ItemData> _consumeItems;

	@Override
	protected boolean readImpl()
	{
		_elementId = readC();

		int itemsCount = readD();
		_consumeItems = new ArrayList<ItemData>(itemsCount);
		for(int i = 0; i < itemsCount; i++)
		{
			int itemId = readD();
			long itemCount = readD();
			_consumeItems.add(new ItemData(itemId, itemCount));
		}
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
			activeChar.sendPacket(new ExElementalSpiritAbsorb(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritAbsorbInfo(activeChar, 0, _elementId));
			activeChar.sendActionFailed();
			return;
		}

		List<ElementalAbsorbItem> absorbItems = new ArrayList<ElementalAbsorbItem>();
		for(ItemData item : _consumeItems)
		{
			if(item.getCount() > 0)
			{
				ElementalAbsorbItem absorbItem = elemental.getTemplate().getAbsorbItem(item.getId());
				if(absorbItem != null)
					absorbItems.add(new ElementalAbsorbItem(item.getId(), item.getCount(), absorbItem.getPower()));
			}
		}

		if(absorbItems.isEmpty())
		{
			activeChar.sendPacket(new ExElementalSpiritAbsorb(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritAbsorbInfo(activeChar, 0, _elementId));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(new ExElementalSpiritAbsorb(activeChar, false, _elementId));
			activeChar.sendPacket(new ExElementalSpiritAbsorbInfo(activeChar, 0, _elementId));
			activeChar.sendPacket(SystemMsg.UNABLE_TO_ABSORB_DURING_BATTLE);
			return;
		}

		long addExp = 0L;

		activeChar.getInventory().writeLock();
		try
		{
			for(ElementalAbsorbItem absorbItem : absorbItems)
			{
				long count = Math.min(absorbItem.getCount(), ItemFunctions.getItemCount(activeChar, absorbItem.getId()));
				if(ItemFunctions.deleteItem(activeChar, absorbItem.getId(), count, true))
				{
					addExp += absorbItem.getPower() * count;
				}
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		elemental.addExp(addExp);

		activeChar.sendPacket(new ExElementalSpiritAbsorb(activeChar, true, _elementId));
		activeChar.sendPacket(new ExElementalSpiritAbsorbInfo(activeChar, 0, _elementId));
	}
}