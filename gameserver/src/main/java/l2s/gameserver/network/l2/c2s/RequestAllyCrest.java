package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.network.l2.s2c.AllianceCrestPacket;

public class RequestAllyCrest extends L2GameClientPacket
{
	// format: cd

	private int _crestId;

	@Override
	protected boolean readImpl()
	{
		_crestId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		if(_crestId == 0)
			return;
		byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);
		if(data != null)
		{
			AllianceCrestPacket ac = new AllianceCrestPacket(_crestId, data);
			sendPacket(ac);
		}
	}
}