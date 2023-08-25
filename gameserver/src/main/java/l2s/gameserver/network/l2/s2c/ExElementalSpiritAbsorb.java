package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.templates.elemental.ElementalLevelData;

/**
 * @author Bonux
**/
public class ExElementalSpiritAbsorb extends L2GameServerPacket
{
	private final boolean _success;
	private final Elemental _elemental;

	public ExElementalSpiritAbsorb(Player player, boolean success, int elementId)
	{
		_success = success;
		_elemental = player.getElementalList().get(elementId);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_success); // Result
		writeC(_elemental.getElementId()); //  Elemental ID

		int evolutionLevel = _elemental.getEvolutionLevel();
		writeC(evolutionLevel > 0);
		if(evolutionLevel > 0)
		{
			writeC(evolutionLevel); // Evolution Level (1-3)
			writeD(_elemental.getEvolution().getId()); // Evolution ID from client
			writeQ(_elemental.getExp()); // Current Exp
			writeQ(_elemental.getMaxExp()); // Min Exp For Current Level
			writeQ(_elemental.getEvolution().getMaxExp()); // Max Exp For Current Level
			writeD(_elemental.getLevel()); // Level 1-10
			writeD(_elemental.getEvolution().getMaxLevel()); // Max Level
			writeD(_elemental.getAvailablePoints()); // Available Points
			writeD(_elemental.getAttackPoints()); // Current Attack Points
			writeD(_elemental.getDefencePoints()); // Current Defence Points
			writeD(_elemental.getCritRatePoints()); // Current Crit Rate
			writeD(_elemental.getCritAttackPoints()); // Current Crit Damage
			writeD(_elemental.getEvolution().getMaxAttackPoints()); // Max Attack Points
			writeD(_elemental.getEvolution().getMaxDefencePoints()); // Max Defence Points
			writeD(_elemental.getEvolution().getMaxCritRatePoints()); // Max Crit Rate
			writeD(_elemental.getEvolution().getMaxCritAttackPoints()); // Max Crit Damage

			ElementalLevelData[] datas = _elemental.getEvolution().getLevelDatas();
			writeC(datas.length);
			for(ElementalLevelData data : datas)
			{
				writeH(data.getLevel()); // Level
				writeQ(data.getExp()); // EXP
			}
		}
	}
}