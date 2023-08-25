package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Costume;
import l2s.gameserver.model.actor.instances.player.CostumeList;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExSendCostumeList extends L2GameServerPacket {
	private final Player player;
	private final CostumeList costumeList;

	public ExSendCostumeList(Player player) {
		this.player = player;
		costumeList = player.getCostumeList();
	}

	@Override
	protected void writeImpl() {
		writeD(costumeList.size()); //CostumeListSize
		for (Costume costume : costumeList) {
			writeD(costume.getId()); //CostumeID
			writeQ(costume.getCount()); //Amount
			writeC(costume.isFlag(Costume.IS_LOCKED)); //LockState 0 - Unlocked, 1 - Locked
			writeC(costume.isFlag(Costume.IS_NEW)); //ChangedType 0 - Normal, 1 - New,
		}
	}
}