package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.PledgeBonusUtils;

/**
 * @author Bonux
**/
public class ExPledgeBonusList extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x00);	// Reward Type (0 - Skill, 1 - Item)
		writeD(PledgeBonusUtils.ATTENDANCE_REWARDS.get(1));    // Login bonus (.25)
		writeD(PledgeBonusUtils.ATTENDANCE_REWARDS.get(2));    // Login bonus (.5)
		writeD(PledgeBonusUtils.ATTENDANCE_REWARDS.get(3));    // Login bonus (.75)
		writeD(PledgeBonusUtils.ATTENDANCE_REWARDS.get(4));    // Login bonus (max)
		writeC(0x01);	// Reward Type (0 - Skill, 1 - Item)
		writeD(PledgeBonusUtils.HUNTING_REWARDS.get(1));    // Hunting bonus (.25)
		writeD(PledgeBonusUtils.HUNTING_REWARDS.get(2));    // Hunting bonus (.5)
		writeD(PledgeBonusUtils.HUNTING_REWARDS.get(3));    // Hunting bonus (.75)
		writeD(PledgeBonusUtils.HUNTING_REWARDS.get(4));    // Hunting bonus (max)
	}
}