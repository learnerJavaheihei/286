package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class ExShowUpgradeSystem extends L2GameServerPacket {
    private final int unk;

    public ExShowUpgradeSystem(int unk) {
        this.unk = unk;
    }

	@Override
	protected void writeImpl()
	{
		writeH(0x01);	// unk, maybe type
		writeH(unk);	// unk, maybe type
		writeH(100);	// unk, maybe chance
		writeD(0x00);	// unk
		writeD(0x00);	// unk
	}
}
