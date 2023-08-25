package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.UpgradeSystemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExUpgradeSystemNormalResult;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.support.Ensoul;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeData;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeResult;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeResultType;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;

import java.util.*;

/**
 * @author Bonux
 **/

// TODO: Использовать пакет результата и сообщения, если необходимо.
//		msg_begin	id=4471	UNK_0=1	message=[Снаряжение улучшено. Вы получили: $s1.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
//		msg_begin	id=4472	UNK_0=1	message=[$c1 успешно улучшает снаряжение и получает $s2.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
//		msg_begin	id=4473	UNK_0=1	message=[Улучшить не удалось.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[Улучшить не удалось.]	gfxscrnparam=[]	type=[none]	msg_end
//		msg_begin	id=4474	UNK_0=1	message=[Нечего улучшать. Улучшение не удалось.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[Нечего улучшать. Улучшение не удалось.]	gfxscrnparam=[]	type=[none]	msg_end
//		msg_begin	id=4475	UNK_0=1	message=[Не хватает материалов. Улучшение не удалось.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[Не хватает материалов. Улучшение не удалось.]	gfxscrnparam=[]	type=[none]	msg_end
//		msg_begin	id=4476	UNK_0=1	message=[Не хватает аден. Улучшение не удалось.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[Не хватает аден. Улучшение не удалось.]	gfxscrnparam=[]	type=[none]	msg_end
//		msg_begin	id=5571	UNK_0=1	message=[Предмет $s1 улучшить не удалось. Ранг предмета остался прежним.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
public class ExUpgradeSystemNormalRequest extends L2GameClientPacket {
	private int targetItemObjectId, upgradeType, upgradeId;

	@Override
	protected boolean readImpl() throws Exception {
		targetItemObjectId = readD();
		upgradeType = readD();
		upgradeId = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isActionsDisabled()) {
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isInStoreMode()) {
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isProcessingRequest()) {
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isFishing()) {
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		if (activeChar.isInTrainingCamp()) {
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		NormalUpgradeData upgradeData = UpgradeSystemHolder.getInstance().getNormalUpgradeData(upgradeId);
		if (upgradeData == null || upgradeData.getType() != upgradeType || !Language.checkLocation(activeChar, upgradeData.getLocationId())) {
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		NormalUpgradeResult successResult = upgradeData.getSuccessResult();
		NormalUpgradeResult failResult = upgradeData.getFailResult();
		if (successResult == null && failResult == null) { // Нету результата, апгрейд невозможен.
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		double totalChance = 0.;
		if (successResult != null)
			totalChance += successResult.getChance();
		if (failResult != null)
			totalChance += failResult.getChance();

		double totalFailChance = 100. - totalChance;
		if (totalFailChance > 0 && Rnd.chance(totalFailChance)) { // Тотальный фейл, не сыграл шанс успеха и фейла.
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}

		List<NormalUpgradeResult> rolledResults = new ArrayList<>();
		int rollCount = 1;
		while (rolledResults.isEmpty()) { // Разыгрываем результаты.
			if (successResult != null && Rnd.chance(successResult.getChance() * rollCount))
				rolledResults.add(successResult);
			if (failResult != null && Rnd.chance(failResult.getChance() * rollCount))
				rolledResults.add(failResult);
			rollCount += 10;
		}

		NormalUpgradeResult result = Rnd.get(rolledResults); // Берем рендомно 1 из разыграных результатов.
		if (result == null) {
			activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
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
				activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if (targetItem.getEnchantLevel() != upgradeData.getEnchantLevel()) { // Заточка улучшаемого итема не соответствует требованиям.
				activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if (activeChar.getAdena() < upgradeData.getPrice()) { // Проверяем наличие адены.
				activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
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

				activeChar.sendPacket(ExUpgradeSystemNormalResult.FAIL);
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERE_ARE_NOT_ENOUGH_INGREDIENTS);
				return;
			}

			variationStoneId = targetItem.getVariationStoneId();
			variation1Id = targetItem.getVariation1Id();
			variation2Id = targetItem.getVariation2Id();
			visualId = targetItem.getVisualId();
			appearanceStoneId = targetItem.getAppearanceStoneId();
			blessed = targetItem.isBlessed();
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

		Map<Integer, ItemInfo> resultItems = new LinkedHashMap<>();

		for (UpgradeItemData resultItem : result.getItems()) { // Выдаем предметы разыгранного результата.
			List<ItemInstance> items;
			if (Objects.requireNonNull(ItemHolder.getInstance().getTemplate(resultItem.getId())).isStackable()) {
				items = ItemFunctions.addItem(activeChar, resultItem.getId(), resultItem.getCount(), resultItem.getEnchantLevel(), true); // Выдаем предмет разыгранного результата.
			} else {
				items = new ArrayList<>();

				for (int i = 0; i < resultItem.getCount(); i++) {
					ItemInstance newItem = ItemFunctions.createItem(resultItem.getId());
					if (newItem.isEquipable()) {
						if (resultItem.getEnchantLevel() > 0)
							newItem.setEnchantLevel(resultItem.getEnchantLevel());
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
						for (int id = 1; id <= normalEnsouls.length; id++) {
							Ensoul ensoul = normalEnsouls[id - 1];
							newItem.addEnsoul(1, id, ensoul, true);
						}
						for (int id = 1; id <= specialEnsouls.length; id++) {
							Ensoul ensoul = specialEnsouls[id - 1];
							newItem.addEnsoul(2, id, ensoul, true);
						}

						variationStoneId = 0;
						variation1Id = 0;
						variation2Id = 0;
						visualId = 0;
						appearanceStoneId = 0;
						blessed = false;
						customFlags = 0;
						lifeTime = 0;
						normalEnsouls = ItemInstance.EMPTY_ENSOULS_ARRAY;
						specialEnsouls = ItemInstance.EMPTY_ENSOULS_ARRAY;
					}

					items.add(newItem);
				}
			}
			giveResultItem1(resultItem, items, resultItems);
		}

		boolean success = result.getType() == NormalUpgradeResultType.SUCCESS;
		if (success) { // При успешном результате разыгрываем и выдаем бонусную награду.
			NormalUpgradeResult bonusResult = upgradeData.getBonusResult();
			if (bonusResult != null && Rnd.chance(bonusResult.getChance())) {
				for (UpgradeItemData resultItem : bonusResult.getItems()) {
					List<ItemInstance> addedItems = ItemFunctions.addItem(activeChar, resultItem.getId(), resultItem.getCount(), resultItem.getEnchantLevel(), true);
					giveResultItem1(resultItem, addedItems, resultItems);
				}
			}
		}

		activeChar.sendPacket(new ExUpgradeSystemNormalResult(upgradeId, success, resultItems.values()));
	}

	private static void giveResultItem1(UpgradeItemData resultItem, List<ItemInstance> addedItems, Map<Integer, ItemInfo> resultItems) {
		for (ItemInstance addedItem : addedItems) {
			ItemInfo itemInfo = resultItems.get(addedItem.getObjectId());
			if (itemInfo == null) {
				itemInfo = new ItemInfo(addedItem);
				itemInfo.setCount(0);
				resultItems.put(addedItem.getObjectId(), itemInfo);
			}

			if (addedItem.isStackable())
				itemInfo.setCount(itemInfo.getCount() + resultItem.getCount());
			else
				itemInfo.setCount(1);
		}
	}
}
