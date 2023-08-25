package l2s.gameserver.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.xml.holder.EnchantStoneHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.PetDataHolder;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.model.items.attachment.PickableAttachment;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.item.support.EnchantStone;

public final class ItemFunctions
{
	private ItemFunctions()
	{}

	public static ItemInstance createItem(int itemId)
	{
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setLocation(ItemLocation.VOID);
		item.setCount(1L);

		return item;
	}

	/**
	 * Добавляет предмет в инвентарь игрока, корректно обрабатывает нестыкуемые вещи
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 */
	public static List<ItemInstance> addItem(Playable playable, int itemId, long count)
	{
		return addItem(playable, itemId, count, 0, true);
	}

	public static List<ItemInstance> addItem(Playable playable, int itemId, long count, boolean notify)
	{
		return addItem(playable, itemId, count, 0, notify);
	}

	/**
	 * Добавляет предмет в инвентарь игрока, корректно обрабатывает нестыкуемые вещи
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 */
	public static List<ItemInstance> addItem(Playable playable, int itemId, long count, int enchantLevel, boolean notify)
	{
		if(playable == null || count < 1)
			return Collections.emptyList();

		Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;

		if(itemId > 0)
		{
			List<ItemInstance> items = new ArrayList<ItemInstance>();

			ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
			if(t.isStackable())
			{
				items.add(player.getInventory().addItem(itemId, count));

				if(notify)
					player.sendPacket(SystemMessagePacket.obtainItems(itemId, count, 0));
			}
			else
			{
				for(long i = 0; i < count; i++)
				{
					ItemInstance item = player.getInventory().addItem(itemId, 1, enchantLevel);
					items.add(item);

					if(notify)
						player.sendPacket(SystemMessagePacket.obtainItems(item));
				}
			}

			return items;
		}
		else if(itemId == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
		{
			player.getPlayer().addPcBangPoints((int) count, false, notify);
		}
		else if(itemId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
		{
			if(player.getPlayer().getClan() != null)
			{
				player.getPlayer().getClan().incReputation((int) count, false, "itemFunction");

				if(notify)
				{
					//
				}
			}
		}
		else if(itemId == ItemTemplate.ITEM_ID_FAME)
		{
			player.getPlayer().setFame((int) count + player.getPlayer().getFame(), "itemFunction", notify);
		}
		else if(itemId == ItemTemplate.ITEM_ID_CRAFT_POINTS)
		{
			player.getPlayer().setCraftPoints((int) count, "itemFunction");
		}
		return Collections.emptyList();
	}

	/**
	 * Возвращает количество предметов в инвентаре игрока
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @return количество
	 */
	public static long getItemCount(Playable playable, int itemId)
	{
		if(playable == null)
			return 0;

		Player player = playable.getPlayer();
		if(itemId > 0)
			return player.getInventory().getCountOf(itemId);

		if(itemId == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
			return player.getPcBangPoints();

		if(itemId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
		{
			if(player.getClan() != null)
				return player.getClan().getReputationScore();

			return 0;
		}
		if(itemId == ItemTemplate.ITEM_ID_FAME)
			return player.getFame();

		return 0;
	}

	/**
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @return true,  если у персонажа есть необходимое количество предметов
	 */
	public static boolean haveItem(Playable playable, int itemId, long count)
	{
		return getItemCount(playable, itemId) >= count;
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, int itemId, long count)
	{
		return deleteItem(playable, itemId, count, true);
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @param notify оповестить игрока системным сообщением
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, int itemId, long count, boolean notify)
	{
		if(playable == null || count < 1)
			return false;

		Player player = playable.getPlayer();

		if(itemId > 0)
		{
			playable.getInventory().writeLock();
			try
			{
				ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
				if(t == null)
					return false;

				if(t.isStackable())
				{
					if(!playable.getInventory().destroyItemByItemId(itemId, count))
					{
						//TODO audit
						return false;
					}
				}
				else
				{
					if(playable.getInventory().getCountOf(itemId) < count)
						return false;

					for(long i = 0; i < count; i++)
					{
						if(!playable.getInventory().destroyItemByItemId(itemId, 1L))
						{
							//TODO audit
							return false;
						}
					}
				}
			}
			finally
			{
				playable.getInventory().writeUnlock();
			}

			if(notify)
				playable.sendPacket(SystemMessagePacket.removeItems(itemId, count));
		}
		else if(itemId == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
		{
			player.reducePcBangPoints((int) count, notify);
		}
		else if(itemId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
		{
			Clan clan = player.getClan();
			if(clan == null)
				return false;

			if(clan.getReputationScore() < count)
				return false;

			clan.incReputation((int) -count, false, "itemFunction");

			if(notify)
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLANS_REPUTATION).addLong(count));
		}
		else if(itemId == ItemTemplate.ITEM_ID_FAME)
		{
			if(player.getFame() < count)
				return false;

			player.setFame((int) (player.getFame() - count), "itemFunction", notify);
		}

		return true;
	}

	/** Удаляет все предметы у персонажа с ивентаря и банка по Item ID **/
	public static void deleteItemsEverywhere(Playable playable, int itemId)
	{
		if(playable == null)
			return;

		Player player = playable.getPlayer();

		if(itemId > 0)
		{
			player.getInventory().writeLock();
			try
			{
				ItemInstance item = player.getInventory().getItemByItemId(itemId);
				while(item != null)
				{
					player.getInventory().destroyItem(item);
					item = player.getInventory().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getInventory().writeUnlock();
			}

			player.getWarehouse().writeLock();
			try
			{
				ItemInstance item = player.getWarehouse().getItemByItemId(itemId);
				while(item != null)
				{
					player.getWarehouse().destroyItem(item);
					item = player.getWarehouse().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getWarehouse().writeUnlock();
			}

			player.getFreight().writeLock();
			try
			{
				ItemInstance item = player.getFreight().getItemByItemId(itemId);
				while(item != null)
				{
					player.getFreight().destroyItem(item);
					item = player.getFreight().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getFreight().writeUnlock();
			}

			player.getRefund().writeLock();
			try
			{
				ItemInstance item = player.getRefund().getItemByItemId(itemId);
				while(item != null)
				{
					player.getRefund().destroyItem(item);
					item = player.getRefund().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getRefund().writeUnlock();
			}

			PetInstance pet = player.getPet();
			if(pet != null)
			{
				pet.getInventory().writeLock();
				try
				{
					ItemInstance item = pet.getInventory().getItemByItemId(itemId);
					while(item != null)
					{
						pet.getInventory().destroyItem(item);
						item = pet.getInventory().getItemByItemId(itemId);
					}
				}
				finally
				{
					pet.getInventory().writeUnlock();
				}
			}
			else
			{
				List<ItemInstance> items = new ArrayList<ItemInstance>();
				items.addAll(ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(player.getObjectId(), ItemLocation.PET_INVENTORY));
				items.addAll(ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(player.getObjectId(), ItemLocation.PET_PAPERDOLL));
				for(ItemInstance item : items)
				{
					if(item.getItemId() == itemId)
					{
						item.setLocData(-1);
						item.setCount(0L);
						item.delete();
					}
				}
			}
		}
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param item   предмет
	 * @param count	количество
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, ItemInstance item, long count)
	{
		return deleteItem(playable, item, count, true);
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param item   предмет
	 * @param count	количество
	 * @param notify оповестить игрока системным сообщением
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, ItemInstance item, long count, boolean notify)
	{
		if(playable == null || count < 1)
			return false;

		if(item.getCount() < count)
			return false;

		playable.getInventory().writeLock();
		try
		{
			if(!playable.getInventory().destroyItem(item, count))
			{
				//TODO audit
				return false;
			}
		}
		finally
		{
			playable.getInventory().writeUnlock();
		}

		if(notify)
			playable.sendPacket(SystemMessagePacket.removeItems(item.getItemId(), count));

		return true;
	}

	public final static IBroadcastPacket checkIfCanEquip(PetInstance pet, ItemInstance item)
	{
		if(!item.isEquipable())
			return SystemMsg.YOUR_PET_CANNOT_CARRY_THIS_ITEM;

		int petId = pet.getNpcId();

		if(item.getTemplate().isPetPendant() //
				|| PetDataHolder.isWolf(petId) && item.getTemplate().isForWolf() //
				|| PetDataHolder.isHatchling(petId) && item.getTemplate().isForHatchling() //
				|| PetDataHolder.isStrider(petId) && item.getTemplate().isForStrider() //
				|| PetDataHolder.isGreatWolf(petId) && item.getTemplate().isForGWolf() //
				|| PetDataHolder.isBabyPet(petId) && item.getTemplate().isForPetBaby() //
				|| PetDataHolder.isImprovedBabyPet(petId) && item.getTemplate().isForPetBaby() //
		)
			return null;

		return SystemMsg.YOUR_PET_CANNOT_CARRY_THIS_ITEM;
	}

	/**
	 * Проверяет возможность носить эту вещь.
	 *
	 * @return null, если вещь носить можно, либо SystemMessage, который можно показать игроку
	 */
	public final static IBroadcastPacket checkIfCanEquip(Player player, ItemInstance item)
	{
		//FIXME [G1ta0] черезмерный хардкод, переделать на условия
		int itemId = item.getItemId();
		long targetSlot = item.getTemplate().getBodyPart();
		Clan clan = player.getClan();

		if(player.getRace() != Race.KAMAEL && (item.getItemType() == WeaponType.CROSSBOW || item.getItemType() == WeaponType.RAPIER || item.getItemType() == WeaponType.ANCIENTSWORD))
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(item.getItemType() == WeaponType.DUALDAGGER && player.getSkillLevel(923) < 1)
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Корона лидера клана, владеющего замком
		if(itemId == 6841 && (clan == null || !player.isClanLeader() || clan.getCastle() == 0))
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(item.isEquipped()) // Валидация уже надетой брони, проверяем одета ли она в нужную ячейку.
		{
			int[] paperdolls = Inventory.getPaperdollIndexes(targetSlot);
			boolean success = false;
			for(int paperdoll : paperdolls)
			{
				if(paperdoll == item.getEquipSlot())
				{
					success = true;
					break;
				}
			}
			if(!success)
				return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}

		if(targetSlot == ItemTemplate.SLOT_DECO)
		{
			ItemInstance bracelet = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RBRACELET);
			if(bracelet == null)
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_WEAR_S1_BECAUSE_YOU_ARE_NOT_WEARING_A_BRACELET).addItemName(itemId);

			int count = player.getTalismanCount();
			if(count <= 0)
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);

			ItemInstance deco;
			for(int paperdoll = Inventory.PAPERDOLL_DECO1; paperdoll <= Inventory.PAPERDOLL_DECO6; paperdoll++)
			{
				deco = player.getInventory().getPaperdollItem(paperdoll);
				if(deco != null)
				{
					if(deco == item)
						return null; // талисман уже одет и количество слотов больше нуля
					// Проверяем на количество слотов
					if(--count <= 0)
						return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);
				}
			}
		}
		else if(targetSlot == ItemTemplate.SLOT_JEWEL)
		{
			ItemInstance brooch = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_BROOCH);
			if(brooch == null)
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_WITHOUT_EQUIPPING_A_BROOCH).addItemName(itemId);

			int count = player.getJewelsLimit();
			if(count <= 0)
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);

			ItemInstance jewel;
			for(int paperdoll = Inventory.PAPERDOLL_JEWEL1; paperdoll <= Inventory.PAPERDOLL_JEWEL6; paperdoll++)
			{
				jewel = player.getInventory().getPaperdollItem(paperdoll);
				if(jewel != null)
				{
					if(jewel == item)
						return null; // камень уже одет и количество слотов больше нуля
					// Проверяем на количество слотов
					if(--count <= 0)
						return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);
				}
			}
		}
		else if(targetSlot == ItemTemplate.SLOT_AGATHION)
		{
			ItemInstance bracelet = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
			if(bracelet == null)
				return SystemMsg.YOU_CANNOT_USE_THE_AGATHIONS_POWER_BECAUSE_YOU_ARE_NOT_WEARING_THE_LEFT_BRACELET;

			if(!player.isActiveMainAgathionSlot())
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId); // TODO: Найти правильное сообщение.

			ItemInstance agathion = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_AGATHION_MAIN);
			if(agathion == null || agathion != null && agathion == item)
				return null;

			int count = player.getSubAgathionsLimit();
			if(count <= 0)
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId); // TODO: Найти правильное сообщение.

			for(int paperdoll = Inventory.PAPERDOLL_AGATHION_1; paperdoll <= Inventory.PAPERDOLL_AGATHION_4; paperdoll++)
			{
				agathion = player.getInventory().getPaperdollItem(paperdoll);
				if(agathion != null)
				{
					if(agathion == item)
						return null; // агатион уже одет и количество слотов больше нуля
					// Проверяем на количество слотов
					if(--count <= 0)
						return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);
				}
			}
		}
		return null;
	}

	public static boolean checkIfCanPickup(Playable playable, ItemInstance item)
	{
		Player player = playable.getPlayer();
		return item.getDropTimeOwner() <= System.currentTimeMillis() || item.getDropPlayers().contains(player.getObjectId());
	}

	public static boolean canAddItem(Player player, ItemInstance item)
	{
		if(!player.getInventory().validateWeight(item))
		{
			player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return false;
		}

		if(!player.getInventory().validateCapacity(item))
		{
			player.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
			return false;
		}

		IItemHandler handler = item.getTemplate().getHandler();
		if(handler != null && !handler.pickupItem(player, item))
			return false;

		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;
		if(attachment != null && !attachment.canPickUp(player))
			return false;

		return true;
	}

	/**
	 * Проверяет возможность передачи вещи
	 *
	 * @param player
	 * @param item
	 * @return
	 */
	public final static boolean checkIfCanDiscard(Player player, ItemInstance item)
	{
		if(item.isHeroItem())
			return false;

		if(player.getMountControlItemObjId() == item.getObjectId())
			return false;

		if(player.getPetControlItem() == item)
			return false;

		if(player.getEnchantScroll() == item)
			return false;

		if(item.getTemplate().isQuest())
			return false;

		return true;
	}

	/**
	 * Проверяет соответствие уровня заточки и вообще катализатор ли это или левый итем
	 *
	 * @param item
	 * @param catalyst
	 * @return true если катализатор соответствует
	 */
	public static final EnchantStone getEnchantStone(ItemInstance item, ItemInstance catalyst)
	{
		if(item == null || catalyst == null)
			return null;

		EnchantStone enchantStone = EnchantStoneHolder.getInstance().getEnchantStone(catalyst.getItemId());
		if(enchantStone == null)
			return null;

		int current = item.getEnchantLevel();
		if(current < (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? enchantStone.getMinFullbodyEnchantLevel() : enchantStone.getMinEnchantLevel()))
			return null;

		if(current > enchantStone.getMaxEnchantLevel())
			return null;

		if(!enchantStone.containsGrade(item.getGrade()))
			return null;

		final int itemType = item.getTemplate().getType2();
		switch(enchantStone.getType())
		{
			case ARMOR:
				if(itemType == ItemTemplate.TYPE2_WEAPON || item.getTemplate().isHairAccessory())
					return null;
				break;
			case WEAPON:
				if(itemType == ItemTemplate.TYPE2_SHIELD_ARMOR || itemType == ItemTemplate.TYPE2_ACCESSORY || item.getTemplate().isHairAccessory())
					return null;
				break;
			case HAIR_ACCESSORY:
				if(!item.getTemplate().isHairAccessory())
					return null;
				break;
		}

		return enchantStone;
	}

	public static int getCrystallizeCrystalAdd(ItemInstance item)
	{
		int result = 0;
		int crystalsAdd = 0;
		if(item.isWeapon())
		{
			switch(item.getGrade())
			{
				case D:
					crystalsAdd = 90;
					break;
				case C:
					crystalsAdd = 45;
					break;
				case B:
					crystalsAdd = 67;
					break;
				case A:
					crystalsAdd = 145;
					break;
				case S:
				case S80:
				case S84:
					crystalsAdd = 250;
					break;
				case R:
				case R95:
				case R99:
					crystalsAdd = 500;
					break;
			}
		}
		else
		{
			switch(item.getGrade())
			{
				case D:
					crystalsAdd = 11;
					break;
				case C:
					crystalsAdd = 6;
					break;
				case B:
					crystalsAdd = 11;
					break;
				case A:
					crystalsAdd = 20;
					break;
				case S:
				case S80:
				case S84:
					crystalsAdd = 25;
					break;
				case R:
				case R95:
				case R99:
					crystalsAdd = 30;
					break;
			}
		}

		if(item.getEnchantLevel() > 3)
		{
			result = crystalsAdd * 3;
			if(item.isWeapon())
				crystalsAdd *= 2;
			else
				crystalsAdd *= 3;
			result += crystalsAdd * (item.getEnchantLevel() - 3);
		}
		else
			result = crystalsAdd * item.getEnchantLevel();

		return result;
	}

	public static boolean checkIsEquipped(Player player, int slot, int itemId, int enchant)
	{
		Inventory inv = player.getInventory();
		if(slot >= 0)
		{
			ItemInstance item = inv.getPaperdollItem(slot);
			if(item == null)
				return itemId == 0;

			return item.getItemId() == itemId && item.getFixedEnchantLevel(player) >= enchant;
		}
		else
		{
			for(int s : Inventory.PAPERDOLL_ORDER)
			{
				ItemInstance item = inv.getPaperdollItem(s);
				if(item == null)
					continue;

				if(item.getItemId() == itemId && item.getFixedEnchantLevel(player) >= enchant)
					return true;
			}
		}
		return false;
	}

	public static boolean checkForceUseItem(Player player, ItemInstance item, boolean sendMsg)
	{
		if(player.isOutOfControl())
		{
			if(sendMsg)
				player.sendActionFailed();
			return false;
		}

		if(player.isStunned() || player.isDecontrolled() || player.isSleeping() || player.isAfraid() || player.isAlikeDead()) //to add more conds
		{
			if(sendMsg)
				player.sendActionFailed();
			return false;
		}

		if(item.getTemplate().isQuest())
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_CANNOT_USE_QUEST_ITEMS);
			return false;
		}
		return true;
	}

	public static boolean checkUseItem(Player player, ItemInstance item, boolean sendMsg)
	{
		if(player.isInTrainingCamp())
			return false;

		if(player.isInStoreMode())
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
			return false;
		}

		int itemId = item.getItemId();
		if(player.isFishing() && item.getTemplate().getItemType() != EtcItemType.FISHSHOT)
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return false;
		}

		if(player.isSharedGroupDisabled(item.getTemplate().getReuseGroup()))
		{
			if(sendMsg)
				player.sendReuseMessage(item);
			return false;
		}

		if(!item.isEquipped() && !item.getTemplate().testCondition(player, item, sendMsg))
			return false;

		if(player.getInventory().isLockedItem(item))
			return false;

		IBroadcastPacket result;
		for(Event e : player.getEvents())
		{
			result = e.canUseItem(player, item);
			if(result != null)
			{
				if(sendMsg)
					player.sendPacket(result);
				return false;
			}
		}

		if(item.getTemplate().isForPet())
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_MAY_NOT_EQUIP_A_PET_ITEM);
			return false;
		}

		if(player.isUseItemDisabled())
		{
			if(sendMsg)
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}
		
		if(player.isOutOfControl())
		{
			if(sendMsg)
				player.sendActionFailed();
			return false;
		}
		return true;
	}
}
