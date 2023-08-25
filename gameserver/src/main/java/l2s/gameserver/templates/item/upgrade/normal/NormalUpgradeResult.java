package l2s.gameserver.templates.item.upgrade.normal;

import l2s.gameserver.templates.item.upgrade.UpgradeItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 02.04.2019
 * Developed for L2-Scripts.com
 **/
public class NormalUpgradeResult {
	private final NormalUpgradeResultType type;
	private final double chance;
	private final List<UpgradeItemData> items = new ArrayList<>();

	public NormalUpgradeResult(NormalUpgradeResultType type, double chance) {
		this.type = type;
		this.chance = chance;
	}

	public NormalUpgradeResultType getType() {
		return type;
	}

	public double getChance() {
		return chance;
	}

	public void addItem(UpgradeItemData item) {
		items.add(item);
	}

	public List<UpgradeItemData> getItems() {
		return items;
	}
}
