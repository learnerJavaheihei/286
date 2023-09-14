package l2s.gameserver.model;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.collections.LazyArrayList;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.listener.Listener;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.PlayableAI.AINextAction;
import l2s.gameserver.data.xml.holder.DamageHolder;
import l2s.gameserver.data.xml.holder.LevelBonusHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.data.xml.holder.TransformTemplateHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.GameObjectTasks.HitTask;
import l2s.gameserver.model.GameObjectTasks.NotifyAITask;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.CreatureMovement;
import l2s.gameserver.model.actor.CreatureSkillCast;
import l2s.gameserver.model.actor.basestats.CreatureBaseStats;
import l2s.gameserver.model.actor.flags.CreatureFlags;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2s.gameserver.model.actor.stat.CreatureStat;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.base.TransformType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reference.L2Reference;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket.FlyType;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.skills.*;
import l2s.gameserver.skills.skillclasses.Charge;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.stats.StatFunctions;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.triggers.RunnableTrigger;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.taskmanager.RegenTaskManager;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.AbnormalsComparator;
import l2s.gameserver.utils.PositionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

public abstract class Creature extends GameObject
{
	public class AbortCastDelayed implements Runnable
	{
		private Creature _cha;
		
		public AbortCastDelayed (Creature cha)
		{
			_cha = cha;
		}
		@Override
		public void run()
		{
			if(_cha == null)
				return;
			_cha.abortCast(true, true);	
		}	
	}

	private static final Logger _log = LoggerFactory.getLogger(Creature.class);

	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	public static final int INTERACTION_DISTANCE = 200;

	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;

	private Future<?> _deleteTask;

	public final static int CLIENT_BAR_SIZE = 352; // 352 - размер полоски CP/HP/MP в клиенте, в пикселях

	private int _lastCpBarUpdate = -1;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;

	protected double _currentCp = 0;
	private double _currentHp = 1;
	protected double _currentMp = 1;

	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;
	private long _lastAttackTime = -1;
	private int _poleAttackCount = 0;
	private static final double[] POLE_VAMPIRIC_MOD = { 1, 0.9, 0, 7, 0.2, 0.01 };

	/** HashMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final IntObjectMap<SkillEntry> _skills = new CTreeIntObjectMap<SkillEntry>();
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;

	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<TimeStamp>();

	protected volatile AbnormalList _effectList;

	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;

	/** Map 32 bits (0x00000000) containing all abnormal effect in progress */
	private Set<AbnormalEffect> _abnormalEffects = new CopyOnWriteArraySet<AbnormalEffect>();

	private AtomicBoolean isDead = new AtomicBoolean();
	protected AtomicBoolean isTeleporting = new AtomicBoolean();

	private boolean _fakeDeath;
	private boolean _isPreserveAbnormal; // Восстанавливает все бафы после смерти
	private boolean _isSalvation; // Восстанавливает все бафы после смерти и полностью CP, MP, HP

	private boolean _meditated;
	private boolean _lockedTarget;

	private boolean _blocked;

	private final Map<EffectHandler, TIntSet> _ignoreSkillsEffects = new HashMap<>();

	private volatile HardReference<? extends Creature> _effectImmunityException = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _damageBlockedException = HardReferences.emptyRef();

	private boolean _flying;

	private boolean _running;

	private volatile HardReference<? extends GameObject> _target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();

	private int _rndCharges = 0;

	private int _heading;

	private CreatureTemplate _template;

	protected volatile CharacterAI _ai;

	protected String _name;
	protected String _title;
	protected TeamType _team = TeamType.NONE;

	private boolean _isRegenerating;
	private final Lock regenLock = new ReentrantLock();
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;

	private List<Zone> _zones = new LazyArrayList<Zone>();
	/** Блокировка для чтения/записи объектов из региона */
	private final ReadWriteLock zonesLock = new ReentrantReadWriteLock();
	private final Lock zonesRead = zonesLock.readLock();
	private final Lock zonesWrite = zonesLock.writeLock();

	protected volatile CharListenerList listeners;

	private final Lock statusListenersLock = new ReentrantLock();

	protected HardReference<? extends Creature> reference;

	private boolean _isInTransformUpdate = false;
	private TransformTemplate _visualTransform = null;

	private boolean _isDualCastEnable = false;

	private boolean _isTargetable = true;

	protected CreatureBaseStats _baseStats = null;
	protected CreatureStat _stat = null;
	protected CreatureFlags _statuses = null;

	private volatile Map<BasicProperty, BasicPropertyResist> _basicPropertyResists;

	private int _gmSpeed = 0;

	private final CreatureMovement _movement = new CreatureMovement(this);
	private final CreatureSkillCast[] _skillCasts = new CreatureSkillCast[SkillCastingType.VALUES.length];
	
	private int _hitBlocks;
	
	private final Map<Integer, DamageHolder> _damageInfo = new ConcurrentHashMap<>();

	public Creature(int objectId, CreatureTemplate template)
	{
		super(objectId);

		_template = template;

		StatFunctions.addPredefinedFuncs(this);

		reference = new L2Reference<Creature>(this);

		if(!isPlayer())	// Игрока начинаем хранить после полного рестора.
			GameObjectsStorage.put(this);
	}

	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}

	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}

	public final void abortAttack(boolean force, boolean message)
	{
		if(isAttackingNow())
		{
			_attackEndTime = 0;
			if(force)
				_isAttackAborted = true;

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
			{
				sendActionFailed();
				sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_FAILED).addName(this));
			}
		}
	}
	
	public void saveDamage(Creature attacker, Skill skill, int value, int type)
	{
		String name = "";
		int id = 0;
		if (attacker != null)
		{
			if (attacker.isPlayable())
			{
				name = attacker.getName();
			}
			else
			{
				id = attacker.getNpcId();
			}
		}
		
		int size = _damageInfo.size();
		while (size > 29)
		{
			_damageInfo.remove(1);
			size--;
		}
		if (skill != null)
		{
			type = 0;
		}
		_damageInfo.put(size + 1, new DamageHolder(id, name, skill, value, type));
	}
	
	public Map<Integer, DamageHolder> getDamageInfo()
	{
		return _damageInfo;
	}
	
	public void resetDamageInfo()
	{
		_damageInfo.clear();
	}

	public final void abortCast(boolean force, boolean message, boolean normalCast, boolean dualCast)
	{
		boolean cancelled = false;

		if(normalCast)
		{
			if(getSkillCast(SkillCastingType.NORMAL).abortCast(force))
				cancelled = true;
		}

		if(dualCast)
		{
			if(getSkillCast(SkillCastingType.NORMAL_SECOND).abortCast(force))
				cancelled = true;
		}

		if(cancelled)
		{
			broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
				sendPacket(SystemMsg.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
		}
	}

	public final void abortCast(boolean force, boolean message)
	{
		abortCast(force, message, true, true);
	}

	// Reworked by Rivelia.
	private double reflectDamage(Creature attacker, Skill skill, double damage)
	{
		if(isDead() || damage <= 0 || !attacker.checkRange(attacker, this) || getCurrentHp() + getCurrentCp() <= damage)
			return 0.;

		final boolean bow = attacker.getBaseStats().getAttackType() == WeaponType.BOW || attacker.getBaseStats().getAttackType() == WeaponType.CROSSBOW || attacker.getBaseStats().getAttackType() == WeaponType.TWOHANDCROSSBOW;
		final double resistReflect = 1 - (attacker.getStat().calc(Stats.RESIST_REFLECT_DAM, 0, null, null) * 0.01); 

		double value = 0.;
		double chanceValue = 0.;
		if(skill != null)
		{
			if(skill.isMagic())
			{
				chanceValue = getStat().calc(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0, attacker, skill);
				value = getStat().calc(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0, attacker, skill);
			}
			else if(skill.isPhysic())
			{
				chanceValue = getStat().calc(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0, attacker, skill);
				value = getStat().calc(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0, attacker, skill);
			}
		}
		else
		{
			chanceValue = getStat().calc(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0, attacker, null);
			if(bow)
				value = getStat().calc(Stats.REFLECT_BOW_DAMAGE_PERCENT, 0, attacker, null);
			else
				value = getStat().calc(Stats.REFLECT_DAMAGE_PERCENT, 0, attacker, null);
		}

		// If we are not lucky, set back value to 0, otherwise set it equal to damage.
		if(chanceValue > 0 && Rnd.chance(chanceValue))
			chanceValue = damage;
		else
			chanceValue = 0.;

		if(value > 0 || chanceValue > 0)
		{
			value = ((value / 100. * damage) + chanceValue) * resistReflect;
			if(Config.REFLECT_DAMAGE_CAPPED_BY_PDEF)	// @Rivelia. If config is on: reflected damage cannot exceed enemy's P. Def.
			{
				int xPDef = attacker.getPDef(this);
				if(xPDef > 0)
					value = Math.min(value, xPDef);
			}
			return value;
		}
		return 0.;
	}

	private void absorbDamage(Creature target, Skill skill, double damage)
	{
		absorbDamageHP(target, skill,damage);
		absorbDamageMP(target, skill,damage);
	}

	private void absorbDamageHP(Creature target, Skill skill, double damage)
	{
		if(target.isDead())
			return;

		if(damage <= 0)
			return;

		if(!Config.SKILL_ABSORB){
			if(skill != null) // On classic skill attack dont use absorb.
				return;
		}

		final boolean bow = getBaseStats().getAttackType() == WeaponType.BOW || getBaseStats().getAttackType() == WeaponType.CROSSBOW || getBaseStats().getAttackType() == WeaponType.TWOHANDCROSSBOW;
		if(!Config.BOW_ABSORB){
			if(bow && skill == null) // On classic range attack dont use absorb.
				return;
		}
		final boolean meele = getBaseStats().getAttackType() == WeaponType.SWORD || getBaseStats().getAttackType() == WeaponType.BLUNT || getBaseStats().getAttackType() == WeaponType.DAGGER || getBaseStats().getAttackType() == WeaponType.POLE || getBaseStats().getAttackType() == WeaponType.FIST || getBaseStats().getAttackType() == WeaponType.DUAL || getBaseStats().getAttackType() == WeaponType.DUALFIST || getBaseStats().getAttackType() == WeaponType.BIGSWORD || getBaseStats().getAttackType() == WeaponType.BIGBLUNT || getBaseStats().getAttackType() == WeaponType.RAPIER || getBaseStats().getAttackType() == WeaponType.ANCIENTSWORD || getBaseStats().getAttackType() == WeaponType.DUALDAGGER || getBaseStats().getAttackType() == WeaponType.DUALBLUNT;
		if(!Config.PHYS_ABSORB){
			if(meele && skill == null)
				return;
		}
		if(target.isDamageBlocked(this))
			return;

		// вампирик
		//damage = (int) (damage - target.getCurrentCp() - target.getCurrentHp()); WTF?

		final double poleMod = POLE_VAMPIRIC_MOD[Math.max(0, Math.min(_poleAttackCount, POLE_VAMPIRIC_MOD.length - 1))];

		double absorb = poleMod * getStat().calc(Stats.VAMPIRIC_ATTACK, 0, this, null);

		if(absorb > 0 && !target.isServitor() && !target.isInvulnerable())
		{
			double limit = getStat().calc(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.;
			if(getCurrentHp() < limit) {
				double absorbDamage = damage * absorb / 100.;
				absorbDamage = Math.min(absorbDamage, (int) target.getCurrentHp());
				setCurrentHp(Math.min(getCurrentHp() + absorbDamage, limit), false);
			}
		}

	}

	private void absorbDamageMP(Creature target, Skill skill, double damage)
	{
		if(target.isDead())
			return;

		if(damage <= 0)
			return;

		if(!Config.SKILL_ABSORBMP){
			if(skill != null) // On classic skill attack dont use absorb.
				return;
		}

		final boolean bow = getBaseStats().getAttackType() == WeaponType.BOW || getBaseStats().getAttackType() == WeaponType.CROSSBOW || getBaseStats().getAttackType() == WeaponType.TWOHANDCROSSBOW;
		if(!Config.BOW_ABSORBMP){
			if(bow && skill == null) // On classic range attack dont use absorb.
				return;
		}
		final boolean meele = getBaseStats().getAttackType() == WeaponType.SWORD || getBaseStats().getAttackType() == WeaponType.BLUNT || getBaseStats().getAttackType() == WeaponType.DAGGER || getBaseStats().getAttackType() == WeaponType.POLE || getBaseStats().getAttackType() == WeaponType.FIST || getBaseStats().getAttackType() == WeaponType.DUAL || getBaseStats().getAttackType() == WeaponType.DUALFIST || getBaseStats().getAttackType() == WeaponType.BIGSWORD || getBaseStats().getAttackType() == WeaponType.BIGBLUNT || getBaseStats().getAttackType() == WeaponType.RAPIER || getBaseStats().getAttackType() == WeaponType.ANCIENTSWORD || getBaseStats().getAttackType() == WeaponType.DUALDAGGER || getBaseStats().getAttackType() == WeaponType.DUALBLUNT;
		if(!Config.PHYS_ABSORBMP){
			if(meele && skill == null)
				return;
		}
		if(target.isDamageBlocked(this))
			return;

		// вампирик
		//damage = (int) (damage - target.getCurrentCp() - target.getCurrentHp()); WTF?

		final double poleMod = POLE_VAMPIRIC_MOD[Math.max(0, Math.min(_poleAttackCount, POLE_VAMPIRIC_MOD.length - 1))];

		double absorb = poleMod * getStat().calc(Stats.MP_VAMPIRIC_ATTACK, 0, target, null);
		if(absorb > 0 && !target.isServitor() && !target.isInvulnerable())
		{
			double limit = getStat().calc(Stats.MP_LIMIT, null, null) * getMaxMp() / 100.;
			if(getCurrentMp() < limit) {
				double absorbDamage = damage * absorb / 100.;
				absorbDamage = Math.min(absorbDamage, (int) target.getCurrentMp());
				setCurrentMp(Math.min(getCurrentMp() + absorbDamage, limit));
			}
		}
	}

	public double absorbToEffector(Creature attacker, double damage)
	{
		if(damage == 0)
			return 0;

		double transferToEffectorDam = getStat().calc(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.);
		if(transferToEffectorDam > 0)
		{
			Collection<Abnormal> abnormals = getAbnormalList().values();
			if(abnormals.isEmpty())
				return damage;

			// TODO: Переписать.
			for(Abnormal abnormal : abnormals)
			{
				for(EffectHandler effect : abnormal.getEffects())
				{
					if(!effect.getName().equalsIgnoreCase("AbsorbDamageToEffector"))
						continue;

					Creature effector = abnormal.getEffector();
					// на мертвого чара, не онлайн игрока - не даем абсорб, и не на самого себя
					if(effector == this || effector.isDead() || !isInRange(effector, 1200))
						return damage;

					Player thisPlayer = getPlayer();
					Player effectorPlayer = effector.getPlayer();
					if(thisPlayer != null && effectorPlayer != null)
					{
						if(thisPlayer != effectorPlayer && (!thisPlayer.isOnline() || !thisPlayer.isInParty() || thisPlayer.getParty() != effectorPlayer.getParty()))
							return damage;
					}
					else
						return damage;

					double transferDamage = (damage * transferToEffectorDam) * .01;
					damage -= transferDamage;

					effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
				}
			}
		}
		return damage;
	}

	private double reduceDamageByMp(Creature attacker, double damage)
	{
		if(damage == 0)
			return 0;

		double power = getStat().calc(Stats.TRANSFER_TO_MP_DAMAGE_PERCENT, 0.);
		if(power <= 0)
			return damage;

		double mpDam = damage - (damage * power) / 100.;
		if(mpDam > 0)
		{
			if(mpDam >= getCurrentMp())
			{
				damage = mpDam - getCurrentMp();
				sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
				setCurrentMp(0);
				getAbnormalList().stop(AbnormalType.MP_SHIELD);
			}
			else
			{
				reduceCurrentMp(mpDam, null);
				sendPacket(new SystemMessagePacket(SystemMsg.ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP).addInteger((int) mpDam));
				return 0;
			}
		}
		return damage;
	}

	public Servitor getServitorForTransfereDamage(double damage)
	{
		return null;
	}

	public double getDamageForTransferToServitor(double damage)
	{
		return 0.;
	}

	public SkillEntry addSkill(SkillEntry newSkillEntry)
	{
		if(newSkillEntry == null)
			return null;

		SkillEntry oldSkillEntry = _skills.get(newSkillEntry.getId());
		if(newSkillEntry.equals(oldSkillEntry))
			return oldSkillEntry;

		// Replace oldSkill by newSkill or Add the newSkill
		_skills.put(newSkillEntry.getId(), newSkillEntry);

		Skill newSkill = newSkillEntry.getTemplate();

		if(oldSkillEntry != null)
		{
			Skill oldSkill = oldSkillEntry.getTemplate();
			if(oldSkill.isToggle())
			{
				if(oldSkill.getLevel() > newSkill.getLevel())
					getAbnormalList().stop(oldSkill, false);
			}

			removeTriggers(oldSkill);

			if(oldSkill.isPassive())
			{
				getStat().removeFuncsByOwner(oldSkill);

				for(EffectTemplate et : oldSkill.getEffectTemplates(EffectUseType.NORMAL))
					getStat().removeFuncsByOwner(et.getHandler());
			}

			onRemoveSkill(oldSkillEntry);
		}

		addTriggers(newSkill);

		if(newSkill.isPassive())
		{
			// Add Func objects of newSkill to the calculator set of the L2Character
			getStat().addFuncs(newSkill.getStatFuncs());

			for(EffectTemplate et : newSkill.getEffectTemplates(EffectUseType.NORMAL))
				getStat().addFuncs(et.getHandler().getStatFuncs());
		}

		onAddSkill(newSkillEntry);

		return oldSkillEntry;
	}

	protected void onAddSkill(SkillEntry skill)
	{
		//
	}

	protected void onRemoveSkill(SkillEntry skillEntry)
	{
		//
	}

	public void altOnMagicUse(Creature aimingTarget, SkillEntry skillEntry)
	{
		if(isAlikeDead() || skillEntry == null)
			return;

		Skill skill = skillEntry.getTemplate();
		int magicId = skill.getDisplayId();
		int level = skill.getDisplayLevel();
		Set<Creature> targets = skill.getTargets(skillEntry,this, aimingTarget, true);
		if(!skill.isNotBroadcastable())
			broadcastPacket(new MagicSkillLaunchedPacket(getObjectId(), magicId, level, targets, SkillCastingType.NORMAL));
		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			double mpConsume2WithStats;
			if(skill.isMagic())
				mpConsume2WithStats = getStat().calc(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2WithStats = getStat().calc(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);

			if(_currentMp < mpConsume2WithStats)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return;
			}
			reduceCurrentMp(mpConsume2WithStats, null);
		}
		callSkill(aimingTarget, skillEntry, targets, false, false);
	}

	public final void forceUseSkill(SkillEntry skillEntry, Creature target)
	{
		if(skillEntry == null)
			return;

		Skill skill = skillEntry.getTemplate();
		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if(target == null)
				return;
		}

		final Set<Creature> targets = skill.getTargets(skillEntry, this, target, true);

		if(!skill.isNotBroadcastable())
		{
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0));
			broadcastPacket(new MagicSkillLaunchedPacket(getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets, SkillCastingType.NORMAL));
		}

		callSkill(target, skillEntry, targets, false, false);
	}

	public void altUseSkill(SkillEntry skillEntry, Creature target)
	{
		if(skillEntry == null)
			return;

		if(isUnActiveSkill(skillEntry.getId()))
			return;

		Skill skill = skillEntry.getTemplate();
		if(isSkillDisabled(skill))
			return;

		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if(target == null)
				return;
		}

		getListeners().onMagicUse(skill, target, true);

		if(!skill.isHandler() && isPlayable())
		{
			if(skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if(skill.isItemConsumeFromMaster())
				{
					Player master = getPlayer();
					if(master == null || !master.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false))
						return;
				}
				else if(!consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false))
					return;
			}
		}

		if(skill.getReferenceItemId() > 0)
		{
			if(!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;
		}

		if(skill.getEnergyConsume() > getAgathionEnergy())
			return;

		if(skill.getEnergyConsume() > 0)
			setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());

		long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);

		if(!skill.isToggle() && !skill.isNotBroadcastable())
		{
			MagicSkillUse msu = new MagicSkillUse(this, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), reuseDelay);
			msu.setReuseSkillId(skill.getReuseSkillId());
			broadcastPacket(msu);
		}

		disableSkill(skill, reuseDelay);

		altOnMagicUse(target, skillEntry);
	}

	public void sendReuseMessage(Skill skill)
	{}

	public void broadcastPacket(IBroadcastPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacket(List<IBroadcastPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacketToOthers(IBroadcastPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		for(Player target : World.getAroundObservers(this))
			target.sendPacket(packets);
	}

	public void broadcastPacketToOthers(List<IBroadcastPacket> packets)
	{
		broadcastPacketToOthers(packets.toArray(new IBroadcastPacket[packets.size()]));
	}

	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;

		broadcastPacket(new StatusUpdate(this, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP, StatusUpdatePacket.CUR_MP, StatusUpdatePacket.MAX_MP));
	}

	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}

	public void callSkill(Creature aimingTarget, SkillEntry skillEntry, Set<Creature> targets, boolean useActionSkills, boolean trigger)
	{
		try
		{
			Skill skill = skillEntry.getTemplate();
			if(useActionSkills)
			{
				if(skill.isDebuff())
				{
					useTriggers(aimingTarget, TriggerType.OFFENSIVE_SKILL_USE, null, skill, 0);

					if(skill.isMagic())
						useTriggers(aimingTarget, TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0);
					else if(skill.isPhysic())
						useTriggers(aimingTarget, TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0);
				}
				else
				{
					useTriggers(aimingTarget, TriggerType.SUPPORT_SKILL_USE, null, skill, 0);

					if(skill.isMagic())
						useTriggers(aimingTarget, TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0);
					else if(skill.isPhysic())
						useTriggers(aimingTarget, TriggerType.SUPPORT_PHYSICAL_SKILL_USE, null, skill, 0);
				}

				useTriggers(this, TriggerType.ON_CAST_SKILL, null, skill, 0);
			}

			final Player player = getPlayer();
			for(Creature target : targets)
			{
				if(target == null)
					continue;

				target.getListeners().onMagicHit(skill, this);

				if(player != null && target.isNpc())
				{
					NpcInstance npc = (NpcInstance) target;
					List<QuestState> ql = player.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
					if(ql != null)
					{
						for(QuestState qs : ql)
							qs.getQuest().notifySkillUse(npc, skill, qs);
					}
				}
			}

			useTriggers(aimingTarget, TriggerType.ON_END_CAST, null, skill, 0);

			skill.onEndCast(this, targets);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
	{
		useTriggers(target, null, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, Set<Creature> targets, TriggerType type, Skill ex, Skill owner, double damage)
	{
		useTriggers(target, targets, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
		useTriggers(target, null, type, ex, owner, triggersOwner, damage);
	}

	public void useTriggers(GameObject target, Set<Creature> targets, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
		Set<TriggerInfo> triggers = null;
		switch(type)
		{
			case ON_START_CAST:
			case ON_TICK_CAST:
			case ON_END_CAST:
			case ON_FINISH_CAST:
			case ON_START_EFFECT:
			case ON_EXIT_EFFECT:
			case ON_FINISH_EFFECT:
			case ON_REVIVE:
				if(triggersOwner != null)
				{
					triggers = new CopyOnWriteArraySet<TriggerInfo>();
					for(TriggerInfo t : triggersOwner.getTriggerList())
					{
						if(t.getType() == type)
							triggers.add(t);
					}
				}
				break;
			case ON_CAST_SKILL:
				if(_triggers != null && _triggers.get(type) != null)
				{
					triggers = new CopyOnWriteArraySet<>();
					for(TriggerInfo t : _triggers.get(type))
					{
						int skillID = t.getArgs() == null || t.getArgs().isEmpty() ? -1 : Integer.parseInt(t.getArgs());
						if(skillID == - 1 || skillID == owner.getId())
							triggers.add(t);
					}
				}
				break;
			default:
				if(_triggers != null)
					triggers = _triggers.get(type);
				break;
		}

		if(triggers != null && !triggers.isEmpty())
		{
			for(TriggerInfo t : triggers)
			{
				SkillEntry skillEntry = t.getSkill();
				if(skillEntry != null)
				{
					if(!skillEntry.getTemplate().equals(ex))
						useTriggerSkill(target == null ? getTarget() : target, targets, t, owner, damage);
				}
			}
		}
	}

	public void useTriggerSkill(GameObject target, Set<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		SkillEntry skillEntry = trigger.getSkill();
		if(skillEntry == null)
			return;

		if(!Rnd.chance(trigger.getChance()))
			return;

		/*if(skill.getTargetType() == SkillTargetType.TARGET_SELF && !skill.isTrigger())
			_log.warn("Self trigger skill dont have trigger flag. SKILL ID[" + skill.getId() + "]");*/

		Skill skill = skillEntry.getTemplate();
		Creature aimTarget = skill.getAimingTarget(this, target);
		if(aimTarget != null && trigger.isIncreasing())
		{
			int increasedTriggerLvl = 0;
			for(Abnormal effect : aimTarget.getAbnormalList())
			{
				if(effect.getSkill().getId() != skillEntry.getId())
					continue;

				increasedTriggerLvl = effect.getSkill().getLevel(); //taking the first one only.
				break;
			}

			if(increasedTriggerLvl == 0)
			{
				loop: for(Servitor servitor : aimTarget.getServitors())
				{
					for(Abnormal effect : servitor.getAbnormalList())
					{
						if(effect.getSkill().getId() != skillEntry.getId())
							continue;

						increasedTriggerLvl = effect.getSkill().getLevel(); //taking the first one only.
						break loop;
					}
				}
			}

			if(increasedTriggerLvl > 0)
			{
				Skill newSkill = SkillHolder.getInstance().getSkill(skillEntry.getId(), increasedTriggerLvl + 1);
				if(newSkill != null)
					skillEntry = SkillEntry.makeSkillEntry(skillEntry.getEntryType(), newSkill);
				else
					skillEntry = SkillEntry.makeSkillEntry(skillEntry.getEntryType(), skillEntry.getId(), increasedTriggerLvl);
				skill = skillEntry.getTemplate();
			}
		}

		if(skill.getReuseDelay() > 0 && isSkillDisabled(skill))
			return;

		// DS: Для шансовых скиллов с TARGET_SELF и условием "пвп" сам кастер будет являться aimTarget,
		// поэтому в условиях для триггера проверяем реальную цель.
		Creature realTarget = target != null && target.isCreature() ? (Creature) target : null;
		if(trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skillEntry.checkCondition(this, aimTarget, true, true, true, false, true))
		{
			if(targets == null)
				targets = skill.getTargets(skillEntry, this, aimTarget, false);

			if(!skill.isNotBroadcastable())
			{
				if(trigger.getType() != TriggerType.IDLE)
				{
					for(Creature cha : targets)
						broadcastPacket(new MagicSkillUse(this, cha, skillEntry.getDisplayId(), skillEntry.getDisplayLevel(), 0, 0));
				}
			}

			callSkill(aimTarget, skillEntry, targets, false, true);
			disableSkill(skill, skill.getReuseDelay());
		}
	}

	private void triggerCancelEffects(TriggerInfo trigger)
	{
		SkillEntry skillEntry = trigger.getSkill();
		if(skillEntry == null)
			return;

		getAbnormalList().stop(skillEntry.getTemplate(), false);
	}

	public boolean checkReflectSkill(Creature attacker, Skill skill)
	{
		if(this == attacker)
			return false;
		if(isDead() || attacker.isDead())
			return false;
		if(!skill.isReflectable())
			return false;
		// Не отражаем, если есть неуязвимость, иначе она может отмениться
		if(isInvulnerable() || attacker.isInvulnerable() || !skill.isDebuff())
			return false;
		// Из магических скилов отражаются только скилы наносящие урон по ХП.
		if(skill.isMagic() && skill.getSkillType() != SkillType.MDAM)
			return false;
		if(Rnd.chance(getStat().calc(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0, attacker, skill)))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_COUNTERED_C1S_ATTACK).addName(attacker));
			attacker.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(this));
			return true;
		}
		return false;
	}

	public boolean checkReflectDebuff(Creature effector, Skill skill)
	{
		if(this == effector)
			return false;
		if(isDead() || effector.isDead())
			return false;
		if(effector.isTrap())
			return false;
		if(effector.isRaid()) // Тестово. Сверить с оффом.
			return false;
		if(!skill.isReflectable())
			return false;
		// Не отражаем, если есть неуязвимость, иначе она может отмениться
		if(isInvulnerable() || effector.isInvulnerable() || !skill.isDebuff())
			return false;
		if(isDebuffImmune())
			return false;
		return Rnd.chance(getStat().calc(skill.isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0, effector, skill));
	}

	public void doCounterAttack(Skill skill, Creature attacker, boolean blow)
	{
		if(isDead()) // если персонаж уже мертв, контратаки быть не должно
			return;
		if(isDamageBlocked(attacker) || attacker.isDamageBlocked(this)) // Не контратакуем, если есть неуязвимость, иначе она может отмениться
			return;
		if(skill == null || skill.hasEffects(EffectUseType.NORMAL) || skill.isMagic() || !skill.isDebuff() || skill.getCastRange() > 200)
			return;
		if(Rnd.chance(getStat().calc(Stats.COUNTER_ATTACK, 0, attacker, skill)))
		{
			double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
			attacker.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			if(blow) // урон х2 для отражения blow скиллов
			{
				sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
				sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(attacker).addInteger((int) damage).addHpChange(getObjectId(), attacker.getObjectId(), (int) -damage));
				attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
			}
			else
				sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(attacker).addInteger((int) damage).addHpChange(getObjectId(), attacker.getObjectId(), (int) -damage));
			attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
		}
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(Skill skill, long delay)
	{
		_skillReuses.put(skill.getReuseHash(), new TimeStamp(skill, delay));
	}

	public abstract boolean isAutoAttackable(Creature attacker);

	public void doAttack(Creature target)
	{
		if(target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isDead() || !isInRange(target, 2000)) //why alikeDead?
			return;

		if(isTransformed() && !getTransform().isNormalAttackable())
			return;

		getListeners().onAttack(target);

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int sAtk = calculateAttackDelay();
		int ssGrade = 0;
		int attackReuseDelay = 0;
		boolean ssEnabled = false;

		if(isNpc())
		{
			attackReuseDelay = ((NpcTemplate) getTemplate()).getBaseReuseDelay();
			NpcTemplate.ShotsType shotType = ((NpcTemplate) getTemplate()).getShots();
			if(shotType != NpcTemplate.ShotsType.NONE && shotType != NpcTemplate.ShotsType.BSPIRIT && shotType != NpcTemplate.ShotsType.SPIRIT)
				ssEnabled = true;
		}
		else
		{
			WeaponTemplate weaponItem = getActiveWeaponTemplate();
			if(weaponItem != null)
			{
				attackReuseDelay = weaponItem.getAttackReuseDelay();
				ssGrade = weaponItem.getGrade().extOrdinal();
			}
			ssEnabled = getChargedSoulshotPower() > 0;
		}

		if(attackReuseDelay > 0)
		{
			int reuse = (500000 + (333 * attackReuseDelay)) / getPAtkSpd();
			if(reuse > 0)
			{
				sendPacket(new SetupGaugePacket(this, SetupGaugePacket.Colors.RED, reuse));
				_attackReuseEndTime = reuse + System.currentTimeMillis() - 75;
				if(reuse > sAtk)
					ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), reuse);
			}
		}

		// DS: скорректировано на 1/100 секунды поскольку AI task вызывается с небольшой погрешностью
		// особенно на слабых машинах и происходит обрыв автоатаки по isAttackingNow() == true
		_attackEndTime = sAtk + System.currentTimeMillis() - 10;
		_isAttackAborted = false;
		_lastAttackTime = System.currentTimeMillis();

		AttackPacket attack = new AttackPacket(this, target, ssEnabled, ssGrade);

		setHeading(PositionUtils.calculateHeadingFrom(this, target), false);

		switch(getBaseStats().getAttackType())
		{
			case BOW:
			case CROSSBOW:
			case TWOHANDCROSSBOW:
				doAttackHitByBow(attack, target, sAtk);
				break;
			case POLE:
				doAttackHitByPole(attack, target, sAtk);
				break;
			case DUAL:
			case DUALFIST:
			case DUALDAGGER:
			case DUALBLUNT:
				doAttackHitByDual(attack, target, sAtk);
				break;
			default:
				doAttackHitSimple(attack, target, sAtk);
				break;
		}

		if(attack.hasHits())
			broadcastPacket(attack);
	}

	private void doAttackHitSimple(AttackPacket attack, Creature target, int sAtk)
	{
		int attackcountmax = (int) Math.round(getStat().calc(Stats.ATTACK_TARGETS_COUNT, 0, target, null));
		if(attackcountmax > 0 && !isInPeaceZone())// Гварды с пикой, будут атаковать только одиночные цели в городе
		{
			int angle = getPhysicalAttackAngle();
			int range = getPhysicalAttackRadius();

			int attackedCount = 1;

			for(Creature t : getAroundCharacters(range, 200))
			{
				if(attackedCount <= attackcountmax)
				{
					if(t == target || t.isDead() || !PositionUtils.isFacing(this, t, angle))
						continue;

					// @Rivelia. Pole should not hit targets that are flagged if we are not flagged.
					if(t.isAutoAttackable(this) && ((this.getPvpFlag() == 0 && t.getPvpFlag() == 0) || this.getPvpFlag() != 0))
					{
						doAttackHitSimple0(attack, t, 1., false, sAtk, false);
						attackedCount++;
					}
				}
				else
					break;
			}
		}
		doAttackHitSimple0(attack, target, 1., true, sAtk, true);
	}

	private void doAttackHitSimple0(AttackPacket attack, Creature target, double multiplier, boolean unchargeSS, int sAtk, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		int elementalDamage1 = 0;
		boolean elementalCrit1 = false;

		if(!miss1)
		{
			AttackInfo info = Formulas.calcAutoAttackDamage(this, target, 1., false, attack._soulshot, true);
			if(info != null)
			{
				damage1 = (int) (info.damage * multiplier);
				shld1 = info.shld;
				crit1 = info.crit;
				elementalDamage1 = (int) info.elementalDamage;
				elementalCrit1 = info.elementalCrit;
			}
		}

		int timeToHit = Formulas.calculateTimeToHit(sAtk, getBaseStats().getAttackType(), false);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify, sAtk, elementalDamage1, elementalCrit1), timeToHit);

		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByBow(AttackPacket attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		int elementalDamage1 = 0;
		boolean elementalCrit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);

		reduceArrowCount();

		if(!miss1)
		{
			AttackInfo info = Formulas.calcAutoAttackDamage(this, target, 1., true, attack._soulshot, true);
			if(info != null)
			{
				damage1 = (int) info.damage;
				shld1 = info.shld;
				crit1 = info.crit;
				elementalDamage1 = (int) info.elementalDamage;
				elementalCrit1 = info.elementalCrit;
			}

			/* В Lindvior атака теперь не зависит от расстояния.
			int range = getPhysicalAttackRange();
			damage1 *= Math.min(range, getDistance(target)) / range * .4 + 0.8; // разброс 20% в обе стороны
			*/
		}

		int timeToHit = Formulas.calculateTimeToHit(sAtk, getBaseStats().getAttackType(), false);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true, sAtk, elementalDamage1, elementalCrit1), timeToHit);

		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByDual(AttackPacket attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		int elementalDamage1 = 0;
		int elementalDamage2 = 0;
		boolean elementalCrit1 = false;
		boolean elementalCrit2 = false;

		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);

		if(!miss1)
		{
			AttackInfo info = Formulas.calcAutoAttackDamage(this, target, 0.5, false, attack._soulshot, true);
			if(info != null)
			{
				damage1 = (int) info.damage;
				shld1 = info.shld;
				crit1 = info.crit;
				elementalDamage1 = (int) info.elementalDamage;
				elementalCrit1 = info.elementalCrit;
			}
		}

		if(!miss2)
		{
			AttackInfo info = Formulas.calcAutoAttackDamage(this, target, 0.5, false, attack._soulshot, true);
			if(info != null)
			{
				damage2 = (int) info.damage;
				shld2 = info.shld;
				crit2 = info.crit;
				elementalDamage2 = (int) info.elementalDamage;
				elementalCrit2 = info.elementalCrit;
			}
		}

		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay
		int timeToHit = Formulas.calculateTimeToHit(sAtk, getBaseStats().getAttackType(), false);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false, sAtk / 2, elementalDamage1, elementalCrit1), timeToHit);
		timeToHit = Formulas.calculateTimeToHit(sAtk, getBaseStats().getAttackType(), true);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true, sAtk, elementalDamage2, elementalCrit2), timeToHit);

		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}

	private void doAttackHitByPole(AttackPacket attack, Creature target, int sAtk)
	{
		// Используем Math.round т.к. обычный кастинг обрезает к меньшему
		// double d = 2.95. int i = (int)d, выйдет что i = 2
		// если 1% угла или 1 дистанции не играет огромной роли, то для
		// количества целей это критично
		int attackcountmax = (int) Math.round(getStat().calc(Stats.POLE_TARGET_COUNT, 0, target, null));
		attackcountmax += (int) Math.round(getStat().calc(Stats.ATTACK_TARGETS_COUNT, 0, target, null));

		if(isBoss())
			attackcountmax += 27;
		else if(isRaid())
			attackcountmax += 12;
		else if(isMonster())
			attackcountmax += getLevel() / 7.5;

		if(attackcountmax > 0 && !isInPeaceZone())// Гварды с пикой, будут атаковать только одиночные цели в городе
		{
			int angle = (int) getStat().calc(Stats.POLE_ATTACK_ANGLE, getPhysicalAttackAngle(), target, null); // TODO: Вынести в датапак.
			int range = getPhysicalAttackRange() + getPhysicalAttackRadius();

			double mult = 1.;
			_poleAttackCount = 1;

			for(Creature t : getAroundCharacters(range, 200))
			{
				if(_poleAttackCount <= attackcountmax)
				{
					if(t == target || t.isDead() || !PositionUtils.isFacing(this, t, angle))
						continue;

					// @Rivelia. Pole should not hit targets that are flagged if we are not flagged.
					if(t.isAutoAttackable(this) && ((this.getPvpFlag() == 0 && t.getPvpFlag() == 0) || this.getPvpFlag() != 0))
					{
						doAttackHitSimple0(attack, t, mult, false, sAtk, false);
						mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
						_poleAttackCount++;
					}
				}
				else
					break;
			}

			_poleAttackCount = 0;
		}
		doAttackHitSimple0(attack, target, 1., true, sAtk, true);
	}

	public boolean doCast(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(getSkillCast(SkillCastingType.NORMAL).doCast(skillEntry, target, forceUse))	// Обычный каст
			return true;
		return getSkillCast(SkillCastingType.NORMAL_SECOND).doCast(skillEntry, target, forceUse);	// Дуал каст
	}

	public Location getFlyLocation(GameObject target, Skill skill)
	{
		if(target != null && target != this)
		{
			Location loc;

			int heading = target.getHeading();
			if(!skill.isFlyDependsOnHeading())
				heading = PositionUtils.calculateHeadingFrom(target, this);

			double radian = PositionUtils.convertHeadingToDegree(heading) + skill.getFlyPositionDegree();
			if(radian > 360)
				radian -= 360;

			radian = (Math.PI * radian) / 180;

			loc = new Location(target.getX() + (int) (Math.cos(radian) * 40), target.getY() + (int) (Math.sin(radian) * 40), target.getZ());

			if(isFlying())
			{
				if(isInFlyingTransform() && ((loc.z <= 0) || (loc.z >= 6000)))
					return null;
				if(GeoEngine.moveCheckInAir(this, loc.x, loc.y, loc.z) == null)
					return null;
			}
			else
			{
				loc.correctGeoZ(getGeoIndex());

				if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc(); // Если не получается встать рядом с объектом, пробуем встать прямо в него
					if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
						return null;
				}
			}

			return loc;
		}

		int x1 = 0;
		int y1 = 0;
		int z1 = 0;
		
		if(skill.getFlyType() == FlyType.THROW_UP)
		{
			x1 = 0;
			y1 = 0;
			z1 = getZ() + skill.getFlyRadius();
		}
		else
		{
			double radian = PositionUtils.convertHeadingToRadian(getHeading());
			x1 = -(int) (Math.sin(radian) * skill.getFlyRadius());
			y1 = (int) (Math.cos(radian) * skill.getFlyRadius());
		}

		if(isFlying())
			return GeoEngine.moveCheckInAir(this, getX() + x1, getY() + y1, getZ() + z1);
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}

	public final void doDie(Creature killer)
	{
		// killing is only possible one time
		if(!isDead.compareAndSet(false, true))
			return;

		onDeath(killer);
	}

	protected void onDeath(Creature killer)
	{
		if(killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if(killerPlayer != null)
				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);

			killer.getListeners().onKill(this);

			if(isPlayer() && killer.isPlayable())
				_currentCp = 0;
		}

		setTarget(null);

		abortCast(true, false);
		abortAttack(true, false);

		getMovement().stopMove();
		stopAttackStanceTask();
		stopRegeneration();

		_currentHp = 0;

		if(isPlayable())
		{
			final TIntSet effectsToRemove = new TIntHashSet();

			// Stop all active skills effects in progress on the L2Character
			if(isPreserveAbnormal() || isSalvation())
			{
				if(isSalvation() && isPlayer() && !getPlayer().isInOlympiadMode())
					getPlayer().reviveRequest(getPlayer(), 100, false);

				for(Abnormal abnormal : getAbnormalList())
				{
					int skillId = abnormal.getId();
					if(skillId == Skill.SKILL_RAID_BLESSING)
						effectsToRemove.add(skillId);
					else
					{
						for(EffectHandler effect : abnormal.getEffects())
						{
							// Noblesse Blessing Buff/debuff effects are retained after
							// death. However, Noblesse Blessing and Lucky Charm are lost as normal.
							if(effect.getName().equalsIgnoreCase("p_preserve_abnormal"))
								effectsToRemove.add(skillId);
							else if(effect.getName().equalsIgnoreCase("AgathionResurrect"))
							{
								if(isPlayer())
									getPlayer().setAgathionRes(true);
								effectsToRemove.add(skillId);
							}
						}
					}
				}
			}
			else
			{
				for(Abnormal abnormal : getAbnormalList())
				{
					// Некоторые эффекты сохраняются при смерти
					if(!abnormal.getSkill().isPreservedOnDeath())
						effectsToRemove.add(abnormal.getSkill().getId());
				}
				deleteCubics(); // TODO: Проверить, должно ли Благословение Дворянина влиять на кубики.
			}

			getAbnormalList().stop(effectsToRemove);
		}

		if(isPlayer())
			getPlayer().sendUserInfo(true); // Принудительно посылаем, исправляет баг, когда персонаж умирает в воздушных оковах.

		broadcastStatusUpdate();

		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null, null));

		if(killer != null)
			killer.useTriggers(this, TriggerType.ON_KILL, null, null, 0);

		getListeners().onDeath(killer);
	}

	protected void onRevive()
	{
		useTriggers(this, TriggerType.ON_REVIVE, null, null, 0);
	}

	public void enableSkill(Skill skill)
	{
		_skillReuses.remove(skill.getReuseHash());
	}

	/**
	 * Return a map of 32 bits (0x00000000) containing all abnormal effects
	 */
	public Set<AbnormalEffect> getAbnormalEffects()
	{
		return _abnormalEffects;
	}

	public AbnormalEffect[] getAbnormalEffectsArray()
	{
		return _abnormalEffects.toArray(new AbnormalEffect[_abnormalEffects.size()]);
	}

	public boolean isContainsAbnormalEffect(AbnormalEffect efc) {
		return this._abnormalEffects.contains(efc);
	}

	public int getPAccuracy()
	{
		return (int) Math.round(getStat().calc(Stats.P_ACCURACY_COMBAT, 0, null, null));
	}

	public int getMAccuracy()
	{
		return (int) getStat().calc(Stats.M_ACCURACY_COMBAT, 0, null, null);
	}

	/**
	 * Возвращает коллекцию скиллов для быстрого перебора
	 */
	public Collection<SkillEntry> getAllSkills()
	{
		return _skills.valueCollection();
	}

	/**
	 * Возвращает массив скиллов для безопасного перебора
	 */
	public final SkillEntry[] getAllSkillsArray()
	{
		return _skills.values(new SkillEntry[_skills.size()]);
	}

	public final double getAttackSpeedMultiplier()
	{
		return 1.1 * getPAtkSpd() / getBaseStats().getPAtkSpd();
	}

	public int getBuffLimit()
	{
		return (int) getStat().calc(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}

	/**
	 * Возвращает шанс физического крита (1000 == 100%)
	 */
	public int getPCriticalHit(Creature target)
	{
		return (int) Math.round(getStat().calc(Stats.BASE_P_CRITICAL_RATE, getBaseStats().getPCritRate(), target, null));
	}

	/**
	 * Возвращает шанс магического крита (1000 == 100%)
	 */
	public int getMCriticalHit(Creature target, Skill skill)
	{
		double Mcrit = getStat().calc(Stats.BASE_M_CRITICAL_RATE, getBaseStats().getMCritRate(), target, skill);
		double McritPcrit = getStat().calc(Stats.M_CRITICAL_RATE_FROM_P_CRITICAL_RATE, 0, null, null);
		McritPcrit = McritPcrit * getPCriticalHit(this) / 100;
		return (int) Math.round(Mcrit + McritPcrit);
	}

	/**
	 * Return the current CP of the L2Character.
	 *
	 */
	public double getCurrentCp()
	{
		return _currentCp;
	}

	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}

	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.;
	}

	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}

	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}

	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.;
	}

	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}

	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1;
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}

	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.;
	}

	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}

	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1;
	}

	public int getINT()
	{
		return (int) getStat().calc(Stats.STAT_INT, getBaseStats().getINT(), null, null);
	}

	public int getSTR()
	{
		return (int) getStat().calc(Stats.STAT_STR, getBaseStats().getSTR(), null, null);
	}

	public int getCON()
	{
		return (int) getStat().calc(Stats.STAT_CON, getBaseStats().getCON(), null, null);
	}

	public int getMEN()
	{
		return (int) getStat().calc(Stats.STAT_MEN, getBaseStats().getMEN(), null, null);
	}

	public int getDEX()
	{
		return (int) getStat().calc(Stats.STAT_DEX, getBaseStats().getDEX(), null, null);
	}

	public int getWIT()
	{
		return (int) getStat().calc(Stats.STAT_WIT, getBaseStats().getWIT(), null, null);
	}

	public int getPEvasionRate(Creature target)
	{
		return (int) Math.round(getStat().calc(Stats.P_EVASION_RATE, 0, target, null));
	}

	public int getMEvasionRate(Creature target)
	{
		return (int) getStat().calc(Stats.M_EVASION_RATE, 0, target, null);
	}

	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}

	public List<NpcInstance> getAroundNpc(int range, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundNpc(this, range, height);
	}

	public boolean knowsObject(GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}

	public final SkillEntry getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}

	public final int getMagicalAttackRange(Skill skill)
	{
		if(skill != null)
			return (int) getStat().calc(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getBaseStats().getAtkRange();
	}

	public int getMAtk(Creature target, Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		
		double mAtk = getStat().calc(Stats.MAGIC_ATTACK, getBaseStats().getMAtk(), target, skill);
		double mAtkByPAtk = getStat().calc(Stats.MAGIC_ATTACK_BY_PHYSIC, 0, null, null);
		mAtkByPAtk = mAtkByPAtk * getPAtk(this) / 100;
		return (int) Math.round(mAtk + mAtkByPAtk);
	}

	public int getMAtkSpd()
	{
		return (int) getStat().calc(Stats.MAGIC_ATTACK_SPEED, getBaseStats().getMAtkSpd(), null, null);
	}

	public int getMaxCp()
	{
		return Math.max(1, (int) getStat().calc(Stats.MAX_CP, getBaseStats().getCpMax(), null, null));
	}

	public int getMaxHp()
	{
		return Math.max(1, (int) getStat().calc(Stats.MAX_HP, getBaseStats().getHpMax(), null, null));
	}

	public int getMaxMp()
	{
		return Math.max(1, (int) getStat().calc(Stats.MAX_MP, getBaseStats().getMpMax(), null, null));
	}

	public int getMDef(Creature target, Skill skill)
	{
		double mDef = getStat().calc(Stats.MAGIC_DEFENCE, getBaseStats().getMDef(), target, skill);
		return (int) Math.max(mDef, getBaseStats().getMDef() / 2.);
	}

	public double getMinDistance(GameObject obj)
	{
		double distance = getCurrentCollisionRadius();

		if(obj != null && obj.isCreature())
			distance += ((Creature) obj).getCurrentCollisionRadius();

		return distance;
	}

	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}

	public String getVisibleName(Player receiver)
	{
		return getName();
	}

	public int getPAtk(Creature target)
	{
		return (int) getStat().calc(Stats.POWER_ATTACK, getBaseStats().getPAtk(), target, null);
	}

	public int getPAtkSpd()
	{
		return (int) getStat().calc(Stats.POWER_ATTACK_SPEED, getBaseStats().getPAtkSpd(), null, null);
	}

	public int getPDef(Creature target)
	{
		double pDef = getStat().calc(Stats.POWER_DEFENCE, getBaseStats().getPDef(), target, null);
		return (int) Math.max(pDef, getBaseStats().getPDef() / 2.);
	}

	public int getPhysicalAttackRange()
	{
		return (int) getStat().calc(Stats.POWER_ATTACK_RANGE, getBaseStats().getAtkRange());
	}

	public int getPhysicalAttackRadius()
	{
		return (int) getStat().calc(Stats.P_ATTACK_RADIUS, getBaseStats().getAttackRadius());
	}

	public int getPhysicalAttackAngle()
	{
		return getBaseStats().getAttackAngle();
	}

	public int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponTemplate();
		if(weaponItem == null)
			return getBaseStats().getRandDam();
		return weaponItem.getRandomDamage();
	}

	public double getReuseModifier(Creature target)
	{
		return getStat().calc(Stats.ATK_REUSE, 1, target, null);
	}

	public final int getShldDef()
	{
		return (int) getStat().calc(Stats.SHIELD_DEFENCE, getBaseStats().getShldDef(), null, null);
	}

	public double getPhysicalAbnormalResist()
	{
		return getStat().calc(Stats.PHYSICAL_ABNORMAL_RESIST, getBaseStats().getPhysicalAbnormalResist());
	}

	public double getMagicAbnormalResist()
	{
		return getStat().calc(Stats.MAGIC_ABNORMAL_RESIST, getBaseStats().getMagicAbnormalResist());
	}

	public int getSkillLevel(int skillId)
	{
		return getSkillLevel(skillId, -1);
	}

	public final int getSkillLevel(int skillId, int def)
	{
		SkillEntry skill = _skills.get(skillId);
		if(skill == null)
			return def;
		return skill.getLevel();
	}

	public GameObject getTarget()
	{
		return _target.get();
	}

	public final int getTargetId()
	{
		GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}

	public CreatureTemplate getTemplate()
	{
		return _template;
	}

	protected void setTemplate(CreatureTemplate template)
	{
		_template = template;
	}

	public String getTitle()
	{
		return StringUtils.defaultString(_title);
	}

	public String getVisibleTitle(Player receiver)
	{
		return getTitle();
	}

	public double headingToRadians(int heading)
	{
		return (heading - 32768) / HEADINGS_IN_PI;
	}

	public final boolean isAlikeDead()
	{
		return _fakeDeath || isDead();
	}

	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}

	public final long getLastAttackTime()
	{
		return _lastAttackTime;
	}

	public final void setLastAttackTime(long value)
	{
		_lastAttackTime = value;
	}

	public final boolean isPreserveAbnormal()
	{
		return _isPreserveAbnormal;
	}

	public final boolean isSalvation()
	{
		return _isSalvation;
	}

	public boolean isEffectImmune(Creature effector)
	{
		Creature exception = _effectImmunityException.get();
		if(exception != null && exception == effector)
			return false;

		return getFlags().getEffectImmunity().get();
	}

	public boolean isBuffImmune()
	{
		return getFlags().getBuffImmunity().get();
	}

	public boolean isDebuffImmune()
	{
		return getFlags().getDebuffImmunity().get() || isPeaceNpc();
	}

	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}

	@Override
	public boolean isFlying()
	{
		return _flying;
	}

	/**
	 * Находится ли персонаж в боевой позе
	 * @return true, если персонаж в боевой позе, атакован или атакует
	 */
	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}

	public boolean isMageClass()
	{
		return getBaseStats().getMAtk() > 3;
	}

	public final boolean isRunning()
	{
		return _running;
	}

	public boolean isSkillDisabled(Skill skill)
	{
		TimeStamp sts = _skillReuses.get(skill.getReuseHash());
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_skillReuses.remove(skill.getReuseHash());
		return false;
	}

	public final boolean isTeleporting()
	{
		return isTeleporting.get();
	}

	public void broadcastMove()
	{
		broadcastPacket(movePacket());
	}

	public void broadcastStopMove()
	{
		broadcastPacket(stopMovePacket());
	}

	/** Возвращает координаты поверхности воды, если мы находимся в ней, или над ней. */
	public int[] getWaterZ()
	{
		int[] waterZ = new int[]{ Integer.MIN_VALUE, Integer.MAX_VALUE };
		if(!isInWater())
			return waterZ;

		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == ZoneType.water)
				{
					if(waterZ[0] == Integer.MIN_VALUE || waterZ[0] > zone.getTerritory().getZmin())
						waterZ[0] = zone.getTerritory().getZmin();
					if(waterZ[1] == Integer.MAX_VALUE || waterZ[1] < zone.getTerritory().getZmax())
						waterZ[1] = zone.getTerritory().getZmax();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return waterZ;
	}

	protected L2GameServerPacket stopMovePacket()
	{
		return new StopMovePacket(this);
	}

	public L2GameServerPacket movePacket()
	{
		if(getMovement().isFollow() && !getMovement().isPathfindMoving())
		{
			Creature target = getMovement().getFollowTarget();
			if(target != null)
				return new MoveToPawnPacket(this, target, getMovement().getMoveOffset());
		}
		return new MTLPacket(this);
	}

	public void updateZones()
	{
		if(isTeleporting())
			return;

		Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;

		LazyArrayList<Zone> entering = null;
		LazyArrayList<Zone> leaving = null;

		Zone zone;

		zonesWrite.lock();
		try
		{
			if(!_zones.isEmpty())
			{
				leaving = LazyArrayList.newInstance();
				for(int i = 0; i < _zones.size(); i++)
				{
					zone = _zones.get(i);
					// зоны больше нет в регионе, либо вышли за территорию зоны
					if(!ArrayUtils.contains(zones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						leaving.add(zone);
				}

				//Покинули зоны, убираем из списка зон персонажа
				if(!leaving.isEmpty())
				{
					for(int i = 0; i < leaving.size(); i++)
					{
						zone = leaving.get(i);
						_zones.remove(zone);
					}
				}
			}

			if(zones.length > 0)
			{
				entering = LazyArrayList.newInstance();
				for(int i = 0; i < zones.length; i++)
				{
					zone = zones[i];
					// в зону еще не заходили и зашли на территорию зоны
					if(!_zones.contains(zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						entering.add(zone);
				}

				//Вошли в зоны, добавим в список зон персонажа
				if(!entering.isEmpty())
				{
					for(int i = 0; i < entering.size(); i++)
					{
						zone = entering.get(i);
						_zones.add(zone);
					}
				}
			}
		}
		finally
		{
			zonesWrite.unlock();
		}

		onUpdateZones(leaving, entering);

		if(leaving != null)
			LazyArrayList.recycle(leaving);

		if(entering != null)
			LazyArrayList.recycle(entering);
	}

	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		Zone zone;

		if(leaving != null && !leaving.isEmpty())
		{
			for(int i = 0; i < leaving.size(); i++)
			{
				zone = leaving.get(i);
				zone.doLeave(this);
			}
		}

		if(entering != null && !entering.isEmpty())
		{
			for(int i = 0; i < entering.size(); i++)
			{
				zone = entering.get(i);
				zone.doEnter(this);
			}
		}
	}

	public boolean isInPeaceZone()
	{
		return isInZone(ZoneType.peace_zone) && !isInZoneBattle();
	}

	public boolean isHaveZoneParam(String param)
	{
		zonesRead.lock();
		try
		{
			for (Zone zone : _zones) {
				if (zone.getTemplate().getParams().getBool(param, false))
					return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			zonesRead.unlock();
		}
		return false;
	}

	public boolean isInZoneBattle()
	{
		for(Event event : getEvents())
		{
			Boolean result = event.isInZoneBattle(this);
			if(result != null)
				return result;
		}
		return isInZone(ZoneType.battle_zone);
	}

	@Override
	public boolean isInWater()
	{
		return isInZone(ZoneType.water) && !(isInBoat() || isBoat() || isFlying());
	}

	public boolean isInSiegeZone()
	{
		return isInZone(ZoneType.SIEGE);
	}

	public boolean isInSSQZone()
	{
		return isInZone(ZoneType.ssq_zone);
	}

	public boolean isInDangerArea()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getTemplate().isShowDangerzone())
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return false;
	}

	public boolean isInZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == type)
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return false;
	}

	public List<Event> getZoneEvents()
	{
		List<Event> e = Collections.emptyList();
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(!zone.getEvents().isEmpty())
				{
					if(e.isEmpty())
						e = new ArrayList<Event>(2);

					e.addAll(zone.getEvents());
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return e;
	}

	public boolean isInZone(String name)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getName().equals(name))
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return false;
	}

	public boolean isInZone(Zone zone)
	{
		zonesRead.lock();
		try
		{
			return _zones.contains(zone);
		}
		finally
		{
			zonesRead.unlock();
		}
	}

	public Zone getZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == type)
					return zone;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return null;
	}

	public List<Zone> getZones()
	{
		return _zones;
	}

	public Location getRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return null;
	}

	public Location getPKRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getPKSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return null;
	}

	@Override
	public int getGeoZ(int x, int y, int z)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return z;

		return super.getGeoZ(x, y, z);
	}

	protected boolean needStatusUpdate()
	{
		if(!isVisible())
			return false;

		boolean result = false;

		int bar;
		bar = (int) (getCurrentHp() * CLIENT_BAR_SIZE / getMaxHp());
		if(bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			result = true;
		}

		bar = (int) (getCurrentMp() * CLIENT_BAR_SIZE / getMaxMp());
		if(bar == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			result = true;
		}

		if(isPlayer())
		{
			bar = (int) (getCurrentCp() * CLIENT_BAR_SIZE / getMaxCp());
			if(bar == 0 || bar != _lastCpBarUpdate)
			{
				_lastCpBarUpdate = bar;
				result = true;
			}
		}

		return result;
	}

	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, int elementalDamage, boolean elementalCrit)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}

		if(target.isDead() || !isInRange(target, 2000))
		{
			sendActionFailed();
			return;
		}

		if(isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle())
		{
			Player player = getPlayer();
			if(player != null)
			{
				player.sendPacket(SystemMsg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}

		target.getListeners().onAttackHit(this);

		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_ATTACK, target, null, damage));
		ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, null, damage));

		boolean checkPvP = checkPvP(target, null);

		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true, true, crit, miss, shld, elementalDamage, elementalCrit);

		if(!miss && damage > 0)
		{
			// Скиллы, кастуемые при физ атаке
			if(!target.isDead())
			{
				if(crit)
					useTriggers(target, TriggerType.CRIT, null, null, damage);

				useTriggers(target, TriggerType.ATTACK, null, null, damage);

				if(Formulas.calcStunBreak(crit, false, false))
				{
					target.getAbnormalList().stop(AbnormalType.STUN);
					//target.getAbnormalList().stop(AbnormalType.TURN_FLEE);
				}

				for(Abnormal abnormal : target.getAbnormalList())
				{
					double chance = crit ? abnormal.getSkill().getOnCritCancelChance() : abnormal.getSkill().getOnAttackCancelChance();
					if(chance > 0 && Rnd.chance(chance))
						abnormal.exit();
				}

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if(Formulas.calcCastBreak(target, crit))
					target.abortCast(false, true);
			}

			if(soulshot && unchargeSS)
				unChargeShots(false);
		}

		if(miss)
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);

		startAttackStanceTask();

		if(checkPvP)
			startPvPFlag(target);
	}

	public void onCastEndTime(SkillEntry skillEntry, Creature aimingTarget, Set<Creature> targets, boolean success)
	{
		if(skillEntry == null)
			return;

		Skill skill = skillEntry.getTemplate();

		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, skill, aimingTarget, success);

		if(success)
		{
			skill.onFinishCast(aimingTarget, this, targets);
			useTriggers(aimingTarget, TriggerType.ON_FINISH_CAST, null, skill, 0);
			if(isPlayer())
			{
				for(ListenerHook hook : getPlayer().getListenerHooks(ListenerHookType.PLAYER_FINISH_CAST_SKILL))
					hook.onPlayerFinishCastSkill(getPlayer(), skill.getId());

				for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_FINISH_CAST_SKILL))
					hook.onPlayerFinishCastSkill(getPlayer(), skill.getId());
			}
		}
	}

	public final void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage)
	{
		reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, false, false, false, false, 0, false);
	}

	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss, boolean shld, double elementalDamage, boolean elementalCrit)
	{
		if(isImmortal())
			return;

		if(canReflectAndAbsorb) // TODO: Сверить с оффом и переделать.
			damage = Math.max(0, damage - getStat().calc(Stats.DAMAGE_BLOCK_COUNT));

		boolean damaged = true;
		if(miss || damage <= 0)
			damaged = false;

		boolean damageBlocked = isDamageBlocked(attacker);
		if(attacker == null || isDead() || (attacker.isDead() && !isDot) || damageBlocked)
			damaged = false;

		if(!damaged)
		{
			if(attacker != this && sendGiveMessage)
				attacker.displayGiveDamageMessage(this, skill, 0, null, 0, crit, miss, shld, damageBlocked, 0, false);
			return;
		}

		double reflectedDamage = 0.;
		double transferedDamage = 0.;
		Servitor servitorForTransfereDamage = null;

		if(canReflectAndAbsorb)
		{
			boolean canAbsorb = canAbsorb(this, attacker);
			if(canAbsorb)
				damage = absorbToEffector(attacker, damage);	// e.g. Noble Sacrifice.

			// TODO: Проверить на оффе, что должно быть первее, поглощение саммоном или МП?
			damage = reduceDamageByMp(attacker, damage);			// e.g. Arcane Barrier.

			// e.g. Transfer Pain
			transferedDamage = getDamageForTransferToServitor(damage);
			servitorForTransfereDamage = getServitorForTransfereDamage(transferedDamage);
			if(servitorForTransfereDamage != null)
				damage -= transferedDamage;
			else
				transferedDamage = 0.;

			reflectedDamage = reflectDamage(attacker, skill, damage);

			if(canAbsorb)
				attacker.absorbDamage(this, skill, damage);
		}

		// Damage can be limited by ultimate effects
		double damageLimit = -1;
		if(skill == null)
			damageLimit = getStat().calc(Stats.RECIEVE_DAMAGE_LIMIT, damage);
		else if(skill.isMagic())
			damageLimit = getStat().calc(Stats.RECIEVE_DAMAGE_LIMIT_M_SKILL, damage);
		else
			damageLimit = getStat().calc(Stats.RECIEVE_DAMAGE_LIMIT_P_SKILL, damage);

		if(damageLimit >= 0. && damage > damageLimit)
			damage = damageLimit;

		getListeners().onCurrentHpDamage(damage, attacker, skill);

		if(attacker != this)
		{
			if(sendGiveMessage)
				attacker.displayGiveDamageMessage(this, skill, (int) damage, servitorForTransfereDamage, (int) transferedDamage, crit, miss, shld, damageBlocked, (int) elementalDamage, elementalCrit);

			if(sendReceiveMessage)
				displayReceiveDamageMessage(attacker, (int) damage, servitorForTransfereDamage, (int) transferedDamage, (int) elementalDamage);

			if(!isDot)
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
		}

		if(servitorForTransfereDamage != null && transferedDamage > 0)
			servitorForTransfereDamage.reduceCurrentHp(transferedDamage, attacker, null, false, false, false, false, true, false, true);

		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);

		if(reflectedDamage > 0.)
		{
			displayGiveDamageMessage(attacker, skill, (int) reflectedDamage, null, 0, false, false, false, false, 0, false);
			attacker.reduceCurrentHp(reflectedDamage, this, null, true, true, false, false, false, false, true);
		}
		
		saveDamage(attacker, skill, (int) damage, 1);
	}

	protected void onReduceCurrentHp(final double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		if(awake && isSleeping())
			getAbnormalList().stop(AbnormalType.SLEEP);

		if(attacker != this || (skill != null && skill.isDebuff()))
		{
			final TIntSet effectsToRemove = new TIntHashSet();
			for(Abnormal effect : getAbnormalList())
			{
				if(effect.getSkill().isDispelOnDamage())
					effectsToRemove.add(effect.getSkill().getId());
			}
			getAbnormalList().stop(effectsToRemove);

			if(isMeditated())
				getAbnormalList().stop("Meditation");

			startAttackStanceTask();
			checkAndRemoveInvisible();
		}

		if(damage <= 0)
			return;

		if(getCurrentHp() - damage < 10 && getStat().calc(Stats.ShillienProtection) == 1)
		{
			setCurrentHp(getMaxHp(), false, !isDot);
			setCurrentCp(getMaxCp(), !isDot);
			if(isDot)
			{
				StatusUpdate su = new StatusUpdate(this, attacker, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.CUR_CP);
				attacker.sendPacket(su);
				sendPacket(su);
				broadcastStatusUpdate();
				sendChanges();
			}
			return;
		}

		boolean isUndying = isUndying();

		setCurrentHp(Math.max(getCurrentHp() - damage, isDot ? 1.5 : (isUndying ? 0.5 : 0)), false, !isDot);
		if(isDot)
		{
			StatusUpdate su = new StatusUpdate(this, attacker, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP);
			attacker.sendPacket(su);
			sendPacket(su);
			broadcastStatusUpdate();
			sendChanges();
		}

		if(isUndying)
		{
			if(getCurrentHp() == 0.5 && (!isPlayer() || !getPlayer().isGMUndying()))
				if(getFlags().getUndying().getFlag().compareAndSet(false, true))
					getListeners().onDeathFromUndying(attacker);
		}
		else if(getCurrentHp() < 0.5)
		{
			if(attacker != this || (skill != null && skill.isDebuff()))
				useTriggers(attacker, TriggerType.DIE, null, null, damage);

			doDie(attacker);
		}
	}

	public void reduceCurrentMp(double i, Creature attacker)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
				getAbnormalList().stop(AbnormalType.SLEEP);

			if(isMeditated())
				getAbnormalList().stop("Meditation");
		}

		if(isDamageBlocked(attacker) && attacker != null && attacker != this)
		{
			attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.isPK() && getAbnormalList().contains(5182) && !isInSiegeZone())
				return;
			// чар с блессингом не может нанести урон ПК
			if(isPK() && attacker.getAbnormalList().contains(5182) && !attacker.isInSiegeZone())
				return;
		}

		i = _currentMp - i;

		if(i < 0)
			i = 0;

		setCurrentMp(i);

		if(attacker != null && attacker != this)
			startAttackStanceTask();
	}

	public void removeAllSkills()
	{
		for(SkillEntry s : getAllSkillsArray())
			removeSkill(s);
	}

	public SkillEntry removeSkill(SkillInfo skillInfo)
	{
		if(skillInfo == null)
			return null;
		return removeSkillById(skillInfo.getId());
	}

	public SkillEntry removeSkillById(int id)
	{
		// Remove the skill from the L2Character _skills
		SkillEntry oldSkillEntry = _skills.remove(id);

		// Remove all its Func objects from the L2Character calculator set
		if(oldSkillEntry != null)
		{
			Skill oldSkill = oldSkillEntry.getTemplate();

			if(oldSkill.isToggle())
				getAbnormalList().stop(oldSkill, false);

			removeTriggers(oldSkill);

			if(oldSkill.isPassive())
			{
				getStat().removeFuncsByOwner(oldSkill);

				for(EffectTemplate et : oldSkill.getEffectTemplates(EffectUseType.NORMAL))
					getStat().removeFuncsByOwner(et.getHandler());
			}

			if(Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler()))
			{
				// Завершаем все эффекты, принадлежащие старому скиллу
				getAbnormalList().stop(oldSkill, false);

				// И с петов тоже
				for(Servitor servitor : getServitors())
					servitor.getAbnormalList().stop(oldSkill, false);
			}

			AINextAction nextAction = getAI().getNextAction();
			if(nextAction != null && nextAction == AINextAction.CAST)
			{
				Object args1 = getAI().getNextActionArgs()[0];
				if(oldSkillEntry.equals(args1))
					getAI().clearNextAction();
			}

			onRemoveSkill(oldSkillEntry);
		}

		return oldSkillEntry;
	}

	public void addTriggers(StatTemplate f)
	{
		if(f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}

	public void addTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			_triggers = new ConcurrentHashMap<TriggerType, Set<TriggerInfo>>();

		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
		{
			hs = new CopyOnWriteArraySet<TriggerInfo>();
			_triggers.put(t.getType(), hs);
		}

		hs.add(t);

		if(t.getType() == TriggerType.ADD)
			useTriggerSkill(this, null, t, null, 0);
		else if(t.getType() == TriggerType.IDLE)
			new RunnableTrigger(this, t).schedule();
	}

	public Map<TriggerType, Set<TriggerInfo>> getTriggers()
	{
		return _triggers;
	}

	public void removeTriggers(StatTemplate f)
	{
		if(_triggers == null || f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
			removeTrigger(t);
	}

	public void removeTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			return;
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
			return;
		hs.remove(t);

		if(t.cancelEffectsOnRemove())
			triggerCancelEffects(t);
	}

	public void sendActionFailed()
	{
		sendPacket(ActionFailPacket.STATIC);
	}

	public boolean hasAI()
	{
		return _ai != null;
	}

	public CharacterAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = new CharacterAI(this);
			}

		return _ai;
	}

	public void setAI(CharacterAI newAI)
	{
		if(newAI == null)
			return;

		CharacterAI oldAI = _ai;

		synchronized (this)
		{
			_ai = newAI;
		}

		if(oldAI != null)
		{
			if(oldAI.isActive())
			{
				oldAI.stopAITask();
				newAI.startAITask();
				newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
		}
	}

	public final void setCurrentHp(double newHp, boolean canResurrect, boolean sendInfo)
	{
		int maxHp = getMaxHp();

		newHp = Math.min(maxHp, Math.max(0, newHp));

		if(isDeathImmune())
			newHp = Math.max(1.1, newHp); // Ставим 1.1, потому что на олимпиаде 1 == Поражение, что вызовет зависание.

		if(_currentHp == newHp)
			return;

		if(newHp >= 0.5 && isDead() && !canResurrect)
			return;

		double hpStart = _currentHp;

		_currentHp = newHp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, _currentHp);

		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentHp < maxHp)
			startRegeneration();

		onChangeCurrentHp(hpStart, newHp);

		getListeners().onChangeCurrentHp(hpStart, newHp);
	}

	public final void setCurrentHp(double newHp, boolean canResurrect)
	{
		setCurrentHp(newHp, canResurrect, true);
	}

	public void onChangeCurrentHp(double oldHp, double newHp)
	{
		//
	}

	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();

		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(_currentMp == newMp)
			return;

		if(newMp >= 0.5 && isDead())
			return;

		double mpStart = _currentMp;

		_currentMp = newMp;

		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentMp < maxMp)
			startRegeneration();

		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}

	public final void setCurrentCp(double newCp, boolean sendInfo)
	{
		if(!isPlayer())
			return;

		int maxCp = getMaxCp();
		newCp = Math.min(maxCp, Math.max(0, newCp));

		if(_currentCp == newCp)
			return;

		if(newCp >= 0.5 && isDead())
			return;

		double cpStart = _currentCp;

		_currentCp = newCp;

		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentCp < maxCp)
			startRegeneration();

		getListeners().onChangeCurrentCp(cpStart, newCp);
	}

	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}

	public void setCurrentHpMp(double newHp, double newMp, boolean canResurrect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();

		newHp = Math.min(maxHp, Math.max(0, newHp));
		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(isDeathImmune())
			newHp = Math.max(1.1, newHp); // Ставим 1.1, потому что на олимпиаде 1 == Поражение, что вызовет зависание.

		if(_currentHp == newHp && _currentMp == newMp)
			return;

		if(newHp >= 0.5 && isDead() && !canResurrect)
			return;

		double hpStart = _currentHp;
		double mpStart = _currentMp;

		_currentHp = newHp;
		_currentMp = newMp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, _currentHp);

		broadcastStatusUpdate();
		sendChanges();

		if(_currentHp < maxHp || _currentMp < maxMp)
			startRegeneration();

		getListeners().onChangeCurrentHp(hpStart, newHp);
		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHpMp(newHp, newMp, false);
	}

	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}

	@Override
	public final int getHeading()
	{
		return _heading;
	}

	public final void setHeading(int heading)
	{
		setHeading(heading, false);
	}

	public final void setHeading(int heading, boolean broadcast)
	{
		_heading = heading;
		if(broadcast)
			broadcastPacket(new ExRotation(getObjectId(), heading));
	}

	public final void setIsTeleporting(boolean value)
	{
		isTeleporting.compareAndSet(!value, value);
	}

	public final void setName(String name)
	{
		_name = name;
	}

	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;
			broadcastPacket(changeMovePacket());
		}
	}

	public void setAggressionTarget(Creature target)
	{
		if(target == null)
			_aggressionTarget = HardReferences.emptyRef();
		else
			_aggressionTarget = target.getRef();
	}

	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}

	public void setTarget(GameObject object)
	{
		if(object != null && !object.isVisible())
			object = null;

		/* DS: на оффе сброс текущей цели не отменяет атаку или каст.
		if(object == null)
		{
			if(isAttackingNow() && getAI().getAttackTarget() == getTarget())
				abortAttack(false, true);
			if(isCastingNow() && getAI().getCastTarget() == getTarget())
				abortCast(false, true);
		}
		*/

		if(object == null)
			_target = HardReferences.emptyRef();
		else
			_target = object.getRef();
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public void setWalking()
	{
		if(_running)
		{
			_running = false;
			broadcastPacket(changeMovePacket());
		}
	}

	protected L2GameServerPacket changeMovePacket()
	{
		return new ChangeMoveTypePacket(this);
	}

	public final void startAbnormalEffect(AbnormalEffect ae)
	{
		if(ae == AbnormalEffect.NONE)
			return;

		_abnormalEffects.add(ae);
		sendChanges();
	}

	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}

	/**
	 * Запускаем задачу анимации боевой позы. Если задача уже запущена, увеличиваем время, которое персонаж будет в боевой позе на 15с
	 */
	protected void startAttackStanceTask0()
	{
		// предыдущая задача еще не закончена, увеличиваем время
		if(isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000L;
			return;
		}

		_stanceEndTime = System.currentTimeMillis() + 15000L;

		broadcastPacket(new AutoAttackStartPacket(getObjectId()));

		// отменяем предыдущую
		final Future<?> task = _stanceTask;
		if(task != null)
			task.cancel(false);

		// Добавляем задачу, которая будет проверять, если истекло время нахождения персонажа в боевой позе,
		// отменяет задачу и останаливает анимацию.
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? _stanceTaskRunnable = new AttackStanceTask() : _stanceTaskRunnable, 1000L, 1000L);
	}

	/**
	 * Останавливаем задачу анимации боевой позы.
	 */
	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;

		final Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
			_stanceTask = null;

			broadcastPacket(new AutoAttackStopPacket(getObjectId()));
		}
	}

	private class AttackStanceTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInCombat())
				stopAttackStanceTask();
		}
	}

	/**
	 * Остановить регенерацию
	 */
	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if(_isRegenerating)
			{
				_isRegenerating = false;

				if(_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	/**
	 * Запустить регенерацию
	 */
	protected void startRegeneration()
	{
		if(!isVisible() || isDead() || getRegenTick() == 0L)
			return;

		if(_isRegenerating)
			return;

		regenLock.lock();
		try
		{
			if(!_isRegenerating)
			{
				_isRegenerating = true;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? _regenTaskRunnable = new RegenTask() : _regenTaskRunnable, getRegenTick(), getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	public long getRegenTick()
	{
		return 3000L;
	}

	private class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			if(isAlikeDead() || getRegenTick() == 0L)
				return;

			double hpStart = _currentHp;
			double mpStart = _currentMp;
			double cpStart = _currentCp;

			int maxHp = getMaxHp();
			int maxMp = getMaxMp();
			int maxCp = isPlayer() ? getMaxCp() : 0;

			double addHp = 0.;
			double addMp = 0.;
			double addCp = 0.;

			regenLock.lock();
			try
			{
				if(_currentHp < maxHp)
					addHp += getHpRegen();

				if(_currentMp < maxMp)
					addMp += getMpRegen();

				if(_currentCp < maxCp)
					addCp += getCpRegen();

				if(isSitting())
				{
					// Added regen bonus when character is sitting
					if(isPlayer() && Config.REGEN_SIT_WAIT)
					{
						Player pl = getPlayer();
						pl.updateWaitSitTime();
						if(pl.getWaitSitTime() > 5)
						{
							addHp += pl.getWaitSitTime();
							addMp += pl.getWaitSitTime();
							addCp += pl.getWaitSitTime();
						}
					}
					else
					{
						// TODO: Вынести значения в датапак?
						addHp += getHpRegen() * 0.5;
						addMp += getMpRegen() * 0.5;
						addCp += getCpRegen() * 0.5;
					}
				}
				else if(!getMovement().isMoving())
				{
					// TODO: Вынести значения в датапак?
					addHp += getHpRegen() * 0.1;
					addMp += getMpRegen() * 0.1;
					addCp += getCpRegen() * 0.1;
				}
				else if(isRunning())
				{
					// TODO: Вынести значения в датапак?
					addHp -= getHpRegen() * 0.3;
					addMp -= getMpRegen() * 0.3;
					addCp -= getCpRegen() * 0.3;
				}

				if(isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}

				_currentHp += Math.max(0, Math.min(addHp, getStat().calc(Stats.HP_LIMIT, null, null) * maxHp / 100. - _currentHp));
				_currentHp = Math.min(maxHp, _currentHp);

				_currentMp += Math.max(0, Math.min(addMp, getStat().calc(Stats.MP_LIMIT, null, null) * maxMp / 100. - _currentMp));
				_currentMp = Math.min(maxMp, _currentMp);

				if(isPlayer())
				{
					_currentCp += Math.max(0, Math.min(addCp, getStat().calc(Stats.CP_LIMIT, null, null) * maxCp / 100. - _currentCp));
					_currentCp = Math.min(maxCp, _currentCp);
				}

				//отрегенились, останавливаем задачу
				if(_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp)
					stopRegeneration();
			}
			finally
			{
				regenLock.unlock();
			}

			getListeners().onChangeCurrentHp(hpStart, _currentHp);
			getListeners().onChangeCurrentMp(mpStart, _currentMp);

			if(isPlayer())
				getListeners().onChangeCurrentCp(cpStart, _currentCp);

			TIntSet updateAttributes = new TIntHashSet(3);
			if(addHp > 0 && _currentHp != hpStart)
				updateAttributes.add(StatusUpdatePacket.CUR_HP);
			if(addMp > 0 && _currentMp != mpStart)
				updateAttributes.add(StatusUpdatePacket.CUR_MP);
			if(addCp > 0 && _currentCp != cpStart)
				updateAttributes.add(StatusUpdatePacket.CUR_CP);
			if(!updateAttributes.isEmpty())
			{
				sendPacket(new StatusUpdate(Creature.this, StatusUpdatePacket.UpdateType.REGEN, updateAttributes.toArray()));
				broadcastStatusUpdate();
				sendChanges();
			}

			checkHpMessages(hpStart, _currentHp);
		}
	}

	public final void stopAbnormalEffect(AbnormalEffect ae)
	{
		_abnormalEffects.remove(ae);
		sendChanges();
	}

	public final void stopAllAbnormalEffects()
	{
		_abnormalEffects.clear();
		sendChanges();
	}

	/**
	 * Блокируем персонажа
	 */
	public void block()
	{
		_blocked = true;
	}

	/**
	 * Разблокируем персонажа
	 */
	public void unblock()
	{
		_blocked = false;
	}

	public void setDamageBlockedException(Creature exception)
	{
		if(exception == null)
			_damageBlockedException = HardReferences.emptyRef();
		else
			_damageBlockedException = exception.getRef();
	}

	public void setEffectImmunityException(Creature exception)
	{
		if(exception == null)
			_effectImmunityException = HardReferences.emptyRef();
		else
			_effectImmunityException = exception.getRef();
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		if(observer != null && getObjectId() == observer.getObjectId())
			return false;

		for(Event event : getEvents())
		{
			Boolean result = event.isInvisible(this, observer);
			if(result != null)
				return result;
		}
		return getFlags().getInvisible().get();
	}

	public boolean startInvisible(Object owner, boolean withServitors)
	{
		boolean result;
		if(owner == null)
			result = getFlags().getInvisible().start();
		else
			result = getFlags().getInvisible().start(owner);

		if(result)
		{
			for(Player p : World.getAroundObservers(this))
			{
				if(isInvisible(p))
					p.sendPacket(p.removeVisibleObject(this, null));
			}

			if(withServitors)
			{
				for(Servitor servitor : getServitors())
					servitor.startInvisible(owner, false);
			}
		}
		return result;
	}

	public final boolean startInvisible(boolean withServitors)
	{
		return startInvisible(null, withServitors);
	}

	public boolean stopInvisible(Object owner, boolean withServitors)
	{
		boolean result;
		if(owner == null)
			result = getFlags().getInvisible().stop();
		else
			result = getFlags().getInvisible().stop(owner);

		if(result)
		{
			List<Player> players = World.getAroundObservers(this);
			for(Player p : players)
			{
				if(isVisible() && !isInvisible(p))
					p.sendPacket(p.addVisibleObject(this, null));
			}

			if(withServitors)
			{
				for(Servitor servitor : getServitors())
					servitor.stopInvisible(owner, false);
			}
		}
		return result;
	}

	public final boolean stopInvisible(boolean withServitors)
	{
		return stopInvisible(null, withServitors);
	}

	public void addIgnoreSkillsEffect(EffectHandler effect, TIntSet skills)
	{
		_ignoreSkillsEffects.put(effect, skills);
	}

	public boolean removeIgnoreSkillsEffect(EffectHandler effect)
	{
		return _ignoreSkillsEffects.remove(effect) != null;
	}

	public boolean isIgnoredSkill(Skill skill)
	{
		for(TIntSet set : _ignoreSkillsEffects.values())
		{
			if(set.contains(skill.getId()))
				return true;
		}
		return false;
	}

	public boolean isUndying()
	{
		return getFlags().getUndying().get();
	}

	public boolean isInvulnerable()
	{
		return getFlags().getInvulnerable().get();
	}

	public void setFakeDeath(boolean value)
	{
		_fakeDeath = value;
	}

	public void breakFakeDeath()
	{
		getAbnormalList().stop("FakeDeath");
	}

	public void setMeditated(boolean value)
	{
		_meditated = value;
	}

	public final void setPreserveAbnormal(boolean value)
	{
		_isPreserveAbnormal = value;
	}

	public final void setIsSalvation(boolean value)
	{
		_isSalvation = value;
	}

	public void setLockedTarget(boolean value)
	{
		_lockedTarget = value;
	}

	public boolean isConfused()
	{
		return getFlags().getConfused().get();
	}

	public boolean isFakeDeath()
	{
		return _fakeDeath;
	}

	public boolean isAfraid()
	{
		return getFlags().getAfraid().get();
	}

	public boolean isBlocked()
	{
		return _blocked;
	}

	public boolean isMuted(Skill skill)
	{
		if(skill == null || skill.isNotAffectedByMute())
			return false;
		return isMMuted() && skill.isMagic() || isPMuted() && !skill.isMagic();
	}

	public boolean isPMuted()
	{
		return getFlags().getPMuted().get();
	}

	public boolean isMMuted()
	{
		return getFlags().getMuted().get();
	}

	public boolean isAMuted()
	{
		return getFlags().getAMuted().get() || isTransformed() && !getTransform().getType().isCanAttack();
	}

	public boolean isSleeping()
	{
		return getFlags().getSleeping().get();
	}

	public boolean isStunned()
	{
		return getFlags().getStunned().get();
	}

	public boolean isMeditated()
	{
		return _meditated;
	}

	public boolean isWeaponEquipBlocked()
	{
		return getFlags().getWeaponEquipBlocked().get();
	}

	public boolean isParalyzed()
	{
		return getFlags().getParalyzed().get();
	}

	public boolean isFrozen()
	{
		return getFlags().getFrozen().get();
	}

	public boolean isImmobilized()
	{
		return getFlags().getImmobilized().get() || getRunSpeed() < 1;
	}

	public boolean isHealBlocked()
	{
		//if(isInvulnerable())	// TODO: Check this.
		//	return true;
		return isAlikeDead() || getFlags().getHealBlocked().get();
	}

	public boolean isDamageBlocked(Creature attacker)
	{
		if(attacker == this)
			return false;

		if(isInvulnerable())
			return true;
		
		if (getFlags().getHitCountBlocked().get())
		{
			if(attacker == null)
				return false;
			
			if (getHitBlocks() > 0)
			{
				setHitBlocks(getHitBlocks() - 1);
				if (getHitBlocks() == 0)
				{
					getAbnormalList().stop(AbnormalType.INVINCIBILITY);
				}
				return true;
			}
			else
			{
				return false;
			}
		}

		Creature exception = _damageBlockedException.get();
		if(exception != null && exception == attacker)
			return false;

		if(getFlags().getDamageBlocked().get())
		{
			double blockRadius = getStat().calc(Stats.DAMAGE_BLOCK_RADIUS);
			if(blockRadius == -1)
				return true;

			if(attacker == null)
				return false;

			if(attacker.getDistance(this) <= blockRadius)
				return true;
		}

		return false;
	}

	public boolean isDistortedSpace()
	{
		return getFlags().getDistortedSpace().get();
	}

	public boolean isCastingNow()
	{
		return getSkillCast(SkillCastingType.NORMAL).isCastingNow() || getSkillCast(SkillCastingType.NORMAL_SECOND).isCastingNow();
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}

	public boolean isMovementDisabled()
	{
		return isBlocked() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isDecontrolled() || isAttackingNow() || isCastingNow() || isFrozen();
	}

	public final boolean isActionsDisabled()
	{
		return isActionsDisabled(true);
	}

	public boolean isActionsDisabled(boolean withCast)
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isDecontrolled() || isAttackingNow() || withCast && isCastingNow() || isFrozen();
	}

	public boolean isUseItemDisabled()
	{
		return isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isFrozen();
	}

	public final boolean isDecontrolled()
	{
		return isParalyzed() || isKnockDowned() || isKnockBacked() || isFlyUp();
	}

	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}

	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid();
	}

	public void checkAndRemoveInvisible()
	{
		getAbnormalList().stop(AbnormalType.HIDE);
	}

	public void teleToLocation(ILocation loc)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), getReflection());
	}

	public void teleToLocation(ILocation loc, Reflection r)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), r);
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}

	public void teleToLocation(Location location, int min, int max)
	{
		teleToLocation(Location.findAroundPosition(location, min, max, 0), getReflection());
	}

	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if(!isTeleporting.compareAndSet(false, true))
			return;

		if(isFakeDeath())
			breakFakeDeath();

		abortCast(true, false);
		if(!isLockedTarget())
			setTarget(null);

		getMovement().stopMove();

		if(!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r))
			z = GeoEngine.getLowerHeight(x, y, z, r.getGeoIndex());

		final Location loc = Location.findPointToStay(x, y, z, 0, 50, r.getGeoIndex());

		//TODO: [Bonux] Check ExTeleportToLocationActivate!
		if(isPlayer())
		{
			Player player = (Player) this;

			if(!player.isInObserverMode())
				sendPacket(new TeleportToLocationPacket(this, loc.x, loc.y, loc.z));

			player.getListeners().onTeleport(loc.x, loc.y, loc.z, r);

			decayMe();

			setLoc(loc);
			
			if ((player.getReflection().getId() <= -5) && (player.getReflection().getId() != r.getId()))
			{
				player.stopTimedHuntingZoneTask(true);
			}

			setReflection(r);

			if(!player.isInObserverMode())
				sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));

			if(player.isInObserverMode() || isFakePlayer())
				onTeleported();
		}
		else
		{
			broadcastPacket(new TeleportToLocationPacket(this, loc.x, loc.y, loc.z));

			World.forgetObject(this);

			setLoc(loc);

			setReflection(r);

			sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));

			onTeleported();
		}
	}

	public boolean onTeleported()
	{
		if(isTeleporting.compareAndSet(true, false))
		{
			updateZones();
			return true;
		}
		return false;
	}

	public void sendMessage(CustomMessage message)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}

	@Override
	public double getCollisionRadius()
	{
		return getBaseStats().getCollisionRadius();
	}

	@Override
	public double getCollisionHeight()
	{
		return getBaseStats().getCollisionHeight();
	}

	public AbnormalList getAbnormalList()
	{
		if(_effectList == null)
		{
			synchronized (this)
			{
				if(_effectList == null)
					_effectList = new AbnormalList(this);
			}
		}

		return _effectList;
	}

	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_attacker_level = 0xFFFF;

		if(isNpc())
		{
			NpcInstance npc = (NpcInstance) this;

			NpcInstance leader = npc.getLeader();
			if(leader != null)
				return leader.paralizeOnAttack(attacker);

			if(isRaid() && !isArenaRaid())
				max_attacker_level = getLevel() + npc.getParameter("ParalizeOnAttack", Config.RAID_MAX_LEVEL_DIFF);
			else
			{
				int max_level_diff = npc.getParameter("ParalizeOnAttack", -1000);
				if(max_level_diff != -1000)
					max_attacker_level = getLevel() + max_level_diff;
			}
		}

		if(attacker.getLevel() > max_attacker_level)
			return true;

		return false;
	}

	@Override
	protected void onDelete()
	{
		CharacterAI ai = getAI();
		if(ai != null)
		{
			ai.stopAllTaskAndTimers();
			ai.notifyEvent(CtrlEvent.EVT_DELETE);
		}

		stopDeleteTask();

		GameObjectsStorage.remove(this);

		getAbnormalList().stopAll();

		super.onDelete();
	}

	// ---------------------------- Not Implemented -------------------------------

	public void addExpAndSp(long exp, long sp)
	{}

	public void broadcastCharInfo()
	{}

	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{}

	public void checkHpMessages(double currentHp, double newHp)
	{}

	public boolean checkPvP(Creature target, SkillEntry skillEntry)
	{
		return false;
	}

	public boolean consumeItem(int itemConsumeId, long itemCount, boolean sendMessage)
	{
		return true;
	}

	public boolean consumeItemMp(int itemId, int mp)
	{
		return true;
	}

	public boolean isFearImmune()
	{
		return isPeaceNpc();
	}

	public boolean isThrowAndKnockImmune()
	{
		return isPeaceNpc();
	}

	public boolean isTransformImmune()
	{
		return isPeaceNpc();
	}

	public boolean isLethalImmune()
	{
		return isBoss() || isRaid();
	}

	public double getChargedSoulshotPower()
	{
		return 0;
	}

	public void setChargedSoulshotPower(double val)
	{
		//
	}

	public double getChargedSpiritshotPower()
	{
		return 0;
	}

	public double getChargedSpiritshotHealBonus()
	{
		return 0;
	}

	public void setChargedSpiritshotPower(double power, int unk, double healBonus)
	{
		//
	}

	public int getIncreasedForce()
	{
		return 0;
	}

	public int getConsumedSouls(int type)
	{
		return 0;
	}

	public int getAgathionEnergy()
	{
		return 0;
	}

	public void setAgathionEnergy(int val)
	{
		//
	}

	public int getKarma()
	{
		return 0;
	}

	public boolean isPK()
	{
		return getKarma() < 0;
	}

	public double getLevelBonus()
	{
		return LevelBonusHolder.getInstance().getLevelBonus(getLevel());
	}

	public int getNpcId()
	{
		return 0;
	}

	public boolean isMyServitor(int objId)
	{
		return false;
	}

	public List<Servitor> getServitors()
	{
		return Collections.emptyList();
	}

	public int getPvpFlag()
	{
		return 0;
	}

	public void setTeam(TeamType t)
	{
		_team = t;
		sendChanges();
	}

	public TeamType getTeam()
	{
		return _team;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isParalyzeImmune()
	{
		return false;
	}

	public void reduceArrowCount()
	{}

	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}

	public void sendMessage(String message)
	{}

	public void sendPacket(IBroadcastPacket mov)
	{}

	public void sendPacket(IBroadcastPacket... mov)
	{}

	public void sendPacket(List<? extends IBroadcastPacket> mov)
	{}

	public int getMaxIncreasedForce()
	{
		return (int) getStat().calc(Stats.MAX_INCREASED_FORCE, Charge.MAX_CHARGE, null, null);
	}

	public void setIncreasedForce(int i)
	{}

	public void setConsumedSouls(int value, int type)
	{}

	public void startPvPFlag(Creature target)
	{}

	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}

	private Future<?> _updateAbnormalIconsTask;

	private class UpdateAbnormalIcons implements Runnable
	{
		@Override
		public void run()
		{
			updateAbnormalIconsImpl();
			_updateAbnormalIconsTask = null;
		}
	}

	public void updateAbnormalIcons()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateAbnormalIconsTask != null)
			{
				_updateAbnormalIconsTask.cancel(false);
				_updateAbnormalIconsTask = null;
			}
			updateAbnormalIconsImpl();
			return;
		}

		if(_updateAbnormalIconsTask != null)
			return;

		_updateAbnormalIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateAbnormalIcons(), Config.USER_INFO_INTERVAL);
	}

	public void updateAbnormalIconsImpl()
	{
		broadcastAbnormalStatus(getAbnormalStatusUpdate());
	}

	public ExAbnormalStatusUpdateFromTargetPacket getAbnormalStatusUpdate()
	{
		Abnormal[] effects = getAbnormalList().toArray();
		Arrays.sort(effects, AbnormalsComparator.getInstance());

		ExAbnormalStatusUpdateFromTargetPacket abnormalStatus = new ExAbnormalStatusUpdateFromTargetPacket(getObjectId());
		for(Abnormal effect : effects)
		{
			if(effect != null && !effect.checkAbnormalType(AbnormalType.HP_RECOVER) && (isPlayable() ? effect.getSkill().isShowPlayerAbnormal() : effect.getSkill().isShowNpcAbnormal()))
				effect.addIcon(abnormalStatus);
		}
		return abnormalStatus;
	}

	public void broadcastAbnormalStatus(ExAbnormalStatusUpdateFromTargetPacket packet)
	{
		if(getTarget() == this)
			sendPacket(packet);

		if(!isVisible())
			return;

		List<Player> players = World.getAroundObservers(this);
		Player target;
		for(int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			if(target.getTarget() == this)
				target.sendPacket(packet);
		}
	}

	/**
	 * Выставить предельные значения HP/MP/CP и запустить регенерацию, если в этом есть необходимость
	 */
	protected void refreshHpMpCp()
	{
		final int maxHp = getMaxHp();
		final int maxMp = getMaxMp();
		final int maxCp = isPlayer() ? getMaxCp() : 0;

		if(_currentHp > maxHp)
			setCurrentHp(maxHp, false);
		if(_currentMp > maxMp)
			setCurrentMp(maxMp, false);
		if(_currentCp > maxCp)
			setCurrentCp(maxCp, false);

		if(_currentHp < maxHp || _currentMp < maxMp || _currentCp < maxCp)
			startRegeneration();
	}

	public void updateStats()
	{
		refreshHpMpCp();
		sendChanges();
	}

	public void setOverhitAttacker(Creature attacker)
	{}

	public void setOverhitDamage(double damage)
	{}

	public boolean isHero()
	{
		return false;
	}

	public int getAccessLevel()
	{
		return 0;
	}

	public Clan getClan()
	{
		return null;
	}

	public int getFormId()
	{
		return 0;
	}

	public boolean isNameAbove()
	{
		return true;
	}

	@Override
	public boolean setLoc(ILocation loc)
	{
		return setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}

	public boolean setLoc(ILocation loc, boolean stopMove)
	{
		return setXYZ(loc.getX(), loc.getY(), loc.getZ(), stopMove);
	}

	@Override
	public boolean setXYZ(int x, int y, int z)
	{
		return setXYZ(x, y, z, false);
	}

	public boolean setXYZ(int x, int y, int z, boolean stopMove)
	{
		if(!stopMove)
			getMovement().stopMove();

		getMovement().getMoveLock().lock();
		try
		{
			if(!super.setXYZ(x, y, z))
				return false;
		}
		finally
		{
			getMovement().getMoveLock().unlock();
		}

		updateZones();
		return true;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		updateStats();
		updateZones();
	}

	@Override
	public void spawnMe(Location loc)
	{
		if(loc.h >= 0)
			setHeading(loc.h);
		super.spawnMe(loc);
	}

	@Override
	protected void onDespawn()
	{
		if(!isLockedTarget())
			setTarget(null);
		getMovement().stopMove();
		stopAttackStanceTask();
		stopRegeneration();

		updateZones();

		super.onDespawn();
	}

	public final void doDecay()
	{
		if(!isDead())
			return;

		onDecay();
	}

	protected void onDecay()
	{
		decayMe();
	}

	// Функция для дизактивации умений персонажа (если умение не активно, то он не дает статтов и имеет серую иконку).
	private TIntSet _unActiveSkills = new TIntHashSet();

	public void addUnActiveSkill(Skill skill)
	{
		if(skill == null || isUnActiveSkill(skill.getId()))
			return;

		if(skill.isToggle())
			getAbnormalList().stop(skill, false);

		getStat().removeFuncsByOwner(skill);
		removeTriggers(skill);

		_unActiveSkills.add(skill.getId());
	}

	public void removeUnActiveSkill(Skill skill)
	{
		if(skill == null || !isUnActiveSkill(skill.getId()))
			return;

		getStat().addFuncs(skill.getStatFuncs());
		addTriggers(skill);

		_unActiveSkills.remove(skill.getId());
	}

	public boolean isUnActiveSkill(int id)
	{
		return _unActiveSkills.contains(id);
	}

	public abstract int getLevel();

	public abstract ItemInstance getActiveWeaponInstance();

	public abstract WeaponTemplate getActiveWeaponTemplate();

	public abstract ItemInstance getSecondaryWeaponInstance();

	public abstract WeaponTemplate getSecondaryWeaponTemplate();

	public CharListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new CharListenerList(this);
			}
		return listeners;
	}

	public <T extends Listener<Creature>> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends Listener<Creature>> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}

	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new CharStatsChangeRecorder<Creature>(this);
			}

		return _statsRecorder;
	}

	@Override
	public boolean isCreature()
	{
		return true;
	}

	public void displayGiveDamageMessage(Creature target, Skill skill, int damage, Servitor servitorTransferedDamage, int transferedDamage, boolean crit, boolean miss, boolean shld, boolean blocked, int elementalDamage, boolean elementalCrit)
	{
		if(miss)
		{
			if(target.isPlayer())
				target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(this));
			return;
		}

		if(blocked)
		{
			//
		}
		else if(shld)
		{
			if(target.isPlayer())
			{
				if(damage == Config.EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE)
					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				else if(damage > 0)
					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			}
		}
	}

	public void displayReceiveDamageMessage(Creature attacker, int damage, Servitor servitorTransferedDamage, int transferedDamage, int elementalDamage)
	{
		//
	}

	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.valueCollection();
	}

	public TimeStamp getSkillReuse(Skill skill)
	{
		return _skillReuses.get(skill.getReuseHash());
	}

	public Sex getSex()
	{
		return Sex.MALE;
	}

	public final boolean isInFlyingTransform()
	{
		if(isTransformed())
			return getTransform().getType() == TransformType.FLYING;
		return false;
	}

	public final boolean isVisualTransformed()
	{
		return getVisualTransform() != null;
	}

	public final int getVisualTransformId()
	{
		if(getVisualTransform() != null)
			return getVisualTransform().getId();

		return 0;
	}

	public final TransformTemplate getVisualTransform()
	{
		if(_isInTransformUpdate)
			return null;

		if(_visualTransform != null)
			return _visualTransform;

		return getTransform();
	}

	public final void setVisualTransform(int id)
	{
		TransformTemplate template = id > 0 ? TransformTemplateHolder.getInstance().getTemplate(getSex(), id) : null;
		setVisualTransform(template);
	}

	public void setVisualTransform(TransformTemplate template)
	{
		if(_visualTransform == template)
			return;

		if(template != null && isVisualTransformed() || template == null && isTransformed())
		{
			_isInTransformUpdate = true;
			_visualTransform = null;

			sendChanges();

			_isInTransformUpdate = false;
		}

		_visualTransform = template;

		Location destLoc = getLoc().correctGeoZ(getGeoIndex()).changeZ((_visualTransform == null ? 0 : _visualTransform.getSpawnHeight()) + (int) getCurrentCollisionHeight());
		sendPacket(new FlyToLocationPacket(this, destLoc, FlyType.DUMMY, 0, 0, 0));
		setLoc(destLoc);

		sendChanges();
	}

	public boolean isTransformed()
	{
		return false;
	}

	public final int getTransformId()
	{
		if(isTransformed())
			return getTransform().getId();

		return 0;
	}

	public TransformTemplate getTransform()
	{
		return null;
	}

	public void setTransform(int id)
	{
		//
	}

	public void setTransform(TransformTemplate template)
	{
		//
	}

	public boolean isDeathImmune()
	{
		return getFlags().getDeathImmunity().get() || isPeaceNpc();
	}

	public final double getMovementSpeedMultiplier()
	{
		return getRunSpeed() * 1. / getBaseStats().getRunSpd();
	}

	@Override
	public int getMoveSpeed()
	{
		if(isRunning())
			return getRunSpeed();

		return getWalkSpeed();
	}

	public int getRunSpeed()
	{
		if(isMounted())
			return getRideRunSpeed();

		if(isFlying())
			return getFlyRunSpeed();

		if(isInWater())
			return getSwimRunSpeed();

		return getSpeed(getBaseStats().getRunSpd());
	}

	public int getWalkSpeed()
	{
		if(isMounted())
			return getRideWalkSpeed();

		if(isFlying())
			return getFlyWalkSpeed();

		if(isInWater())
			return getSwimWalkSpeed();

		return getSpeed(getBaseStats().getWalkSpd());
	}

	public final int getSwimRunSpeed()
	{
		return getSpeed(getBaseStats().getWaterRunSpd());
	}

	public final int getSwimWalkSpeed()
	{
		return getSpeed(getBaseStats().getWaterWalkSpd());
	}

	public final int getFlyRunSpeed()
	{
		return getSpeed(getBaseStats().getFlyRunSpd());
	}

	public final int getFlyWalkSpeed()
	{
		return getSpeed(getBaseStats().getFlyWalkSpd());
	}

	public final int getRideRunSpeed()
	{
		return getSpeed(getBaseStats().getRideRunSpd());
	}

	public final int getRideWalkSpeed()
	{
		return getSpeed(getBaseStats().getRideWalkSpd());
	}

	public final double relativeSpeed(GameObject target)
	{
		return getMoveSpeed() - target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
	}

	public final int getSpeed(double baseSpeed)
	{
		return (int) Math.max(1, getStat().calc(Stats.RUN_SPEED, baseSpeed, null, null));
	}

	public double getHpRegen()
	{
		return getStat().calc(Stats.REGENERATE_HP_RATE, getBaseStats().getHpReg());
	}

	public double getMpRegen()
	{
		return getStat().calc(Stats.REGENERATE_MP_RATE, getBaseStats().getMpReg());
	}

	public double getCpRegen()
	{
		return getStat().calc(Stats.REGENERATE_CP_RATE, getBaseStats().getCpReg());
	}

	public int getEnchantEffect()
	{
		return 0;
	}

	public final boolean isKnockDowned()
	{
		return getFlags().getKnockDowned().get();
	}

	public final boolean isKnockBacked()
	{
		return getFlags().getKnockBacked().get();
	}

	public final boolean isFlyUp()
	{
		return getFlags().getFlyUp().get();
	}

	public void setRndCharges(int value)
	{
		_rndCharges = value;
	}

	public int getRndCharges()
	{
		return _rndCharges;
	}
	
	public void setHitBlocks(int value)
	{
		_hitBlocks = value;
	}
	
	public int getHitBlocks()
	{
		return _hitBlocks;
	}

	public void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		//
	}

	public void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		//
	}

	public boolean isPeaceNpc()
	{
		return false;
	}

	// Получаем дистанцию для взаимодействия(атака, диалог и т.д.) с целью.
	public int getInteractionDistance(GameObject target)
	{
		int range = (int) Math.max(10, getMinDistance(target));
		if(target.isNpc())
		{
			range += INTERACTION_DISTANCE / 2;
			if(!target.isInRangeZ(this, range) && !GeoEngine.canMoveToCoord(getX(), getY(), getZ(), target.getX(), target.getY(), target.getZ(), getGeoIndex()))
			{
				List<Location> _moveList = GeoEngine.MoveList(getX(), getY(), getZ(), target.getX(), target.getY(), getGeoIndex(), false);
				if(_moveList != null)
				{
					Location moveLoc = _moveList.get(_moveList.size() - 1).geo2world();
					if(!target.isInRangeZ(moveLoc, range) && target.isInRangeZ(moveLoc, range + (INTERACTION_DISTANCE / 2)))
						range = target.getDistance3D(moveLoc) + 16;
				}
			}
		}
		else
			range += INTERACTION_DISTANCE;
		return range;
	}

	public boolean checkInteractionDistance(GameObject target)
	{
		return isInRangeZ(target, getInteractionDistance(target) + 32);
	}

	public void setDualCastEnable(boolean val)
	{
		_isDualCastEnable = val;
	}

	public boolean isDualCastEnable()
	{
		return _isDualCastEnable;
	}

	public boolean isTargetable(Creature creature)
	{
		if(creature != null)
		{
			if(creature == this)
				return true;

			if(creature.isPlayer())
			{
				if(creature.getPlayer().isGM())
					return true;
			}
		}
		return _isTargetable;
	}

	public boolean isTargetable()
	{
		return isTargetable(null);
	}

	public void setTargetable(boolean value)
	{
		_isTargetable = value;
	}
	
	private boolean checkRange(Creature caster, Creature target)
	{
		return caster.isInRange(target, Config.REFLECT_MIN_RANGE);
	}
	
	private boolean canAbsorb(Creature attacked, Creature attacker)
	{
		if(attacked.isPlayable() || !Config.DISABLE_VAMPIRIC_VS_MOB_ON_PVP)
			return true;
		return attacker.getPvpFlag() == 0;		
	}

	public CreatureBaseStats getBaseStats()
	{
		if(_baseStats == null)
			_baseStats = new CreatureBaseStats(this);
		return _baseStats;
	}

	public CreatureStat getStat()
	{
		if(_stat == null)
			_stat = new CreatureStat(this);
		return _stat;
	}

	public CreatureFlags getFlags()
	{
		if(_statuses == null)
			_statuses = new CreatureFlags(this);
		return _statuses;
	}

	public boolean isSpecialAbnormal(Skill skill)
	{
		return false;
	}

	// Аналог isInvul, но оно не блокирует атаку, а просто не отнимает ХП.
	public boolean isImmortal()
	{
		return false;
	}

	public boolean isChargeBlocked()
	{
		return true;
	}

	public int getAdditionalVisualSSEffect()
	{
		return 0;
	}

	public boolean isSymbolInstance()
	{
		return false;
	}

	public boolean isTargetUnderDebuff()
	{
		for(Abnormal effect : getAbnormalList())
		{
			if(effect.isOffensive())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isSitting()
	{
		return false;
	}
	
	public void sendChannelingEffect(Creature target, int state)
	{
		broadcastPacket(new ExShowChannelingEffectPacket(this, target, state));
	}

	public void startDeleteTask(long delay)
	{
		stopDeleteTask();
		_deleteTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), delay);
	}

	public void stopDeleteTask()
	{
		if(_deleteTask != null)
		{
			_deleteTask.cancel(false);
			_deleteTask = null;
		}
	}

	public boolean isDeleteTaskScheduled() {
		return _deleteTask != null;
	}

	public void deleteCubics()
	{
		//
	}

	public void onZoneEnter(Zone zone)
	{
		//
	}

	public void onZoneLeave(Zone zone)
	{
		//
	}

	/**
	 * Возвращает тип атакующего элемента
	 */
	public Element getAttackElement()
	{
		return Formulas.getAttackElement(this, null);
	}

	/**
	 * Возвращает силу атаки элемента
	 *
	 * @return значение атаки
	 */
	public int getAttack(Element element)
	{
		Stats stat = element.getAttack();
		if(stat != null)
			return (int) getStat().calc(stat);
		return 0;
	}

	/**
	 * Возвращает защиту от элемента
	 *
	 * @return значение защиты
	 */
	public int getDefence(Element element)
	{
		Stats stat = element.getDefence();
		if(stat != null)
			return (int) getStat().calc(stat);
		return 0;
	}

	public boolean hasBasicPropertyResist()
	{
		return true;
	}

	public BasicPropertyResist getBasicPropertyResist(BasicProperty basicProperty)
	{
		if(_basicPropertyResists == null)
		{
			synchronized(this)
			{
				if(_basicPropertyResists == null)
					_basicPropertyResists = new ConcurrentHashMap<>();
			}
		}
		return _basicPropertyResists.computeIfAbsent(basicProperty, k -> new BasicPropertyResist());
	}

	public boolean isMounted()
	{
		return false;
	}

	@Override
	protected Shape makeGeoShape()
	{
		int x = getX();
		int y = getY();
		int z = getZ();
		Circle circle = new Circle(x, y, (int) getCollisionRadius());
		circle.setZmin(z - Config.MAX_Z_DIFF);
		circle.setZmax(z + (int) getCollisionHeight());
		return circle;
	}

	public int getGmSpeed()
	{
		return _gmSpeed;
	}

	public void setGmSpeed(int value)
	{
		_gmSpeed = value;
	}

	public CreatureMovement getMovement()
	{
		return _movement;
	}

	public CreatureSkillCast getSkillCast(SkillCastingType castingType)
	{
		CreatureSkillCast skillCast = _skillCasts[castingType.ordinal()];
		if(skillCast == null)
		{
			skillCast = new CreatureSkillCast(this, castingType);
			_skillCasts[castingType.ordinal()] = skillCast;
			
		}
		return skillCast;
	}

	public ElementalElement getActiveElement()
	{
		return ElementalElement.NONE;
	}
}