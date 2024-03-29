package l2s.gameserver.network.l2.c2s;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.EnchantItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.EnchantResultPacket;
import l2s.gameserver.network.l2.s2c.ExItemAnnounce;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.EnchantScroll;
import l2s.gameserver.templates.item.support.EnchantStone;
import l2s.gameserver.templates.item.support.EnchantVariation;
import l2s.gameserver.templates.item.support.EnchantVariation.EnchantLevel;
import l2s.gameserver.templates.item.support.FailResultType;
import l2s.gameserver.utils.DropSpecialItemAnnounce;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.LogGeneral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestEnchantItem extends L2GameClientPacket
{
	private static final int ENCHANT_DELAY = 1500;

	private static final Logger _log = LoggerFactory.getLogger(RequestEnchantItem.class);

	private static final int SUCCESS_VISUAL_EFF_ID = 5965;
	private static final int FAIL_VISUAL_EFF_ID = 5949;

	private int _objectId, _catalystObjId;

	@Override
	protected boolean readImpl()
	{
		_objectId = readD();
		_catalystObjId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(player.isInTrade())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(System.currentTimeMillis() <= (player.getLastEnchantItemTime() + ENCHANT_DELAY))
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(player.isInStoreMode())
		{
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResultPacket.CANCEL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendActionFailed();
			return;
		}

		final PcInventory inventory = player.getInventory();
		inventory.writeLock();
		try
		{
			final ItemInstance item = inventory.getItemByObjectId(_objectId);
			final ItemInstance scroll = player.getEnchantScroll();

			ItemInstance catalyst = _catalystObjId > 0 ? inventory.getItemByObjectId(_catalystObjId) : null;
			EnchantStone enchantStone = ItemFunctions.getEnchantStone(item, catalyst);
			if(enchantStone == null)
				catalyst = null;

			if(item == null || scroll == null)
			{
				player.sendActionFailed();
				return;
			}

			final EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
			if(enchantScroll == null)
			{
				player.sendActionFailed();
				return;
			}

			if(item.getEnchantLevel() < enchantScroll.getMinEnchant() || enchantScroll.getMaxEnchant() != -1 && item.getEnchantLevel() >= enchantScroll.getMaxEnchant())
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			if(enchantScroll.getItems().size() > 0)
			{
				if(!enchantScroll.getItems().contains(item.getItemId()))
				{
					player.sendPacket(EnchantResultPacket.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}
			}
			else
			{
				if(!enchantScroll.containsGrade(item.getGrade()))
				{
					player.sendPacket(EnchantResultPacket.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}

				final int itemType = item.getTemplate().getType2();
				switch(enchantScroll.getType())
				{
					case ARMOR:
						if(itemType == ItemTemplate.TYPE2_WEAPON || item.getTemplate().isHairAccessory())
						{
							player.sendPacket(EnchantResultPacket.CANCEL);
							player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
							player.sendActionFailed();
							return;
						}
						break;
					case WEAPON:
						if(itemType == ItemTemplate.TYPE2_SHIELD_ARMOR || itemType == ItemTemplate.TYPE2_ACCESSORY || item.getTemplate().isHairAccessory())
						{
							player.sendPacket(EnchantResultPacket.CANCEL);
							player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
							player.sendActionFailed();
							return;
						}
						break;
					case HAIR_ACCESSORY:
						if(!item.getTemplate().isHairAccessory())
						{
							player.sendPacket(EnchantResultPacket.CANCEL);
							player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
							player.sendActionFailed();
							return;
						}
						break;
				}
			}

			if(!enchantScroll.getItems().contains(item.getItemId()) && !item.canBeEnchanted())
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			final EnchantVariation variation = EnchantItemHolder.getInstance().getEnchantVariation(enchantScroll.getVariationId());
			if(variation == null)
			{
				player.sendActionFailed();
				_log.warn("RequestEnchantItem: Cannot find variation ID[" + enchantScroll.getVariationId() + "] for enchant scroll ID[" + enchantScroll.getItemId() + "]!");
				return;
			}

			int minEnchantSteep = enchantScroll.getMinEnchantStep();
			int maxEnchantSteep = enchantScroll.getMaxEnchantStep();
			if(enchantStone != null)
			{
				minEnchantSteep = Math.max(minEnchantSteep, enchantStone.getMinEnchantStep());
				maxEnchantSteep = Math.max(maxEnchantSteep, enchantStone.getMaxEnchantStep());
			}
			int oldEnchantLevel =item.getEnchantLevel();
			int newEnchantLvl = item.getEnchantLevel() + Rnd.get(minEnchantSteep, maxEnchantSteep);
			newEnchantLvl = Math.min(newEnchantLvl, enchantScroll.getMaxEnchant());
			if(newEnchantLvl < item.getEnchantLevel()) //  А вдруг?
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendActionFailed();
				return;
			}

			final EnchantLevel enchantLevel = variation.getLevel(item.getEnchantLevel() + 1);
			if(enchantLevel == null)
			{
				player.sendActionFailed();
				_log.warn("RequestEnchantItem: Cannot find variation ID[" + enchantScroll.getVariationId() + "] enchant level[" + (item.getEnchantLevel() + 1) + "] for enchant scroll ID[" + enchantScroll.getItemId() + "]!");
				return;
			}

			if(!inventory.destroyItem(scroll, 1L) || catalyst != null && !inventory.destroyItem(catalyst, 1L))
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendActionFailed();
				return;
			}

			final double baseChance;
			if(item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
				baseChance = enchantLevel.getFullBodyChance();
			else if(item.getTemplate().isMagicWeapon())
				baseChance = enchantLevel.getMagicWeaponChance();
			else
				baseChance = enchantLevel.getBaseChance();

			double chance = baseChance;

			if(enchantStone != null)
				chance += enchantStone.getChance();

			chance += player.getPremiumAccount().getEnchantChanceBonus();
			chance += player.getVIP().getTemplate().getEnchantChanceBonus();

			if(item.getGrade() != ItemGrade.NONE)
				chance *= player.getEnchantChanceModifier();

			chance = Math.min(100, chance);
			boolean success =true;
			if(Rnd.chance(chance))
			{
				item.setEnchantLevel(newEnchantLvl);
				item.setJdbcState(JdbcEntityState.UPDATED);
				item.update();

				player.getInventory().refreshEquip(item);

				player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, item));

				player.sendPacket(new EnchantResultPacket(0, 0, 0, item.getEnchantLevel(),item.getEnchantLevel()));

				if(baseChance < 100.)
					player.broadcastPacket(new ExItemAnnounce(player.getName(), item.getItemId(), item.getEnchantLevel()));

				if(enchantLevel.haveSuccessVisualEffect())
				{
					player.broadcastPacket(new SystemMessage(SystemMessage.C1_HAS_SUCCESSFULY_ENCHANTED_A__S2_S3).addName(player).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
					player.broadcastPacket(new MagicSkillUse(player, player, SUCCESS_VISUAL_EFF_ID, 1, 500, 1500));
				}

				player.getListeners().onEnchantItem(item, true);
			}
			else
			{
				success=false;
				FailResultType resultType = enchantScroll.getResultType();
				if(enchantStone != null && enchantStone.getResultType().ordinal() > resultType.ordinal())
					resultType = enchantStone.getResultType();

				switch(resultType)
				{
					case CRYSTALS:
						if(item.isEquipped())
							player.sendDisarmMessage(item);

						Log.LogItem(player, Log.EnchantFail, item);

						if(!inventory.destroyItem(item, 1L))
						{
							player.sendActionFailed();
							return;
						}

						int crystalId = item.getGrade().getCrystalId();
						int crystalAmount = item.getCrystalCountOnEchant();
						int[] stone = item.getEnchantFailStone();
						int stoneId = stone[0];
						int stoneCount = stone[1];
						if(crystalId > 0 && crystalAmount > 0 && !item.isFlagNoCrystallize())
						{
							player.sendPacket(new EnchantResultPacket(1, crystalId, crystalAmount, 0));
							ItemFunctions.addItem(player, crystalId, crystalAmount, true);
							if(stoneId > 0 && stoneCount > 0)
								ItemFunctions.addItem(player, stoneId, stoneCount, true);
						}
						else
							player.sendPacket(EnchantResultPacket.FAILED_NO_CRYSTALS);

						if(enchantScroll.showFailEffect())
							player.broadcastPacket(new MagicSkillUse(player, player, FAIL_VISUAL_EFF_ID, 1, 500, 1500));
						break;
					case DROP_ENCHANT:
						int enchantDropCount = enchantScroll.getEnchantDropCount();
						if(enchantStone != null && enchantStone.getEnchantDropCount() < enchantDropCount)
							enchantDropCount = enchantStone.getEnchantDropCount();

						item.setEnchantLevel(Math.max(item.getEnchantLevel() - enchantDropCount, 0));
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();

						player.getInventory().refreshEquip(item);

						player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, item));
						player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
						player.sendPacket(EnchantResultPacket.BLESSED_FAILED);
						break;
					case NOTHING:
						player.sendPacket(EnchantResultPacket.ANCIENT_FAILED);
						break;
				}
				player.getListeners().onEnchantItem(item, false);
			}

			if (DropSpecialItemAnnounce.dropSpecialItems.contains(item.getItemId())) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
				String log_content = date+"["+player.getName()+"]强化道具["+item.getName(player)+"]"+(success?"成功":"失败")+",原有强化等级："+oldEnchantLevel+"强化后等级："+item.getEnchantLevel()+",角色ID："+player.getObjectId()+",道具ID："+item.getItemId();
				ThreadPoolManager.getInstance().schedule(new LogGeneral("specialItemsAnnounce/enchant",log_content),1000L);
			}
		}
		finally
		{
			inventory.writeUnlock();

			player.updateStats();
		}

		player.setLastEnchantItemTime(System.currentTimeMillis());
	}
}