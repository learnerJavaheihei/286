package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.templates.elemental.ElementalLevelData;

/**
 * @author Bonux
**/
public class ExElementalSpiritInfo extends L2GameServerPacket
{
	private final int _unk, _activeElementId;
	private final Collection<Elemental> _elementals;

	public ExElementalSpiritInfo(Player player, int unk, int activeElementId)
	{
		_unk = unk;
		_activeElementId = activeElementId;
		_elementals = player.getElementalList().values();
	}

	public ExElementalSpiritInfo(Player player, int unk)
	{
		this(player, unk, player.getActiveElement().getId());
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_unk); // Value received from client (RequestExElementalSpiritInfo) Maybe canOpen?
		writeC(_activeElementId);	// Active Element ID
		writeC(_elementals.size()); // Elementals Count
		for(Elemental elemental : _elementals)
		{
			writeC(elemental.getElementId()); //  Elemental ID

			int evolutionLevel = elemental.getEvolutionLevel();
			writeC(evolutionLevel > 0);
			if(evolutionLevel > 0)
			{
				writeC(evolutionLevel); // Evolution Level (1-3)
				writeD(elemental.getEvolution().getId()); // Evolution ID from client
				writeQ(elemental.getExp()); // Current Exp
				writeQ(elemental.getMaxExp()); // Max Exp for this level
				writeQ(elemental.getEvolution().getMaxExp()); // Max Exp for this evolution
				writeD(elemental.getLevel()); // Level 1-10
				writeD(elemental.getEvolution().getMaxLevel()); // Max Level
				writeD(elemental.getAvailablePoints()); // Available Points
				writeD(elemental.getAttackPoints()); // Current Attack Points
				writeD(elemental.getDefencePoints()); // Current Defence Points
				writeD(elemental.getCritRatePoints()); // Current Crit Rate
				writeD(elemental.getCritAttackPoints()); // Current Crit Damage
				writeD(elemental.getEvolution().getMaxAttackPoints()); // Max Attack Points
				writeD(elemental.getEvolution().getMaxDefencePoints()); // Max Defence Points
				writeD(elemental.getEvolution().getMaxCritRatePoints()); // Max Crit Rate
				writeD(elemental.getEvolution().getMaxCritAttackPoints()); // Max Crit Damage

				ElementalLevelData[] datas = elemental.getEvolution().getLevelDatas();
				writeC(datas.length);
				for(ElementalLevelData data : datas)
				{
					writeH(data.getLevel()); // Level
					writeQ(data.getExp()); // EXP
				}
			}
		}
		writeD(1); // UNK
		writeD(Config.ELEMENTAL_RESET_POINTS_ITEM_ID); // Stats reset cost Item ID
		writeQ(Config.ELEMENTAL_RESET_POINTS_ITEM_COUNT);	// Stats reset cost
	}
}