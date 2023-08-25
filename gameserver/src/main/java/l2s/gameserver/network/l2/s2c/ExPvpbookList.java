package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Pvpbook;
import l2s.gameserver.model.actor.instances.player.PvpbookInfo;
import java.util.Collection;

public class ExPvpbookList extends L2GameServerPacket {
	private final int locationShowCount;
	private final int teleportCount;
	private final Collection<PvpbookInfo> pvpbookInfos;

	public ExPvpbookList(Player player) {
		Pvpbook pvpbook = player.getPvpbook();
		locationShowCount = pvpbook.getLocationShowCount();
		teleportCount = pvpbook.getTeleportCount();
		pvpbookInfos = pvpbook.getInfos(false);
	}

	@Override
	public void writeImpl() {
		writeD(locationShowCount); // Show locations count
		writeD(teleportCount); // Teleports count
		writeD(pvpbookInfos.size());
		for (PvpbookInfo pvpbookInfo : pvpbookInfos) {
			writeString(pvpbookInfo.getName()); // Char name
			writeString(pvpbookInfo.getClanName()); // Clan name
			writeD(pvpbookInfo.getLevel()); // Level
			writeD(0x01); // UNK
			writeD(pvpbookInfo.getClassId()); // Class ID
			writeD(pvpbookInfo.getDeathTime()); // Death time
			writeC(pvpbookInfo.isOnline()); // Online
		}
	}
}