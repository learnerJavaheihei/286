package l2s.gameserver.model.items;

import java.util.Comparator;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.model.items.listeners.StatsListener;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.item.ExItemType;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Inventory extends ItemContainer
{
	private static final Logger _log = LoggerFactory.getLogger(Inventory.class);

	public static final int PAPERDOLL_PENDANT = 0;
	public static final int PAPERDOLL_REAR = 1;
	public static final int PAPERDOLL_LEAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_RFINGER = 4;
	public static final int PAPERDOLL_LFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_LRHAND = 14;
	public static final int PAPERDOLL_HAIR = 15;
	public static final int PAPERDOLL_DHAIR = 16;
	public static final int PAPERDOLL_RBRACELET = 17;
	public static final int PAPERDOLL_LBRACELET = 18;
	public static final int PAPERDOLL_AGATHION_MAIN = 19;
	public static final int PAPERDOLL_AGATHION_1 = 20;
	public static final int PAPERDOLL_AGATHION_2 = 21;
	public static final int PAPERDOLL_AGATHION_3 = 22;
	public static final int PAPERDOLL_AGATHION_4 = 23;
	public static final int PAPERDOLL_DECO1 = 24;
	public static final int PAPERDOLL_DECO2 = 25;
	public static final int PAPERDOLL_DECO3 = 26;
	public static final int PAPERDOLL_DECO4 = 27;
	public static final int PAPERDOLL_DECO5 = 28;
	public static final int PAPERDOLL_DECO6 = 29;
	public static final int PAPERDOLL_BELT = 30;
	public static final int PAPERDOLL_BROOCH = 31;
	public static final int PAPERDOLL_JEWEL1 = 32;
	public static final int PAPERDOLL_JEWEL2 = 33;
	public static final int PAPERDOLL_JEWEL3 = 34;
	public static final int PAPERDOLL_JEWEL4 = 35;
	public static final int PAPERDOLL_JEWEL5 = 36;
	public static final int PAPERDOLL_JEWEL6 = 37;
	public static final int PAPERDOLL_ARTIFACT_BOOK = 38;
	public static final int PAPERDOLL_BALANCE_ARTIFACT1 = 39; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT2 = 40; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT3 = 41; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT4 = 42; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT5 = 43; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT6 = 44; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT7 = 45; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT8 = 46; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT9 = 47; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT10 = 48; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT11 = 49; // Artifact Balance
	public static final int PAPERDOLL_BALANCE_ARTIFACT12 = 50; // Artifact Balance
	public static final int PAPERDOLL_SPIRIT_ARTIFACT1 = 51; // Artifact Spirit
	public static final int PAPERDOLL_SPIRIT_ARTIFACT2 = 52; // Artifact Spirit
	public static final int PAPERDOLL_SPIRIT_ARTIFACT3 = 53; // Artifact Spirit
	public static final int PAPERDOLL_PROTECTION_ARTIFACT1 = 54; // Artifact Protection
	public static final int PAPERDOLL_PROTECTION_ARTIFACT2 = 55; // Artifact Protection
	public static final int PAPERDOLL_PROTECTION_ARTIFACT3 = 56; // Artifact Protection
	public static final int PAPERDOLL_SUPPORT_ARTIFACT1 = 57; // Artifact Support
	public static final int PAPERDOLL_SUPPORT_ARTIFACT2 = 58; // Artifact Support
	public static final int PAPERDOLL_SUPPORT_ARTIFACT3 = 59; // Artifact Support

	public static final int PAPERDOLL_MAX = 60;

	public static final int[] PAPERDOLL_ORDER =
	{
			Inventory.PAPERDOLL_PENDANT,
			Inventory.PAPERDOLL_REAR,
			Inventory.PAPERDOLL_LEAR,
			Inventory.PAPERDOLL_NECK,
			Inventory.PAPERDOLL_RFINGER,
			Inventory.PAPERDOLL_LFINGER,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_BACK,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR,
			Inventory.PAPERDOLL_RBRACELET,
			Inventory.PAPERDOLL_LBRACELET,
			Inventory.PAPERDOLL_DECO1,
			Inventory.PAPERDOLL_DECO2,
			Inventory.PAPERDOLL_DECO3,
			Inventory.PAPERDOLL_DECO4,
			Inventory.PAPERDOLL_DECO5,
			Inventory.PAPERDOLL_DECO6,
			Inventory.PAPERDOLL_BELT,
			Inventory.PAPERDOLL_BROOCH,
			Inventory.PAPERDOLL_JEWEL1,
			Inventory.PAPERDOLL_JEWEL2,
			Inventory.PAPERDOLL_JEWEL3,
			Inventory.PAPERDOLL_JEWEL4,
			Inventory.PAPERDOLL_JEWEL5,
			Inventory.PAPERDOLL_JEWEL6,
			Inventory.PAPERDOLL_AGATHION_MAIN,
			Inventory.PAPERDOLL_AGATHION_1,
			Inventory.PAPERDOLL_AGATHION_2,
			Inventory.PAPERDOLL_AGATHION_3,
			Inventory.PAPERDOLL_AGATHION_4,
			Inventory.PAPERDOLL_ARTIFACT_BOOK,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT1,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT2,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT3,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT4,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT5,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT6,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT7,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT8,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT9,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT10,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT11,
			Inventory.PAPERDOLL_BALANCE_ARTIFACT12,
			Inventory.PAPERDOLL_SPIRIT_ARTIFACT1,
			Inventory.PAPERDOLL_SPIRIT_ARTIFACT2,
			Inventory.PAPERDOLL_SPIRIT_ARTIFACT3,
			Inventory.PAPERDOLL_PROTECTION_ARTIFACT1,
			Inventory.PAPERDOLL_PROTECTION_ARTIFACT2,
			Inventory.PAPERDOLL_PROTECTION_ARTIFACT3,
			Inventory.PAPERDOLL_SUPPORT_ARTIFACT1,
			Inventory.PAPERDOLL_SUPPORT_ARTIFACT2,
			Inventory.PAPERDOLL_SUPPORT_ARTIFACT3
	};

	public static final int UPDATE_STATS_FLAG = 1 << 0;
	public static final int UPDATE_SKILLS_FLAG = UPDATE_STATS_FLAG | (1 << 1);

	public static class ItemOrderComparator implements Comparator<ItemInstance>
	{
		private static final Comparator<ItemInstance> instance = new ItemOrderComparator();

		public static final Comparator<ItemInstance> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(ItemInstance o1, ItemInstance o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o1.getLocData() - o2.getLocData();
		}
	}

	protected final int _ownerId;

	protected final ItemInstance[] _paperdoll = new ItemInstance[PAPERDOLL_MAX];

	private final ListenerList<Playable> _listeners = new ListenerList<Playable>();

	protected int _totalWeight;
	// used to quickly check for using of items of special type
	protected long _wearedMask;

	protected Inventory(int ownerId)
	{
		_ownerId = ownerId;

		addListener(StatsListener.getInstance());
	}

	public abstract Playable getActor();

	protected abstract ItemLocation getBaseLocation();

	protected abstract ItemLocation getEquipLocation();

	public int getOwnerId()
	{
		return _ownerId;
	}

	protected void onRestoreItem(ItemInstance item)
	{
		_totalWeight += item.getTemplate().getWeight() * item.getCount();

		IItemHandler handler = item.getTemplate().getHandler();
		if(handler != null)
			handler.onRestoreItem(getActor(), item);
	}

	@Override
	protected void onAddItem(ItemInstance item)
	{
		item.setOwnerId(getOwnerId());
		item.setLocation(getBaseLocation());
		item.setLocData(findSlot(item.getTemplate().isQuest()));
		if(item.getJdbcState().isSavable())
		{
			item.save();
		}
		else
		{
			item.setJdbcState(JdbcEntityState.UPDATED);
			item.update();
		}

		sendAddItem(item);
		refreshWeight();

		IItemHandler handler = item.getTemplate().getHandler();
		if(handler != null)
			handler.onAddItem(getActor(), item);
	}

	@Override
	protected void onModifyItem(ItemInstance item)
	{
		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();

		sendModifyItem(item);
		refreshWeight();
	}

	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		if(item.isEquipped())
			unEquipItem(item);

		sendRemoveItem(item);

		item.setLocData(-1);

		refreshWeight();

		IItemHandler handler = item.getTemplate().getHandler();
		if(handler != null)
			handler.onRemoveItem(getActor(), item);
	}

	@Override
	protected void onDestroyItem(ItemInstance item)
	{
		item.setCount(0L);
		item.delete();
	}

	protected boolean onEquip(int slot, ItemInstance item)
	{
		if(!checkPaperdollItem(item, slot))
			return false;

		item.setLocation(getEquipLocation());
		item.setLocData(slot);
		item.setEquipped(true);
		item.setJdbcState(JdbcEntityState.UPDATED);

		item.onEquip(slot, getActor());

		_wearedMask |= item.getTemplate().getItemMask();

		sendEquipInfo(slot);
		sendModifyItem(item);
		return true;
	}

	protected boolean onReequip(int slot, ItemInstance newItem, ItemInstance oldItem)
	{
		oldItem.setLocation(getBaseLocation());
		oldItem.setLocData(findSlot(oldItem.getTemplate().isQuest()));
		oldItem.setEquipped(false);
		oldItem.setJdbcState(JdbcEntityState.UPDATED);

		oldItem.setChargedSoulshotPower(0);
		oldItem.setChargedSpiritshotPower(0, 0, 0);
		oldItem.setChargedFishshotPower(0);

		oldItem.onUnequip(slot, getActor());

		_wearedMask &= ~oldItem.getTemplate().getItemMask();

		if(checkPaperdollItem(newItem, slot))
		{
			newItem.setLocation(getEquipLocation());
			newItem.setLocData(slot);
			newItem.setEquipped(true);
			newItem.setJdbcState(JdbcEntityState.UPDATED);

			newItem.onEquip(slot, getActor());

			_wearedMask |= newItem.getTemplate().getItemMask();

			sendEquipInfo(slot);
			sendModifyItem(newItem, oldItem);
			return true;
		}
		sendEquipInfo(slot);
		sendModifyItem(oldItem);
		return false;
	}

	protected void onUnequip(int slot, ItemInstance item)
	{
		item.setLocation(getBaseLocation());
		item.setLocData(findSlot(item.getTemplate().isQuest()));
		item.setEquipped(false);
		item.setJdbcState(JdbcEntityState.UPDATED);

		item.setChargedSoulshotPower(0);
		item.setChargedSpiritshotPower(0, 0, 0);
		item.setChargedFishshotPower(0);

		item.onUnequip(slot, getActor());

		_wearedMask &= ~item.getTemplate().getItemMask();

		sendEquipInfo(slot);
		sendModifyItem(item);
	}

	/**
	 * Находит и возвращает пустой слот в инвентаре.
	 */
	private int findSlot(boolean quest)
	{
		ItemInstance item;
		int slot = 0;
		loop: for(slot = 0; slot < _items.size(); slot++)
		{
			for(int i = 0; i < _items.size(); i++)
			{
				item = _items.get(i);
				if(item.isEquipped()) // игнорируем надетое и квестовые вещи
					continue;
				if(!quest && item.getTemplate().isQuest())
					continue;
				if(quest && !item.getTemplate().isQuest())
					continue;
				if(item.getEquipSlot() == slot) // слот занят?
					continue loop;
			}
			break;
		}
		return slot; // слот не занят, возвращаем
	}

	public ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}

	public ItemInstance[] getPaperdollItems()
	{
		return _paperdoll;
	}

	public int getPaperdollItemId(int slot)
	{
		ItemInstance item = getPaperdollItem(slot);
		if(item != null)
			return item.getItemId();
		return 0;
	}

	public int getPaperdollVisualId(int slot)
	{
		ItemInstance item = getPaperdollItem(slot);
		if(item != null)
		{
			if(item.getVisualId() > 0)
				return item.getVisualId();
		}
		return 0;
	}

	public int getPaperdollObjectId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null)
			return item.getObjectId();
		return 0;
	}

	public void addListener(OnEquipListener listener)
	{
		_listeners.add(listener);
	}

	public void removeListener(OnEquipListener listener)
	{
		_listeners.remove(listener);
	}

	public ItemInstance setPaperdollItem(int slot, ItemInstance item)
	{
		ItemInstance old;
		writeLock();
		try
		{
			old = _paperdoll[slot];
			if(old != item)
			{
				if(old != null && item != null)
				{
					_paperdoll[slot] = item;
					onReequip(slot, item, old);
				}
				else
				{
					if(old != null)
					{
						_paperdoll[slot] = null;
						onUnequip(slot, old);
					}
					if(item != null)
					{
						_paperdoll[slot] = item;
						onEquip(slot, item);
					}
				}
			}
		}
		finally
		{
			writeUnlock();
		}
		return old;
	}

	public long getWearedMask()
	{
		return _wearedMask;
	}

	public void unEquipItem(ItemInstance item)
	{
		if(item.isEquipped())
			unEquipItemInBodySlot(item.getBodyPart(), item);
	}

	public void unEquipItemInBodySlot(long bodySlot)
	{
		unEquipItemInBodySlot(bodySlot, null);
	}

	private void unEquipItemInBodySlot(long bodySlot, ItemInstance item)
	{
		int pdollSlot = -1;

		if(bodySlot == ItemTemplate.SLOT_NECK)
			pdollSlot = PAPERDOLL_NECK;
		else if(bodySlot == ItemTemplate.SLOT_L_EAR)
			pdollSlot = PAPERDOLL_LEAR;
		else if(bodySlot == ItemTemplate.SLOT_R_EAR)
			pdollSlot = PAPERDOLL_REAR;
		else if(bodySlot == (ItemTemplate.SLOT_L_EAR | ItemTemplate.SLOT_R_EAR))
		{
			if(item == null)
				return;
			if(getPaperdollItem(PAPERDOLL_LEAR) == item)
				pdollSlot = PAPERDOLL_LEAR;
			if(getPaperdollItem(PAPERDOLL_REAR) == item)
				pdollSlot = PAPERDOLL_REAR;
		}
		else if(bodySlot == ItemTemplate.SLOT_L_FINGER)
			pdollSlot = PAPERDOLL_LFINGER;
		else if(bodySlot == ItemTemplate.SLOT_R_FINGER)
			pdollSlot = PAPERDOLL_RFINGER;
		else if(bodySlot == (ItemTemplate.SLOT_L_FINGER | ItemTemplate.SLOT_R_FINGER))
		{
			if(item == null)
				return;
			if(getPaperdollItem(PAPERDOLL_LFINGER) == item)
				pdollSlot = PAPERDOLL_LFINGER;
			if(getPaperdollItem(PAPERDOLL_RFINGER) == item)
				pdollSlot = PAPERDOLL_RFINGER;
		}
		else if(bodySlot == ItemTemplate.SLOT_HAIR)
			pdollSlot = PAPERDOLL_HAIR;
		else if(bodySlot == ItemTemplate.SLOT_DHAIR)
			pdollSlot = PAPERDOLL_DHAIR;
		else if(bodySlot == ItemTemplate.SLOT_HAIRALL)
		{
			setPaperdollItem(PAPERDOLL_DHAIR, null); // This should be the same as in DHAIR
			pdollSlot = PAPERDOLL_HAIR;
		}
		else if(bodySlot == ItemTemplate.SLOT_HEAD)
			pdollSlot = PAPERDOLL_HEAD;
		else if(bodySlot == ItemTemplate.SLOT_R_HAND)
			pdollSlot = PAPERDOLL_RHAND;
		else if(bodySlot == ItemTemplate.SLOT_L_HAND)
			pdollSlot = PAPERDOLL_LHAND;
		else if(bodySlot == ItemTemplate.SLOT_GLOVES)
			pdollSlot = PAPERDOLL_GLOVES;
		else if(bodySlot == ItemTemplate.SLOT_LEGS)
			pdollSlot = PAPERDOLL_LEGS;
		else if(bodySlot == ItemTemplate.SLOT_CHEST || bodySlot == ItemTemplate.SLOT_FULL_ARMOR || bodySlot == ItemTemplate.SLOT_FORMAL_WEAR)
			pdollSlot = PAPERDOLL_CHEST;
		else if(bodySlot == ItemTemplate.SLOT_BACK)
			pdollSlot = PAPERDOLL_BACK;
		else if(bodySlot == ItemTemplate.SLOT_FEET)
			pdollSlot = PAPERDOLL_FEET;
		else if(bodySlot == ItemTemplate.SLOT_BELT)
			pdollSlot = PAPERDOLL_BELT;
		else if(bodySlot == ItemTemplate.SLOT_LR_HAND)
		{
			setPaperdollItem(PAPERDOLL_LHAND, null);
			pdollSlot = PAPERDOLL_RHAND;
		}
		else if(bodySlot == ItemTemplate.SLOT_PENDANT)
			pdollSlot = PAPERDOLL_PENDANT;
		else if(bodySlot == ItemTemplate.SLOT_L_BRACELET)
		{
			pdollSlot = PAPERDOLL_LBRACELET;
			// При снятии левого браслета, снимаем и агатионы тоже
			setPaperdollItem(PAPERDOLL_AGATHION_MAIN, null);
			setPaperdollItem(PAPERDOLL_AGATHION_1, null);
			setPaperdollItem(PAPERDOLL_AGATHION_2, null);
			setPaperdollItem(PAPERDOLL_AGATHION_3, null);
			setPaperdollItem(PAPERDOLL_AGATHION_4, null);
		}
		else if(bodySlot == ItemTemplate.SLOT_R_BRACELET)
		{
			pdollSlot = PAPERDOLL_RBRACELET;
			// При снятии правого браслета, снимаем и талисманы тоже
			setPaperdollItem(PAPERDOLL_DECO1, null);
			setPaperdollItem(PAPERDOLL_DECO2, null);
			setPaperdollItem(PAPERDOLL_DECO3, null);
			setPaperdollItem(PAPERDOLL_DECO4, null);
			setPaperdollItem(PAPERDOLL_DECO5, null);
			setPaperdollItem(PAPERDOLL_DECO6, null);
		}
		else if(bodySlot == ItemTemplate.SLOT_DECO)
		{
			if(item == null)
				return;
			else if(getPaperdollItem(PAPERDOLL_DECO1) == item)
				pdollSlot = PAPERDOLL_DECO1;
			else if(getPaperdollItem(PAPERDOLL_DECO2) == item)
				pdollSlot = PAPERDOLL_DECO2;
			else if(getPaperdollItem(PAPERDOLL_DECO3) == item)
				pdollSlot = PAPERDOLL_DECO3;
			else if(getPaperdollItem(PAPERDOLL_DECO4) == item)
				pdollSlot = PAPERDOLL_DECO4;
			else if(getPaperdollItem(PAPERDOLL_DECO5) == item)
				pdollSlot = PAPERDOLL_DECO5;
			else if(getPaperdollItem(PAPERDOLL_DECO6) == item)
				pdollSlot = PAPERDOLL_DECO6;
		}
		else if(bodySlot == ItemTemplate.SLOT_BROOCH)
		{
			pdollSlot = PAPERDOLL_BROOCH;
			// При снятии брошки, снимаем и камни тоже
			setPaperdollItem(Inventory.PAPERDOLL_JEWEL1, null);
			setPaperdollItem(Inventory.PAPERDOLL_JEWEL2, null);
			setPaperdollItem(Inventory.PAPERDOLL_JEWEL3, null);
			setPaperdollItem(Inventory.PAPERDOLL_JEWEL4, null);
			setPaperdollItem(Inventory.PAPERDOLL_JEWEL5, null);
			setPaperdollItem(Inventory.PAPERDOLL_JEWEL6, null);
		}
		else if(bodySlot == ItemTemplate.SLOT_JEWEL)
		{
			if(item == null)
				return;
			else if(getPaperdollItem(PAPERDOLL_JEWEL1) == item)
				pdollSlot = PAPERDOLL_JEWEL1;
			else if(getPaperdollItem(PAPERDOLL_JEWEL2) == item)
				pdollSlot = PAPERDOLL_JEWEL2;
			else if(getPaperdollItem(PAPERDOLL_JEWEL3) == item)
				pdollSlot = PAPERDOLL_JEWEL3;
			else if(getPaperdollItem(PAPERDOLL_JEWEL4) == item)
				pdollSlot = PAPERDOLL_JEWEL4;
			else if(getPaperdollItem(PAPERDOLL_JEWEL5) == item)
				pdollSlot = PAPERDOLL_JEWEL5;
			else if(getPaperdollItem(PAPERDOLL_JEWEL6) == item)
				pdollSlot = PAPERDOLL_JEWEL6;
		}
		else if(bodySlot == ItemTemplate.SLOT_AGATHION)
		{
			if(item == null)
				return;
			else if(getPaperdollItem(PAPERDOLL_AGATHION_MAIN) == item)
				pdollSlot = PAPERDOLL_AGATHION_MAIN;
			else if(getPaperdollItem(PAPERDOLL_AGATHION_1) == item)
				pdollSlot = PAPERDOLL_AGATHION_1;
			else if(getPaperdollItem(PAPERDOLL_AGATHION_2) == item)
				pdollSlot = PAPERDOLL_AGATHION_2;
			else if(getPaperdollItem(PAPERDOLL_AGATHION_3) == item)
				pdollSlot = PAPERDOLL_AGATHION_3;
			else if(getPaperdollItem(PAPERDOLL_AGATHION_4) == item)
				pdollSlot = PAPERDOLL_AGATHION_4;
		}
		else
		{
			_log.warn("Requested invalid body slot: " + bodySlot + ", Item: " + item + ", ownerId: '" + getOwnerId() + "'");
			return;
		}

		if(pdollSlot >= 0)
			setPaperdollItem(pdollSlot, null);
	}

	public void equipItem(ItemInstance item)
	{
		long bodySlot = item.getBodyPart();

		//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
		double hp = getActor().getCurrentHp();
		double mp = getActor().getCurrentMp();
		double cp = getActor().getCurrentCp();

		if(bodySlot == ItemTemplate.SLOT_LR_HAND)
		{
			setPaperdollItem(PAPERDOLL_LHAND, null);
			setPaperdollItem(PAPERDOLL_RHAND, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_L_HAND)
		{
			final ItemInstance rHandItem = getPaperdollItem(PAPERDOLL_RHAND);

			final ItemTemplate rHandItemTemplate = rHandItem == null ? null : rHandItem.getTemplate();
			final ItemTemplate newItem = item.getTemplate();

//			if(newItem.getItemType() == EtcItemType.ARROW || newItem.getItemType() == EtcItemType.ARROW_QUIVER)
//			{
//				System.out.println(1);
//				// arrows can be equipped only with bow
//				if(rHandItemTemplate == null)
//					return;
//				if(rHandItemTemplate.getItemType() != WeaponType.BOW)
//					return;
//				if(rHandItemTemplate.getGrade().extOrdinal() != newItem.getGrade().extOrdinal())
//					return;
//			}
//			else if(newItem.getItemType() == EtcItemType.BOLT || newItem.getItemType() == EtcItemType.BOLT_QUIVER)
//			{
//				// bolts can be equipped only with crossbow
//				if(rHandItemTemplate == null)
//					return;
//				if(rHandItemTemplate.getItemType() != WeaponType.CROSSBOW && rHandItemTemplate.getItemType() != WeaponType.TWOHANDCROSSBOW)
//					return;
//				if(rHandItemTemplate.getGrade().extOrdinal() != newItem.getGrade().extOrdinal())
//					return;
//			}
			if(newItem.getItemType() == EtcItemType.LURE)
			{
				// baits can be equipped only with rods
				if(rHandItemTemplate == null)
					return;
				if(rHandItemTemplate.getItemType() != WeaponType.ROD)
					return;
			}
			else
			{
				// unequip two-hand weapon 
				if(rHandItemTemplate != null && rHandItemTemplate.getBodyPart() == ItemTemplate.SLOT_LR_HAND && newItem.getExType() != ExItemType.SIGIL)
					setPaperdollItem(PAPERDOLL_RHAND, null);
			}

			setPaperdollItem(PAPERDOLL_LHAND, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_R_HAND)
		{
			final ItemInstance lHandItem = getPaperdollItem(PAPERDOLL_LHAND);
			if(lHandItem != null)
			{
				final ItemTemplate lHandItemTemplate = lHandItem.getTemplate();
				final ItemTemplate newItem = item.getTemplate();

				if(lHandItemTemplate.getItemType() == EtcItemType.ARROW || lHandItemTemplate.getItemType() == EtcItemType.ARROW_QUIVER)
				{
					if(newItem.getItemType() != WeaponType.BOW || newItem.getGrade().extOrdinal() != newItem.getGrade().extOrdinal())
						setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				else if(lHandItemTemplate.getItemType() == EtcItemType.BOLT || lHandItemTemplate.getItemType() == EtcItemType.BOLT_QUIVER)
				{
					if(newItem.getItemType() != WeaponType.CROSSBOW && newItem.getItemType() != WeaponType.TWOHANDCROSSBOW || newItem.getGrade().extOrdinal() != newItem.getGrade().extOrdinal())
						setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				else if(lHandItemTemplate.getItemType() == EtcItemType.LURE)
				{
					if(newItem.getItemType() != WeaponType.ROD)
						setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
			setPaperdollItem(PAPERDOLL_RHAND, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_L_EAR || bodySlot == ItemTemplate.SLOT_R_EAR || bodySlot == (ItemTemplate.SLOT_L_EAR | ItemTemplate.SLOT_R_EAR))
		{
			if(_paperdoll[PAPERDOLL_LEAR] == null)
				setPaperdollItem(PAPERDOLL_LEAR, item);
			else if(_paperdoll[PAPERDOLL_REAR] == null)
				setPaperdollItem(PAPERDOLL_REAR, item);
			else
			{
				double lEarMDef = 0.;
				final FuncTemplate[] lEarFuncTemplates = _paperdoll[PAPERDOLL_LEAR].getTemplate().getAttachedFuncs();
				for(FuncTemplate func : lEarFuncTemplates)
				{
					if(func._stat == Stats.MAGIC_DEFENCE)
					{
						lEarMDef = func._value;
						break;
					}
				}

				double rEarMDef = 0.;
				final FuncTemplate[] rEarFuncTemplates = _paperdoll[PAPERDOLL_REAR].getTemplate().getAttachedFuncs();
				for(FuncTemplate func : rEarFuncTemplates)
				{
					if(func._stat == Stats.MAGIC_DEFENCE)
					{
						rEarMDef = func._value;
						break;
					}
				}

				if(lEarMDef > rEarMDef)
					setPaperdollItem(PAPERDOLL_REAR, item);
				else
					setPaperdollItem(PAPERDOLL_LEAR, item);
			}
		}
		else if(bodySlot == ItemTemplate.SLOT_L_FINGER || bodySlot == ItemTemplate.SLOT_R_FINGER || bodySlot == (ItemTemplate.SLOT_L_FINGER | ItemTemplate.SLOT_R_FINGER))
		{
			if(_paperdoll[PAPERDOLL_LFINGER] == null)
				setPaperdollItem(PAPERDOLL_LFINGER, item);
			else if(_paperdoll[PAPERDOLL_RFINGER] == null)
				setPaperdollItem(PAPERDOLL_RFINGER, item);
			else
			{
				double lFingerMDef = 0.;
				final FuncTemplate[] lFingerFuncTemplates = _paperdoll[PAPERDOLL_LFINGER].getTemplate().getAttachedFuncs();
				for(FuncTemplate func : lFingerFuncTemplates)
				{
					if(func._stat == Stats.MAGIC_DEFENCE)
					{
						lFingerMDef = func._value;
						break;
					}
				}

				double rFingerMDef = 0.;
				final FuncTemplate[] rFingerFuncTemplates = _paperdoll[PAPERDOLL_RFINGER].getTemplate().getAttachedFuncs();
				for(FuncTemplate func : rFingerFuncTemplates)
				{
					if(func._stat == Stats.MAGIC_DEFENCE)
					{
						rFingerMDef = func._value;
						break;
					}
				}

				if(lFingerMDef > rFingerMDef)
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				else
					setPaperdollItem(PAPERDOLL_LFINGER, item);
			}
		}
		else if(bodySlot == ItemTemplate.SLOT_NECK)
			setPaperdollItem(PAPERDOLL_NECK, item);
		else if(bodySlot == ItemTemplate.SLOT_FULL_ARMOR)
		{
			setPaperdollItem(PAPERDOLL_LEGS, null);
			setPaperdollItem(PAPERDOLL_CHEST, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_CHEST)
			setPaperdollItem(PAPERDOLL_CHEST, item);
		else if(bodySlot == ItemTemplate.SLOT_LEGS)
		{
			// handle full armor
			ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
			if(chest != null && chest.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
				setPaperdollItem(PAPERDOLL_CHEST, null);
			else if(getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
				setPaperdollItem(PAPERDOLL_CHEST, null);

			setPaperdollItem(PAPERDOLL_LEGS, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_FEET)
		{
			if(getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
				setPaperdollItem(PAPERDOLL_CHEST, null);
			setPaperdollItem(PAPERDOLL_FEET, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_GLOVES)
		{
			if(getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
				setPaperdollItem(PAPERDOLL_CHEST, null);
			setPaperdollItem(PAPERDOLL_GLOVES, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_HEAD)
		{
			if(getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
				setPaperdollItem(PAPERDOLL_CHEST, null);
			setPaperdollItem(PAPERDOLL_HEAD, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_HAIR)
		{
			setPaperdollItem(PAPERDOLL_HAIR, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_DHAIR)
		{
			ItemInstance slot2 = getPaperdollItem(PAPERDOLL_HAIR);
			if(slot2 != null && slot2.getBodyPart() == ItemTemplate.SLOT_HAIRALL)
				setPaperdollItem(PAPERDOLL_HAIR, null);
			setPaperdollItem(PAPERDOLL_DHAIR, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_HAIRALL)
		{
			setPaperdollItem(PAPERDOLL_HAIR, item);
			setPaperdollItem(PAPERDOLL_DHAIR, null);
		}
		else if(bodySlot == ItemTemplate.SLOT_R_BRACELET)
		{
			setPaperdollItem(PAPERDOLL_RBRACELET, item);
			// При смене браслета, снимаем талисманы (TODO: Сверить с оффом.)
			for (int p = PAPERDOLL_DECO1; p <= PAPERDOLL_DECO6; p++)
				setPaperdollItem(p, null);
		}
		else if(bodySlot == ItemTemplate.SLOT_L_BRACELET)
		{
			setPaperdollItem(PAPERDOLL_LBRACELET, item);
			// При смене браслета, снимаем агатионы (TODO: Сверить с оффом.)
			for (int p = PAPERDOLL_AGATHION_MAIN; p <= PAPERDOLL_AGATHION_4; p++)
				setPaperdollItem(p, null);
		}
		else if(bodySlot == ItemTemplate.SLOT_PENDANT)
			setPaperdollItem(PAPERDOLL_PENDANT, item);
		else if(bodySlot == ItemTemplate.SLOT_BACK)
			setPaperdollItem(PAPERDOLL_BACK, item);
		else if(bodySlot == ItemTemplate.SLOT_BELT)
			setPaperdollItem(PAPERDOLL_BELT, item);
		else if(bodySlot == ItemTemplate.SLOT_DECO)
		{
			for (int p = PAPERDOLL_DECO1; p <= PAPERDOLL_DECO6; p++) {
				if (_paperdoll[p] == null) {
					setPaperdollItem(p, item);
					break;
				}
			}
		}
		else if(bodySlot == ItemTemplate.SLOT_FORMAL_WEAR)
		{
			// При одевании свадебного платья руки не трогаем
			setPaperdollItem(PAPERDOLL_LEGS, null);
			setPaperdollItem(PAPERDOLL_HEAD, null);
			setPaperdollItem(PAPERDOLL_FEET, null);
			setPaperdollItem(PAPERDOLL_GLOVES, null);
			setPaperdollItem(PAPERDOLL_CHEST, item);
		}
		else if(bodySlot == ItemTemplate.SLOT_BROOCH)
		{
			setPaperdollItem(PAPERDOLL_BROOCH, item);
			// При смене брошки, снимаем камни (TODO: Сверить с оффом.)
			for (int p = PAPERDOLL_JEWEL1; p <= PAPERDOLL_JEWEL6; p++)
				setPaperdollItem(p, null);
		}
		else if(bodySlot == ItemTemplate.SLOT_JEWEL)
		{
			for (int p = PAPERDOLL_JEWEL1; p <= PAPERDOLL_JEWEL6; p++) {
				if (_paperdoll[p] == null) {
					setPaperdollItem(p, item);
					break;
				}
			}
		}
		else if(bodySlot == ItemTemplate.SLOT_AGATHION)
		{
			for (int p = PAPERDOLL_AGATHION_MAIN; p <= PAPERDOLL_AGATHION_4; p++) {
				if (_paperdoll[p] == null) {
					setPaperdollItem(p, item);
					break;
				}
			}
		}
		else
		{
			_log.warn("unknown body slot:" + bodySlot + " for item id: " + item.getItemId());
			return;
		}

		//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
		getActor().setCurrentHp(hp, false);
		getActor().setCurrentMp(mp);
		getActor().setCurrentCp(cp);

		if(getActor().isPlayer())
			((Player) getActor()).autoShot();
	}

	public abstract void sendAddItem(ItemInstance item);

	public abstract void sendModifyItem(ItemInstance... items);

	public abstract void sendRemoveItem(ItemInstance item);

	public void sendEquipInfo(int slot)
	{
		//
	}

	/**
	 * Refresh the weight of equipment loaded
	 */
	protected void refreshWeight()
	{
		int weight = 0;

		readLock();
		try
		{
			ItemInstance item;
			for(int i = 0; i < _items.size(); i++)
			{
				item = _items.get(i);
				weight += item.getTemplate().getWeight() * item.getCount();
			}
		}
		finally
		{
			readUnlock();
		}

		if(_totalWeight == weight)
			return;

		_totalWeight = weight;

		onRefreshWeight();
	}

	protected abstract void onRefreshWeight();

	public int getTotalWeight()
	{
		return _totalWeight;
	}

	public boolean validateCapacity(ItemInstance item)
	{
		long slots = 0;
		if(!item.isStackable() || getItemByItemId(item.getItemId()) == null)
			slots++;
		return validateCapacity(slots);
	}

	public boolean validateCapacity(int itemId, long count)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return validateCapacity(item, count);
	}

	public boolean validateCapacity(ItemTemplate item, long count)
	{
		long slots = 0;
		if(!item.isStackable() || getItemByItemId(item.getItemId()) == null)
			slots = count;
		return validateCapacity(slots);
	}

	public boolean validateCapacity(long slots)
	{
		if(slots == 0)
			return true;
		if(slots < Integer.MIN_VALUE || slots > Integer.MAX_VALUE)
			return false;
		if(getSize() + (int) slots < 0)
			return false;
		return getSize() + slots <= getActor().getInventoryLimit();
	}

	public boolean validateWeight(ItemInstance item)
	{
		long weight = item.getTemplate().getWeight() * item.getCount();
		return validateWeight(weight);
	}

	public boolean validateWeight(int itemId, long count)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return validateWeight(item, count);
	}

	public boolean validateWeight(ItemTemplate item, long count)
	{
		long weight = item.getWeight() * count;
		return validateWeight(weight);
	}

	public boolean validateWeight(long weight)
	{
		if(weight == 0L)
			return true;
		if(weight < Integer.MIN_VALUE || weight > Integer.MAX_VALUE)
			return false;
		if(getTotalWeight() + (int) weight < 0)
			return false;
		return getTotalWeight() + weight <= getActor().getMaxLoad();
	}

	public abstract void restore();

	public abstract void store();

	public static boolean checkPaperdollItem(ItemInstance item, int paperdoll)
	{
		return ArrayUtils.contains(getPaperdollIndexes(item.getBodyPart()), paperdoll);
	}

	public static int[] getPaperdollIndexes(long slot)
	{
		if(slot == ItemTemplate.SLOT_PENDANT)
			return new int[]{ PAPERDOLL_PENDANT };
		if(slot == ItemTemplate.SLOT_R_EAR)
			return new int[]{ PAPERDOLL_REAR };
		if(slot == ItemTemplate.SLOT_L_EAR)
			return new int[]{ PAPERDOLL_LEAR };
		if(slot == (ItemTemplate.SLOT_L_EAR | ItemTemplate.SLOT_R_EAR))
			return new int[]{ PAPERDOLL_REAR, PAPERDOLL_LEAR };
		if(slot == ItemTemplate.SLOT_NECK)
			return new int[]{ PAPERDOLL_NECK };
		if(slot == ItemTemplate.SLOT_R_FINGER)
			return new int[]{ PAPERDOLL_RFINGER };
		if(slot == ItemTemplate.SLOT_L_FINGER)
			return new int[]{ PAPERDOLL_LFINGER };
		if(slot == (ItemTemplate.SLOT_L_FINGER | ItemTemplate.SLOT_R_FINGER))
			return new int[]{ PAPERDOLL_RFINGER, PAPERDOLL_LFINGER };
		if(slot == ItemTemplate.SLOT_HEAD)
			return new int[]{ PAPERDOLL_HEAD };
		if(slot == ItemTemplate.SLOT_R_HAND)
			return new int[]{ PAPERDOLL_RHAND };
		if(slot == ItemTemplate.SLOT_L_HAND)
			return new int[]{ PAPERDOLL_LHAND };
		if(slot == ItemTemplate.SLOT_LR_HAND)
			return new int[]{ PAPERDOLL_RHAND, PAPERDOLL_LHAND, PAPERDOLL_LRHAND };
		if(slot == ItemTemplate.SLOT_GLOVES)
			return new int[]{ PAPERDOLL_GLOVES };
		if(slot == ItemTemplate.SLOT_CHEST || slot == ItemTemplate.SLOT_FULL_ARMOR || slot == ItemTemplate.SLOT_FORMAL_WEAR)
			return new int[]{ PAPERDOLL_CHEST };
		if(slot == ItemTemplate.SLOT_LEGS)
			return new int[]{ PAPERDOLL_LEGS };
		if(slot == ItemTemplate.SLOT_FEET)
			return new int[]{ PAPERDOLL_FEET };
		if(slot == ItemTemplate.SLOT_BACK)
			return new int[]{ PAPERDOLL_BACK };
		if(slot == ItemTemplate.SLOT_HAIR)
			return new int[]{ PAPERDOLL_HAIR };
		if(slot == ItemTemplate.SLOT_DHAIR)
			return new int[]{ PAPERDOLL_DHAIR };
		if(slot == ItemTemplate.SLOT_HAIRALL)
			return new int[]{ PAPERDOLL_HAIR, PAPERDOLL_DHAIR };
		if(slot == ItemTemplate.SLOT_R_BRACELET)
			return new int[]{ PAPERDOLL_RBRACELET };
		if(slot == ItemTemplate.SLOT_L_BRACELET)
			return new int[]{ PAPERDOLL_LBRACELET };
		if(slot == ItemTemplate.SLOT_DECO)
			return new int[]{ PAPERDOLL_DECO1, PAPERDOLL_DECO2, PAPERDOLL_DECO3, PAPERDOLL_DECO4, PAPERDOLL_DECO5, PAPERDOLL_DECO6 };
		if(slot == ItemTemplate.SLOT_BELT)
			return new int[]{ PAPERDOLL_BELT };
		if(slot == ItemTemplate.SLOT_BROOCH)
			return new int[]{ PAPERDOLL_BROOCH };
		if(slot == ItemTemplate.SLOT_JEWEL)
			return new int[]{ PAPERDOLL_JEWEL1, PAPERDOLL_JEWEL2, PAPERDOLL_JEWEL3, PAPERDOLL_JEWEL4, PAPERDOLL_JEWEL5, PAPERDOLL_JEWEL6 };
		if(slot == ItemTemplate.SLOT_AGATHION)
			return new int[]{ PAPERDOLL_AGATHION_MAIN, PAPERDOLL_AGATHION_1, PAPERDOLL_AGATHION_2, PAPERDOLL_AGATHION_3, PAPERDOLL_AGATHION_4 };
		return new int[]{ -1 };
	}

	public static int getPaperdollIndex(long slot)
	{
		if(slot == ItemTemplate.SLOT_PENDANT)
			return PAPERDOLL_PENDANT;
		if(slot == ItemTemplate.SLOT_R_EAR)
			return PAPERDOLL_REAR;
		if(slot == ItemTemplate.SLOT_L_EAR)
			return PAPERDOLL_LEAR;
		if(slot == ItemTemplate.SLOT_NECK)
			return PAPERDOLL_NECK;
		if(slot == ItemTemplate.SLOT_R_FINGER)
			return PAPERDOLL_RFINGER;
		if(slot == ItemTemplate.SLOT_L_FINGER)
			return PAPERDOLL_LFINGER;
		if(slot == ItemTemplate.SLOT_HEAD)
			return PAPERDOLL_HEAD;
		if(slot == ItemTemplate.SLOT_R_HAND)
			return PAPERDOLL_RHAND;
		if(slot == ItemTemplate.SLOT_L_HAND)
			return PAPERDOLL_LHAND;
		if(slot == ItemTemplate.SLOT_GLOVES)
			return PAPERDOLL_GLOVES;
		if(slot == ItemTemplate.SLOT_CHEST || slot == ItemTemplate.SLOT_FULL_ARMOR || slot == ItemTemplate.SLOT_FORMAL_WEAR)
			return PAPERDOLL_CHEST;
		if(slot == ItemTemplate.SLOT_LEGS)
			return PAPERDOLL_LEGS;
		if(slot == ItemTemplate.SLOT_FEET)
			return PAPERDOLL_FEET;
		if(slot == ItemTemplate.SLOT_BACK)
			return PAPERDOLL_BACK;
		if(slot == ItemTemplate.SLOT_HAIR)
			return PAPERDOLL_HAIR;
		if(slot == ItemTemplate.SLOT_DHAIR)
			return PAPERDOLL_DHAIR;
		if(slot == ItemTemplate.SLOT_R_BRACELET)
			return PAPERDOLL_RBRACELET;
		if(slot == ItemTemplate.SLOT_L_BRACELET)
			return PAPERDOLL_LBRACELET;
		if(slot == ItemTemplate.SLOT_DECO)
			return PAPERDOLL_DECO1;
		if(slot == ItemTemplate.SLOT_DECO2)
			return PAPERDOLL_DECO2;
		if(slot == ItemTemplate.SLOT_DECO3)
			return PAPERDOLL_DECO3;
		if(slot == ItemTemplate.SLOT_DECO4)
			return PAPERDOLL_DECO4;
		if(slot == ItemTemplate.SLOT_DECO5)
			return PAPERDOLL_DECO5;
		if(slot == ItemTemplate.SLOT_DECO6)
			return PAPERDOLL_DECO6;
		if(slot == ItemTemplate.SLOT_BELT)
			return PAPERDOLL_BELT;
		if(slot == ItemTemplate.SLOT_BROOCH)
			return PAPERDOLL_BROOCH;
		if(slot == ItemTemplate.SLOT_JEWEL)
			return PAPERDOLL_JEWEL1;
		if(slot == ItemTemplate.SLOT_JEWEL2)
			return PAPERDOLL_JEWEL2;
		if(slot == ItemTemplate.SLOT_JEWEL3)
			return PAPERDOLL_JEWEL3;
		if(slot == ItemTemplate.SLOT_JEWEL4)
			return PAPERDOLL_JEWEL4;
		if(slot == ItemTemplate.SLOT_JEWEL5)
			return PAPERDOLL_JEWEL5;
		if(slot == ItemTemplate.SLOT_JEWEL6)
			return PAPERDOLL_JEWEL6;
		if(slot == ItemTemplate.SLOT_AGATHION_MAIN)
			return PAPERDOLL_AGATHION_MAIN;
		if(slot == ItemTemplate.SLOT_AGATHION_1)
			return PAPERDOLL_AGATHION_1;
		if(slot == ItemTemplate.SLOT_AGATHION_2)
			return PAPERDOLL_AGATHION_2;
		if(slot == ItemTemplate.SLOT_AGATHION_3)
			return PAPERDOLL_AGATHION_3;
		if(slot == ItemTemplate.SLOT_AGATHION_4)
			return PAPERDOLL_AGATHION_4;
		return -1;
	}

	public ListenerList<Playable> getListeners()
	{
		return _listeners;
	}
}