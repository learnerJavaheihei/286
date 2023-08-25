package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.UpgradeSystemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemAttributes;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExUpgradeSystemResult;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.support.Ensoul;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;
import l2s.gameserver.templates.item.upgrade.rare.RareUpgradeData;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Bonux
 **/

public class RequestUpgradeSystemResult extends L2GameClientPacket {
	private int targetItemObjectId, upgradeId;

	@Override
	protected boolean readImpl() {
		targetItemObjectId = readD();
		upgradeId = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isActionsDisabled()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isInStoreMode()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isProcessingRequest()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isFishing()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isInTrainingCamp()) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		RareUpgradeData upgradeData = UpgradeSystemHolder.getInstance().getRareUpgradeData(upgradeId);
		if (upgradeData == null || !Language.checkLocation(activeChar, upgradeData.getLocationId())) {
			activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		int customFlags = 0;
		int lifeTime = 0;
		int variationStoneId;
		int variation1Id;
		int variation2Id;
		int visualId;
		boolean blessed;
		int appearanceStoneId;
		Ensoul[] normalEnsouls;
		Ensoul[] specialEnsouls;

		activeChar.getInventory().writeLock();
		try {
			ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(targetItemObjectId);
			if (targetItem == null) { // Улучшаемый итем не найден.
				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if (targetItem.getEnchantLevel() != upgradeData.getEnchantLevel()) { // Заточка улучшаемого итема не соответствует требованиям.
				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if (activeChar.getAdena() < upgradeData.getPrice()) { // Проверяем наличие адены.
				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERES_NOT_ENOUGH_ADENA);
				return;
			}

			loop1:
			for (UpgradeItemData requiredItem : upgradeData.getRequiredItems()) { // Проверяем наличие требуемых предметов.
				if (requiredItem.getCount() == 0)
					continue;

				List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
				for (ItemInstance item : items) {
					if (item == null || item.getCount() < requiredItem.getCount() || item.getEnchantLevel() != requiredItem.getEnchantLevel())
						continue;
					continue loop1;
				}

				activeChar.sendPacket(new ExUpgradeSystemResult(0, targetItemObjectId));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERE_ARE_NOT_ENOUGH_INGREDIENTS);
				return;
			}

			variationStoneId = targetItem.getVariationStoneId();
			variation1Id = targetItem.getVariation1Id();
			variation2Id = targetItem.getVariation2Id();
			visualId = targetItem.getVisualId();
			blessed = targetItem.isBlessed();
			appearanceStoneId = targetItem.getAppearanceStoneId();
			normalEnsouls = targetItem.getNormalEnsouls();
			specialEnsouls = targetItem.getSpecialEnsouls();

			if (targetItem.isWeapon() || targetItem.isArmor() || targetItem.isAccessory()) {
				customFlags = targetItem.getCustomFlags();
				lifeTime = targetItem.getTemporalLifeTime();
			}

			activeChar.getInventory().destroyItem(targetItem, 1);
			activeChar.sendPacket(SystemMessagePacket.removeItems(targetItem.getItemId(), 1));
			activeChar.reduceAdena(upgradeData.getPrice(), true); // Забираем оплату.

			loop2:
			for (UpgradeItemData requiredItem : upgradeData.getRequiredItems()) { // Забираем требуемые предметы.
				if (requiredItem.getCount() == 0)
					continue;

				List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
				for (ItemInstance item : items) {
					if (item == null || item.getCount() < requiredItem.getCount() || item.getEnchantLevel() != requiredItem.getEnchantLevel())
						continue;

					if (!activeChar.getInventory().destroyItemByObjectId(item.getObjectId(), requiredItem.getCount()))
						continue;//TODO audit

					activeChar.sendPacket(SystemMessagePacket.removeItems(requiredItem.getId(), requiredItem.getCount()));
					continue loop2;
				}
			}
		} finally {
			activeChar.getInventory().writeUnlock();
		}

		List<ItemInstance> items;
		if (Objects.requireNonNull(ItemHolder.getInstance().getTemplate(upgradeData.getResultItemId())).isStackable()) {
			items = ItemFunctions.addItem(activeChar, upgradeData.getResultItemId(), upgradeData.getResultItemCount(), upgradeData.getResultItemEnchant(), true); // Выдаем предмет разыгранного результата.
		} else {
			items = new ArrayList<>();

			for (int i = 0; i < upgradeData.getResultItemCount(); i++) {
				ItemInstance newItem = ItemFunctions.createItem(upgradeData.getResultItemId());
				if (newItem.isEquipable()) {
					if (upgradeData.getResultItemEnchant() > 0)
						newItem.setEnchantLevel(upgradeData.getResultItemEnchant());
					if (newItem.canBeAugmented(activeChar)) {
						if (variationStoneId > 0)
							newItem.setVariationStoneId(variationStoneId);
						if (variation1Id != 0)
							newItem.setVariation1Id(variation1Id);
						if (variation2Id != 0)
							newItem.setVariation2Id(variation2Id);
					}
					if (newItem.canBeAppearance()) {
						if (visualId != 0)
							newItem.setVisualId(visualId);
						if (appearanceStoneId != 0)
							newItem.setAppearanceStoneId(appearanceStoneId);
					}
					
					if (newItem.canBeBlessed() && blessed)
						newItem.setBlessed(true);						

					if (customFlags > 0) {
						newItem.setCustomFlags(customFlags);
						if (lifeTime > 0)
							newItem.setLifeTime((int) (System.currentTimeMillis() / 1000L) + lifeTime);
					}
				}

				activeChar.sendPacket(SystemMessagePacket.obtainItems(newItem));
				activeChar.getInventory().addItem(newItem);

				if (newItem.isEquipable()) {
					if (normalEnsouls.length > 0 || specialEnsouls.length > 0) {
						for (int id = 1; id <= normalEnsouls.length; id++) {
							Ensoul ensoul = normalEnsouls[id - 1];
							newItem.addEnsoul(1, id, ensoul, true);
						}
						for (int id = 1; id <= specialEnsouls.length; id++) {
							Ensoul ensoul = specialEnsouls[id - 1];
							newItem.addEnsoul(2, id, ensoul, true);
						}
						activeChar.sendPacket(new InventoryUpdatePacket().addModifiedItem(activeChar, newItem));
					}

					variationStoneId = 0;
					variation1Id = 0;
					variation2Id = 0;
					visualId = 0;
					appearanceStoneId = 0;
					customFlags = 0;
					lifeTime = 0;
					normalEnsouls = ItemInstance.EMPTY_ENSOULS_ARRAY;
					specialEnsouls = ItemInstance.EMPTY_ENSOULS_ARRAY;
				}

				items.add(newItem);
			}
		}
		activeChar.sendPacket(new ExUpgradeSystemResult(1, items.get(0).getObjectId()));
	}
}
