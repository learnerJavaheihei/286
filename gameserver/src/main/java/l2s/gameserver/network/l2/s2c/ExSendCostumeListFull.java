package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExSendCostumeListFull extends L2GameServerPacket {
	public ExSendCostumeListFull() {
	}

	@Override
	protected void writeImpl() {
		/*"d" //CostumeListSize
		if (CostumeListSize <= 0) {
			"d" //CostumeShortCutSize
			"d" //CostumeCollectID
			"d" //CollectReuseCooltime
			for (CostumeShortCutSize) {
				"d" //Page
				"d" //SlotIndex
				"d" //?????
			}
		} else {
			for (CostumeListSize) {
				"d" //CostumeID
				"Q" //Amount
				"c" //LockState
				"c" //ChangedType
			}
		}*/
	}
}