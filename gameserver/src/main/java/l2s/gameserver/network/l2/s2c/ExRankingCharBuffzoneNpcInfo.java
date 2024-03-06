package l2s.gameserver.network.l2.s2c;

import java.util.concurrent.TimeUnit;

import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.network.l2.ServerPacketOpcodes;

/**
 * @author nexvill
 */
public class ExRankingCharBuffzoneNpcInfo extends L2GameServerPacket 
{
	private final int remainedCooltime;

	public ExRankingCharBuffzoneNpcInfo(int remainedCooltime) {
		this.remainedCooltime = remainedCooltime;
	}

	@Override
	protected void writeImpl() {
		writeD(remainedCooltime);
	}
}
