package l2s.gameserver.network.l2.s2c;

public class ExUpgradeSystemResult extends L2GameServerPacket {
	private final int result;
	private final int objectId;

	public ExUpgradeSystemResult(int result, int objectId) {
		this.result = result;
		this.objectId = objectId;
	}

	@Override
	protected final void writeImpl() {
		writeH(result);
		writeD(objectId);
	}
}