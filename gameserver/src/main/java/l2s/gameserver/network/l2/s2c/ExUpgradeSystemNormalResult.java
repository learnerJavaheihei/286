package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 21.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExUpgradeSystemNormalResult extends L2GameServerPacket {
	public static final ExUpgradeSystemNormalResult FAIL = new ExUpgradeSystemNormalResult();

	private final int upgradeId;
	private final int result;
	private final boolean success;
	private final Collection<ItemInfo> items;

	public ExUpgradeSystemNormalResult(int upgradeId, boolean success, Collection<ItemInfo> items) {
		this.upgradeId = upgradeId;
		this.result = 1;
		this.success = success;
		this.items = items;
	}

	protected ExUpgradeSystemNormalResult() {
		this.upgradeId = 0;
		this.result = 0;
		this.success = false;
		this.items = Collections.emptyList();
	}

	@Override
	protected final void writeImpl() {
		writeH(result); //Result
		writeD(upgradeId);   //ResultID
		writeC(success);   //IsSuccess
		int unk = 0;
		writeD(unk);   //Count
		if (unk <= 0) {
			writeC(0);   //IsBonus
			writeD(items.size());
			for (ItemInfo item : items) {
				writeD(item.getObjectId());   //ItemServerID
				writeD(item.getItemId());   //ItemClassID
				writeD(item.getEnchantLevel());   //ItemEnchant
				writeD((int) item.getCount());   //ItemCount
			}
		} else {
			for (ItemInfo item : items) {
				writeD(item.getObjectId());   //ItemServerID
				writeD(item.getItemId());   //ItemClassID
				writeD(item.getEnchantLevel());   //ItemEnchant
				writeD((int) item.getCount());   //ItemCount
			}
		}
	}
}