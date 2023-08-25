package l2s.gameserver.model.actor.recorder;

import org.apache.commons.lang3.StringUtils;
import l2s.commons.collections.CollectionUtils;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill.SkillMagicType;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.s2c.ExChangeMPCost;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritInfo;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.ExUserInfoAbnormalVisualEffect;
import l2s.gameserver.network.l2.s2c.ExUserInfoEquipSlot;
import l2s.gameserver.network.l2.s2c.ExUserInfoInvenWeight;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;

/**
 * @author G1ta0
 */
public final class PlayerStatsChangeRecorder extends CharStatsChangeRecorder<Player>
{
	public static final int BROADCAST_KARMA = 1 << 6;
	public static final int SEND_STORAGE_INFO = 1 << 7;
	public static final int SEND_INVENTORY_LOAD = 1 << 8;
	public static final int BROADCAST_CHAR_INFO2 = 1 << 9;
	public static final int FORCE_SEND_CHAR_INFO = 1 << 10;
	public static final int CHAGE_MP_COST_PHYSIC = 1 << 11;
	public static final int CHAGE_MP_COST_MAGIC = 1 << 12;
	public static final int CHAGE_MP_COST_MUSIC = 1 << 13;
	public static final int BROADCAST_EQUIP_SLOT = 1 << 14;
	/*public static final int ELEMENTAL_STATS = 1 << 15;*/

	private int _maxCp;

	private int _maxLoad;
	private int _curLoad;

	private int[] _attackElement = new int[6];
	private int[] _defenceElement = new int[6];

	private long _exp;
	private long _sp;
	private int _karma;
	private int _pk;
	private int _pvp;
	private int _fame;

	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _trade;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _partyRoom;

	private double _physicMPCost;
	private double _magicMPCost;
	private double _musicMPCost;

	private String _title = StringUtils.EMPTY;

	private int _cubicsHash;

	private int _weaponEnchant;
	private int _armorSetEnchant;
	private int _weaponVariation1;
	private int _weaponVariation2;

	/*private final int[] _elementalAttack = new int[ElementalElement.values().length];
	private final int[] _elementalDefence = new int[ElementalElement.values().length];
	private final int[] _elementalCritRate = new int[ElementalElement.values().length];
	private final int[] _elementalCritAttack = new int[ElementalElement.values().length];*/

	public PlayerStatsChangeRecorder(Player activeChar)
	{
		super(activeChar);
	}

	@Override
	protected void refreshStats()
	{
		_maxCp = set(SEND_STATUS_INFO, _maxCp, _activeChar.getMaxCp());

		super.refreshStats();

		_maxLoad = set(SEND_INVENTORY_LOAD, _maxLoad, _activeChar.getMaxLoad());
		_curLoad = set(SEND_INVENTORY_LOAD, _curLoad, _activeChar.getCurrentLoad());

		for(Element e : Element.VALUES)
		{
			_attackElement[e.getId()] = set(SEND_CHAR_INFO, _attackElement[e.getId()], _activeChar.getAttack(e));
			_defenceElement[e.getId()] = set(SEND_CHAR_INFO, _defenceElement[e.getId()], _activeChar.getDefence(e));
		}

		/*for(ElementalElement e : ElementalElement.VALUES)
		{
			_elementalAttack[e.ordinal()] = set(ELEMENTAL_STATS, _elementalAttack[e.ordinal()], _activeChar.getStat().getElementalAttackPower(e));
			_elementalDefence[e.ordinal()] = set(ELEMENTAL_STATS, _elementalDefence[e.ordinal()], _activeChar.getStat().getElementalDefence(e));
			_elementalCritRate[e.ordinal()] = set(ELEMENTAL_STATS, _elementalCritRate[e.ordinal()], _activeChar.getStat().getElementalCritRate(e));
			_elementalCritAttack[e.ordinal()] = set(ELEMENTAL_STATS, _elementalCritAttack[e.ordinal()], _activeChar.getStat().getElementalCritAttack(e));
		}*/

		_exp = set(SEND_CHAR_INFO, _exp, _activeChar.getExp());
		_sp = set(SEND_CHAR_INFO, _sp, _activeChar.getSp());
		_pk = set(SEND_CHAR_INFO, _pk, _activeChar.getPkKills());
		_pvp = set(SEND_CHAR_INFO, _pvp, _activeChar.getPvpKills());
		_fame = set(SEND_CHAR_INFO, _fame, _activeChar.getFame());

		_karma = set(BROADCAST_KARMA, _karma, _activeChar.getKarma());

		_inventory = set(SEND_STORAGE_INFO, _inventory, _activeChar.getInventoryLimit());
		_warehouse = set(SEND_STORAGE_INFO, _warehouse, _activeChar.getWarehouseLimit());
		_clan = set(SEND_STORAGE_INFO, _clan, Config.WAREHOUSE_SLOTS_CLAN);
		_trade = set(SEND_STORAGE_INFO, _trade, _activeChar.getTradeLimit());
		_recipeDwarven = set(SEND_STORAGE_INFO, _recipeDwarven, _activeChar.getDwarvenRecipeLimit());
		_recipeCommon = set(SEND_STORAGE_INFO, _recipeCommon, _activeChar.getCommonRecipeLimit());
		_cubicsHash = set(BROADCAST_CHAR_INFO, _cubicsHash, CollectionUtils.hashCode(_activeChar.getCubics()));
		_partyRoom = set(BROADCAST_CHAR_INFO, _partyRoom, _activeChar.getMatchingRoom() != null && _activeChar.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && _activeChar.getMatchingRoom().getLeader() == _activeChar ? _activeChar.getMatchingRoom().getId() : 0);
		_team = set(BROADCAST_CHAR_INFO2, _team, _activeChar.getTeam());
		_title = set(BROADCAST_CHAR_INFO, _title, _activeChar.getTitle());

		_physicMPCost = set(CHAGE_MP_COST_PHYSIC, _physicMPCost, _activeChar.getMPCostDiff(SkillMagicType.PHYSIC));
		_magicMPCost = set(CHAGE_MP_COST_MAGIC, _magicMPCost, _activeChar.getMPCostDiff(SkillMagicType.MAGIC));
		_musicMPCost = set(CHAGE_MP_COST_MUSIC, _musicMPCost, _activeChar.getMPCostDiff(SkillMagicType.MUSIC));

		_weaponEnchant = set(BROADCAST_CHAR_INFO, _weaponEnchant, _activeChar.getEnchantEffect());
		_armorSetEnchant = set(BROADCAST_CHAR_INFO, _armorSetEnchant, _activeChar.getArmorSetEnchant());
		_weaponVariation1 = set(BROADCAST_EQUIP_SLOT, _weaponVariation1, _activeChar.getVariation1Id());
		_weaponVariation2 = set(BROADCAST_EQUIP_SLOT, _weaponVariation2, _activeChar.getVariation2Id());
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();

		if((_changes & FORCE_BROADCAST_CHAR_INFO) == FORCE_BROADCAST_CHAR_INFO)
			_activeChar.broadcastUserInfo(true);
		else if((_changes & BROADCAST_CHAR_INFO) == BROADCAST_CHAR_INFO || (_changes & BROADCAST_CHAR_INFO2) == BROADCAST_CHAR_INFO2 || (_changes & SEND_ABNORMAL_INFO) == SEND_ABNORMAL_INFO)
		{
			if((_changes & FORCE_SEND_CHAR_INFO) == FORCE_SEND_CHAR_INFO)
				_activeChar.broadcastUserInfo(true);
			else
				_activeChar.broadcastCharInfo();
		} else {
			if ((_changes & FORCE_SEND_CHAR_INFO) == FORCE_SEND_CHAR_INFO) {
				if ((_changes & BROADCAST_EQUIP_SLOT) == BROADCAST_EQUIP_SLOT)
					_activeChar.broadcastUserInfo(true);
				else
					_activeChar.sendUserInfo(true);
			}
			else if ((_changes & SEND_CHAR_INFO) == SEND_CHAR_INFO) {
				if ((_changes & BROADCAST_EQUIP_SLOT) == BROADCAST_EQUIP_SLOT)
					_activeChar.broadcastCharInfo();
				else
					_activeChar.sendUserInfo();
			}
		}

		if((_changes & BROADCAST_CHAR_INFO2) == BROADCAST_CHAR_INFO2) {
			for (Servitor servitor : _activeChar.getServitors())
				servitor.broadcastCharInfo();
		}

		if((_changes & SEND_TRANSFORMATION_INFO) == SEND_TRANSFORMATION_INFO)
		{
			_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar));
			_activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(_activeChar));
		}
		else {
			if ((_changes & BROADCAST_EQUIP_SLOT) == BROADCAST_EQUIP_SLOT) {
				_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar));
				_activeChar.broadcastCharInfoImpl();
			}
			if ((_changes & SEND_ABNORMAL_INFO) == SEND_ABNORMAL_INFO)
				_activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(_activeChar));
		}

		if((_changes & SEND_INVENTORY_LOAD) == SEND_INVENTORY_LOAD)
			_activeChar.sendPacket(new ExUserInfoInvenWeight(_activeChar));

		if((_changes & BROADCAST_KARMA) == BROADCAST_KARMA)
			_activeChar.sendStatusUpdate(true, false, StatusUpdatePacket.KARMA);

		if((_changes & SEND_STORAGE_INFO) == SEND_STORAGE_INFO)
			_activeChar.sendPacket(new ExStorageMaxCountPacket(_activeChar));

		if((_changes & CHAGE_MP_COST_PHYSIC) == CHAGE_MP_COST_PHYSIC)
			_activeChar.sendPacket(new ExChangeMPCost(SkillMagicType.PHYSIC, _physicMPCost));

		if((_changes & CHAGE_MP_COST_MAGIC) == CHAGE_MP_COST_MAGIC)
			_activeChar.sendPacket(new ExChangeMPCost(SkillMagicType.MAGIC, _magicMPCost));

		if((_changes & CHAGE_MP_COST_MUSIC) == CHAGE_MP_COST_MUSIC)
			_activeChar.sendPacket(new ExChangeMPCost(SkillMagicType.MUSIC, _musicMPCost));

		/*if((_changes & ELEMENTAL_STATS) == ELEMENTAL_STATS)
			_activeChar.sendPacket(new ExElementalSpiritInfo(_activeChar, 0));*/
	}
}