package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

/**
 * @author VISTALL
 */
public class ExShowUpgradeSystemNormal extends L2GameServerPacket
{
	private final int type;

	public ExShowUpgradeSystemNormal(int type)
	{
		this.type = type;
	}

	@Override
	protected void writeImpl()
	{
		writeH(0x01);	// unk, maybe type
		writeH(type);	// unk, maybe type
		writeH(100);	// unk, maybe chance
		writeD(0x00);	// unk
		writeD(0x00);	// unk
	}
}