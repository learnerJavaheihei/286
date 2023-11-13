package l2s.gameserver.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import l2s.commons.geometry.Polygon;
import l2s.commons.lang.ArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.MountType;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.ChestInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExShowTracePacket;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket.FlyType;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.*;
import l2s.gameserver.skills.skillclasses.*;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.conditions.ConditionPlayerOlympiad;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.PositionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class Skill extends StatTemplate implements SkillInfo, Cloneable
{
	protected static final Logger _log = LoggerFactory.getLogger(Skill.class);

	public static class AddedSkill
	{
		public static final AddedSkill[] EMPTY_ARRAY = new AddedSkill[0];

		private final SkillEntryType entryType;
		public final int id;
		public final int level;
		private SkillEntry _skillEntry;

		public AddedSkill(SkillEntryType entryType, int id, int level)
		{
			this.entryType = entryType;
			this.id = id;
			this.level = level;
		}

		public SkillEntryType getEntryType() {
			return entryType;
		}

		public SkillEntry getSkill()
		{
			if(_skillEntry == null)
				_skillEntry = SkillEntry.makeSkillEntry(entryType, id, level);
			if(_skillEntry == null)
				_log.warn("Cannot find added skill ID[" + id + "] LEVEL[" + level + "]!");
			return _skillEntry;
		}
	}

	public static enum EnchantType
	{
		/*0*/NORMAL,
		/*1*/SAFE,
		/*2*/UNTRAIN,
		/*3*/CHANGE,
		/*4*/IMMORTAL;

		public static final EnchantType[] VALUES = values();
	}

	public static enum NextAction
	{
		ATTACK,
		CAST,
		DEFAULT,
		MOVE,
		NONE
	}

	public static enum Ternary
	{
		TRUE,
		FALSE,
		DEFAULT
	}

	public static enum SkillMagicType
	{
		PHYSIC, // Offlike: 0
		MAGIC, // Offlike: 1
		SPECIAL, // Offlike: 2
		MUSIC, // Offlike: 3
		ITEM, // Offlike: 4
		UNK_MAG_TYPE_21, // Offlike: 21
		AWAKED_BUFF, // Offlike: 22
		UNK_MAG_TYPE_23,
		UNK_MAG_TYPE_24,
		UNK_MAG_TYPE_25,
		UNK_MAG_TYPE_26,
		UNK_MAG_TYPE_27,
		UNK_MAG_TYPE_28,
		UNK_MAG_TYPE_29,
		UNK_MAG_TYPE_30,
		UNK_MAG_TYPE_31,
		UNK_MAG_TYPE_32,
		UNK_MAG_TYPE_33,
		UNK_MAG_TYPE_34,
		UNK_MAG_TYPE_35,
		UNK_MAG_TYPE_36,
		UNK_MAG_TYPE_37,
		UNK_MAG_TYPE_38,
		UNK_MAG_TYPE_39,
		UNK_MAG_TYPE_40,
		UNK_MAG_TYPE_41,
		UNK_MAG_TYPE_42,
		UNK_MAG_TYPE_43,
		UNK_MAG_TYPE_44,
		UNK_MAG_TYPE_45,
		UNK_MAG_TYPE_46,
		UNK_MAG_TYPE_47,
		UNK_MAG_TYPE_48,
		UNK_MAG_TYPE_49,
		UNK_MAG_TYPE_50,
		UNK_MAG_TYPE_51,
		UNK_MAG_TYPE_52;
	}

	public static enum SkillTargetType
	{
		TARGET_ALLY,
		TARGET_AREA,
		TARGET_AREA_AIM_CORPSE,
		TARGET_AURA,
		TARGET_SERVITOR_AURA,
		TARGET_CHEST,
		TARGET_CLAN,
		TARGET_CLAN_ONE,
		TARGET_CLAN_ONLY,
		TARGET_CORPSE,
		TARGET_CORPSE_PLAYER,
		TARGET_ENEMY_PET,
		TARGET_ENEMY_SUMMON,
		TARGET_ENEMY_SERVITOR,
		TARGET_FLAGPOLE,
		TARGET_COMMCHANNEL,
		TARGET_HOLY,
		TARGET_ITEM,
		TARGET_NONE,
		TARGET_ONE,
		TARGET_OWNER,
		TARGET_PARTY,
		TARGET_PARTY_WITHOUT_ME,
		TARGET_PARTY_ONE,
		TARGET_PARTY_ONE_WITHOUT_ME,
		TARGET_SERVITORS,
		TARGET_SUMMON,
		TARGET_PET,
		TARGET_ONE_SERVITOR,
		TARGET_ONE_SERVITOR_NO_TARGET,
		TARGET_SELF_AND_SUMMON,
		TARGET_SELF,
		TARGET_SIEGE,
		TARGET_UNLOCKABLE,
		TARGET_GROUND,
		// PTS target types
		TARGET_FAN,
		TARGET_FAN_PB,
		TARGET_SQUARE,
		TARGET_SQUARE_PB,
		TARGET_RANGE,
		TARGET_RING_RANGE
	}

	public static enum SkillType
	{
		PDAM_PATTACK_MUL(PDAM_PATTACK_MUL.class),
		AIEFFECTS(Continuous.class),
		BALANCE(Balance.class),
		BUFF(Continuous.class),
		BUFF_CHARGER(BuffCharger.class),
		CALL(Call.class),
		CHAIN_HEAL(ChainHeal.class),
		CHARGE(Charge.class),
		CLAN_GATE(ClanGate.class),
		CPDAM(CPDam.class),
		CPHOT(Continuous.class),
		CRAFT(Craft.class),
		DEBUFF_RENEWAL(DebuffRenewal.class),
		DECOY(Decoy.class),
		DEBUFF(Continuous.class),
		DELETE_HATE(Continuous.class),
		DESTROY_SUMMON(DestroySummon.class),
		DEFUSE_TRAP(DefuseTrap.class),
		DETECT_TRAP(DetectTrap.class),
		DISCORD(Continuous.class),
		DOT(Continuous.class),
		DRAIN(Drain.class),
		DRAIN_SOUL(DrainSoul.class),
		EFFECT(Skill.class),
		EFFECTS_FROM_SKILLS(EffectsFromSkills.class),
		ENERGY_REPLENISH(EnergyReplenish.class),
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		EXTRACT_STONE(ExtractStone.class),
		HARDCODED(Skill.class),
		HEAL(Continuous.class),
		HEAL_PERCENT(Continuous.class),
		HOT(Continuous.class),
		HIDE_HAIR_ACCESSORIES(HideHairAccessories.class),
		LETHAL_SHOT(LethalShot.class),
		LUCK,
		MANADAM(ManaDam.class),
		MDAM(MDam.class),
		MDOT(Continuous.class),
		MPHOT(Continuous.class),
		MUTE(Disablers.class),
		ADD_PC_BANG(PcBangPointsAdd.class),
		NOTDONE,
		NOTUSED,
		PARALYZE(Disablers.class),
		PASSIVE,
		PDAM(PDam.class),
		PET_FEED(PetFeed.class),
		PET_SUMMON(PetSummon.class),
		POISON(Continuous.class),
		RECALL(Recall.class),
		RESURRECT(Resurrect.class),
		REPLACE(Replace.class),
		RIDE(Ride.class),
		ROOT(Disablers.class),
		SHIFT_AGGRESSION(ShiftAggression.class),
		SLEEP(Disablers.class),
		SACRIFICE(Sacrifice.class),
		STEAL_BUFF(StealBuff.class),
		STUN(Disablers.class),
		SUMMON(Summon.class),
		SUMMON_FLAG(SummonSiegeFlag.class),
		RESTORATION(Restoration.class),
		SWEEP(Sweep.class),
		TAKECASTLE(TakeCastle.class),
		TRAP_ACTIVATION(TrapActivation.class),
		UNLOCK(Unlock.class),
		WATCHER_GAZE(Continuous.class);

		private final Class<? extends Skill> clazz;

		private SkillType()
		{
			clazz = Default.class;
		}

		private SkillType(Class<? extends Skill> clazz)
		{
			this.clazz = clazz;
		}

		public Skill makeSkill(StatsSet set)
		{
			try
			{
				Constructor<? extends Skill> c = clazz.getConstructor(StatsSet.class);
				return c.newInstance(set);
			}
			catch(Exception e)
			{
				_log.error("Skill ID[" + set.getInteger("skill_id") + "], LEVEL[" + set.getInteger("level") + "]", e);
				throw new RuntimeException(e);
			}
		}

		/**
		 * Работают только против npc
		 */
		public final boolean isPvM()
		{
			switch(this)
			{
				case DISCORD:
					return true;
				default:
					return false;
			}
		}

		/**
		 * Такие скиллы не аггрят цель, и не флагают чара, но являются "плохими"
		 */
		public boolean isAI()
		{
			switch(this)
			{
				case AIEFFECTS:
				case DELETE_HATE:
					return true;
				default:
					return false;
			}
		}
		public boolean isOffensive()
		{
			switch(this)
			{
				case AIEFFECTS:
				case DEBUFF:
				case DOT:
				case DRAIN:
				case DRAIN_SOUL:
				case LETHAL_SHOT:
				case MANADAM:
				case MDAM:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case PDAM:
				case CPDAM:
				case POISON:
				case ROOT:
				case SLEEP:
				case STUN:
				case SWEEP:
				case DELETE_HATE:
				case DESTROY_SUMMON:
				case STEAL_BUFF:
				case DISCORD:
				case DEBUFF_RENEWAL:
				case PDAM_PATTACK_MUL:
					return true;
				default:
					return false;
			}
		}
		public final boolean isPvpSkill()
		{
			switch(this)
			{
				case DEBUFF:
				case DOT:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case POISON:
				case ROOT:
				case SLEEP:
				case MANADAM:
				case STEAL_BUFF:
				case DELETE_HATE:
				case DEBUFF_RENEWAL:
					return true;
				default:
					return false;
			}
		}

		public boolean isDebuff()
		{
			switch(this)
			{
				case AIEFFECTS:
				case DEBUFF:
				case DOT:
				case DRAIN:
				case DRAIN_SOUL:
				case LETHAL_SHOT:
				case MANADAM:
				case MDAM:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case PDAM:
				case CPDAM:
				case POISON:
				case ROOT:
				case SLEEP:
				case STUN:
				case SWEEP:
				case DELETE_HATE:
				case STEAL_BUFF:
				case DISCORD:
				case DEBUFF_RENEWAL:
				case PDAM_PATTACK_MUL:
					return true;
				default:
					return false;
			}
		}
	}

	public enum SkillAutoUseType {
		NONE,
		BUFF,
		ATTACK,
		APPEARANCE
	}

	public static final Skill[] EMPTY_ARRAY = new Skill[0];

	//public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_CRAFTING = 172;
	public static final int SKILL_COMMON_CRAFTING = 1320;
	public static final int SKILL_POLEARM_MASTERY = 216;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_WEAPON_MAGIC_MASTERY1 = 249;
	public static final int SKILL_WEAPON_MAGIC_MASTERY2 = 250;
	public static final int SKILL_BLINDING_BLOW = 321;
	public static final int SKILL_STRIDER_ASSAULT = 325;
	public static final int SKILL_WYVERN_AEGIS = 327;
	public static final int SKILL_BLUFF = 358;
	public static final int SKILL_HEROIC_MIRACLE = 395;
	public static final int SKILL_HEROIC_BERSERKER = 396;
	public static final int SKILL_TRANSFORM_DISPEL = 619;
	public static final int SKILL_FINAL_FLYING_FORM = 840;
	public static final int SKILL_AURA_BIRD_FALCON = 841;
	public static final int SKILL_AURA_BIRD_OWL = 842;
	public static final int SKILL_DETECTION = 933;
	public static final int SKILL_DETECTION2 = 10785;
	public static final int SKILL_RECHARGE = 1013;
	public static final int SKILL_TRANSFER_PAIN = 1262;
	public static final int SKILL_SUMMON_CP_POTION = 1324;
	public static final int SKILL_HEROIC_VALOR = 1374;
	public static final int SKILL_HEROIC_GRANDEUR = 1375;
	public static final int SKILL_HEROIC_DREAD = 1376;
	public static final int SKILL_MYSTIC_IMMUNITY = 1411;
	public static final int SKILL_RAID_BLESSING = 2168;
	public static final int SKILL_HINDER_STRIDER = 4258;
	public static final int SKILL_WYVERN_BREATH = 4289;
	public static final int SKILL_RAID_CURSE = 4515;
	public static final int SKILL_RAID_CURSE_2 = 4215;
	public static final int SKILL_EVENT_TIMER = 5239;
	public static final int SKILL_BATTLEFIELD_DEATH_SYNDROME = 5660;
	public static final int SKILL_SERVITOR_SHARE = 1557;
	public static final int SKILL_CONFUSION = 1570;

	private final TIntObjectMap<List<EffectTemplate>> _effectTemplates = new TIntObjectHashMap<List<EffectTemplate>>(EffectUseType.VALUES.length);

	private final AddedSkill[] _addedSkills;

	private final long _itemConsume;
	private final int _itemConsumeId;
	private final boolean _itemConsumeFromMaster;
	private final int[] _relationSkillsId;
	private final int _referenceItemId; // для талисманов
	private final int _referenceItemMpConsume; // количество потребляемой мп талисмана

	private final boolean _isBehind;
	private final boolean _isCancelable;
	private final boolean _isCorpse;
	private final boolean _isItemHandler;
	private final boolean _isDebuff;
	private final boolean _isPvpSkill;
	private final boolean _isOffensive;

	private final boolean _isNotUsedByAI;
	private final boolean _isPvm;
	private final boolean _isForceUse;
	private final boolean _isNewbie;
	private final boolean _isPreservedOnDeath;
	private final boolean _isSaveable;
	private final boolean _isSkillTimePermanent;
	private final boolean _isReuseDelayPermanent;
	private final boolean _isReflectable;
	private final boolean _isSuicideAttack;
	private final boolean _isShieldignore;
	private final double _shieldIgnorePercent;
	private final boolean _isUndeadOnly;
	private final Ternary _isUseSS;
	private final boolean _isOverhit;
	private final boolean _isChargeBoost;
	private final boolean _isIgnoreResists;
	private final boolean _isIgnoreInvul;
	private final boolean _isTrigger;
	private final boolean _isNotAffectedByMute;
	private final boolean _basedOnTargetDebuff;
	private final boolean _deathlink;
	private final boolean _hideStartMessage;
	private final boolean _hideUseMessage;
	private final boolean _skillInterrupt;
	private final boolean _flyingTransformUsage;
	private final boolean _canUseTeleport;
	private final boolean _isProvoke;
	private boolean _isCubicSkill;
	private final boolean _isSelfDispellable;
	private final boolean _abortable;
	private final boolean _isRelation;
	private final double _decreaseOnNoPole;
	private final double _increaseOnPole;
	private final boolean _canUseWhileAbnormal;
	private final int _lethal2SkillDepencensyAddon;
	private final double _lethal2Addon;
	private final int _lethal1SkillDepencensyAddon;
	private final double _lethal1Addon;

	private final SkillType _skillType;
	private final SkillOperateType _operateType;
	private final SkillTargetType _targetType;
	private final SkillMagicType _magicType;
	private final SkillTrait _traitType;
	private final boolean _dispelOnDamage;
	private final NextAction _nextAction;
	private final Element _element;

	private final FlyType _flyType;
	private final boolean _flyDependsOnHeading;
	private final int _flyRadius;
	private final int _flyPositionDegree;
	private final int _flySpeed;
	private final int _flyDelay;
	private final int _flyAnimationSpeed;

	private Condition[] _preCondition = Condition.EMPTY_ARRAY;

	private final int _id;
	private final int _level;
	private final int _maxLevel;
	private final int _displayId;
	private int _displayLevel;

	private final int _activateRate;
	private final double _minChance;
	private final double _maxChance;
	private final int _castRange;
	private final int _condCharges;
	private final int _coolTime;
	private final int _effectPoint;
	private final int _energyConsume;
	private final int _cprConsume;
	private final int _fameConsume;
	private final int _elementPower;
	private final int _hitTime;
	private final int _hpConsume;
	private final int _levelBonusRate;
	private final int _magicLevel;
	private final int _matak;
	private final PledgeRank _minPledgeRank;
	private final boolean _clanLeaderOnly;
	private final int _npcId;
	private final int _numCharges;
	private final int _hitCancelTime;
	private final AddedSkill _attachedSkill;
	private final int _channelingStart;
	private final int _affectRange;
	private final int[] _fanRange;
	private final int[] _affectLimit;
	private final int _effectiveRange;
	private final int _behindRadius;
	private final int _tickInterval;
	private final int _criticalRate;
	private final double _criticalRateMod;

	private final int _reuseDelay;

	private final double _power;
	private final double _chargeEffectPower;
	private final double _chargeDefectPower;
	private final double _powerPvP;
	private final double _chargeEffectPowerPvP;
	private final double _chargeDefectPowerPvP;
	private final double _powerPvE;
	private final double _chargeEffectPowerPvE;
	private final double _chargeDefectPowerPvE;
	private final double _mpConsume1;
	private final double _mpConsume2;
	private final double _mpConsumeTick;
	private final double _lethal1;
	private final double _lethal2;
	private final double _absorbPart;
	private final double _defenceIgnorePercent;

	private final String _name;
	private final String _baseValues;
	private final String _icon;

	public boolean _isStandart = false;

	private final int _hashCode;

	private final int _reuseSkillId;
	private final int _reuseHash;

	private final int _toggleGroupId;
	private final boolean _isNecessaryToggle;
	private final boolean _isNotDispelOnSelfBuff;

	private final int _abnormalTime;
	private final int _abnormalLvl;
	private final AbnormalType _abnormalType;
	private final AbnormalEffect[] _abnormalEffects;
	private final boolean _abnormalHideTime;
	private final boolean _abnormalCancelOnAction;
	private final boolean _irreplaceableBuff;
	private final boolean _abnormalInstant;
	private boolean _detectPcHide;

	private final int _rideState;

	private final boolean _isSelfDebuff;
	private final boolean _applyEffectsOnSummon;
	private final boolean _applyEffectsOnPet;

	// @Rivelia.
	private final boolean _applyMinRange;
	private final int _masteryLevel;

	private final boolean _altUse;

	private final boolean _isItemSkill;
	// .

	private final boolean _addSelfTarget;
	private final double _percentDamageIfTargetDebuff;
	private final boolean _noFlagNoForce;
	private final boolean _renewal;
	private final int _buffSlotType;
	
	private final BasicProperty _basicProperty;

	private final boolean _isDouble;

	private final double _onAttackCancelChance;
	private final double _onCritCancelChance;

	private final boolean _noEffectsIfFailSkill;

	// Для заглушки отображения кондишона требующий эффект у цели.
	private boolean showPlayerAbnormal = false;
	private boolean showNpcAbnormal = false;

	private final SkillAutoUseType autoUseType;

	/**
	 * Внимание!!! У наследников вручную надо поменять тип на public
	 * @param set парамерты скилла
	 */
	public Skill(StatsSet set)
	{
		//_set = set;
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_displayId = set.getInteger("display_id", _id);
		_displayLevel = set.getInteger("display_level", _level);
		_maxLevel = set.getInteger("max_level");
		_name = set.getString("name");
		_operateType = set.getEnum("operate_type", SkillOperateType.class);
		_isNewbie = set.getBool("isNewbie", false);
		_isSelfDispellable = set.getBool("isSelfDispellable", true);
		_isPreservedOnDeath = set.getBool("isPreservedOnDeath", false);
		_energyConsume = set.getInteger("energyConsume", 0);
		_cprConsume = set.getInteger("clanRepConsume", 0);
		_fameConsume = set.getInteger("fameConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_isChargeBoost = set.getBool("chargeBoost", false);
		_isProvoke = set.getBool("provoke", false);
		_matak = set.getInteger("mAtk", 0);
		_isUseSS = Ternary.valueOf(set.getString("useSS", Ternary.DEFAULT.toString()).toUpperCase());
		_magicLevel = set.getInteger("magicLevel", 0);
		_tickInterval = Math.max(-1, (int) (set.getDouble("tick_interval", -1) * 1000));
		_castRange = set.getInteger("castRange", -1);
		_baseValues = set.getString("baseValues", null);

		_abnormalTime = set.getInteger("abnormal_time", -1);
		_abnormalLvl = set.getInteger("abnormal_level", 0);

		_abnormalType = AbnormalType.valueOf(set.getString("abnormal_type", AbnormalType.NONE.toString()).toUpperCase());

		String[] abnormalEffects = set.getString("abnormal_effect", AbnormalEffect.NONE.toString()).split(";");
		_abnormalEffects = new AbnormalEffect[abnormalEffects.length];
		for(int i = 0; i < abnormalEffects.length; i++)
			_abnormalEffects[i] = AbnormalEffect.valueOf(abnormalEffects[i].toUpperCase());

		_abnormalHideTime = set.getBool("abnormal_hide_time", false);
		_abnormalCancelOnAction = set.getBool("abnormal_cancel_on_action", false);
		_irreplaceableBuff = set.getBool("irreplaceable_buff", false);
		_abnormalInstant = set.getBool("abnormal_instant", false);

		String[] ride_state = set.getString("ride_state", MountType.NONE.toString()).split(";");
		int rideState = 0;
		for(int i = 0; i < ride_state.length; i++)
			rideState |= (1 << MountType.valueOf(ride_state[i].toUpperCase()).ordinal());
		_rideState = rideState;

		_toggleGroupId = set.getInteger("toggle_group_id", 0);
		_isNecessaryToggle = set.getBool("is_necessarytg", false);
		_isNotDispelOnSelfBuff = set.getBool("doNotDispelOnSelfBuff", false);

		_itemConsume = set.getLong("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_itemConsumeFromMaster = set.getBool("consume_item_from_master", false);

		String s3 = set.getString("relationSkillsId", "");
		if(s3.length() == 0)
		{
			_isRelation = false;
			_relationSkillsId = new int[] { 0 };
		}
		else
		{
			_isRelation = true;
			String[] s = s3.split(";");
			_relationSkillsId = new int[s.length];
			for(int i = 0; i < s.length; i++)
				_relationSkillsId[i] = Integer.parseInt(s[i]);
		}

		_referenceItemId = set.getInteger("referenceItemId", 0);
		_referenceItemMpConsume = set.getInteger("referenceItemMpConsume", 0);

		_isItemHandler = set.getBool("isHandler", false);
		_isSaveable = set.getBool("isSaveable", _operateType.isActive());
		_coolTime = set.getInteger("coolTime", 0);
		_hitCancelTime = set.getInteger("hitCancelTime", 0);

		int[] attachedSkill = set.getIntegerArray("attached_skill", new int[2], "-"); // TODO: Учитывать subLevel
		if(attachedSkill.length > 0 && attachedSkill[0] > 0)
			_attachedSkill = new AddedSkill(SkillEntryType.NONE, attachedSkill[0], attachedSkill.length > 1 ? attachedSkill[1] : 1);
		else
			_attachedSkill = null;

		_channelingStart = (int) (set.getDouble("channeling_start", 0) * 1000);
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_hitTime = set.getInteger("hitTime", 0);
		_affectRange = set.getInteger("affect_range", 80);
		_fanRange = set.getIntegerArray("fan_range", new int[4]);	// unk;startDegree;fanAffectRange;fanAffectAngle
		_affectLimit = set.getIntegerArray("affect_limit", new int[3]);	// minAffected;additionalRandom;unk
		_effectiveRange = set.getInteger("effective_range", -1);

		_behindRadius = Math.min(360, Math.max(0, set.getInteger("behind_radius", 0)));

		SkillTargetType targetType = set.getEnum("target", SkillTargetType.class, null);
		if(targetType == null)
		{
			String affectScope = set.getString("affect_scope", null);
			if(affectScope != null)
			{
				try
				{
					targetType = SkillTargetType.valueOf("TARGET_" + affectScope.toUpperCase());
				}
				catch(Exception e)
				{
					//
				}
			}
		}

		if(targetType == null)
			targetType = SkillTargetType.TARGET_SELF;

		_targetType = targetType;

		_magicType = set.getEnum("magicType", SkillMagicType.class, SkillMagicType.PHYSIC);

		int mpConsume = set.getInteger("mp_consume", 0);
		_mpConsume1 = set.getInteger("mp_consume1", _magicType == SkillMagicType.MAGIC ? (mpConsume / 4) : 0);
		_mpConsume2 = set.getInteger("mp_consume2", _magicType == SkillMagicType.MAGIC ? (mpConsume / 4) * 3 : mpConsume);
		_mpConsumeTick = set.getInteger("mp_consume_tick", 0);

		String traitName = set.getString("trait", "NONE").toUpperCase();
		if(traitName.startsWith("TRAIT_"))
			traitName = traitName.substring(6).trim();
		_traitType = SkillTrait.valueOf(traitName);

		_dispelOnDamage = set.getBool("dispelOnDamage", false);
		_hideStartMessage = set.getBool("isHideStartMessage", false);
		_hideUseMessage = set.getBool("isHideUseMessage", false);
		_isUndeadOnly = set.getBool("undeadOnly", false);
		_isCorpse = set.getBool("corpse", false);
		_power = set.getDouble("power", 0.);
		_chargeEffectPower = set.getDouble("chargeEffectPower", _power);
		_chargeDefectPower = set.getDouble("chargeDefectPower", _power);
		_powerPvP = set.getDouble("powerPvP", 0.);
		_chargeEffectPowerPvP = set.getDouble("chargeEffectPowerPvP", _powerPvP);
		_chargeDefectPowerPvP = set.getDouble("chargeDefectPowerPvP", _powerPvP);
		_powerPvE = set.getDouble("powerPvE", 0.);
		_chargeEffectPowerPvE = set.getDouble("chargeEffectPowerPvE", _powerPvE);
		_chargeDefectPowerPvE = set.getDouble("chargeDefectPowerPvE", _powerPvE);
		_effectPoint = set.getInteger("effectPoint", 1);
		_skillType = set.getEnum("skillType", SkillType.class, SkillType.EFFECT);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
		_isReuseDelayPermanent = set.getBool("isReuseDelayPermanent", false);
		_deathlink = set.getBool("deathlink", false);
		_basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
		_isNotUsedByAI = set.getBool("isNotUsedByAI", false);
		_isIgnoreResists = set.getBool("isIgnoreResists", false);
		_isIgnoreInvul = set.getBool("isIgnoreInvul", false);
		_isTrigger = set.getBool("isTrigger", false);
		_isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
		_flyingTransformUsage = set.getBool("flyingTransformUsage", false);
		_canUseTeleport = set.getBool("canUseTeleport", true);
		_altUse = set.getBool("alt_use", false);

		String element = set.getString("element", "NONE");
		if(NumberUtils.isCreatable(element))
			_element = Element.getElementById(Integer.parseInt(element));
		else
			_element = Element.getElementByName(element.toUpperCase());

		_elementPower = set.getInteger("elementPower", 0);

		_activateRate = set.getInteger("activateRate", -1);
		_minChance = set.getDouble("min_chance", Config.MIN_ABNORMAL_SUCCESS_RATE);
		_maxChance = set.getDouble("max_chance", Config.MAX_ABNORMAL_SUCCESS_RATE);
		_levelBonusRate = set.getInteger("lv_bonus_rate", 0);
		_isCancelable = set.getBool("cancelable", true);
		_isReflectable = set.getBool("reflectable", true);
		_isShieldignore = set.getBool("shieldignore", false);
		_shieldIgnorePercent = set.getDouble("shield_ignore_percent", 0.);
		_criticalRate = set.getInteger("criticalRate", 0);
		_criticalRateMod = set.getDouble("critical_rate_modifier", 1.);
		_isOverhit = set.getBool("overHit", false);
		_minPledgeRank = set.getEnum("min_pledge_rank", PledgeRank.class, PledgeRank.VAGABOND);
		_clanLeaderOnly = set.getBool("clan_leader_only", false);
		_isDebuff = set.getBool("debuff", _skillType.isDebuff());
		_isPvpSkill = set.getBool("isPvpSkill", _skillType.isPvpSkill());
		_isOffensive = set.getBool("isOffensive", _skillType.isOffensive());
		_isPvm = set.getBool("isPvm", _skillType.isPvM());
		_isForceUse = set.getBool("isForceUse", false);
		_isBehind = set.getBool("behind", false);
		_npcId = set.getInteger("npcId", 0);

		_flyType = FlyType.valueOf(set.getString("fly_type", "NONE").toUpperCase());
		_flyDependsOnHeading = set.getBool("fly_depends_on_heading", false);
		_flySpeed = set.getInteger("fly_speed", 0);
		_flyDelay = set.getInteger("fly_delay", 0);
		_flyAnimationSpeed = set.getInteger("fly_animation_speed", 0);
		_flyRadius = set.getInteger("fly_radius", 200);
		_flyPositionDegree = set.getInteger("fly_position_degree", 0);

		_numCharges = set.getInteger("num_charges", 0);
		_condCharges = set.getInteger("cond_charges", 0);
		_skillInterrupt = set.getBool("skillInterrupt", false);
		_lethal1 = set.getDouble("lethal1", 0.);
		_decreaseOnNoPole = set.getDouble("decreaseOnNoPole", 0.);
		_increaseOnPole = set.getDouble("increaseOnPole", 0.);
		_lethal2 = set.getDouble("lethal2", 0.);
		_lethal2Addon = set.getDouble("lethal2DepensencyAddon", 0.);
		_lethal2SkillDepencensyAddon = set.getInteger("lethal2SkillDepencensyAddon", 0);
		_lethal1Addon = set.getDouble("lethal1DepensencyAddon", 0.);
		_lethal1SkillDepencensyAddon = set.getInteger("lethal1SkillDepencensyAddon", 0);
		_absorbPart = set.getDouble("absorbPart", 0.);
		_icon = set.getString("icon", "");
		_canUseWhileAbnormal = set.getBool("canUseWhileAbnormal", false);
		_abortable = set.getBool("is_abortable", true);
		_defenceIgnorePercent = set.getDouble("defence_ignore_percent", 0.);

		AddedSkill[] addedSkills = AddedSkill.EMPTY_ARRAY;

		StringTokenizer st = new StringTokenizer(set.getString("addSkills", ""), ";");
		while(st.hasMoreTokens())
		{
			int id = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());
			if(level == -1)
				level = _level;
			addedSkills = ArrayUtils.add(addedSkills, new AddedSkill(SkillEntryType.NONE, id, level));
		}

		_addedSkills = addedSkills;

		final NextAction nextAction = NextAction.valueOf(set.getString("nextAction", "DEFAULT").toUpperCase());
		if(nextAction == NextAction.DEFAULT)
		{
			switch(_skillType)
			{
				case PDAM:
				case CPDAM:
				case LETHAL_SHOT:
				case STUN:
				case DRAIN_SOUL:
					_nextAction = NextAction.ATTACK;
					break;
				default:
					_nextAction = NextAction.NONE;
			}
		}
		else
			_nextAction = nextAction;

		_reuseSkillId = set.getInteger("reuse_skill_id", -1);
		_reuseHash = SkillHolder.getInstance().getHashCode(_reuseSkillId > 0 ? _reuseSkillId : _id, _level);
		_detectPcHide = set.getBool("detectPcHide", false);
		_hashCode = SkillHolder.getInstance().getHashCode(_id, _level);

		_isSelfDebuff = set.getBool("self_debuff", _isDebuff);
		_applyEffectsOnSummon = set.getBool("apply_effects_on_summon", true);
		_applyEffectsOnPet = set.getBool("apply_effects_on_pet", true);

		// @Rivelia.
		// applyMinRange is to bypass CHARGE flytype minimal range requirements. False = no range requirements.
		_applyMinRange = set.getBool("applyMinRange", true);
		// masteryLevel corresponds to the mastery calculation found in Formulas. If value is -1, default rule will be applied.
		_masteryLevel = set.getInteger("masteryLevel", -1);
		// .

		_isItemSkill = set.getBool("is_item_skill", false);

		for(EffectUseType type : EffectUseType.VALUES)
			_effectTemplates.put(type.ordinal(), new ArrayList<EffectTemplate>(0));

		_addSelfTarget = set.getBool("add_self_target", false);
		_percentDamageIfTargetDebuff = set.getDouble("percent_damage_if_target_debuff", 1.);
		_noFlagNoForce = set.getBool("noFlag_noForce", false);
		_renewal = set.getBool("renewal", true);
		_buffSlotType = set.getInteger("buff_slot_type", -2);

		_basicProperty = BasicProperty.valueOf(set.getString("basic_property", "none").toUpperCase());

		_isDouble = set.getBool("is_double", false);

		_onAttackCancelChance = set.getDouble("on_attack_cancel_chance", 0.);
		_onCritCancelChance = set.getDouble("on_crit_cancel_chance", 0.);

		_noEffectsIfFailSkill = set.getBool("no_effects_if_fail_skill", false);

		if(isDebuff()) {
			showPlayerAbnormal = Config.SHOW_TARGET_PLAYER_DEBUFF_EFFECTS;
			showNpcAbnormal = Config.SHOW_TARGET_NPC_DEBUFF_EFFECTS;
		} else {
			showPlayerAbnormal = Config.SHOW_TARGET_PLAYER_BUFF_EFFECTS;
			showNpcAbnormal = Config.SHOW_TARGET_NPC_BUFF_EFFECTS;
		}

		autoUseType = set.getEnum("autouse_type", SkillAutoUseType.class, SkillAutoUseType.NONE);

		if(!set.getBool("olympiad_use", true))
		{
			Condition cond = new ConditionPlayerOlympiad(false);
			cond.setSystemMsg(1509);
			attachCondition(cond);
		}
	}

	public void init()
	{
		if(!isPassive())
		{
			// Прописанные статты активным скиллам переводим в эффект.
			FuncTemplate[] funcs = removeAttachedFuncs();
			if(funcs.length > 0 || getAbnormalTime() > 0 && !hasEffects(EffectUseType.NORMAL))
			{
				EffectTemplate template = new EffectTemplate(this, StatsSet.EMPTY, EffectUseType.NORMAL, EffectTargetType.NORMAL);
				template.attachFuncs(funcs);
				attachEffect(template);
			}
		}

		if(!showPlayerAbnormal || !showNpcAbnormal)
		{
			// Для заглушки отображения кондишона требующий эффект у цели.
			for(Condition cond : getConditions())
				cond.init();
		}
	}

	public final boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first, true, false);
	}

	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		Player player = activeChar.getPlayer();

		if(activeChar.isDead())
			return false;

		if(!isHandler() && activeChar.isMuted(this))
			return false;

		if(activeChar.isUnActiveSkill(_id))
			return false;

		if(target != null && activeChar.getReflection() != target.getReflection())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}

		if(!trigger && (player != null && player.isInZone(ZoneType.JUMPING) || target != null && target.isInZone(ZoneType.JUMPING)))
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_SKILLS_IN_THE_CORRESPONDING_REGION);
			return false;
		}

		//TODO: Если у предмета который использует данный скилл есть откат, то не учитываем откат умения.
		if(first && activeChar.isSkillDisabled(this))
		{
			if(sendMsg)
				activeChar.sendReuseMessage(this);
			return false;
		}

		// DS: Clarity не влияет на mpConsume1 
		if(first && activeChar.getCurrentMp() < (isMagic() ? _mpConsume1 + activeChar.getStat().calc(Stats.MP_MAGIC_SKILL_CONSUME, _mpConsume2, target, this) : _mpConsume1 + activeChar.getStat().calc(Stats.MP_PHYSICAL_SKILL_CONSUME, _mpConsume2, target, this)))
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			return false;
		}

		if(activeChar.getCurrentHp() < _hpConsume + 1)
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.NOT_ENOUGH_HP);
			return false;
		}

		//recheck the sys messages, this are the suitible ones.
		if(getFameConsume() > 0)
		{
			if(player == null || player.getFame() < _fameConsume)
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.YOU_DONT_HAVE_ENOUGH_REPUTATION_TO_DO_THAT);
				return false;
			}
		}

		//must be in clan - no need to check it again
		if(getClanRepConsume() > 0)
		{
			if(player == null || player.getClan() == null || player.getClan().getReputationScore() < _cprConsume)
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
				return false;
			}
		}

		if(_targetType == SkillTargetType.TARGET_GROUND)
		{
			if(!activeChar.isPlayer())
				return false;

			if(player.getGroundSkillLoc() == null)
				return false;
		}

		if(isNotTargetAoE() && isDebuff() && activeChar.isInPeaceZone())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
			return false;
		}

		if(activeChar.getIncreasedForce() < getCondCharges())
		{
			if(sendMsg)
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		if(player != null)
		{
			if(player.isInFlyingTransform() && isHandler() && !flyingTransformUsage())
			{
				if(sendMsg)
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}

			if(!checkRideState(player.getMountType()))
			{
				if(sendMsg)
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}

			if(player.isInObserverMode())
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.OBSERVERS_CANNOT_PARTICIPATE);
				return false;
			}

			if(!isHandler() && activeChar.isPlayable() && first && getItemConsumeId() > 0 && getItemConsume() > 0)
			{
				if(ItemFunctions.getItemCount(isItemConsumeFromMaster() ? player : (Playable) activeChar, getItemConsumeId()) < getItemConsume())
				{
					if((isItemConsumeFromMaster() || activeChar == player) && sendMsg)
						player.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return false;
				}
			}

			if(player.isFishing() && !skillEntry.isAltUse() && !activeChar.isServitor())
			{
				if(activeChar == player && sendMsg)
					player.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
				return false;
			}

			if(player.isInTrainingCamp())
			{
				if(sendMsg)
					player.sendPacket(SystemMsg.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
				return false;
			}
		}

		switch(getFlyType())
		{
			case WARP_BACK:
			case WARP_FORWARD:
			case CHARGE:
			case DUMMY:
				if(activeChar.getStat().calc(Stats.BlockFly) == 1)
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
		}

		// Warp (628) && Shadow Step (821) can be used while rooted
		if(getFlyType() != FlyType.NONE && getId() != 628 && getId() != 821 && activeChar.isImmobilized())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(first && target != null && getFlyType() == FlyType.CHARGE)
		{
			if(isApplyMinRange() && activeChar.isInRange(target.getLoc(), Math.min(150, getFlyRadius())) && getTargetType() != SkillTargetType.TARGET_SELF && !activeChar.isServitor())
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED);
				return false;
			}

			Location flyLoc = activeChar.getFlyLocation(target, this);
			if(flyLoc == null)
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.THE_TARGET_IS_LOCATED_WHERE_YOU_CANNOT_CHARGE);
				return false;
			}
		}

		SystemMsg msg = checkTarget(skillEntry, activeChar, target, target, forceUse, first, trigger);
		if(msg != null && player != null)
		{
			if(sendMsg)
				player.sendPacket(msg);
			return false;
		}

		if(_preCondition.length == 0)
			return true;

		Env env = new Env();
		env.character = activeChar;
		env.skill = this;
		env.target = target;

		if(first)
		{
			for(Condition с : _preCondition)
			{
				if(!с.test(env))
				{
					if(sendMsg)
					{
						SystemMsg cond_msg = с.getSystemMsg();
						if(cond_msg != null)
						{
							if(cond_msg.size() > 0)
								activeChar.sendPacket(new SystemMessagePacket(cond_msg).addSkillName(this));
							else
								activeChar.sendPacket(cond_msg);
						}
					}
					return false;
				}
			}
		}

		return true;
	}

	public final SystemMsg checkTarget(SkillEntry skillEntry, Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first)
	{
		return checkTarget(skillEntry, activeChar, target, aimingTarget, forceUse, first, false);
	}

	public SystemMsg checkTarget(SkillEntry skillEntry, Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first, boolean trigger)
	{
		if(target == activeChar && isNotTargetAoE())
			return null;
		if(target == null)
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		if(target == activeChar)
		{
			if(_targetType != SkillTargetType.TARGET_SELF && isDebuff())
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			else
				return null;
		}
		if(isPvpSkill() && target.isPeaceNpc()) // TODO: [Bonux] Запретить юзать только дебафф скиллы (оффлайк).
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		if(activeChar.getReflection() != target.getReflection())
			return SystemMsg.CANNOT_SEE_TARGET;
		//if(target.isInvisible(activeChar))
		//	return SystemMsg.CANNOT_SEE_TARGET;
		// Попадает ли цель в радиус действия в конце каста
		if(!trigger) // TODO: Логично, но не вылазят ли косяки?
		{
			if(!first && target == aimingTarget && getCastRange() > 0 && !activeChar.isInRange(target.getLoc(), getCastRange() + (getCastRange() < 200 ? 400 : 500)))
				return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
		}
		if(activeChar.isMyServitor(target.getObjectId()) && _targetType == SkillTargetType.TARGET_SERVITOR_AURA)
			return null;
		// Для этих скиллов дальнейшие проверки не нужны
		if(_skillType == SkillType.TAKECASTLE)
			return null;
		// Проверка на каст по трупу
		boolean isCorpseSkill = isCorpse() || (target == aimingTarget && _targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE);
		if(target.isDead() != isCorpseSkill || _isUndeadOnly && !target.isUndead())
			return SystemMsg.INVALID_TARGET;
		if(_targetType == SkillTargetType.TARGET_CORPSE || (target == aimingTarget && _targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE))
		{
			if(!target.isNpc() && !target.isSummon())
				return SystemMsg.INVALID_TARGET;
			return null;
		}
		// Для различных бутылок, и для скилла кормления, дальнейшие проверки не нужны
		if(skillEntry.isAltUse() || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST)
			return null;
		if(isDebuff() &&	target.isFakePlayer() && target.isInPeaceZone())
			return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
		Player player = activeChar.getPlayer();
		if(player != null)
		{
			// Запрет на атаку мирных NPC в осадной зоне на TW. Иначе таким способом набивают очки.
			//if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
			//	return SystemMsg.INVALID_TARGET;

			Player pcTarget = target.getPlayer();
			if(pcTarget != null)
			{
				if(isPvM())
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

				if(pcTarget != player)
				{
					if(player.isInZone(ZoneType.epic) != pcTarget.isInZone(ZoneType.epic))
						return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

					if(pcTarget.isInOlympiadMode() && (!player.isInOlympiadMode() || player.getOlympiadGame() != pcTarget.getOlympiadGame())) // На всякий случай
						return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				}

				if(isDebuff())
				{
					if(pcTarget != player)
					{
						if(player.isInOlympiadMode() && !player.isOlympiadCompStart()) // Бой еще не начался
							return SystemMsg.INVALID_TARGET;
						if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcTarget.getOlympiadSide() && !forceUse) // Свою команду атаковать нельзя
							return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
						if(pcTarget.isInNonPvpTime())
							return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
					}

					if(isAoE() && !GeoEngine.canSeeTarget(activeChar, target))
						return SystemMsg.CANNOT_SEE_TARGET;

					if(pcTarget != player)
					{
						if(activeChar.isInZoneBattle() != target.isInZoneBattle() && !player.getPlayerAccess().PeaceAttack)
							return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
					}

					if((activeChar.isInPeaceZone() || target.isInPeaceZone()) && !player.getPlayerAccess().PeaceAttack)
						return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;

					if(pcTarget != player)
					{
						SystemMsg msg = null;
						for(Event e : activeChar.getEvents())
							if((msg = e.checkForAttack(target, activeChar, this, forceUse)) != null)
								return msg;

						for(Event e : activeChar.getEvents())
							if(e.canAttack(target, activeChar, this, forceUse, false))
								return null;

						if(activeChar.isInZoneBattle())
						{
							if(!forceUse && !isForceUse() && player.getParty() != null && player.getParty() == pcTarget.getParty())
								return SystemMsg.INVALID_TARGET;
							return null; // Остальные условия на аренах и на олимпиаде проверять не требуется
						}

						if(isProvoke())
						{
							if(!forceUse && player.getParty() != null && player.getParty() == pcTarget.getParty())
								return SystemMsg.INVALID_TARGET;
							return null;
						}
					}

					if(isPvpSkill() || !forceUse || isAoE())
					{
						if(player == pcTarget)
							return SystemMsg.INVALID_TARGET;

						if(player.getParty() != null && player.getParty() == pcTarget.getParty())
							return SystemMsg.INVALID_TARGET;
						if(player.isInParty() && player.getParty().getCommandChannel() != null && pcTarget.isInParty() && pcTarget.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel())
							return SystemMsg.INVALID_TARGET;

						if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
							return SystemMsg.INVALID_TARGET;
						if(player.getClan() != null && player.getClan().getAlliance() != null && pcTarget.getClan() != null && pcTarget.getClan().getAlliance() != null && player.getClan().getAlliance() == pcTarget.getClan().getAlliance())
							return SystemMsg.INVALID_TARGET;

						/*if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
							return SystemMsg.INVALID_TARGET;   */
					}

					if(pcTarget != player)
					{
						if(activeChar.isInSiegeZone() && target.isInSiegeZone())
							return null;

						if(player.atMutualWarWith(pcTarget))
							return null;
					}

					if(isForceUse())
						return null;

					// DS: Убрано. Защита от развода на флаг с копьем
					/*if(!forceUse && player.getPvpFlag() == 0 && pcTarget.getPvpFlag() != 0 && aimingTarget != target)
						return SystemMsg.INVALID_TARGET;*/

					if(pcTarget != player)
					{
						if(pcTarget.getPvpFlag() != 0)
							return null;
						if(pcTarget.isPK())
							return null;
					}

					if(forceUse && !isPvpSkill() && (!isAoE() || aimingTarget == target))
						return null;

					return SystemMsg.INVALID_TARGET;
				}

				if(pcTarget == player)
					return null;

				if(player.isInOlympiadMode() && !forceUse && player.getOlympiadSide() != pcTarget.getOlympiadSide()) // Чужой команде помогать нельзя
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				//TODO [VISTALL] что за?
				if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() != TeamType.NONE && player.getTeam() != pcTarget.getTeam()) // Чужой команде помогать нельзя
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

				if(!activeChar.isInZoneBattle() && target.isInZoneBattle())
					return SystemMsg.INVALID_TARGET;
				// DS: на оффе можно использовать неатакующие скиллы из мирной зоны в поле.
				/*if(activeChar.isInPeaceZone() && !target.isInPeaceZone())
					return SystemMsg.INVALID_TARGET;*/

				if(forceUse || isForceUse())
					return null;

				/*if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
					return SystemMsg.INVALID_TARGET;
				if(player != pcTarget && player.getDuel() != null && pcTarget.getDuel() != null && pcTarget.getDuel() == pcTarget.getDuel())
					return SystemMsg.INVALID_TARGET;*/

				if(player.getParty() != null && player.getParty() == pcTarget.getParty())
					return null;
				if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
					return null;

				if(player.atMutualWarWith(pcTarget))
					return SystemMsg.INVALID_TARGET;
				if(pcTarget.getPvpFlag() != 0)
					return SystemMsg.INVALID_TARGET;
				if(pcTarget.isPK())
					return SystemMsg.INVALID_TARGET;

				return null;
			}
		}

		if(!trigger || target != aimingTarget) // TODO: Логично, но не вылазят ли косяки?
		{
			if(isAoE() && isDebuff() && !GeoEngine.canSeeTarget(activeChar, target))
				return SystemMsg.CANNOT_SEE_TARGET;
		}
		if(!forceUse && !isForceUse() && !isNoFlagNoForce())
		{
			if(!isDebuff() && target.isAutoAttackable(activeChar))
				return SystemMsg.INVALID_TARGET;
			if(isDebuff() && !target.isAutoAttackable(activeChar))
				return SystemMsg.INVALID_TARGET;
		}
		if(!target.isAttackable(activeChar))
			return SystemMsg.INVALID_TARGET;

		return null;
	}

	public final Creature getAimingTarget(Creature activeChar, GameObject obj)
	{
		Creature target = obj == null || !obj.isCreature() ? null : (Creature) obj;
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_PARTY_WITHOUT_ME:
			case TARGET_CLAN_ONLY:
			case TARGET_SELF:
				return activeChar;
			case TARGET_AURA:
			case TARGET_COMMCHANNEL:
			case TARGET_GROUND:
			case TARGET_FAN_PB:
			case TARGET_SQUARE_PB:
				return activeChar;
			case TARGET_HOLY:
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			case TARGET_FLAGPOLE:
				return activeChar;
			case TARGET_UNLOCKABLE:
				return target != null && target.isDoor() || target instanceof ChestInstance ? target : null;
			case TARGET_CHEST:
				return target instanceof ChestInstance ? target : null;
			case TARGET_SERVITORS:
			case TARGET_SELF_AND_SUMMON:
				return activeChar;
			case TARGET_ONE_SERVITOR:
			case TARGET_SERVITOR_AURA:
				return target != null && target.isServitor() && activeChar.isMyServitor(target.getObjectId()) && target.isDead() == isCorpse() ? target : null;
			case TARGET_ONE_SERVITOR_NO_TARGET:
				target = activeChar.getPlayer().getAnyServitor();
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_SUMMON:
				target = activeChar.isPlayer() ? activeChar.getPlayer().getSummon() : null;
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_PET:
				target = activeChar.isPlayer() ? activeChar.getPlayer().getPet() : null;
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_OWNER:
				if(activeChar.isServitor())
					target = activeChar.getPlayer();
				else
					return null;
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_ENEMY_PET:
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isPet())
					return null;
				return target;
			case TARGET_ENEMY_SUMMON:
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isSummon())
					return null;
				return target;
			case TARGET_ENEMY_SERVITOR:
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isServitor())
					return null;
				return target;
			case TARGET_ONE:
				return target != null && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_CLAN_ONE:
//				return target != null && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()) && activeChar.getPlayer().isInSameClan(target.getPlayer()) ? target : null;
				if(target == null)
					return null;
				Player cplayer = activeChar.getPlayer();
				Player cptarget = target.getPlayer();
				// self or self pet.
				if(cptarget != null && cptarget == activeChar)
					return target;
				// olympiad party member or olympiad party member pet.
				if(cplayer != null && cplayer.isInOlympiadMode() && cptarget != null && cplayer.getOlympiadSide() == cptarget.getOlympiadSide() && cplayer.getOlympiadGame() == cptarget.getOlympiadGame() && target.isDead() == _isCorpse && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(cptarget != null && cplayer != null && cplayer.getClan() != null && cplayer.isInSameClan(cptarget) && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_PARTY_ONE:
				if(target == null)
					return null;
				Player player = activeChar.getPlayer();
				Player ptarget = target.getPlayer();
				// self or self pet.
				if(ptarget != null && ptarget == activeChar)
					return target;
				// olympiad party member or olympiad party member pet.
				if(player != null && player.isInOlympiadMode() && ptarget != null && player.getOlympiadSide() == ptarget.getOlympiadSide() && player.getOlympiadGame() == ptarget.getOlympiadGame() && target.isDead() == _isCorpse && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_PARTY_ONE_WITHOUT_ME:
				if(target == null)
					return null;
				player = activeChar.getPlayer();
				ptarget = target.getPlayer();
				// self or self pet.
				if(ptarget != null && ptarget == activeChar)
					return null;
				// olympiad party member or olympiad party member pet.
				if(player != null && player.isInOlympiadMode() && ptarget != null && player.getOlympiadSide() == ptarget.getOlympiadSide() && player.getOlympiadGame() == ptarget.getOlympiadGame() && target.isDead() == _isCorpse && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_AREA:
			case TARGET_FAN:
			case TARGET_SQUARE:
			case TARGET_RANGE:
			case TARGET_RING_RANGE:
				return target != null && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_AREA_AIM_CORPSE:
				return target != null && target.isDead() ? target : null;
			case TARGET_CORPSE:
				if(target == null || !target.isDead())
					return null;
				if(target.isSummon() && !activeChar.isMyServitor(target.getObjectId())) // использовать собственного мертвого самона нельзя
					return target;
				return target.isNpc() ? target : null;
			case TARGET_CORPSE_PLAYER:
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			case TARGET_SIEGE:
				return target != null && !target.isDead() && target.isDoor() ? target : null;
			default:
				activeChar.sendMessage("Target type of skill is not currently handled");
				return null;
		}
	}

	public Set<Creature> getTargets(SkillEntry skillEntry, Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		Set<Creature> targets;
		if(oneTarget() || isAoE() && isDebuff() && activeChar.isInPeaceZone())
		{
			targets = new HashSet<Creature>(1);
			if(!aimingTarget.isInvisible(activeChar))
				targets.add(aimingTarget);
			if(_addSelfTarget)
				targets.add(activeChar);

			for(Event e : activeChar.getEvents())
				e.checkTargetsForSkill(this, targets, activeChar, aimingTarget, forceUse);

			return targets;
		}
		else
			targets = new HashSet<Creature>();

		if(_addSelfTarget)
			targets.add(activeChar);

		switch(_targetType)
		{
			case TARGET_SELF_AND_SUMMON:
			{
				targets.add(activeChar);

				if(!activeChar.isPlayer())
					break;

				SummonInstance summon = activeChar.getPlayer().getSummon();
				if(summon != null)
				{
					int fanAffectRange = getFanRange()[2];
					if(fanAffectRange <= 0 || !summon.isInRange(activeChar, fanAffectRange))
					{
						if(activeChar.isInRange(summon, getAffectRange()))
						{
							if(!summon.isInvisible(activeChar))
								targets.add(summon);
						}
					}
				}
				break;
			}
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AREA:
			case TARGET_FAN:
			case TARGET_SQUARE:
			case TARGET_RANGE:
			{
				if(aimingTarget.isDead() == isCorpse() && (!_isUndeadOnly || aimingTarget.isUndead()))
				{
					if(!aimingTarget.isInvisible(activeChar))
						targets.add(aimingTarget);
				}
				addTargetsToList(skillEntry, targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_AURA:
			case TARGET_GROUND:
			case TARGET_FAN_PB:
			case TARGET_SQUARE_PB:
			case TARGET_RING_RANGE:
			{
				addTargetsToList(skillEntry, targets, activeChar, activeChar, forceUse);
				break;
			}
			case TARGET_COMMCHANNEL:
			{
				if(activeChar.getPlayer() != null)
				{
					if(activeChar.getPlayer().isInParty())
					{
						if(activeChar.getPlayer().getParty().isInCommandChannel())
						{
							for(Player p : activeChar.getPlayer().getParty().getCommandChannel())
							{
								int fanAffectRange = getFanRange()[2];
								if(fanAffectRange > 0 && p.isInRange(activeChar, fanAffectRange))
									continue;

								if(!p.isDead() && (getAffectRange() == -1 || p.isInRange(activeChar, getAffectRange() == 0 ? 600 : getAffectRange())))
								{
									if(!p.isInvisible(activeChar))
										targets.add(p);
								}
							}
							addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
							break;
						}
						for(Player p : activeChar.getPlayer().getParty().getPartyMembers())
						{
							int fanAffectRange = getFanRange()[2];
							if(fanAffectRange > 0 && p.isInRange(activeChar, fanAffectRange))
								continue;

							if(!p.isDead() && p.isInRange(activeChar, (getAffectRange() == -1 || getAffectRange() == 0 ? 600 : getAffectRange())))
							{
								if(!p.isInvisible(activeChar))
									targets.add(p);
							}
						}
						addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
						break;
					}
					targets.add(activeChar);
					addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
				}
				break;
			}
			case TARGET_SERVITORS:
			{
				for(Servitor servitor : activeChar.getServitors())
				{
					int fanAffectRange = getFanRange()[2];
					if(fanAffectRange > 0 && servitor.isInRange(activeChar, fanAffectRange))
						continue;

					if(activeChar.isInRange(servitor, getAffectRange()))
					{
						if(!servitor.isInvisible(activeChar))
							targets.add(servitor);
					}
				}
				break;
			}
			case TARGET_SERVITOR_AURA:
			{
				addTargetsToList(skillEntry, targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_PARTY:
			case TARGET_PARTY_WITHOUT_ME:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_ALLY:
			{
				if(activeChar.isMonster() || activeChar.isSiegeGuard())
				{
					if(_targetType != SkillTargetType.TARGET_PARTY_WITHOUT_ME)
						targets.add(activeChar);
					for(Creature c : World.getAroundCharacters(activeChar, getAffectRange(), 600))
					{
						int fanAffectRange = getFanRange()[2];
						if(fanAffectRange > 0 && c.isInRange(activeChar, fanAffectRange))
							continue;

						if(!c.isDead() && (c.isMonster() || c.isSiegeGuard()) /*&& ((L2MonsterInstance) c).getFactionId().equals(mob.getFactionId())*/)
						{
							if(!c.isInvisible(activeChar))
								targets.add(c);
						}
					}
					break;
				}
				Player player = activeChar.getPlayer();
				if(player == null)
					break;
				for(Player target : World.getAroundPlayers(activeChar, getAffectRange(), 600))
				{
					boolean check = false;
					switch(_targetType)
					{
						case TARGET_PARTY_WITHOUT_ME:
						case TARGET_PARTY:
							check = player.getParty() != null && player.getParty() == target.getParty();
							break;
						case TARGET_CLAN:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getParty() != null && target.getParty() == player.getParty();
							break;
						case TARGET_CLAN_ONLY:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId();
							break;
						case TARGET_ALLY:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getAllyId() != 0 && target.getAllyId() == player.getAllyId();
							break;
					}
					if(!check)
						continue;
					// игнорируем противника на олимпиаде
					if(player.isInOlympiadMode() && target.isInOlympiadMode() && player.getOlympiadSide() != target.getOlympiadSide())
						continue;

					int fanAffectRange = getFanRange()[2];
					if(fanAffectRange > 0 && target.isInRange(activeChar, fanAffectRange))
						continue;

					if(checkTarget(skillEntry, player, target, aimingTarget, forceUse, false) != null)
						continue;
					addTargetAndPetToList(targets, activeChar, target);
				}
				addTargetAndPetToList(targets, activeChar, player);
				break;
			}
		}

		for(Event e : activeChar.getEvents())
			e.checkTargetsForSkill(this, targets, activeChar, aimingTarget, forceUse);

		//FIXME [G1ta0] тупой хак
		if(getId() == SKILL_DETECTION || getId() == SKILL_DETECTION2)
		{
			for(Creature target : targets)
				target.checkAndRemoveInvisible();
		}
		return targets;
	}

	private void addTargetAndPetToList(Set<Creature> targets, Creature actor, Creature target)
	{
		int fanAffectRange = getFanRange()[2];
		if((actor == target || getAffectRange() == -1 || actor.isInRange(target, getAffectRange())) && (fanAffectRange <= 0 || !actor.isInRange(target, fanAffectRange)) && target.isDead() == isCorpse())
		{
			//if(!target.isInvisible(actor))
				targets.add(target);
		}

		for(Servitor servitor : target.getServitors())
		{
			if(fanAffectRange > 0 && actor.isInRange(servitor, fanAffectRange))
				continue;

			if((getAffectRange() == -1 || actor.isInRange(servitor, getAffectRange())) && servitor.isDead() == isCorpse())
			{
				if(!servitor.isInvisible(actor))
					targets.add(servitor);
			}
		}
	}

	private void addTargetsToList(SkillEntry skillEntry, Set<Creature> targets, Creature aimingTarget, Creature activeChar, boolean forceUse)
	{
		if(_targetType == SkillTargetType.TARGET_FAN || _targetType == SkillTargetType.TARGET_FAN_PB)
		{
			final double headingAngle = PositionUtils.convertHeadingToDegree(activeChar.getHeading());
			final int fanStartAngle = getFanRange()[1];
			final int fanRadius = getFanRange()[2];
			final int fanAngle = getFanRange()[3];
			final double fanHalfAngle = fanAngle / 2; // Half left and half right.
			final int affectLimit = getAffectLimit();

			// Target checks.
			int affectedCount = targets.size();

			for(Creature c : activeChar.getAroundCharacters(fanRadius, 300))
			{
				if(affectedCount >= affectLimit)
					break;

				if(c == null || activeChar == c || activeChar.getPlayer() != null && activeChar.getPlayer() == c.getPlayer())
					continue;

				if(Math.abs(PositionUtils.calculateAngleFrom(activeChar, c) - (headingAngle + fanStartAngle)) > fanHalfAngle)
					continue;

				if(checkTarget(skillEntry, activeChar, c, aimingTarget, forceUse, false) != null)
					continue;

				//if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
					//continue;

				targets.add(c);

				if(activeChar.getPlayer() != null && activeChar.getPlayer().isDebug())
				{
					activeChar.sendPacket(new ExShowTracePacket(30000).addTrace(c));
				}

				affectedCount++;
			}
		}
		else if(_targetType == SkillTargetType.TARGET_SQUARE || _targetType == SkillTargetType.TARGET_SQUARE_PB)
		{
			final int squareStartAngle = getFanRange()[1];
			final int squareLength = getFanRange()[2];
			final int squareWidth = getFanRange()[3];
			final int radius = (int) Math.sqrt((squareLength * squareLength) + (squareWidth * squareWidth));
			final int affectLimit = getAffectLimit();

			final double rectX = activeChar.getX();
			final double rectY = activeChar.getY() - (squareWidth / 2);
			final double heading = Math.toRadians(squareStartAngle + PositionUtils.convertHeadingToDegree(activeChar.getHeading()));
			final double cos = Math.cos(-heading);
			final double sin = Math.sin(-heading);

			// Target checks.
			int affectedCount = targets.size();

			for(Creature c : activeChar.getAroundCharacters(radius * (_targetType == SkillTargetType.TARGET_SQUARE ? 2 : 1), 300))
			{
				if(affectedCount >= affectLimit)
					break;

				if(c == null || activeChar == c || activeChar.getPlayer() != null && activeChar.getPlayer() == c.getPlayer())
					continue;

				// Check if inside square.
				double xp = c.getX() - activeChar.getX();
				double yp = c.getY() - activeChar.getY();
				double xr = (activeChar.getX() + (xp * cos)) - (yp * sin);
				double yr = activeChar.getY() + (xp * sin) + (yp * cos);
				if((xr > rectX) && (xr < (rectX + squareLength)) && (yr > rectY) && (yr < (rectY + squareWidth)))
				{
					if(checkTarget(skillEntry, activeChar, c, aimingTarget, forceUse, false) != null)
						continue;

					//if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
						//continue;

					targets.add(c);

					if(activeChar.getPlayer() != null && activeChar.getPlayer().isDebug())
					{
						activeChar.sendPacket(new ExShowTracePacket(30000).addTrace(c));
					}

					affectedCount++;
				}
			}
		}
		else if(_targetType == SkillTargetType.TARGET_RANGE)
		{
			final int affectRange = getAffectRange();
			final int affectLimit = getAffectLimit();

			// Target checks.
			int affectedCount = targets.size();

			for(Creature c : aimingTarget.getAroundCharacters(affectRange, 300))
			{
				if(affectedCount >= affectLimit)
					break;

				if(c == null || activeChar == c || activeChar.getPlayer() != null && activeChar.getPlayer() == c.getPlayer())
					continue;

				if(checkTarget(skillEntry, activeChar, c, aimingTarget, forceUse, false) != null)
					continue;

				//if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
					//continue;

				targets.add(c);

				if(activeChar.getPlayer() != null && activeChar.getPlayer().isDebug())
				{
					activeChar.sendPacket(new ExShowTracePacket(30000).addTrace(c));
				}

				affectedCount++;
			}
			return;
		}
		else if(_targetType == SkillTargetType.TARGET_RING_RANGE)
		{
			final int affectRange = getAffectRange();
			final int affectLimit = getAffectLimit();
			final int startRange = getFanRange()[2];

			// Target checks.
			int affectedCount = targets.size();

			for(Creature c : aimingTarget.getAroundCharacters(affectRange, 300))
			{
				if(affectedCount >= affectLimit)
					break;

				if(c == null || activeChar == c || activeChar.getPlayer() != null && activeChar.getPlayer() == c.getPlayer())
					continue;

				if(c.isInRange(aimingTarget, startRange))
					continue;

				if(checkTarget(skillEntry, activeChar, c, aimingTarget, forceUse, false) != null)
					continue;

				//if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
					//continue;

				targets.add(c);

				if(activeChar.getPlayer() != null && activeChar.getPlayer().isDebug())
				{
					activeChar.sendPacket(new ExShowTracePacket(30000).addTrace(c));
				}

				affectedCount++;
			}
			return;
		}
		else
		{
			// Старые таргеты. Ничего не меняем, постепенно вводим новое, удаляя старое.

			List<Creature> arround;

			Polygon terr = null;
			if(_targetType == SkillTargetType.TARGET_GROUND)
			{
				if(!activeChar.isPlayer())
					return;

				Location loc = activeChar.getPlayer().getGroundSkillLoc();
				if(loc == null)
					return;

				arround = World.getAroundCharacters(loc, aimingTarget.getObjectId(), aimingTarget.getReflectionId(), getAffectRange(), 300);
			}
			else
			{
				arround = aimingTarget.getAroundCharacters(getAffectRange(), 300);

				if(_targetType == SkillTargetType.TARGET_AREA)
				{
					if(getBehindRadius() > 0)
					{
						int zmin1 = activeChar.getZ() - 200;
						int zmax1 = activeChar.getZ() + 200;
						int zmin2 = aimingTarget.getZ() - 200;
						int zmax2 = aimingTarget.getZ() + 200;

						double radian = PositionUtils.convertHeadingToDegree(activeChar.getHeading()) + getBehindRadius() / 2;
						if(radian > 360)
							radian -= 360;

						radian = (Math.PI * radian) / 180;

						int x1 = aimingTarget.getX() + (int) (Math.cos(radian) * getAffectRange());
						int y1 = aimingTarget.getY() + (int) (Math.sin(radian) * getAffectRange());

						radian = PositionUtils.convertHeadingToDegree(activeChar.getHeading()) - getBehindRadius() / 2;
						if(radian > 360)
							radian -= 360;

						radian = (Math.PI * radian) / 180;

						int x2 = aimingTarget.getX() + (int) (Math.cos(radian) * getAffectRange());
						int y2 = aimingTarget.getY() + (int) (Math.sin(radian) * getAffectRange());

						terr = new Polygon().add(aimingTarget.getX(), aimingTarget.getY()).add(x1, y1).add(x2, y2).setZmin(Math.min(zmin1, zmin2)).setZmax(Math.max(zmax1, zmax2));
					}
				}
			}

			int affectLimit = getAffectLimit();
			if(affectLimit == 0)
			{
				// Скиллам со старыми таргетами прощаем отсутствие прописанных лимитов.
				if(isDebuff() && !activeChar.isRaid())
					affectLimit = 20;
				else
					affectLimit = 256;
			}

			// Target checks.
			int affectedCount = targets.size();

			for(Creature target : arround)
			{
				if(affectedCount >= affectLimit)
					break;

				if(terr != null && !terr.isInside(target.getX(), target.getY(), target.getZ()))
					continue;

				if(target == null || activeChar == target || activeChar.getPlayer() != null && activeChar.getPlayer() == target.getPlayer())
					continue;

				if(checkTarget(skillEntry, activeChar, target, aimingTarget, forceUse, false) != null)
					continue;

				//if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && target.isNpc())
					//continue;

				targets.add(target);

				if(activeChar.getPlayer() != null && activeChar.getPlayer().isDebug())
				{
					activeChar.sendPacket(new ExShowTracePacket(30000).addTrace(target));
				}

				affectedCount++;
			}
		}
	}

	public void checkTargetsEffectiveRange(Creature caster, Set<Creature> targets)
	{
		if(targets == null)
			return;

		for(Iterator<Creature> iterator = targets.iterator(); iterator.hasNext();)
		{
			Creature target = iterator.next();
			if(getEffectiveRange() != -1 && !caster.isInRangeZ(target, getEffectiveRange()))
				iterator.remove();

			//if(target.isInvisible(caster)) // TODO: Надо ли?
			//	iterator.remove();
		}
	}

	public boolean calcCriticalBlow(Creature caster, Creature target)
	{
		return false;
	}

	public final boolean calcEffectsSuccess(final Creature effector, final Creature effected, final boolean showMsg)
	{
		final int chance = getActivateRate();
		if(chance >= 0)
		{
			if(!Formulas.calcEffectsSuccess(effector, effected, this, chance))
			{
				if(showMsg)
				{
					effector.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(effected).addSkillName(this));
					effector.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.RESISTED));
				}
				return false;
			}
			/*На оффе не пишет.
			if(showMsg && isMagic())
				effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_SUCCEEDED).addSkillName(this));*/
		}
		if(effected.getStat().calc(Stats.MarkOfTrick) == 1 && Rnd.chance(20))
		{
			if(showMsg)
				effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(this));
			return false;
		}
		return true;
	}

	public final boolean getEffects(final Creature effector, final Creature effected)
	{
		return getEffects(effector, effected, true);
	}

	public final boolean getEffects(final Creature effector, final Creature effected, final boolean saveable)
	{
		double timeMult = 1.0;

		if(isMusic())
			timeMult = Config.SONGDANCETIME_MODIFIER;
		else if(getId() >= 4342 && getId() <= 4360)
			timeMult = Config.CLANHALL_BUFFTIME_MODIFIER;
		else if(Config.BUFFTIME_MODIFIER_SKILLS.length > 0)
		{
			for(int i : Config.BUFFTIME_MODIFIER_SKILLS)
			{
				if(i == getId())
					timeMult = Config.BUFFTIME_MODIFIER;
			}
		}

		return getEffects(effector, effected, 0, timeMult, saveable);
	}

	public final boolean getEffects(final Creature effector, final Creature effected, final int timeConst, final double timeMult)
	{
		return getEffects(effector, effected, timeConst, timeMult, true);
	}

	public final boolean getEffects(final Creature effector, final Creature effected, final int timeConst, final double timeMult, final boolean saveable)
	{
		return getEffects(effector, effected, EffectUseType.NORMAL, timeConst, timeMult, saveable);
	}

	/**
	 * Применить эффекты скилла
	 * 
	 * @param effector персонаж, со стороны которого идет действие скилла, кастующий
	 * @param effected персонаж, на которого действует скилл
	 * @param timeConst изменить время действия эффектов до данной константы (в миллисекундах)
	 * @param timeMult изменить время действия эффектов с учетом данного множителя
	 */
	private final boolean getEffects(final Creature effector, final Creature effected, final EffectUseType useType, final int timeConst, final double timeMult, final boolean saveable)
	{
		if(isPassive() || effector == null)
			return false;

		if(useType.isInstant())
		{
			_log.warn("Cannot get effects from instant effect use type:");
			Thread.dumpStack();
			return false;
		}

		if(!isToggle())
		{
			/*if(useType.isSelf() && !_operateType.isSelfContinuous())
				return false;
			if(!useType.isSelf() && !_operateType.isContinuous())
				return false;*/
		}

		/*TODO: Правильо ли? Скорее всего просто в эфффект нужно добавить кондишон.
		if(effector.isAlikeDead())
			return false;
		*/

		if(!hasEffects(useType))
			return true;

		if(effected == null || effected.isDoor() || effected.isDead() && !isPreservedOnDeath()) //why alike dead??
			return false;

		if(effector != effected)
		{
			if(useType == EffectUseType.NORMAL)
			{
				if(effected.isEffectImmune(effector))
					return false;
			}
		}

		boolean reflected = false;
		if(useType == EffectUseType.NORMAL)
			reflected = effected.checkReflectDebuff(effector, this);

		Set<Creature> targets = new HashSet<Creature>(1);
		if(useType == EffectUseType.SELF)
			targets.add(effector);
		else
		{
			// TODO: При рефлекте должен ли накладываться эффект на цель?
			if(reflected)
				targets.add(effector);
			else
				targets.add(effected);
		}

		if(useType == EffectUseType.NORMAL)
		{
			if((applyEffectsOnSummon() || applyEffectsOnPet()) && !isDebuff() && !isToggle() && !isCubicSkill())
			{
				Creature owner;
				/*if(useType == EffectUseType.SELF) // TODO: Проверить, SELF эффекты тоже кидать на саммона?
					owner = effector;
				else
				{*/
					if(reflected)
						owner = effector;
					else
						owner = effected;
				//}

				if(owner.isPlayer())
				{
					for(Servitor servitor : owner.getPlayer().getServitors())
					{
						if(applyEffectsOnSummon() && servitor.isSummon())
							targets.add(servitor);
						else if(applyEffectsOnPet() && servitor.isPet())
							targets.add(servitor);
					}
				}
			}
		}

		boolean successOnEffected = false;

		for(Creature target : targets)
		{
			final Abnormal abnormal = new Abnormal(effector, target, this, useType, saveable);

			double abnormalTimeModifier = Math.max(1., timeMult);
			if(!isToggle() && !isCubicSkill()) // TODO: [Bonux] Добавить условия, на какие баффы множитель не влияет.
				abnormalTimeModifier *= target.getStat().calc(isDebuff() ? Stats.DEBUFF_TIME_MODIFIER : Stats.BUFF_TIME_MODIFIER, null, null);

			int duration = abnormal.getDuration();

			if(timeConst > 0)
				duration = timeConst / 1000; // TODO: Пределать, чтобы посылалось в секундах.
			else if(abnormalTimeModifier > 1.0)
				duration *= abnormalTimeModifier;

			abnormal.setDuration(duration);

			if(abnormal.apply(effected))
			{
				if(abnormal.isActive())
				{
					for(EffectHandler effect : abnormal.getEffects())
						effect.onApplied(abnormal, abnormal.getEffector(), abnormal.getEffected());
				}

				// Check for mesmerizing debuffs and increase resist level.
				if(isDebuff() && (getBasicProperty() != BasicProperty.NONE) && target.hasBasicPropertyResist())
					target.getBasicPropertyResist(getBasicProperty()).increaseResistLevel();

				if(target == effected)
					successOnEffected = true;
			}

			if(target == effected)
			{
				if(reflected)
				{
					target.sendPacket(new SystemMessage(SystemMessage.YOU_COUNTERED_C1S_ATTACK).addName(effector));
					effector.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(target));
				}
			}
		}
		return successOnEffected;
	}

	public final void attachEffect(EffectTemplate effect)
	{
		if(effect == null)
			return;

		_effectTemplates.get(effect.getUseType().ordinal()).add(effect);
	}

	public List<EffectTemplate> getEffectTemplates(EffectUseType useType)
	{
		return _effectTemplates.get(useType.ordinal());
	}

	public int getEffectsCount(EffectUseType useType)
	{
		return getEffectTemplates(useType).size();
	}

	public boolean hasEffects(EffectUseType useType)
	{
		return getEffectsCount(useType) > 0;
	}

	public boolean hasEffect(EffectUseType useType, String name)
	{
		List<EffectTemplate> templates = getEffectTemplates(useType);
		for(EffectTemplate et : templates)
		{
			if(et.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;

		if(!(obj instanceof Skill))
			return false;

		Skill skill = (Skill) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(getId(), skill.getId());
		builder.append(getLevel(), skill.getLevel());
		builder.append(getClass(), skill.getClass());
		return builder.isEquals();
	}

	public int getReuseSkillId()
	{
		return _reuseSkillId;
	}

	public int getReuseHash()
	{
		return _reuseHash;
	}

	@Override
	public int hashCode()
	{
		return _hashCode;
	}

	public final void attachCondition(Condition c)
	{
		_preCondition = ArrayUtils.add(_preCondition, c);
	}

	public final Condition[] getConditions()
	{
		return _preCondition;
	}

	public final boolean isAltUse(SkillEntryType entryType)
	{
		return (_altUse || _isItemHandler || entryType == SkillEntryType.CUNSUMABLE_ITEM) && _hitTime <= 0;
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public AddedSkill[] getAddedSkills()
	{
		return _addedSkills;
	}

	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}

	public final int getAOECastRange()
	{
		return Math.max(getCastRange(), getAffectRange());
	}

	public int getCondCharges()
	{
		return _condCharges;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public boolean isCorpse()
	{
		return _isCorpse || _targetType == SkillTargetType.TARGET_CORPSE || _targetType == SkillTargetType.TARGET_CORPSE_PLAYER;
	}

	@Override
	public final int getDisplayId()
	{
		return _displayId;
	}

	@Override
	public int getDisplayLevel()
	{
		return _displayLevel;
	}

	@Override
	public final Skill getTemplate() {
		return this;
	}

	public int getEffectPoint()
	{
		return _effectPoint;
	}

	public Abnormal getSameByAbnormalType(Collection<Abnormal> list)
	{
		for(Abnormal abnormal : list)
		{
			if(abnormal != null && AbnormalList.checkAbnormalType(abnormal.getSkill(), this))
				return abnormal;
		}
		return null;
	}

	public Abnormal getSameByAbnormalType(AbnormalList list)
	{
		return getSameByAbnormalType(list.values());
	}

	public Abnormal getSameByAbnormalType(Creature actor)
	{
		return getSameByAbnormalType(actor.getAbnormalList());
	}

	public final Element getElement()
	{
		return _element;
	}

	public final int getElementPower()
	{
		return _elementPower;
	}

	public SkillEntry getFirstAddedSkill()
	{
		if(_addedSkills.length == 0)
			return null;
		return _addedSkills[0].getSkill();
	}

	public int getFlyRadius()
	{
		return _flyRadius;
	}

	public int getFlyPositionDegree()
	{
		return _flyPositionDegree;
	}

	public FlyType getFlyType()
	{
		return _flyType;
	}

	public boolean isFlyDependsOnHeading()
	{
		return _flyDependsOnHeading;
	}

	public int getFlySpeed()
	{
		return _flySpeed;
	}

	public int getFlyDelay()
	{
		return _flyDelay;
	}

	public int getFlyAnimationSpeed()
	{
		return _flyAnimationSpeed;
	}

	public final int getHitTime()
	{
		if(_hitTime < Config.MIN_HIT_TIME)
			return Config.MIN_HIT_TIME;
		return _hitTime;
	}

	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the id.
	 */
	@Override
	public final int getId()
	{
		return _id;
	}

	/**
	 * @return Returns the itemConsume.
	 */
	public final long getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	public final boolean isItemConsumeFromMaster()
	{
		return _itemConsumeFromMaster;
	}

	/**
	 * @return Возвращает ид предмета(талисмана)
	 * ману которого надо использовать
	 */
	public final int getReferenceItemId()
	{
		return _referenceItemId;
	}

	/**
	 * @return Возвращает используемое для каста количество маны
	 * предмета(талисмана) 
	 */
	public final int getReferenceItemMpConsume()
	{
		return _referenceItemMpConsume;
	}

	/**
	 * @return Returns the level.
	 */
	@Override
	public final int getLevel()
	{
		return _level;
	}

	public final int getMaxLevel()
	{
		return _maxLevel;
	}

	public final int getLevelBonusRate()
	{
		return _levelBonusRate;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public int getMatak()
	{
		return _matak;
	}

	public PledgeRank getMinPledgeRank()
	{
		return _minPledgeRank;
	}

	public boolean clanLeaderOnly()
	{
		return _clanLeaderOnly;
	}

	/**
	 * @return Returns the mpConsume as _mpConsume1 + _mpConsume2.
	 */
	public final double getMpConsume()
	{
		return _mpConsume1 + _mpConsume2;
	}

	/**
	 * @return Returns the mpConsume1.
	 */
	public final double getMpConsume1()
	{
		return _mpConsume1;
	}

	/**
	 * @return Returns the mpConsume2.
	 */
	public final double getMpConsume2()
	{
		return _mpConsume2;
	}

	/**
	 * @return Returns the mpConsumeTick.
	 */
	public final double getMpConsumeTick()
	{
		return _mpConsumeTick;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	public final String getName(Player player)
	{
		String name = SkillNameHolder.getInstance().getSkillName(player, this);
		return name == null ? _name : name;
	}

	public final NextAction getNextAction()
	{
		return _nextAction;
	}

	public final int getNpcId()
	{
		return _npcId;
	}

	public final int getNumCharges()
	{
		return _numCharges;
	}

	public final double getPower(Creature target)
	{
		if(target != null)
		{
			if(target.isPlayable())
				return getPowerPvP();
			if(target.isNpc())
				return getPowerPvE();
		}
		return getPower();
	}

	public final double getPower()
	{
		return _power;
	}

	public final double getPowerPvP()
	{
		return _powerPvP != 0 ? _powerPvP : _power;
	}

	public final double getPowerPvE()
	{
		return _powerPvE != 0 ? _powerPvE : _power;
	}

	public final int getReuseDelay()
	{
		return _reuseDelay;
	}

	public final boolean getShieldIgnore()
	{
		return _isShieldignore;
	}

	public final double getShieldIgnorePercent()
	{
		return _shieldIgnorePercent;
	}

	public final boolean isReflectable()
	{
		return _isReflectable;
	}

	public final int getHitCancelTime()
	{
		return _hitCancelTime;
	}

	public final AddedSkill getAttachedSkill()
	{
		return _attachedSkill;
	}

	public final int getChannelingStart()
	{
		return _channelingStart;
	}

	public final int getAffectRange()
	{
		return _affectRange;
	}

	public final int[] getFanRange()
	{
		return _fanRange;
	}

	public final int getAffectLimit()
	{
		if(_affectLimit[0] > 0 || _affectLimit[1] > 0)
			return _affectLimit[0] + (_affectLimit[1] > 0 ? Rnd.get(_affectLimit[1]) : 0);
		return 0;
	}

	public final int getEffectiveRange()
	{
		return _effectiveRange;
	}

	public final SkillType getSkillType()
	{
		return _skillType;
	}

	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final SkillTrait getTraitType()
	{
		return _traitType;
	}

	public final boolean isDispelOnDamage()
	{
		return _dispelOnDamage;
	}

	public double getLethal1(Creature self)
	{
		return _lethal1 + getAddedLethal1(self);
	}

	public double getIncreaseOnPole()
	{
		return _increaseOnPole;
	}

	public double getDecreaseOnNoPole()
	{
		return _decreaseOnNoPole;
	}

	public boolean isDetectPC()
	{
		return _detectPcHide;
	}
	
	public double getLethal2(Creature self)
	{
		return _lethal2 + getAddedLethal2(self);
	}

	private double getAddedLethal2(Creature self)
	{
		Player player = self.getPlayer();
		if(player == null)
			return 0.;

		if(_lethal2Addon == 0. || _lethal2SkillDepencensyAddon == 0)
			return 0.;

		if(player.getAbnormalList().contains(_lethal2SkillDepencensyAddon))
			return _lethal2Addon;

		return 0.;
	}

	private double getAddedLethal1(Creature self)
	{
		Player player = self.getPlayer();
		if(player == null)
			return 0.;

		if(_lethal1Addon == 0. || _lethal1SkillDepencensyAddon == 0)
			return 0.;

		if(player.getAbnormalList().contains(_lethal1SkillDepencensyAddon))
			return _lethal1Addon;

		return 0.;
	}

	public String getBaseValues()
	{
		return _baseValues;
	}

	public final boolean isCancelable()
	{
		return _isCancelable && _isSelfDispellable && !hasEffect(EffectUseType.NORMAL, "Transformation") && !isToggle() && !isAura();
	}

	public final boolean isSelfDispellable()
	{
		if(isMusic() && Config.CAN_SELF_DISPEL_SONG)
			return true;
		return _isSelfDispellable && !hasEffect(EffectUseType.NORMAL, "Transformation") && !isToggle() && !isDebuff() && !isMusic();
	}

	public final int getCriticalRate()
	{
		return _criticalRate;
	}

	public final double getCriticalRateMod()
	{
		return _criticalRateMod;
	}

	public final boolean isHandler()
	{
		return _isItemHandler;
	}

	public final boolean isMagic()
	{
		return _magicType == SkillMagicType.MAGIC || _magicType == SkillMagicType.SPECIAL || _magicType == SkillMagicType.AWAKED_BUFF;
	}

	public final boolean isPhysic()
	{
		if(_magicType == SkillMagicType.UNK_MAG_TYPE_21) // TODO: Check.
			return true;
		return _magicType == SkillMagicType.PHYSIC || _magicType == SkillMagicType.MUSIC || _magicType == SkillMagicType.ITEM;
	}

	public final boolean isSpecial()
	{
		return _magicType == SkillMagicType.SPECIAL;
	}

	public final boolean isMusic()
	{
		return _magicType == SkillMagicType.MUSIC;
	}

	public final SkillMagicType getMagicType()
	{
		return _magicType;
	}

	public final boolean isNewbie()
	{
		return _isNewbie;
	}

	public final boolean isPreservedOnDeath()
	{
		return _isPreservedOnDeath || isNecessaryToggle();
	}

	public final boolean isOverhit()
	{
		return _isOverhit;
	}

	public boolean isSaveable()
	{
		if(!Config.ALT_SAVE_UNSAVEABLE && (isMusic() || isAbnormalInstant()))
			return false;
		return _isSaveable || (isToggle() && isNecessaryToggle());
	}

	/**
	 * На некоторые скиллы и хендлеры предметов скорости каста/атаки не влияет
	 */
	public final boolean isSkillTimePermanent()
	{
		return _isSkillTimePermanent || isHandler() || _name.contains("Talisman") || isChanneling();
	}

	public final boolean isReuseDelayPermanent()
	{
		return _isReuseDelayPermanent || isHandler();
	}

	public boolean isDeathlink()
	{
		return _deathlink;
	}

	public boolean isBasedOnTargetDebuff()
	{
		return _basedOnTargetDebuff;
	}

	public boolean isChargeBoost()
	{
		return _isChargeBoost;
	}

	public boolean isBehind()
	{
		return _isBehind;
	}

	public boolean isHideStartMessage()
	{
		return _hideStartMessage || isHidingMesseges();
	}

	public boolean isHideUseMessage()
	{
		return _hideUseMessage || isHidingMesseges();
	}

	/**
	 * Может ли скилл тратить шоты, для хендлеров всегда false
	 */
	public boolean isSSPossible()
	{
		if(isAura())
			return false;

		if(_isUseSS == Ternary.TRUE)
			return true;

		if(_isUseSS == Ternary.FALSE)
			return false;

		if(_isUseSS == Ternary.DEFAULT)
		{
			if(isHandler())
				return false;
			if(isSpecial())
				return false;
			if(isMusic())
				return false;
			if(!isActive())
				return false;
			if(getTargetType() == SkillTargetType.TARGET_SELF && !isMagic())
				return false;
			//i_p_attack;i_p_soul_attack;i_m_attack;i_mp_burn;i_hp_drain;i_fatal_blow;i_soul_blow;i_backstab;i_death_link;i_p_attack_hp_link;i_energy_attack;i_seven_arrows
			if(isPhysic())
			{
				if(getSkillType() == SkillType.CHARGE)
					return true;
				else if(getSkillType() == SkillType.DRAIN)
					return true;
				else if(getSkillType() == SkillType.LETHAL_SHOT)
					return true;
				else if(getSkillType() == SkillType.PDAM)
					return true;
				else if(getSkillType() == SkillType.DEBUFF)
				{
					for(EffectUseType useType : EffectUseType.VALUES)
					{
						if(useType.isSelf())
							continue;
						if(hasEffect(useType, "i_p_attack"))
							return true;
						if(hasEffect(useType, "i_m_attack"))
							return true;
						if(hasEffect(useType, "i_hp_drain"))
							return true;
					}
				}
				return false;
			}
			return true;
		}
		return false;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	public boolean isActive()
	{
		return _operateType.isActive();
	}

	public boolean isPassive()
	{
		return _operateType.isPassive();
	}

	public boolean isToggle()
	{
		return _operateType.isToggle();
	}

	public boolean isToggleGrouped()
	{
		return _operateType.isToggleGrouped();
	}

	public boolean isAura()
	{
		return _operateType.isAura();
	}

	public boolean isHidingMesseges()
	{
		return _operateType.isHidingMesseges();
	}

	public boolean isNotBroadcastable()
	{
		return _operateType.isNotBroadcastable();
	}

	public boolean isContinuous()
	{
		return _operateType.isContinuous() || isSelfContinuous();
	}

	public boolean isSelfContinuous()
	{
		return _operateType.isSelfContinuous();
	}

	public boolean isChanneling()
	{
		return _operateType.isChanneling();
	}

	public boolean isSynergy()
	{
		return _operateType.isSynergy();
	}

	public void setDisplayLevel(int lvl)
	{
		_displayLevel = lvl;
	}

	public final boolean isItemSkill()
	{
		return _isItemSkill;
	}

	@Override
	public String toString()
	{
		return String.format("%s[id=%d, lvl=%d, hash_code=%d]", _name, _id, _level, hashCode());
	}

	private final boolean checkCastTarget(Creature target)
	{
		//Фильтруем неуязвимые цели
		/*TODO: Переделать.
		if(isDebuff() && target.isInvulnerable())
		{
			Player player = target.getPlayer();
			if((!isIgnoreInvul() || player != null && player.isGM()) && !target.isArtefact())
				return false;
		}*/

		return !target.isIgnoredSkill(this);
	}

	private final boolean applyEffectPoint(Creature activeChar, Creature target)
	{
		if(!isAI() && getEffectPoint() < 0)
		{
			activeChar.getAI().notifyEvent(CtrlEvent.EVT_ATTACK, target, this, Math.abs(getEffectPoint()));
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, this, Math.abs(getEffectPoint()));
			return true;
		}
		return false;
	}

	public final void onStartCast(SkillEntry skillEntry, Creature activeChar, Creature target)
	{
		if(target == null)
			return;

		if(isPassive())
			return;

		if(!hasEffects(EffectUseType.START))
			return;

		boolean startAttackStance = false;

		if(!checkCastTarget(target))
			return;

		if(applyEffectPoint(activeChar, target))
			startAttackStance = true;

		for(EffectTemplate et : getEffectTemplates(EffectUseType.START))
			useInstantEffect(et, activeChar, target, false);

		if(isSSPossible()) // TODO: Проверить, должно оно ли при старте каста юзать соски.
		{
			if(!(Config.SAVING_SPS && _skillType == SkillType.BUFF))
				activeChar.unChargeShots(isMagic());
		}

		if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public final void onTickCast(Creature activeChar, Set<Creature> targets)
	{
		if(isPassive())
			return;

		if(!isChanneling())
			return;

		boolean startAttackStance = false;

		AddedSkill attachedSkill = getAttachedSkill();
		if(attachedSkill != null)
		{
			//TODO: Уровень скилла должен зависит от количества кастующих данный тик.

			SkillEntry skillEntry = attachedSkill.getSkill();
			if(skillEntry == null)
			{
				// TODO: Надо ли аборт атаки?
				return;
			}

			Skill skill = skillEntry.getTemplate();
			
			for(Creature target : targets)
			{
				if(target == null)
					continue;

				if(!skill.checkCastTarget(target))
					continue;

				if(skill.applyEffectPoint(activeChar, target))
					startAttackStance = true;

				final boolean reflected = target.checkReflectSkill(activeChar, skill);
				final boolean successEffect = skill.hasEffects(EffectUseType.NORMAL) && skill.calcEffectsSuccess(activeChar, target, true);

				if(successEffect || !skill.hasEffects(EffectUseType.NORMAL) || !_noEffectsIfFailSkill) { // TODO: Избавиться, переделать по оффу.
					skill.useSkill(activeChar, target, reflected);

					for (EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL_INSTANT))
						skill.useInstantEffect(et, activeChar, target, reflected);
				}

				if(successEffect)
				{
					skill.getEffects(activeChar, target);
				}
			}
		}
		else if(hasEffects(EffectUseType.TICK))
		{
			for(Creature target : targets)
			{
				if(target == null)
					continue;

				if(!checkCastTarget(target))
					continue;

				if(applyEffectPoint(activeChar, target))
					startAttackStance = true;

				for(EffectTemplate et : getEffectTemplates(EffectUseType.TICK))
					useInstantEffect(et, activeChar, target, false);
			}
		}

		if(isSSPossible()) // TODO: Проверить, должно оно ли при тике каста юзать соски.
		{
			if(!(Config.SAVING_SPS && _skillType == SkillType.BUFF))
				activeChar.unChargeShots(isMagic());
		}

		if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public void onEndCast(Creature activeChar, Set<Creature> targets)
	{
		if(isPassive())
			return;

		// Особое условие для атакующих аура-скиллов (Vengeance 368):
		// если ни одна цель не задета то селфэффекты не накладываются
		if(!(isNotTargetAoE() && isDebuff() && targets.size() == 0))	// TODO: Check this.
		{
			for(EffectTemplate et : getEffectTemplates(EffectUseType.SELF_INSTANT))
				useInstantEffect(et, activeChar, activeChar, false);

			getEffects(activeChar, activeChar, EffectUseType.SELF, 0, 1.0, true);
		}

		boolean startAttackStance = false;

		for(Creature target : targets)
		{
			if(target == null)
				continue;

			if(!checkCastTarget(target))
				continue;

			if(applyEffectPoint(activeChar, target))
				startAttackStance = true;

			// 判定是否是同血盟 队友 团队
			if (this.getSkillType().equals(SkillType.BUFF)) {
				Player tPlayer = target.getPlayer();
				if (tPlayer!=null && tPlayer.getBlockBuffs() && target.isPlayable() && activeChar.isPlayable()) {
					if (tPlayer.getBlockBuffs() && !tPlayer.isInOlympiadMode() && !Skill.getBlockBuffConditions(activeChar, tPlayer)) {
						continue;
					}
				}
			}

			final boolean reflected = target.checkReflectSkill(activeChar, this);
			final boolean successEffect = hasEffects(EffectUseType.NORMAL) && calcEffectsSuccess(activeChar, target, true);

			if(successEffect || !hasEffects(EffectUseType.NORMAL) || !_noEffectsIfFailSkill) { // TODO: Избавиться, переделать по оффу.
				useSkill(activeChar, target, reflected);

				for (EffectTemplate et : getEffectTemplates(EffectUseType.NORMAL_INSTANT))
					useInstantEffect(et, activeChar, target, reflected);
			}

			if(successEffect)
			{
				getEffects(activeChar, target);
			}
		}

		if(isSSPossible())
		{
			if(!(Config.SAVING_SPS && _skillType == SkillType.BUFF))
				activeChar.unChargeShots(isMagic());
		}

		if(isSuicideAttack()) // TODO: Переделать на селф эффект.
			activeChar.doDie(null);
		else if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public static boolean getBlockBuffConditions(Creature activeChar, Creature aimingTarget)
	{
		if (activeChar.isPlayable() && activeChar.getPlayer() != null)
		{
			if (aimingTarget.isMonster())
			{
				return false;
			}

			final Player player = activeChar.getPlayer();
			if (aimingTarget.isPlayable() && aimingTarget.getPlayer() != null)
			{
				final Player target = aimingTarget.getPlayer();

				if (player.getParty() != null && target.getParty() != null && player.getParty() == target.getParty())
				{
					return true;
				}
				if (player.getClan() != null && target.getClan() != null)
				{
					if (player.getClan().getClanId() == target.getClan().getClanId() && !player.isInOlympiadMode())
					{
						return true;
					}
					if (player.getAllyId() > 0 && target.getAllyId() > 0 && player.getAllyId() == target.getAllyId() && !player.isInOlympiadMode())
					{
						return true;
					}
				}
				if (player.isInOlympiadMode() && player.getOlympiadSide() == target.getOlympiadSide())
				{
					return true;
				}
				if (player.getPlayerGroup() == target.getPlayerGroup())
				{
					return true;
				}
				if (player.isInSiegeZone())
				{
					return false;
				}
				if (player.isInZoneBattle())
				{
					return false;
				}
				if (target.getKarma() > 0)
				{
					return false;
				}
			}
		}
		return false;
	}

	public void onFinishCast(Creature aimingTarget, Creature activeChar, Set<Creature> targets)
	{
		if(isDebuff())
		{
			if(getTargetType() == SkillTargetType.TARGET_AREA_AIM_CORPSE)
			{
				if(aimingTarget.isNpc())
					((NpcInstance) aimingTarget).endDecayTask();
				else if(aimingTarget.isSummon())
					((SummonInstance) aimingTarget).endDecayTask();
			}
			else if(getTargetType() == SkillTargetType.TARGET_CORPSE)
			{
				for(Creature target : targets)
				{
					if(target.isNpc())
						((NpcInstance) target).endDecayTask();
					else if(target.isSummon())
						((SummonInstance) target).endDecayTask();
				}
			}
		}
	}

	public void onAbnormalTimeEnd(Creature activeChar, Creature target)
	{
		if(!checkCastTarget(target))
			return;

		for(EffectTemplate et : getEffectTemplates(EffectUseType.END))
			useInstantEffect(et, activeChar, target, false);
	}

	/**
	*	activeChar - Кастующий персонаж.
	*	target - цель использования скилла.
	*	targets - все цели, на которых будет использован скилл (включая target).
	*	reflected - отражен ли скилл.
	**/
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		//
	}

	private boolean useInstantEffect(EffectTemplate et, Creature activeChar, Creature target, boolean reflected)
	{
		if(!et.isInstant())
			return false;

		if(!et.getTargetType().checkTarget(target))
			return false;

		if(et.getChance() >= 0 && !Rnd.chance(et.getChance()))
			return false;

		final EffectHandler handler = et.getHandler();
		if(!handler.checkConditionImpl(activeChar, target))
			return false;

		handler.instantUse(activeChar, target, reflected);
		return true;
	}

	public boolean isAoE()
	{
		switch(_targetType)
		{
			case TARGET_AREA:
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AURA:
			case TARGET_SERVITOR_AURA:
			case TARGET_GROUND:
			case TARGET_FAN:
			case TARGET_FAN_PB:
			case TARGET_SQUARE:
			case TARGET_SQUARE_PB:
			case TARGET_RANGE:
			case TARGET_RING_RANGE:
				return true;
			default:
				return false;
		}
	}

	public boolean isNotTargetAoE()
	{
		switch(_targetType)
		{
			case TARGET_AURA:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_PARTY:
			case TARGET_PARTY_WITHOUT_ME:
			case TARGET_GROUND:
			case TARGET_FAN_PB:
			case TARGET_SQUARE_PB:
				return true;
			default:
				return false;
		}
	}

	public boolean isDebuff()
	{
		return _isDebuff;
	}

	public final boolean isForceUse()
	{
		return _isForceUse;
	}

	public boolean isAI()
	{
		return _skillType.isAI();
	}

	public boolean isPvM()
	{
		return _isPvm;
	}

	public final boolean isPvpSkill()
	{
		return _isPvpSkill;
	}

	public boolean isOffensive()
	{
		return _isOffensive;
	}

	public boolean isTrigger()
	{
		return _isTrigger;
	}

	public boolean oneTarget()
	{
		switch(_targetType)
		{
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_HOLY:
			case TARGET_FLAGPOLE:
			case TARGET_ITEM:
			case TARGET_NONE:
			case TARGET_ONE:
			case TARGET_CLAN_ONE:
			case TARGET_PARTY_ONE:
			case TARGET_PARTY_ONE_WITHOUT_ME:
			case TARGET_ONE_SERVITOR:
			case TARGET_ONE_SERVITOR_NO_TARGET:
			case TARGET_SUMMON:
			case TARGET_PET:
			case TARGET_OWNER:
			case TARGET_ENEMY_PET:
			case TARGET_ENEMY_SUMMON:
			case TARGET_ENEMY_SERVITOR:
			case TARGET_SELF:
			case TARGET_UNLOCKABLE:
			case TARGET_CHEST:
			case TARGET_SIEGE:
				return true;
			default:
				return false;
		}
	}

	public boolean isSkillInterrupt()
	{
		return _skillInterrupt;
	}

	public boolean isNotUsedByAI()
	{
		return _isNotUsedByAI;
	}

	/**
	 * Игнорирование резистов
	 */
	public boolean isIgnoreResists()
	{
		return _isIgnoreResists;
	}

	/**
	 * Игнорирование неуязвимости
	 */
	public boolean isIgnoreInvul()
	{
		return _isIgnoreInvul;
	}

	public boolean isNotAffectedByMute()
	{
		return _isNotAffectedByMute;
	}

	public boolean flyingTransformUsage()
	{
		return _flyingTransformUsage;
	}

	public final boolean canUseTeleport()
	{
		return _canUseTeleport;
	}

	public int getTickInterval()
	{
		return _tickInterval;
	}

	public double getSimpleDamage(Creature attacker, Creature target)
	{
		if(isMagic())
		{
			// магический урон
			double mAtk = attacker.getMAtk(target, this);
			double mdef = target.getMDef(null, this);
			double power = getPower();
			double shotPower = (100 + (isSSPossible() ? attacker.getChargedSpiritshotPower() : 0)) / 100.;
			return 91 * power * Math.sqrt(shotPower * mAtk) / mdef;
		}
		// физический урон
		double pAtk = attacker.getPAtk(target);
		double pdef = target.getPDef(attacker);
		double power = getPower();
		double shotPower = (100 + (isSSPossible() ? attacker.getChargedSoulshotPower() : 0)) / 100.;
		return shotPower * (pAtk + power) * 70. / pdef;
	}

	public long getReuseForMonsters()
	{
		long min = 1000;
		switch(_skillType)
		{
			case PARALYZE:
			case DEBUFF:
			case STEAL_BUFF:
				min = 10000;
				break;
			case MUTE:
			case ROOT:
			case SLEEP:
			case STUN:
				min = 5000;
				break;
		}
		return Math.max(Math.max(_hitTime + _coolTime, _reuseDelay), min);
	}

	public double getAbsorbPart()
	{
		return _absorbPart;
	}

	public boolean isProvoke()
	{
		return _isProvoke;
	}

	public String getIcon()
	{
		return _icon;
	}

	public int getEnergyConsume()
	{
		return _energyConsume;
	}

	public int getClanRepConsume()
	{
		return _cprConsume;
	}

	public int getFameConsume()
	{
		return _fameConsume;
	}

	public void setCubicSkill(boolean value)
	{
		_isCubicSkill = value;
	}

	public boolean isCubicSkill()
	{
		return _isCubicSkill;
	}

	public int[] getRelationSkills()
	{
		return _relationSkillsId;
	}

	public boolean isRelationSkill()
	{
		return _isRelation;
	}

	public boolean isAbortable()
	{
		return _abortable;
	}
	
	public boolean isCanUseWhileAbnormal()
	{
		return _canUseWhileAbnormal;
	}

	public int getToggleGroupId()
	{
		return _toggleGroupId;
	}

	/**
	 * Используется TOGGLE-скиллами. Отключает возможность отключения тугла.
	**/
	public boolean isNecessaryToggle()
	{
		return isToggle() && _isNecessaryToggle;
	}

	public boolean isDoNotDispelOnSelfBuff()
	{
		return _isNotDispelOnSelfBuff;
	}

	public int getAbnormalTime()
	{
		return _abnormalTime;
	}

	public int getAbnormalLvl()
	{
		return _abnormalLvl;
	}

	public AbnormalType getAbnormalType()
	{
		return _abnormalType;
	}

	public AbnormalEffect[] getAbnormalEffects()
	{
		return _abnormalEffects;
	}

	public boolean isAbnormalHideTime()
	{
		return _abnormalHideTime || _operateType.isAura();
	}

	public boolean isAbnormalCancelOnAction()
	{
		return _abnormalCancelOnAction;
	}

	public boolean isIrreplaceableBuff()
	{
		return _irreplaceableBuff;
	}

	public boolean isAbnormalInstant()
	{
		return _abnormalInstant;
	}

	public boolean checkRideState(MountType mountType)
	{
		int v = 1 << mountType.ordinal();
		return (_rideState & v) == v;
	}

	public final boolean applyEffectsOnSummon()
	{
		return _applyEffectsOnSummon;
	}

	public final boolean applyEffectsOnPet()
	{
		return _applyEffectsOnPet;
	}

	// @Rivelia.
	public final boolean isApplyMinRange()
	{
		return _applyMinRange;
	}
	public final int getMasteryLevel()
	{
		return _masteryLevel;
	}
	// .

	public final boolean isSelfDebuff()
	{
		return _isSelfDebuff;
	}
	
	public boolean canBeEvaded()
	{
		switch(getSkillType())
		{
			case CHARGE:
			case PDAM:
				return true;
		}
		return false;
	}

	public double getDefenceIgnorePercent()
	{
		return _defenceIgnorePercent;
	}

	public int getBehindRadius()
	{
		return _behindRadius;
	}

	public double getPercentDamageIfTargetDebuff()
	{
		return _percentDamageIfTargetDebuff;
	}

	public boolean isNoFlagNoForce()
	{
		return _noFlagNoForce;
	}

	public boolean isRenewal()
	{
		return _renewal;
	}

	public int getBuffSlotType()
	{
		return _buffSlotType;
	}

	public BasicProperty getBasicProperty()
	{
		return _basicProperty;
	}

	public boolean isDouble()
	{
		return _isDouble;
	}

	/**
	 * Return custom minimum skill/effect chance.
	 * @return
	 */
	public double getMinChance()
	{
		return _minChance;
	}

	/**
	 * Return custom maximum skill/effect chance.
	 * @return
	 */
	public double getMaxChance()
	{
		return _maxChance;
	}

	public double getOnAttackCancelChance()
	{
		return _onAttackCancelChance;
	}

	public double getOnCritCancelChance()
	{
		return _onCritCancelChance;
	}

	public void setShowPlayerAbnormal(boolean value)
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		showPlayerAbnormal = value;
	}

	public boolean isShowPlayerAbnormal()
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		return showPlayerAbnormal;
	}

	public void setShowNpcAbnormal(boolean value)
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		showNpcAbnormal = value;
	}

	public boolean isShowNpcAbnormal()
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		return showNpcAbnormal;
	}

	public SkillAutoUseType getAutoUseType() {
		return autoUseType;
	}
}
