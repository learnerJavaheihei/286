package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExChooseCostumeItem extends L2GameServerPacket {
	private final int itemId;

	public ExChooseCostumeItem(int itemId) {
		this.itemId = itemId;
	}

	@Override
	protected void writeImpl() {
		writeD(itemId); //ItemClassID*/
	}
}