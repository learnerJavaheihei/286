package l2s.gameserver.network.l2.s2c.olympiad;

import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExOlympiadInfo extends L2GameServerPacket
{
	public ExOlympiadInfo()
	{
		
	}

	@Override
	protected void writeImpl() {
		if (Olympiad._inCompPeriod)
			writeD((int) (Olympiad.getMillisToCompEnd() / 4)); // koreans has 250ms in 1s xD
		else
			writeD(0);
		writeC(0);
		writeC(Olympiad.getCurrentCycle());
	}
}
