package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.RankersAuthorityDecoy;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;

/**
 * @author nexvill
 */
public class ExRankingCharBuffzoneNpcPosition extends L2GameServerPacket 
{
	private final boolean spawned;
	private final int x, y, z;

	public ExRankingCharBuffzoneNpcPosition() {
		RankersAuthorityDecoy decoy = PlayerRankingManager.getInstance().getRankersAuthorityDecoy();
		if (decoy != null) {
			this.spawned = true;
			Location loc = decoy.getLoc();
			this.x = loc.getX();
			this.y = loc.getY();
			this.z = loc.getZ();
		} else {
			this.spawned = false;
			this.x = 0;
			this.y = 0;
			this.z = 0;
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(spawned);
		writeD(x);
		writeD(y);
		writeD(z);
	}
}
