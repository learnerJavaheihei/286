package l2s.gameserver.network.l2.s2c;

public class ExPvpbookNewPk extends L2GameServerPacket {
	private final String killerName;

	public ExPvpbookNewPk(String killerName) {
		this.killerName = killerName;
	}

	@Override
	public void writeImpl() {
		writeString(killerName);
	}
}
