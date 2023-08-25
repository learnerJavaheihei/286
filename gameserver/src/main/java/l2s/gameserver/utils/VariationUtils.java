package l2s.gameserver.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.VariationDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket;
import l2s.gameserver.templates.item.ExItemType;
import l2s.gameserver.templates.item.VariationType;
import l2s.gameserver.templates.item.support.variation.VariationCategory;
import l2s.gameserver.templates.item.support.variation.VariationFee;
import l2s.gameserver.templates.item.support.variation.VariationGroup;
import l2s.gameserver.templates.item.support.variation.VariationInfo;
import l2s.gameserver.templates.item.support.variation.VariationOption;
import l2s.gameserver.templates.item.support.variation.VariationStone;

/**
 * @author Bonux
**/
public final class VariationUtils
{
	public static long getRemovePrice(ItemInstance item)
	{
		if(item == null)
			return -1;

		VariationGroup group = VariationDataHolder.getInstance().getGroup(item.getTemplate().getVariationGroupId());
		if(group == null)
			return -1;

		int stoneId = item.getVariationStoneId();
		if(stoneId == -1)
			return 0;

		VariationFee fee = group.getFee(stoneId);
		if(fee == null)
			return -1;

		return fee.getCancelFee();
	}

	public static VariationFee getVariationFee(ItemInstance item, ItemInstance stone)
	{
		if(item == null)
			return null;

		if(stone == null)
			return null;

		VariationGroup group = VariationDataHolder.getInstance().getGroup(item.getTemplate().getVariationGroupId());
		if(group == null)
			return null;

		return group.getFee(stone.getItemId());
	}

	public static boolean tryAugmentItem(Player player, ItemInstance targetItem, ItemInstance refinerItem, ItemInstance feeItem, long feeItemCount)
	{
		if(!targetItem.canBeAugmented(player))
			return false;

		if(refinerItem.getTemplate().isBlocked(player, refinerItem))
			return false;

		int stoneId = refinerItem.getItemId();
		VariationStone stone = null;
		
		int[] standardRings = 
		{
			91952, // Ring of Insolence
			893, // Majestic Ring
			902, // Phoenix Ring
			889 // Tateossian Ring
		};
		int[] standardEarrings =
		{
			91953, // Dragon Valley Earring
			862, // Majestic Earring
			871, // Phoenix Earring
			858 // Tateossian Earring
		};
		int[] standardNecklaces =
		{
			924, // Majestic Necklace
			933, // Phoenix Necklace
			920 // Tateossian Necklace
		};
		int[] rareRings =
		{
			6660, // Queen Ant's Ring Lv. 1
			49574, // Queen Ant's Ring Lv. 2
			49577, // Queen Ant's Ring Lv. 3
			91378, // Queen Ant's Ring Lv. 4
			6662, // Ring of Core Lv. 1
			49576, // Ring of Core Lv. 2
			49579, // Ring of Core Lv. 3
			91380, // Ring of Core Lv. 4
			49580, // Baium's Ring Lv. 1
			49581, // Baium's Ring Lv. 2
			49582, // Baium's Ring Lv. 3
			91381 // Baium's Ring Lv. 4
		};
		int[] rareEarrings =
		{
			6661, // Orfen's Earring Lv. 1
			49575, // Orfen's Earring Lv. 2
			49578, // Orfen's Earring Lv. 3
			91379, // Orfen's Earring Lv. 4
			90763, // Zaken's Earring Lv. 1
			90764, // Zaken's Earring Lv. 2
			90765, // Zaken's Earring Lv. 3
			91382, // Zaken's Earring Lv. 4
			90992, // Antharas' Earring Lv. 1
			91138, // Antharas' Earring Lv. 2
			91139, // Antharas' Earring Lv. 3
			91383 // Antharas' Earring Lv. 4
		};
		int[] rareNecklaces = 
		{
			91550, // Frintezza's Necklace Lv. 1
			91551, // Frintezza's Necklace Lv. 2
			91552, // Frintezza's Necklace Lv. 3
			91553, // Frintezza's Necklace Lv. 4
			91117, // Nebula Necklace
			91119, // Ignis Necklace
			91121, // Procella Necklace
			91123 // Petram Necklace
		};
		
		if (targetItem.isWeapon())
			stone = VariationDataHolder.getInstance().getStone(VariationType.WEAPON, stoneId);
		else if ((stoneId == 94188) || (stoneId == 94212))
		{
			if ((targetItem.getTemplate().getExType() == ExItemType.UPPER_PIECE) || (targetItem.getTemplate().getExType() == ExItemType.FULL_BODY))
				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_CHEST, stoneId);
			else if (targetItem.getTemplate().getExType() == ExItemType.LOWER_PIECE)
				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_LEGS, stoneId);
			else if (targetItem.getTemplate().getExType() == ExItemType.HELMET)
				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_HELMET, stoneId);
			else if (targetItem.getTemplate().getExType() == ExItemType.GLOVES)
				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_GLOVES, stoneId);
			else if (targetItem.getTemplate().getExType() == ExItemType.FEET)
				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR_BOOTS, stoneId);
		}
		else if (targetItem.isArmor() && !targetItem.getTemplate().isHairAccessory() && !targetItem.getTemplate().isCloak())
		{
			ExItemType itemType = targetItem.getTemplate().getExType();
			if ((itemType == ExItemType.UPPER_PIECE) || (itemType == ExItemType.LOWER_PIECE || (itemType == ExItemType.FULL_BODY) || (itemType == ExItemType.HELMET)
					|| (itemType == ExItemType.GLOVES) || (itemType == ExItemType.FEET)))
				stone = VariationDataHolder.getInstance().getStone(VariationType.ARMOR, stoneId);
		}
		else if (ArrayUtils.contains(standardRings, targetItem.getItemId()))
			stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_STANDARD_RING, stoneId);
		else if (ArrayUtils.contains(standardEarrings, targetItem.getItemId()))
			stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_STANDARD_EARRING, stoneId);
		else if (ArrayUtils.contains(standardNecklaces, targetItem.getItemId()))
			stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_STANDARD_NECKLACE, stoneId);
		else if (ArrayUtils.contains(rareRings, targetItem.getItemId()))
			stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_RARE_RING, stoneId);
		else if (ArrayUtils.contains(rareEarrings, targetItem.getItemId()))
			stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_RARE_EARRING, stoneId);
		else if (ArrayUtils.contains(rareNecklaces, targetItem.getItemId()))
			stone = VariationDataHolder.getInstance().getStone(VariationType.ACCESSORY_RARE_NECKLACE, stoneId);
		else if (targetItem.getItemId() == 94213)
			stone = VariationDataHolder.getInstance().getStone(VariationType.HEROIC_CIRCLET, stoneId);
		else if (targetItem.getTemplate().isHairAccessory())
			stone = VariationDataHolder.getInstance().getStone(VariationType.HAIR_ACCESSORY, stoneId);
		else if (targetItem.getTemplate().getExType() == ExItemType.CLOAK)
		{
			if ((stoneId == 70889) || (stoneId == 71126))
				stone = VariationDataHolder.getInstance().getStone(VariationType.ANCIENT_KINGDOM, stoneId);
			else if ((stoneId == 70890) || (stoneId == 71127))
				stone = VariationDataHolder.getInstance().getStone(VariationType.ELMOREDEN, stoneId);
			else if ((stoneId == 70891) || (stoneId == 71128))
				stone = VariationDataHolder.getInstance().getStone(VariationType.ADEN, stoneId);
			else if ((stoneId == 70892) || (stoneId == 71129))
				stone = VariationDataHolder.getInstance().getStone(VariationType.ELMORE, stoneId);
			else if ((stoneId == 70893) || (stoneId == 71130))
				stone = VariationDataHolder.getInstance().getStone(VariationType.FERIOS, stoneId);
		}
		
		if(stone == null)
			return false;

		int variation1Id = getRandomOptionId(stone.getVariation(1));
		int variation2Id = getRandomOptionId(stone.getVariation(2));
		if(variation1Id == 0 && variation2Id == 0)
			return false;

		if(player.getInventory().getCountOf(refinerItem.getItemId()) < 1L)
			return false;

		if(player.getInventory().getCountOf(feeItem.getItemId()) < feeItemCount)
			return false;

		if(!player.getInventory().destroyItem(refinerItem, 1L))
			return false;

		if(!player.getInventory().destroyItem(feeItem, feeItemCount))
			return false;

		setVariation(player, targetItem, stoneId, variation1Id, variation2Id);
		return true;
	}

	public static void setVariation(Player player, ItemInstance item, int variationStoneId, int variation1Id, int variation2Id) {
		item.setVariationStoneId(variationStoneId);
		if (item.getTemplate().getExType() != ExItemType.CLOAK)
		{
			item.setVariation1Id(variation1Id);
			item.setVariation2Id(variation2Id);
		}
		else 
		{
			if ((variationStoneId == 70889) || (variationStoneId == 71126))
			{
				item.setVariation1Id(variation1Id);
			}
			else
				item.setVariation2Id(variation2Id);
		}

		player.getInventory().refreshEquip(item);

		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();

		player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, item));

		for(ShortCut sc : player.getAllShortCuts())
		{
			if(sc.getId() == item.getObjectId() && sc.getType() == ShortCut.ShortCutType.ITEM)
				player.sendPacket(new ShortCutRegisterPacket(player, sc));
		}

		player.sendChanges();
	}

	private static int getRandomOptionId(VariationInfo variation)
	{
		if(variation == null)
			return 0;

		double probalityAmount = 0.;
		VariationCategory[] categories = variation.getCategories();
		for(VariationCategory category : categories)
			probalityAmount += category.getProbability();

		if(Rnd.chance(probalityAmount))
		{
			double probalityMod = (100. - probalityAmount) / categories.length;
			List<VariationCategory> successCategories = new ArrayList<VariationCategory>();
			int tryCount = 0;
			while(successCategories.isEmpty())
			{
				tryCount++;
				for(VariationCategory category : categories)
				{
					if((tryCount % 10) == 0) //Немного теряем шанс, но зато зацикливания будут меньше.
						probalityMod += 1.;
					if(Rnd.chance(category.getProbability() + probalityMod))
						successCategories.add(category);
				}
			}

			VariationCategory[] categoriesArray = successCategories.toArray(new VariationCategory[successCategories.size()]);

			return getRandomOptionId(categoriesArray[Rnd.get(categoriesArray.length)]);
		}
		else
			return 0;
	}

	private static int getRandomOptionId(VariationCategory category)
	{
		if(category == null)
			return 0;

		double chanceAmount = 0.;
		VariationOption[] options = category.getOptions();
		for(VariationOption option : options)
			chanceAmount += option.getChance();

		if(Rnd.chance(chanceAmount))
		{
			double chanceMod = (100. - chanceAmount) / options.length;
			List<VariationOption> successOptions = new ArrayList<VariationOption>();
			int tryCount = 0;
			while(successOptions.isEmpty())
			{
				tryCount++;
				for(VariationOption option : options)
				{
					if((tryCount % 10) == 0) //Немного теряем шанс, но зато зацикливания будут меньше.
						chanceMod += 1.;
					if(Rnd.chance(option.getChance() + chanceMod))
						successOptions.add(option);
				}
			}

			VariationOption[] optionsArray = successOptions.toArray(new VariationOption[successOptions.size()]);

			return optionsArray[Rnd.get(optionsArray.length)].getId();
		}
		else
			return 0;
	}
}
