package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.ServerPacketOpcodes;

public class ExShowUsmPacket extends L2GameServerPacket {
	private final int _usmVideoId;

	public ExShowUsmPacket(int usmVideoId) {
		_usmVideoId = usmVideoId;
	}

	@Override
	protected void writeImpl() {
		writeD(_usmVideoId);
	}
}