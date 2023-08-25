package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeUseItem extends L2GameServerPacket {
	private final boolean success;
	private final int costumeId;

	public ExCostumeUseItem(boolean success, int costumeId) {
		this.success = success;
		this.costumeId = costumeId;
	}

	@Override
	protected void writeImpl() {
		writeC(success);    //IsSuccess
		writeD(costumeId);  //CostumeId
	}
}