package l2s.gameserver.model.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.listener.Listener;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.dao.ItemsEnsoulDAO;
import l2s.gameserver.data.xml.holder.AppearanceStoneHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PlayerGroup;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.attachment.ItemAttachment;
import l2s.gameserver.network.l2.s2c.DropItemPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SpawnItemPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.item.ExItemType;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemType;
import l2s.gameserver.templates.item.support.AppearanceStone;
import l2s.gameserver.templates.item.support.Ensoul;
import l2s.gameserver.utils.ItemFunctions;

import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public final class ItemInstance extends GameObject implements JdbcEntity
{
	public static final int[] EMPTY_ENCHANT_OPTIONS = new int[3];
	public static final Ensoul[] EMPTY_ENSOULS_ARRAY = new Ensoul[0];

	private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();

	/** Enumeration of locations for item */
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		PET_INVENTORY,
		PET_PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		FREIGHT, // востановлен, используется в Dimension Manager
		//@Deprecated
		LEASE,
		MAIL
	}

	public static final int FLAG_NO_DROP = 1 << 0;
	public static final int FLAG_NO_TRADE = 1 << 1;
	public static final int FLAG_NO_TRANSFER = 1 << 2;
	public static final int FLAG_NO_CRYSTALLIZE = 1 << 3;
	public static final int FLAG_NO_ENCHANT = 1 << 4;
	public static final int FLAG_NO_DESTROY = 1 << 5;
	public static final int FLAG_NO_SHAPE_SHIFTING = 1 << 6;
	public static final int FLAG_LIFE_TIME = 1 << 6;

	/** ID of the owner */
	private int ownerId;
	/** ID of the item */
	private int itemId;
	/** Quantity of the item */
	private long count;
	/** Level of enchantment of the item */
	private int enchantLevel = -1;
	/** Location of the item */
	private ItemLocation loc;
	/** Slot where item is stored */
	private int locData;
	/** Custom item types (used loto, race tickets) */
	private int customType1;
	private int customType2;
	/** Время жизни временных вещей */
	private int lifeTime;
	/** Спецфлаги для конкретного инстанса */
	private int customFlags;
	/** Атрибуты вещи */
	private ItemAttributes attrs = new ItemAttributes();
	/** Аугментация вещи */
	private int[] _enchantOptions = EMPTY_ENCHANT_OPTIONS;

	/** Object L2Item associated to the item */
	private ItemTemplate template;
	/** Флаг, что вещь одета, выставляется в инвентаре **/
	private boolean isEquipped;

	/** Item drop time for autodestroy task */
	private long _dropTime;

	private IntSet _dropPlayers = Containers.EMPTY_INT_SET;
	private long _dropTimeOwner;

	// Charged shot's power.
	private double _chargedSoulshotPower = 0;
	private double _chargedSpiritshotPower = 0;
	private double _chargedSpiritshotHealBonus = 0;
	private double _chargedFishshotPower = 0;

	private int _agathionEnergy;
	private int _visualId;

	private int _variationStoneId = 0;
	private int _variation1Id = 0;
	private int _variation2Id = 0;

	private ItemAttachment _attachment;
	private JdbcEntityState _state = JdbcEntityState.CREATED;

	private int _appearanceStoneId = 0;
	private List<SkillEntry> _appearanceStoneSkills = null;

	private Map<Integer, Ensoul> _normalEnsouls = null;
	private Map<Integer, Ensoul> _specialEnsouls = null;

	private final Lock _onEquipUnequipLock = new ReentrantLock();

	private Map<Object, IntObjectMap<SkillEntry>> _equippedSkills = null;
	private Map<Object, IntObjectMap<OptionDataTemplate>> _equippedOptionDatas = null;
	
	private boolean _blessed;

	public ItemInstance(int objectId)
	{
		super(objectId);
	}

	/**
	 * Constructor<?> of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		setItemId(itemId);
		setLifeTime(-1);
		setAgathionEnergy(getTemplate().getAgathionEnergy());
		setLocData(-1);
		setEnchantLevel(getTemplate().getBaseEnchantLevel());
	}

	public int getOwnerId()
	{
		return ownerId;
	}

	public void setOwnerId(int ownerId)
	{
		this.ownerId = ownerId;
	}

	public int getItemId()
	{
		return itemId;
	}

	public void setItemId(int id)
	{
		itemId = id;
		template = ItemHolder.getInstance().getTemplate(id);
		setCustomFlags(getCustomFlags());
	}

	public long getCount()
	{
		return count;
	}

	public void setCount(long count)
	{
		if(count < 0)
			count = 0;

		if(!isStackable() && count > 1L)
		{
			this.count = 1L;
			//TODO audit
			return;
		}

		this.count = count;
	}

	public int getEnchantLevel()
	{
		return enchantLevel;
	}

	public int getFixedEnchantLevel(Player owner)
	{
		if(owner != null)
		{
			if(enchantLevel > 0)
			{
				if(Config.OLYMPIAD_ENABLE_ENCHANT_LIMIT && owner.isInOlympiadMode())
				{
					if(isWeapon())
						return Math.min(Config.OLYMPIAD_WEAPON_ENCHANT_LIMIT, enchantLevel);
					if(isArmor())
						return Math.min(Config.OLYMPIAD_ARMOR_ENCHANT_LIMIT, enchantLevel);
					if(isAccessory())
						return Math.min(Config.OLYMPIAD_JEWEL_ENCHANT_LIMIT, enchantLevel);
				}
			}
		}
		return enchantLevel;
	}

	public void setEnchantLevel(int value)
	{
		final int old = enchantLevel;

		enchantLevel = Math.max(getTemplate().getBaseEnchantLevel(), value);
		_enchantOptions = EMPTY_ENCHANT_OPTIONS;

		if(old != enchantLevel && getTemplate().getEnchantOptions().size() > 0)
		{
			int[] enchantOptions = null;
			for(int i = enchantLevel; i >= 0; i--) {
				enchantOptions = getTemplate().getEnchantOptions().get(enchantLevel);
				if(enchantOptions != null) {
					_enchantOptions = enchantOptions;
					break;
				}
			}
		}
	}

	public void setLocName(String loc)
	{
		this.loc = ItemLocation.valueOf(loc);
	}

	public String getLocName()
	{
		return loc.name();
	}

	public void setLocation(ItemLocation loc)
	{
		this.loc = loc;
	}

	public ItemLocation getLocation()
	{
		return loc;
	}

	public void setLocData(int locData)
	{
		this.locData = locData;
	}

	public int getLocData()
	{
		return locData;
	}

	public int getCustomType1()
	{
		return customType1;
	}

	public void setCustomType1(int newtype)
	{
		customType1 = newtype;
	}

	public int getCustomType2()
	{
		return customType2;
	}

	public void setCustomType2(int newtype)
	{
		customType2 = newtype;
	}

	public int getLifeTime()
	{
		return lifeTime;
	}

	public void setLifeTime(int lifeTime)
	{
		if(lifeTime == -1)
			this.lifeTime = getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + getTemplate().getDurability() * 60 : (getTemplate().isShadowItem() ? getTemplate().getDurability() : -1);
		else
			this.lifeTime = Math.max(0, lifeTime);
	}

	public int getCustomFlags()
	{
		return customFlags;
	}

	public void setCustomFlags(int flags)
	{
		customFlags = flags;
	}

	public ItemAttributes getAttributes()
	{
		return attrs;
	}

	public void setAttributes(ItemAttributes attrs)
	{
		this.attrs = attrs;
	}

	public int getShadowLifeTime()
	{
		if(!isShadowItem())
			return -1;
		return getLifeTime();
	}

	public int getTemporalLifeTime()
	{
		if(getVisualId() > 0 && getLifeTime() >= 0 || isTemporalItem() || isFlagLifeTime())
			return getLifeTime() - (int) (System.currentTimeMillis() / 1000L);
		return -9999;
	}

	private ScheduledFuture<?> _manaConsumeTask;

	public void startManaConsumeTask(PcInventory.ManaConsumeTask r)
	{
		if(_manaConsumeTask == null)
			_manaConsumeTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(r, 0, 60000L);
	}

	public void stopManaConsumeTask()
	{
		if(_manaConsumeTask != null)
		{
			_manaConsumeTask.cancel(false);
			_manaConsumeTask = null;
		}
	}

	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return template.isEquipable();
	}

	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return isEquipped;
	}

	public void setEquipped(boolean isEquipped)
	{
		this.isEquipped = isEquipped;
	}

	public long getBodyPart()
	{
		return template.getBodyPart();
	}

	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getEquipSlot()
	{
		return getLocData();
	}

	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public ItemTemplate getTemplate()
	{
		return template;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	public long getLastDropTime()
	{
		return _dropTime;
	}

	public long getDropTimeOwner()
	{
		return _dropTimeOwner;
	}

	/**
	 * Returns the type of item
	 * @return Enum
	 */
	public ItemType getItemType()
	{
		return template.getItemType();
	}

	public boolean isArmor()
	{
		return template.isArmor();
	}

	public boolean isAccessory()
	{
		return template.isAccessory();
	}

	public boolean isOther()
	{
		return template.isOther();
	}

	public boolean isWeapon()
	{
		return template.isWeapon();
	}

	/**
	 * Returns the reference price of the item
	 * @return int
	 */
	public int getReferencePrice()
	{
		return template.getReferencePrice();
	}

	/**
	 * Returns if item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return template.isStackable();
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, ItemInstance.class, this, true))
			return;

		player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}

	public boolean isAugmented()
	{
		/*if (!getTemplate().isHairAccessory() && !getTemplate().isCloak())//修复后的
		// if (!getTemplate().isHairAccessory())原来的
			return getVariation1Id() != 0 && getVariation2Id() != 0;
		else*/
			return getVariation1Id() != 0 || getVariation2Id() != 0;
	}

	public int getVariation1Id()
	{
		return _variation1Id;
	}

	public void setVariation1Id(int val)
	{
		_variation1Id = val;
	}

	public int getVariation2Id()
	{
		return _variation2Id;
	}

	public void setVariation2Id(int val)
	{
		_variation2Id = val;
	}
	
	public boolean isBlessed()
	{
		return _blessed;
	}
	
	public void setBlessed(boolean val)
	{
		_blessed = val;
	}

	public class FuncAttack extends Func
	{
		private final Element element;

		public FuncAttack(Element element, int order, Object owner)
		{
			super(element.getAttack(), order, owner);
			this.element = element;
		}

		@Override
		public void calc(Env env, StatModifierType modifierType)
		{
			env.value += getAttributeElementValue(element, true);
		}
	}

	public class FuncDefence extends Func
	{
		private final Element element;

		public FuncDefence(Element element, int order, Object owner)
		{
			super(element.getDefence(), order, owner);
			this.element = element;
		}

		@Override
		public void calc(Env env, StatModifierType modifierType)
		{
			env.value += getAttributeElementValue(element, true);
		}
	}

	/**
	 * This function basically returns a set of functions from
	 * L2Item/L2Armor/L2Weapon, but may add additional
	 * functions, if this particular item instance is enhanched
	 * for a particular player.
	 * @return Func[]
	 */
	public Func[] getStatFuncs()
	{
		Func[] result = Func.EMPTY_FUNC_ARRAY;

		LazyArrayList<Func> funcs = LazyArrayList.newInstance();

		if(template.getAttachedFuncs().length > 0)
			for(FuncTemplate t : template.getAttachedFuncs())
			{
				Func f = t.getFunc(this);
				if(f != null)
					funcs.add(f);
			}

		for(Element e : Element.VALUES)
		{
			if(isWeapon())
				funcs.add(new FuncAttack(e, 0x40, this));
			if(isArmor())
				funcs.add(new FuncDefence(e, 0x40, this));
		}

		if(!funcs.isEmpty())
			result = funcs.toArray(new Func[funcs.size()]);

		LazyArrayList.recycle(funcs);

		return result;
	}

	/**
	 * Return true if item is hero-item
	 * @return boolean
	 */
	public boolean isHeroWeapon()
	{
		return template.isHeroWeapon();
	}

	public boolean isHeroItem()
	{
		return template.isHeroItem();
	}

	public boolean isOlympiadItem()
	{
		return template.isOlympiadItem();
	}

	/**
	 * Return true if item can be destroyed
	 */
	public boolean canBeDestroyed(Player player)
	{
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(isHeroItem())
			return false;

		if(player.getMountControlItemObjId() == getObjectId())
			return false;

		if(player.getPetControlItem() == this)
			return false;

		if(player.getEnchantScroll() == this)
			return false;

		return template.isDestroyable();
	}

	/**
	 * Return true if item can be dropped
	 */
	public boolean canBeDropped(Player player, boolean pk)
	{
		if(player.isGM())
			return true;

		if(HidenItemsDAO.isHidden(this))
			return false;
			
		if((customFlags & FLAG_NO_DROP) == FLAG_NO_DROP)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && (!pk || !Config.DROP_ITEMS_AUGMENTED) && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		return template.isDropable();
	}

	public boolean canBeTraded(Player player)
	{
		if(isEquipped())
			return false;

		if(player.isGM() || Config.LIST_OF_TRABLE_ITEMS.equals(getItemId()))
			return true;
			
		if(HidenItemsDAO.isHidden(this))
			return false;
			
		if((customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		return template.isTradeable();
	}

	public boolean canBePrivateStore(Player player)
	{
		if(getItemId() == ItemTemplate.ITEM_ID_ADENA)
			return false;

		if(!canBeTraded(player))
			return false;

		return template.isPrivatestoreable();
	}

	/**
	 * Можно ли продать в магазин NPC
	 */
	public boolean canBeSold(Player player)
	{
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if((customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
			return false;

		if(getItemId() == ItemTemplate.ITEM_ID_ADENA)
			return false;

		if(HidenItemsDAO.isHidden(this))
			return false;

		if(Config.LIST_OF_SELLABLE_ITEMS.equals(getItemId()))
			return true;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(!template.isDestroyable())
			return false;

		return template.isSellable();
	}

	/**
	 * Можно ли положить на клановый склад
	 */
	public boolean canBeStored(Player player, boolean privatewh)
	{
		if((customFlags & FLAG_NO_TRANSFER) == FLAG_NO_TRANSFER)
			return false;

		if(!getTemplate().isStoreable())
			return false;

		if(!privatewh && (isShadowItem() || isTemporalItem()))
			return false;

		if(!privatewh && isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(HidenItemsDAO.isHidden(this))
			return false;
			
		return privatewh || template.isTradeable();
	}

	public boolean canBeCrystallized(Player player)
	{
		if(isFlagNoCrystallize())
			return false;

		if(isHeroItem())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		return template.isCrystallizable();
	}

	public boolean canBeEnchanted()
	{
		if((customFlags & FLAG_NO_ENCHANT) == FLAG_NO_ENCHANT)
			return false;

		if(isHeroItem())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isCommonItem())
			return false;

		return template.canBeEnchanted();
	}

	public boolean canBeAppearance()
	{
		if(!isEquipable())
			return false;

		if(isHeroItem())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isCommonItem())
			return false;

		return template.canBeAppearance();
	}

	public boolean canBeAugmented(Player player)
	{
		if(!getTemplate().isAugmentable())
			return false;

		if(isHeroItem())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isCommonItem())
			return false;

		if(template.isPvP())
			return false;

		return true;
	}
	
	public boolean canBeBlessed()
	{
		if (!getTemplate().isBlessable())
			return false;
		
		if (isBlessed())
			return false;
		
		if(isHeroItem())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isCommonItem())
			return false;
		
		return true;
	}

	public boolean canBeExchanged(Player player)
	{
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(HidenItemsDAO.isHidden(this))
			return false;
			
		return template.isDestroyable();
	}

	public boolean canBeEnsoul(int ensoulId)
	{
		if(isHeroItem())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isCommonItem())
			return false;

		return template.canBeEnsoul(ensoulId);
	}

	public boolean isShadowItem()
	{
		return template.isShadowItem();
	}

	public boolean isTemporalItem()
	{
		return template.isTemporal();
	}

	public boolean isCommonItem()
	{
		return template.isCommonItem();
	}

	public boolean isHiddenItem()
	{
		return HidenItemsDAO.isHidden(this);
	}		
	/**
	 * Бросает на землю лут с NPC
	 */
	public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc)
	{
		Creature dropper = fromNpc;
		if(dropper == null)
			dropper = lastAttacker;

		Location pos = Location.findAroundPosition(dropper, 100);

		// activate non owner penalty
		if(lastAttacker != null) // lastAttacker в данном случае top damager
		{
			_dropPlayers = new HashIntSet(1, 2);

			PlayerGroup group = lastAttacker.getParty();
			if(group == null)
				group = lastAttacker;

			if(fromNpc != null && fromNpc.isBoss()) // На эпиках, дроп поднимает лидер CC.
			{
				group = lastAttacker.getPlayerGroup();
				if (group != null && (group instanceof CommandChannel)) {
					Player ccLeader = group.getGroupLeader();
					if (ccLeader != null) {
						group = ccLeader.getParty();
						if (group == null)
							group = lastAttacker;
					}
				}
			}

			for(Player $member : group)
				_dropPlayers.add($member.getObjectId());

			_dropTimeOwner = System.currentTimeMillis() + Config.NONOWNER_ITEM_PICKUP_DELAY + (fromNpc != null && fromNpc.isRaid() ? 285000 : 0);
		}

		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		dropMe(dropper, pos);
	}

	/**
	 * Бросает вещь на землю туда, где ее можно поднять
	 */
	public void dropToTheGround(Creature dropper, Location dropPos)
	{
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());
	}

	/**
	 * Бросает вещь на землю из инвентаря туда, где ее можно поднять
	 */
	public void dropToTheGround(Playable dropper, Location dropPos)
	{
		setLocation(ItemLocation.VOID);
		if(getJdbcState().isPersisted())
		{
			setJdbcState(JdbcEntityState.UPDATED);
			update();
		}

		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());
	}

	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion </li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR><BR>
	 *
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop item</li>
	 * <li> Call Pet</li><BR>
	 *
	 * @param dropper Char that dropped item
	 * @param loc drop coordinates
	 */
	public void dropMe(Creature dropper, Location loc)
	{
		if(dropper != null)
			setReflection(dropper.getReflection());

		spawnMe0(loc, dropper);

		if(dropper != null && dropper.isPlayable())
		{
			if(Config.AUTODESTROY_PLAYER_ITEM_AFTER > 0)
				ItemsAutoDestroy.getInstance().addPlayerItem(this);
		}
		else
		{
			// Add drop to auto destroy item task
			if(isHerb())
				ItemsAutoDestroy.getInstance().addHerb(this);
			else if(Config.AUTODESTROY_ITEM_AFTER > 0)
				ItemsAutoDestroy.getInstance().addItem(this);
		}
	}

	public final void pickupMe()
	{
		decayMe();
		setReflection(ReflectionManager.MAIN);
	}

	/**
	 * Возвращает защиту от элемента.
	 * @return значение защиты
	 */
	private int getDefence(Element element)
	{
		return isArmor() ? getAttributeElementValue(element, true) : 0;
	}

	/**
	 * Возвращает защиту от элемента: огонь.
	 * @return значение защиты
	 */
	public int getDefenceFire()
	{
		return getDefence(Element.FIRE);
	}

	/**
	 * Возвращает защиту от элемента: вода.
	 * @return значение защиты
	 */
	public int getDefenceWater()
	{
		return getDefence(Element.WATER);
	}

	/**
	 * Возвращает защиту от элемента: воздух.
	 * @return значение защиты
	 */
	public int getDefenceWind()
	{
		return getDefence(Element.WIND);
	}

	/**
	 * Возвращает защиту от элемента: земля.
	 * @return значение защиты
	 */
	public int getDefenceEarth()
	{
		return getDefence(Element.EARTH);
	}

	/**
	 * Возвращает защиту от элемента: свет.
	 * @return значение защиты
	 */
	public int getDefenceHoly()
	{
		return getDefence(Element.HOLY);
	}

	/**
	 * Возвращает защиту от элемента: тьма.
	 * @return значение защиты
	 */
	public int getDefenceUnholy()
	{
		return getDefence(Element.UNHOLY);
	}

	/**
	 * Возвращает значение элемента.
	 * @return
	 */
	public int getAttributeElementValue(Element element, boolean withBase)
	{
		return attrs.getValue(element) + (withBase ? template.getBaseAttributeValue(element) : 0);
	}

	/**
	 * Возвращает элемент атрибуции предмета.<br>
	 */
	public Element getAttributeElement()
	{
		return attrs.getElement();
	}

	public int getAttributeElementValue()
	{
		return attrs.getValue();
	}

	public Element getAttackElement()
	{
		Element element = isWeapon() ? getAttributeElement() : Element.NONE;
		if(element == Element.NONE)
			for(Element e : Element.VALUES)
				if(template.getBaseAttributeValue(e) > 0)
					return e;
		return element;
	}

	public int getAttackElementValue()
	{
		return isWeapon() ? getAttributeElementValue(getAttackElement(), true) : 0;
	}

	/**
	 * Устанавливает элемент атрибуции предмета.<br>
	 * Element (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -1 - None)
	 * @param element элемент
	 * @param value
	 */
	public void setAttributeElement(Element element, int value)
	{
		attrs.setValue(element, value);
	}

	/**
	 * Проверяет, является ли данный инстанс предмета хербом
	 * @return true если предмет является хербом
	 */
	public boolean isHerb()
	{
		return getTemplate().isHerb();
	}
	
	public long getPriceLimitForItem()
	{
		return getTemplate().getPriceLimitForItem();
	}
	
	public ItemGrade getGrade()
	{
		return template.getGrade();
	}

	@Override
	public String getName()
	{
		return getTemplate().getName();
	}

	public String getName(Player player)
	{
		return getTemplate().getName(player);
	}

	@Override
	public void save()
	{
		_itemsDAO.save(this);
	}

	@Override
	public void update()
	{
		_itemsDAO.update(this);
	}

	@Override
	public void delete()
	{
		_itemsDAO.delete(this);
		ItemsEnsoulDAO.getInstance().delete(getObjectId());
		stopManaConsumeTask();
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		//FIXME кажись дроппер у нас есть в итеме как переменная, ток проверить время? [VISTALL]
		L2GameServerPacket packet = null;
		if(dropper != null)
			packet = new DropItemPacket(this, dropper.getObjectId());
		else
			packet = new SpawnItemPacket(this);

		return Collections.singletonList(packet);
	}

	/**
	 * Returns the item in String format
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(getTemplate().getItemId());
		sb.append(" ");
		if(getEnchantLevel() > 0)
		{
			sb.append("+");
			sb.append(getEnchantLevel());
			sb.append(" ");
		}
		sb.append(getTemplate().getName());
		if(!getTemplate().getAdditionalName().isEmpty())
		{
			sb.append(" ");
			sb.append("\\").append(getTemplate().getAdditionalName()).append("\\");
		}
		sb.append(" ");
		sb.append("(");
		sb.append(getCount());
		sb.append(")");
		sb.append("[");
		sb.append(getObjectId());
		sb.append("]");

		return sb.toString();

	}

	@Override
	public void setJdbcState(JdbcEntityState state)
	{
		_state = state;
	}

	@Override
	public JdbcEntityState getJdbcState()
	{
		return _state;
	}

	@Override
	public boolean isItem()
	{
		return true;
	}

	public ItemAttachment getAttachment()
	{
		return _attachment;
	}

	public void setAttachment(ItemAttachment attachment)
	{
		ItemAttachment old = _attachment;
		_attachment = attachment;
		if(_attachment != null)
			_attachment.setItem(this);
		if(old != null)
			old.setItem(null);
	}

	public int getAgathionEnergy()
	{
		return _agathionEnergy;
	}

	public void setAgathionEnergy(int agathionEnergy)
	{
		_agathionEnergy = agathionEnergy;
	}

	public int getVisualId()
	{
		return _visualId;
	}

	public void setVisualId(int val)
	{
		_visualId = val;
	}

	public int getAppearanceStoneId()
	{
		return _appearanceStoneId;
	}

	public void setAppearanceStoneId(int val)
	{
		if(val == _appearanceStoneId)
			return;

		_appearanceStoneId = val;

		if(_appearanceStoneSkills != null)
			_appearanceStoneSkills.clear();

		if(_appearanceStoneId > 0)
		{
			AppearanceStone stone = AppearanceStoneHolder.getInstance().getAppearanceStone(_appearanceStoneId);
			if(stone != null)
			{
				if(_appearanceStoneSkills == null)
					_appearanceStoneSkills = new ArrayList<SkillEntry>();
				_appearanceStoneSkills.addAll(stone.getSkills());
			}
		}
	}

	public List<SkillEntry> getAppearanceStoneSkills()
	{
		if(_appearanceStoneSkills == null)
			return Collections.emptyList();
		return _appearanceStoneSkills;
	}


	public int[] getEnchantOptions()
	{
		return _enchantOptions;
	}

	public IntSet getDropPlayers()
	{
		return _dropPlayers;
	}

	public int getCrystalCountOnCrystallize()
	{
		int crystalsAdd = ItemFunctions.getCrystallizeCrystalAdd(this);
		return template.getCrystalCount() + crystalsAdd;
	}

	public int getCrystalCountOnEchant()
	{
		int defaultCrystalCount = template.getCrystalCount();
		if(defaultCrystalCount > 0)
		{
			int crystalsAdd = ItemFunctions.getCrystallizeCrystalAdd(this);
			return (int) Math.ceil(defaultCrystalCount / 2.) + crystalsAdd;
		}
		return 0;
	}

	private static final int[][] ENCHANT_FAIL_WEAPON_STONES = new int[][]{
			{ 0 },  // NONE
			{ 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14 }, // D
			{ 0, 0, 0, 0, 0, 0, 0, 2, 3, 4, 7, 8, 9, 12, 13, 14, 17, 18, 19, 25 },  // C
			{ 0, 0, 0, 0, 0, 0, 0, 3, 4, 5, 8, 9, 10, 13, 14, 15, 18, 19, 20, 28 }, // B
			{ 0, 0, 0, 0, 0, 0, 0, 5, 6, 7, 12, 13, 14, 19, 20, 21, 26, 27, 28, 38 },   // A
			{ 0, 0, 0, 0, 0, 0, 0, 15, 18, 21, 25, 28, 29, 30, 31, 32, 33, 34, 35, 36 } // S
	};

	private static final int[][] ENCHANT_FAIL_ARMOR_STONES = new int[][]{
			{ 0 },  // NONE
			{ 0, 0, 0, 0, 0, 0, 1, 2, 3, 5, 6, 7, 12 }, // D
			{ 0, 0, 0, 0, 0, 0, 2, 3, 4, 7, 8, 9, 15 }, // C
			{ 0, 0, 0, 0, 0, 0, 3, 4, 5, 9, 10, 11, 19 },   // B
			{ 0, 0, 0, 0, 0, 0, 5, 6, 7, 13, 14, 16, 26 },  // A
			{ 0, 0, 0, 0, 0, 0, 10, 15, 20, 25, 27, 28, 30 }    // S
	};

	public int[] getEnchantFailStone() {
		final int enchantLevel = getEnchantLevel();
		final int gradeOrdinal = getGrade().extOrdinal();

		int stoneId;
		int stoneCount = 0;
		if(isWeapon()) {
			stoneId = 91462;
			if(gradeOrdinal >= 0 && gradeOrdinal < ENCHANT_FAIL_WEAPON_STONES.length) {
				int[] countArr = ENCHANT_FAIL_WEAPON_STONES[gradeOrdinal];
				stoneCount = countArr[Math.min(enchantLevel, countArr.length - 1)];
			}
		} else {
			stoneId = 91463;
			if(gradeOrdinal >= 0 && gradeOrdinal < ENCHANT_FAIL_ARMOR_STONES.length) {
				int[] countArr = ENCHANT_FAIL_ARMOR_STONES[gradeOrdinal];
				stoneCount = countArr[Math.min(enchantLevel, countArr.length - 1)];
			}
		}
		return new int[]{ stoneId, stoneCount };
	}

	public ExItemType getExType()
	{
		return getTemplate().getExType();
	}

	public void setVariationStoneId(int id)
	{
		_variationStoneId = id;
	}

	public int getVariationStoneId()
	{
		return _variationStoneId;
	}

	public double getChargedSoulshotPower()
	{
		return _chargedSoulshotPower;
	}

	public void setChargedSoulshotPower(double val)
	{
		_chargedSoulshotPower = val;
	}

	public double getChargedSpiritshotPower()
	{
		return _chargedSpiritshotPower;
	}

	public double getChargedSpiritshotHealBonus()
	{
		return _chargedSpiritshotHealBonus;
	}

	public void setChargedSpiritshotPower(double power, int unk, double healBonus)
	{
		_chargedSpiritshotPower = power;
		_chargedSpiritshotHealBonus = healBonus;
	}

	public double getChargedFishshotPower()
	{
		return _chargedFishshotPower;
	}

	public void setChargedFishshotPower(double val)
	{
		_chargedFishshotPower = val;
	}

	public Ensoul[] getNormalEnsouls()
	{
		if(_normalEnsouls == null)
			return EMPTY_ENSOULS_ARRAY;

		return _normalEnsouls.values().toArray(new Ensoul[_normalEnsouls.size()]);
	}

	public Ensoul[] getSpecialEnsouls()
	{
		if(_specialEnsouls == null)
			return EMPTY_ENSOULS_ARRAY;

		return _specialEnsouls.values().toArray(new Ensoul[_specialEnsouls.size()]);
	}

	public void restoreEnsoul()
	{
		ItemsEnsoulDAO.getInstance().restore(this);
	}

	public boolean containsEnsoul(int type, int id)
	{
		return getEnsoul(type, id) != null;
	}

	public Ensoul getEnsoul(int type, int id)
	{
		if(type == 1)
		{
			if(_normalEnsouls != null)
				return _normalEnsouls.get(id);
		}
		else if(type == 2)
		{
			if(_specialEnsouls != null)
				return _specialEnsouls.get(id);
		}
		return null;
	}

	public void addEnsoul(int type, int id, Ensoul ensoul, boolean store)
	{
		if(!canBeEnsoul(ensoul.getItemId()))
			return;

		if(type == 1)
		{
			if(_normalEnsouls == null)
				_normalEnsouls = new TreeMap<Integer, Ensoul>();
			_normalEnsouls.put(id, ensoul);
		}
		else if(type == 2)
		{
			if(_specialEnsouls == null)
				_specialEnsouls = new TreeMap<Integer, Ensoul>();
			_specialEnsouls.put(id, ensoul);
		}
		else
			return;

		if(store)
			ItemsEnsoulDAO.getInstance().insert(getObjectId(), type, id, ensoul.getId());
	}

	public Ensoul removeEnsoul(int type, int id, boolean store)
	{
		Ensoul ensoul = null;
		if(type == 1)
		{
			if(_normalEnsouls != null)
				ensoul = _normalEnsouls.remove(id);
		}
		else if(type == 2)
		{
			if(_specialEnsouls != null)
				ensoul = _specialEnsouls.remove(id);
		}

		if(store && ensoul != null)
			ItemsEnsoulDAO.getInstance().delete(getObjectId(), type, id);

		return ensoul;
	}

	public boolean isFlagLifeTime()
	{
		return (customFlags & FLAG_LIFE_TIME) == FLAG_LIFE_TIME;
	}

	public boolean isFlagNoCrystallize()
	{
		return (customFlags & FLAG_NO_CRYSTALLIZE) == FLAG_NO_CRYSTALLIZE;
	}

	public void onEquip(int slot, Playable actor)
	{
		if(!isEquipped() && !getTemplate().isRune())
			return;

		_onEquipUnequipLock.lock();
		try
		{
			int flags = 0;
			for(Listener<Playable> listener : actor.getInventory().getListeners())
				flags |= ((OnEquipListener) listener).onEquip(slot, this, actor);

			if((flags & Inventory.UPDATE_STATS_FLAG) == Inventory.UPDATE_STATS_FLAG)
				actor.updateStats();

			if((flags & Inventory.UPDATE_SKILLS_FLAG) == Inventory.UPDATE_SKILLS_FLAG)
			{
				if(actor.isPlayer())
					actor.getPlayer().sendSkillList();
			}
		}
		finally
		{
			_onEquipUnequipLock.unlock();
		}
	}

	public void onEquip(Playable actor)
	{
		onEquip(getEquipSlot(), actor);
	}

	public void onUnequip(int slot, Playable actor, boolean refreshEquip)
	{
		// Слушатели снятия можно применить и на одетой вещи.
		if(!isEquipable() && !getTemplate().isRune())
			return;

		_onEquipUnequipLock.lock();
		try
		{
			int flags = 0;
			for(Listener<Playable> listener : actor.getInventory().getListeners())
				flags |= ((OnEquipListener) listener).onUnequip(slot, this, actor);

			if(refreshEquip)
			{
				for(ItemInstance item : actor.getInventory().getItems())
				{
					if(item != this)
						flags |= item.onRefreshEquip(actor, false);
				}
			}

			if((flags & Inventory.UPDATE_STATS_FLAG) == Inventory.UPDATE_STATS_FLAG)
				actor.updateStats();

			if((flags & Inventory.UPDATE_SKILLS_FLAG) == Inventory.UPDATE_SKILLS_FLAG)
			{
				if(actor.isPlayer())
					actor.getPlayer().sendSkillList();
			}
		}
		finally
		{
			_onEquipUnequipLock.unlock();
		}
	}

	public void onUnequip(int slot, Playable actor)
	{
		onUnequip(slot, actor, true);
	}

	public void onUnequip(Playable actor)
	{
		onUnequip(getEquipSlot(), actor);
	}

	public int onRefreshEquip(Playable actor, boolean update)
	{
		if(!isEquipped() && !getTemplate().isRune())
			return 0;

		_onEquipUnequipLock.lock();
		try
		{
			int flags = 0;
			for(Listener<Playable> listener : actor.getInventory().getListeners())
				flags |= ((OnEquipListener) listener).onRefreshEquip(this, actor);

			if(update)
			{
				if((flags & Inventory.UPDATE_STATS_FLAG) == Inventory.UPDATE_STATS_FLAG)
					actor.updateStats();

				if((flags & Inventory.UPDATE_SKILLS_FLAG) == Inventory.UPDATE_SKILLS_FLAG)
				{
					if(actor.isPlayer())
						actor.getPlayer().sendSkillList();
				}
			}
			return flags;
		}
		finally
		{
			_onEquipUnequipLock.unlock();
		}
	}

	public int onRefreshEquip(Playable actor)
	{
		return onRefreshEquip(actor, true);
	}

	public SkillEntry addEquippedSkill(Object owner, SkillEntry skill)
	{
		if(_equippedSkills == null)
			_equippedSkills = new ConcurrentHashMap<>();

		IntObjectMap<SkillEntry> skillsMap = _equippedSkills.computeIfAbsent(owner, (m) -> new HashIntObjectMap<>());
		return skillsMap.put(skill.getId(), skill);
	}

	public IntObjectMap<SkillEntry> removeEquippedSkills(Object owner)
	{
		if(_equippedSkills == null)
			return null;
		return _equippedSkills.remove(owner);
	}

	public int getEquippedSkillLevel(int skillId) {
		if(_equippedSkills == null)
			return 0;

		int skillLevel = 0;
		for(IntObjectMap<SkillEntry> skillsMap : _equippedSkills.values()) {
			SkillEntry skillEntry = skillsMap.get(skillId);
			if(skillEntry != null) {
				if(skillEntry.getLevel() > skillLevel)
					skillLevel = skillEntry.getLevel();
			}
		}
		return skillLevel;
	}

	public OptionDataTemplate addEquippedOptionData(Object owner, OptionDataTemplate optionData)
	{
		if(_equippedOptionDatas == null)
			_equippedOptionDatas = new ConcurrentHashMap<>();

		IntObjectMap<OptionDataTemplate> optionDataMap = _equippedOptionDatas.computeIfAbsent(owner, (m) -> new HashIntObjectMap<>());
		return optionDataMap.put(optionData.getId(), optionData);
	}

	public IntObjectMap<OptionDataTemplate> removeEquippedOptionDatas(Object owner)
	{
		if(_equippedOptionDatas == null)
			return null;
		return _equippedOptionDatas.remove(owner);
	}
}