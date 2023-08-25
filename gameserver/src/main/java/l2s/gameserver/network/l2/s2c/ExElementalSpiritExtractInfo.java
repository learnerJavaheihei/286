package l2s.gameserver.network.l2.s2c;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author Bonux
**/
public class ExElementalSpiritExtractInfo extends L2GameServerPacket
{
	private final int _elementId;
	private final ItemData _extractItem;
	private final List<ItemData> _exctractCost;

	public ExElementalSpiritExtractInfo(Player player, int elementId)
	{
		_elementId = elementId;

		Elemental elemental = player.getElementalList().get(elementId);
		if(elemental != null)
		{
			_extractItem = elemental.getLevelData().getExtractItem();
			_exctractCost = elemental.getLevelData().getExtractCost();
		}
		else
		{
			_extractItem = null;
			_exctractCost = Collections.emptyList();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_elementId); // Value received from client (RequestExElementalSpiritExtractInfo)
		writeC(0x01);	// UNK
		writeC(_exctractCost.size()); // Items Count
		for(ItemData costItem : _exctractCost)
		{
			writeD(costItem.getId()); // Item ID
			writeD((int) costItem.getCount()); // Item Count
		}

		// Result Item
		if(_extractItem != null)
		{
			writeD(_extractItem.getId()); // Item ID
			writeD((int) _extractItem.getCount()); // Item Count
		}
		else
		{
			writeD(0); // Item ID
			writeD(0); // Item Count
		}
	}
}