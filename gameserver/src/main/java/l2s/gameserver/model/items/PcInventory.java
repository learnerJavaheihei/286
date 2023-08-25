package l2s.gameserver.model.items;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.model.items.listeners.*;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.taskmanager.DelayedItemsManager;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;
import java.util.Collections;

public class PcInventory extends Inventory
{
	private final Player _owner;

	// locks
	private LockType _lockType = LockType.NONE;
	private int[] _lockItems = ArrayUtils.EMPTY_INT_ARRAY;

	private int questItemsSize = 0;

	public PcInventory(Player owner)
	{
		super(owner.getObjectId());
		_owner = owner;

		addListener(ItemSkillsListener.getInstance());
		addListener(ItemAugmentationListener.getInstance());
		addListener(ItemEnchantOptionsListener.getInstance());
		addListener(ArmorSetListener.getInstance());
		addListener(BowListener.getInstance());
		addListener(AccessoryListener.getInstance());
		addListener(RodListener.getInstance());
	}

	@Override
	public Player getActor()
	{
		return _owner;
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}

	/**
	 * Добавляет адену игроку.<BR><BR>
	 * @param amount - сколько адены дать
	 * @return L2ItemInstance - новое количество адены
	 */
	public ItemInstance addAdena(long amount)
	{
		return addItem(ItemTemplate.ITEM_ID_ADENA, amount);
	}

	public boolean reduceAdena(long adena)
	{
		return destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, adena);
	}

	public int getPaperdollVariation1Id(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null && item.isAugmented())
			return item.getVariation1Id();
		return 0;
	}

	public int getPaperdollVariation2Id(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null && item.isAugmented())
			return item.getVariation2Id();
		return 0;
	}

	@Override
	public int getPaperdollVisualId(int slot)
	{
		Player player = getActor();

		int itemId = super.getPaperdollVisualId(slot);

		if(player.isInTrainingCamp())
		{
			if(slot == PAPERDOLL_RHAND || slot == PAPERDOLL_LRHAND)
				itemId = 135;
		}
		return itemId;
	}

	@Override
	protected void onRefreshWeight()
	{
		// notify char for overload checking
		getActor().refreshOverloaded();
	}

	/**
	 * Функция для валидации вещей в инвентаре.
	 * Снимает все вещи, которые нельзя носить.
	 * Применяется при входе в игру, смене саба, захвате замка, выходе из клана.
	 */
	public void validateItems()
	{
		for(ItemInstance item : _paperdoll)
			if(item != null && (ItemFunctions.checkIfCanEquip(getActor(), item) != null || !item.getTemplate().testCondition(getActor(), item, false)))
			{
				unEquipItem(item);
				getActor().sendDisarmMessage(item);
			}
	}

	public void refreshEquip()
	{
		final Player actor = getActor();

		int flags = 0;
		for(ItemInstance item : getItems())
			flags |= item.onRefreshEquip(actor, false);

		if((flags & UPDATE_STATS_FLAG) == UPDATE_STATS_FLAG)
			actor.updateStats();

		if((flags & UPDATE_SKILLS_FLAG) == UPDATE_SKILLS_FLAG)
			actor.sendSkillList();
	}

	public void refreshEquip(ItemInstance item)
	{
		if(containsItem(item))
			item.onRefreshEquip(getActor());
	}

	/**
	 * Вызывается из RequestSaveInventoryOrder
	 */
	public void sort(int[][] order)
	{
		boolean needSort = false;
		for(int[] element : order)
		{
			ItemInstance item = getItemByObjectId(element[0]);
			if(item == null)
				continue;
			if(item.getLocation() != ItemLocation.INVENTORY)
				continue;
			if(item.getLocData() == element[1])
				continue;
			item.setLocData(element[1]);
			item.setJdbcState(JdbcEntityState.UPDATED); // lazy update
			needSort = true;
		}
		if(needSort)
			Collections.sort(_items, ItemOrderComparator.getInstance());
	}

	public ItemInstance findArrowForBow(ItemTemplate bow)
	{
		ItemInstance res = null;
		for(ItemInstance temp : getItems())
		{
			if(temp.getItemType() == EtcItemType.ARROW || temp.getItemType() == EtcItemType.ARROW_QUIVER)
			{
				if(bow.getGrade().extOrdinal() == temp.getGrade().extOrdinal())
				{
					//if(temp.getLocation() == ItemLocation.PAPERDOLL && temp.getEquipSlot() == PAPERDOLL_LHAND)
						return temp;
//					else if(res == null || temp.getItemId() < res.getItemId())
//						res = temp;
				}
			}
		}
		return res;
	}

	public ItemInstance findArrowForCrossbow(ItemTemplate crossbow)
	{
		ItemInstance res = null;
		for(ItemInstance temp : getItems())
		{
			if(temp.getItemType() == EtcItemType.BOLT || temp.getItemType() == EtcItemType.BOLT_QUIVER)
			{
				if(crossbow.getGrade().extOrdinal() == temp.getGrade().extOrdinal())
				{
					//if(temp.getLocation() == ItemLocation.PAPERDOLL && temp.getEquipSlot() == PAPERDOLL_LHAND)
						return temp;
					//else if(res == null || temp.getItemId() < res.getItemId())
						//res = temp;
				}
			}
		}
		return res;
	}

	public void lockItems(LockType lock, int[] items)
	{
		if(_lockType != LockType.NONE)
			return;

		_lockType = lock;
		_lockItems = items;

		getActor().sendItemList(false);
	}

	public void unlock()
	{
		if(_lockType == LockType.NONE)
			return;

		_lockType = LockType.NONE;
		_lockItems = ArrayUtils.EMPTY_INT_ARRAY;

		getActor().sendItemList(false);
	}

	public boolean isLockedItem(ItemInstance item)
	{
		switch(_lockType)
		{
			case INCLUDE:
				return ArrayUtils.contains(_lockItems, item.getItemId());
			case EXCLUDE:
				return !ArrayUtils.contains(_lockItems, item.getItemId());
			default:
				return false;
		}
	}

	public LockType getLockType()
	{
		return _lockType;
	}

	public int[] getLockItems()
	{
		return _lockItems;
	}

	@Override
	protected void onRestoreItem(ItemInstance item)
	{
		super.onRestoreItem(item);

		if(item.getTemplate().isRune())
			item.onEquip(-1, getActor());

		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);
	}

	@Override
	protected void onAddItem(ItemInstance item)
	{ Player p = getActor();
		super.onAddItem(item);

		if(item.getItemId() == 999999999) {		//chaos aura
			p.broadcastUserInfo(true);
		}


		if(item.getTemplate().isRune())
			item.onEquip(-1, getActor());

		if(item.getTemplate().isArrow() || item.getTemplate().isBolt() || item.getTemplate().isQuiver())
			getActor().checkAndEquipArrows();

		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);

		if(item.getTemplate().isQuest())
			refreshItemsSize();
	}

	@Override
	protected void onModifyItem(ItemInstance item)
	{
		super.onModifyItem(item);

		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);

		if(item.getTemplate().isQuest())
			refreshItemsSize();
	}

	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		super.onRemoveItem(item);

		Player owner = getActor();
		owner.removeItemFromShortCut(item.getObjectId());

		if(item.getItemId() == 999999999) {
			owner.broadcastUserInfo(true);
		}

		if(item.getTemplate().isRune())
			item.onUnequip(-1, getActor());

		if(owner.getMountControlItemObjId() == item.getObjectId())
			owner.setMount(null);

		if(owner.getPetControlItem() == item)
		{
			PetInstance pet = owner.getPet();
			if(pet != null)
				pet.unSummon(false);
		}

		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);

		if(item.getTemplate().isQuest())
			refreshItemsSize();
	}

	@Override
	protected boolean onEquip(int slot, ItemInstance item)
	{
		if(!super.onEquip(slot, item))
			return false;

		if(item.isShadowItem())
			item.startManaConsumeTask(new ManaConsumeTask(item));

		return true;
	}

	@Override
	protected boolean onReequip(int slot, ItemInstance newItem, ItemInstance oldItem)
	{
		boolean equipped = super.onReequip(slot, newItem, oldItem);

		if(oldItem.isShadowItem())
			oldItem.stopManaConsumeTask();

		if(equipped)
		{
			if(newItem.isShadowItem())
				newItem.startManaConsumeTask(new ManaConsumeTask(newItem));
			return true;
		}
		return false;
	}

	@Override
	protected void onUnequip(int slot, ItemInstance item)
	{
		super.onUnequip(slot, item);

		if(item.isShadowItem())
			item.stopManaConsumeTask();
	}

	@Override
	public void restore()
	{
		final int ownerId = getOwnerId();

		writeLock();
		try
		{
			Collection<ItemInstance> items = _itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getBaseLocation());

			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
			}
			Collections.sort(_items, ItemOrderComparator.getInstance());

			items = _itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getEquipLocation());

			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
				if(item.getEquipSlot() >= PAPERDOLL_MAX || !checkPaperdollItem(item, item.getEquipSlot()) || getPaperdollItem(item.getEquipSlot()) != null)
				{
					// Неверный слот - возвращаем предмет в инвентарь.
					item.setLocation(getBaseLocation());
					item.setLocData(0); // Немного некрасиво, но инвентарь еще весь не загружен и свободный слот не найти
					item.setEquipped(false);
					item.setJdbcState(JdbcEntityState.UPDATED);
					continue;
				}
				setPaperdollItem(item.getEquipSlot(), item);
			}
		}
		finally
		{
			writeUnlock();
		}

		DelayedItemsManager.getInstance().loadDelayed(getActor(), false);

		refreshWeight();
		//checkItems(); Проверку итемов запускаем после входа в игру в EnterWorld для отображения сообщений.
	}

	@Override
	public void store()
	{
		writeLock();
		try
		{
			_itemsDAO.update(_items);
		}
		finally
		{
			writeUnlock();
		}
	}

	@Override
	public void sendAddItem(ItemInstance item)
	{
		Player actor = getActor();

		actor.sendPacket(new InventoryUpdatePacket().addNewItem(actor, item));
		if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
			actor.sendPacket(new ExAdenaInvenCount(actor));
		if (item.getItemId() == ItemTemplate.ITEM_ID_MONEY_L)
			actor.sendPacket(new ExBloodyCoinCount(actor));
		if(item.getTemplate().getAgathionMaxEnergy() > 0)
			actor.sendPacket(new ExBR_AgathionEnergyInfoPacket(1, item));
	}

	@Override
	public void sendModifyItem(ItemInstance... items)
	{
		Player actor = getActor();

		InventoryUpdatePacket iu = new InventoryUpdatePacket();
		for(ItemInstance item : items)
			iu.addModifiedItem(actor, item);

		actor.sendPacket(iu);

		for(ItemInstance item : items)
		{
			if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
				actor.sendPacket(new ExAdenaInvenCount(actor));
			if (item.getItemId() == ItemTemplate.ITEM_ID_MONEY_L)
				actor.sendPacket(new ExBloodyCoinCount(actor));
			if(item.getTemplate().getAgathionMaxEnergy() > 0)
				actor.sendPacket(new ExBR_AgathionEnergyInfoPacket(1, item));
		}
	}

	@Override
	public void sendRemoveItem(ItemInstance item)
	{
		Player actor = getActor();
		actor.sendPacket(new InventoryUpdatePacket().addRemovedItem(actor, item));
		if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
			actor.sendPacket(new ExAdenaInvenCount(actor));
		if (item.getItemId() == ItemTemplate.ITEM_ID_MONEY_L)
			actor.sendPacket(new ExBloodyCoinCount(actor));
	}

	@Override
	public void sendEquipInfo(int slot)
	{
		getActor().broadcastUserInfo(true);
		getActor().sendPacket(new ExUserInfoEquipSlot(getActor(), slot));
	}

	@Override
	protected void onItemVisualTimeEnd(ItemInstance item) {
		if(item.isEquipped())
			sendEquipInfo(item.getEquipSlot());
		sendModifyItem(item);
		// TODO: [Bonux] Должно ли быть какое-то сообщение?
	}

	@Override
	protected void onTemporalItemTimeEnd(ItemInstance item) {
		getActor().sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_EXPIRED).addItemName(item.getItemId()));
	}

	public void startTimers()
	{
		//
	}

	public void stopTimers()
	{
		for(ItemInstance item : getItems()) {
			item.stopManaConsumeTask();
		}
	}

	protected class ManaConsumeTask implements Runnable
	{
		private ItemInstance item;

		ManaConsumeTask(ItemInstance item)
		{
			this.item = item;
		}

		@Override
		public void run()
		{
			Player player = getActor();

			if(!item.isEquipped())
				return;

			int mana = item.getShadowLifeTime();

			if(mana > 0) {
				item.setLifeTime(item.getLifeTime() - 1);
				mana = item.getShadowLifeTime();
			}

			if(mana <= 0)
				destroyItem(item);

			SystemMessage sm = null;
			if(mana == 10)
				sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_10);
			else if(mana == 5)
				sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_5);
			else if(mana == 1)
				sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_1_IT_WILL_DISAPPEAR_SOON);
			else if(mana <= 0)
				sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED);

			if(sm != null)
			{
				sm.addItemName(item.getItemId());
				player.sendPacket(sm);
			}
		}
	}

	@Override
	public int getSize()
	{
		return super.getSize() - getQuestSize();
	}

	private void refreshItemsSize()
	{
		int size = 0;
		for(ItemInstance item : getItems())
		{
			if(item.getTemplate().isQuest())
				size++;
		}
		questItemsSize = size;
	}

	public int getAllSize()
	{
		return super.getSize();
	}

	public int getQuestSize()
	{
		return questItemsSize;
	}
}