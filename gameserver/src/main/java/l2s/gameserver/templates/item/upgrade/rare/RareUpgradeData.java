package l2s.gameserver.templates.item.upgrade.rare;

import l2s.gameserver.templates.item.upgrade.UpgradeItemData;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 02.04.2019
 * Developed for L2-Scripts.com
 **/
public class RareUpgradeData {
	private final int id;
	private final int itemId;
	private final int enchantLevel;
	private final long price;
	private final int[] locationId;
	private final int resultItemId;
	private final long resultItemCount;
	private final int resultItemEnchant;

	private final List<UpgradeItemData> requiredItems = new ArrayList<>();
	private final List<Integer> unkData = new ArrayList<>();

	public RareUpgradeData(int id, int itemId, int enchantLevel, long price, int[] locationId, int resultItemId, long resultItemCount, int resultItemEnchant) {
		this.id = id;
		this.itemId = itemId;
		this.enchantLevel = enchantLevel;
		this.price = price;
		this.locationId = locationId;
		this.resultItemId = resultItemId;
		this.resultItemCount = resultItemCount;
		this.resultItemEnchant = resultItemEnchant;
	}

	public int getId() {
		return id;
	}

	public int getItemId() {
		return itemId;
	}

	public int getEnchantLevel() {
		return enchantLevel;
	}

	public long getPrice() {
		return price;
	}

	public int[] getLocationId() {
		return locationId;
	}

	public int getResultItemId() {
		return resultItemId;
	}

	public long getResultItemCount() {
		return resultItemCount;
	}

	public int getResultItemEnchant() {
		return resultItemEnchant;
	}

	public void addRequiredItem(UpgradeItemData item) {
		requiredItems.add(item);
	}

	public List<UpgradeItemData> getRequiredItems() {
		return requiredItems;
	}

	public void addUnkData(int data) {
		unkData.add(data);
	}

	public List<Integer> getUnkData() {
		return unkData;
	}
}
