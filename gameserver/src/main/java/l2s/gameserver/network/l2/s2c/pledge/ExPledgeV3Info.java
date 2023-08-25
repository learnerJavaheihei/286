package l2s.gameserver.network.l2.s2c.pledge;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author Eden
 */
public class ExPledgeV3Info extends L2GameServerPacket
{
	private final int points, rank;
	private final String announce;
	private final boolean isShowOnEnter;

	public ExPledgeV3Info(int points, int rank, String announce, boolean isShowOnEnter)
	{
		this.points = points;
		this.rank = rank;
		this.announce = announce;
		this.isShowOnEnter = isShowOnEnter;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(points);
		writeD(rank);
		writeString(announce);
		writeC(isShowOnEnter);
	}
}