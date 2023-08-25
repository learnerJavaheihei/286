package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.ExShowAgitSiegeInfo;

/**
 * @author Bonux
**/
public class RequestShowAgitSiegeInfo extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new ExShowAgitSiegeInfo());
	}
}