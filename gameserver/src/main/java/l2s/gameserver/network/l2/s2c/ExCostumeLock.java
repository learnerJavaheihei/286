package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeLock extends L2GameServerPacket {
	public static final ExCostumeLock FAIL = new ExCostumeLock(0, 0, false);

	private final int result;
	private final int costumeId;
	private final boolean locked;

	public ExCostumeLock(int result, int costumeId, boolean locked) {
		this.result = result;
		this.costumeId = costumeId;
		this.locked = locked;
	}

	@Override
	protected void writeImpl() {
		writeC(result);   //Result
		writeD(costumeId);   //CostumeID
		writeC(locked);   //LockState
	}
}