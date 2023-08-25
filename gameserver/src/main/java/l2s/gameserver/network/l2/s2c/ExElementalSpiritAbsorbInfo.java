package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.templates.elemental.ElementalAbsorbItem;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class ExElementalSpiritAbsorbInfo extends L2GameServerPacket
{
	private final int _unk;
	private final int _elementId;
	private final Elemental _elemental;
	private final List<ElementalAbsorbItem> _absorbItems;

	public ExElementalSpiritAbsorbInfo(Player player, int unk, int elementId)
	{
		_unk = unk;
		_elementId = elementId;
		_elemental = player.getElementalList().get(elementId);

		if(_elemental != null)
		{
			_absorbItems = new ArrayList<ElementalAbsorbItem>();
			for(ElementalAbsorbItem item : _elemental.getTemplate().getAbsorbItems())
			{
				_absorbItems.add(new ElementalAbsorbItem(item.getId(), ItemFunctions.getItemCount(player, item.getId()), item.getPower()));
			}
		}
		else
			_absorbItems = Collections.emptyList();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_unk); // Value received from client (RequestExElementalSpiritAbsorbInfo)
		writeC(_elementId); // Element ID
		if(_elemental != null)
		{
			writeC(_elemental.getEvolutionLevel()); // Unk
			writeQ(_elemental.getExp()); // Current Exp
			writeQ(_elemental.getMaxExp()); // Min Exp For Current Level
			writeQ(_elemental.getEvolution().getMaxExp()); // Max Exp For Current Level
			writeD(_elemental.getLevel()); // Level 1-10
			writeD(_elemental.getEvolution().getMaxLevel()); // Max Level

			writeD(_absorbItems.size()); // Elementals Count
			for(ElementalAbsorbItem item : _absorbItems)
			{
				writeD(item.getId()); // Item ID
				writeD((int) item.getCount()); // Item Count
				writeD(item.getPower()); // Exp Per Item
			}
		}
		else
		{
			writeC(0); // Unk
			writeQ(0); // Current Exp
			writeQ(0); // Min Exp For Current Level
			writeQ(0); // Max Exp For Current Level
			writeD(0); // Level 1-10
			writeD(0); // Max Level
			writeD(0); // Elementals Count
		}
	}
}