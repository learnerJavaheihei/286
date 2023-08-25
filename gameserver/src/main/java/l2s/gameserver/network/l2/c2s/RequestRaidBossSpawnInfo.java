package l2s.gameserver.network.l2.c2s;

import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import l2s.gameserver.network.l2.s2c.ExRaidBossSpawnInfo;

/**
 * @author Bonux
**/
public class RequestRaidBossSpawnInfo extends L2GameClientPacket
{
	private int _count;
	private IntSet _ids = new HashIntSet();
	@Override
	protected boolean readImpl()
	{
		_count = readD();
		for (int i = 0; i < _count; i++)
		{
			_ids.add(readD());
		}
		return true;
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new ExRaidBossSpawnInfo(_count, _ids));
	}
}