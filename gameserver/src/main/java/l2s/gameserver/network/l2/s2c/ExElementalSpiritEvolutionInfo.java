package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.templates.elemental.ElementalEvolution;
import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author Bonux
**/
public class ExElementalSpiritEvolutionInfo extends L2GameServerPacket
{
	private final int _elementId;
	private final Elemental _elemental;
	private final boolean _canRise;
	private final int _currentEvolutionId;
	private final int _nextEvolutionId;

	public ExElementalSpiritEvolutionInfo(Player player, int elementId)
	{
		_elementId = elementId;
		_elemental = player.getElementalList().get(elementId);
		if(_elemental != null)
		{
			_currentEvolutionId = _elemental.getEvolution().getId();

			ElementalEvolution nextEvolution = _elemental.getTemplate().getEvolution(_elemental.getEvolutionLevel() + 1);
			if(nextEvolution != null)
			{
				_canRise = true;
				_nextEvolutionId = nextEvolution.getId();
			}
			else
			{
				_canRise = false;
				_nextEvolutionId = 0;
			}
		}
		else
		{
			_canRise = false;
			_currentEvolutionId = 0;
			_nextEvolutionId = 0;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_elementId);
		writeD(_currentEvolutionId);
		writeD(_canRise);
		if(_canRise)
		{
			writeD(_nextEvolutionId);
			writeF(100); // Required percents?!?

			List<ItemData> riseLevelCost = _elemental.getEvolution().getRiseLevelCost();
			writeD(riseLevelCost.size()); // Elementals Count
			for(ItemData item : riseLevelCost)
			{
				writeD(item.getId()); // Item ID
				writeQ(item.getCount()); // Item Count
			}
		}
	}
}