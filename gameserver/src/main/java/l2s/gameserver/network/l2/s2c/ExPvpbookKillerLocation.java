package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.network.l2.ServerPacketOpcodes;

public class ExPvpbookKillerLocation extends L2GameServerPacket {
	private final String killerName;
	private final ILocation killerLoc;

	public ExPvpbookKillerLocation(String killerName, ILocation killerLoc) {
		this.killerName = killerName;
		this.killerLoc = killerLoc;
	}

	@Override
	public void writeImpl() {
		writeString(killerName);
		writeD(killerLoc.getX());
		writeD(killerLoc.getY());
		writeD(killerLoc.getZ());
	}
}