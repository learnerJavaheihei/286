package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.ServerPacketOpcodes;

/**
 * @author VISTALL
 */
public class ExShowUpgradeSystem extends L2GameServerPacket {
	private final int unk;

	public ExShowUpgradeSystem(int unk) {
		this.unk = unk;
	}

	@Override
	protected void writeImpl() {
		writeH(unk);    // unk, maybe type
		writeH(100); // price percent
		writeD(0x00); // unk
		writeD(0x00); // unk
	}
}
