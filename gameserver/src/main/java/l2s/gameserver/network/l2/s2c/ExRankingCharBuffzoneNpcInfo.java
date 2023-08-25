package l2s.gameserver.network.l2.s2c;

import java.util.concurrent.TimeUnit;

import l2s.gameserver.instancemanager.ServerVariables;

/**
 * @author nexvill
 */
public class ExRankingCharBuffzoneNpcInfo extends L2GameServerPacket 
{

	public ExRankingCharBuffzoneNpcInfo() 
	{
	}

	@Override
	public void writeImpl() 
	{
		long minsToReset = ServerVariables.getLong("buffNpcReset", 0);
		long diff = minsToReset - System.currentTimeMillis();
		if (diff <= 0)
			writeD(0);
		else
			writeD((int) TimeUnit.MILLISECONDS.toSeconds(diff));
	}
}
