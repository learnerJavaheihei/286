package l2s.gameserver.network.l2.s2c.olympiad;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExOlympiadMatchMakingResult extends L2GameServerPacket
{
	private boolean _success;
	
	public ExOlympiadMatchMakingResult(boolean success)
	{
		_success = success;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_success)
		{
			writeC(1);
			writeC(1);
		}
		else
		{
			writeC(0);
			writeC(1);
		}
	}
}