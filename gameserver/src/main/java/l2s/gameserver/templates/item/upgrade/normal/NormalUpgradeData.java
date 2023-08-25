package l2s.gameserver.templates.item.upgrade.normal;

import l2s.gameserver.templates.item.upgrade.UpgradeItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 02.04.2019
 * Developed for L2-Scripts.com
 **/
public class NormalUpgradeData {
	private final int id;
	private final int type;
	private final int itemId;
	private final int enchantLevel;
	private final long price;
	private final int[] locationId;

	private final List<UpgradeItemData> requiredItems = new ArrayList<>();
	private final List<Integer> unkData = new ArrayList<>();

	private NormalUpgradeResult successResult = null;
	private NormalUpgradeResult failResult = null;
	private NormalUpgradeResult bonusResult = null;

	public NormalUpgradeData(int id, int type, int itemId, int enchantLevel, long price, int[] locationId) {
		this.id = id;
		this.type = type;
		this.itemId = itemId;
		this.enchantLevel = enchantLevel;
		this.price = price;
		this.locationId = locationId;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
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

	public NormalUpgradeResult getSuccessResult() {
		return successResult;
	}

	public void setSuccessResult(NormalUpgradeResult successResult) {
		if (this.successResult != null)
			return;
		this.successResult = successResult;
	}

	public NormalUpgradeResult getFailResult() {
		return failResult;
	}

	public void setFailResult(NormalUpgradeResult failResult) {
		if (this.failResult != null)
			return;
		this.failResult = failResult;
	}

	public NormalUpgradeResult getBonusResult() {
		return bonusResult;
	}

	public void setBonusResult(NormalUpgradeResult bonusResult) {
		if (this.bonusResult != null)
			return;
		this.bonusResult = bonusResult;
	}
}
