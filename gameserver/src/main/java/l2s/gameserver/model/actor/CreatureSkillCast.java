package l2s.gameserver.model.actor;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.*;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket.FlyType;
import l2s.gameserver.network.l2.s2c.MagicSkillCanceled;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.MoveToPawnPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.PositionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @auhor Bonux
**/
public class CreatureSkillCast
{
	private class MagicLaunchedTask implements Runnable
	{
		@Override
		public void run()
		{
			final SkillEntry skillEntry = getSkillEntry();
			if(skillEntry == null)
				return;

			final Creature target = getTarget();
			if(target == null)
				return;

			final Set<Creature> targets = skillEntry.getTemplate().getTargets(skillEntry, _actor, target, _forceUse);

			_targets = targets;

			if(!skillEntry.getTemplate().isNotBroadcastable())
				_actor.broadcastPacket(new MagicSkillLaunchedPacket(_actor.getObjectId(), skillEntry.getDisplayId(), skillEntry.getDisplayLevel(), targets, _castingType));

			if(_castLeftTime > 0)
				_skillTask = ThreadPoolManager.getInstance().schedule(CreatureSkillCast.this::onMagicUseTimer, _castLeftTime);
			else
				onMagicUseTimer();
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(CreatureSkillCast.class);

	private final Creature _actor;
	private final SkillCastingType _castingType;

	private final AtomicBoolean _isCastingNow = new AtomicBoolean(false);

	private HardReference<? extends Creature> _target = HardReferences.emptyRef();

	private Set<Creature> _targets = null;

	private boolean _forceUse = false;

	private SkillEntry _skillEntry = null;

	private long _animationEndTime;
	private int _castLeftTime;

	private boolean _isCriticalBlow = false;

	private Future<?> _skillTask = null;
	private Future<?> _skillTickTask = null;

	private Location _flyLoc = null;

	public CreatureSkillCast(Creature actor, SkillCastingType castingType)
	{
		_actor = actor;
		_castingType = castingType;
	}

	public Creature getActor()
	{
		return _actor;
	}

	public SkillCastingType getCastingType()
	{
		return _castingType;
	}

	public boolean isDual()
	{
		return _castingType == SkillCastingType.NORMAL_SECOND;
	}

	public boolean isCastingNow()
	{
		return _isCastingNow.get();
	}

	public Creature getTarget()
	{
		return _target == null ? null : _target.get();
	}

	public Set<Creature> getTargets()
	{
		return _targets;
	}

	public SkillEntry getSkillEntry()
	{
		return _skillEntry;
	}

	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}

	public int getCastLeftTime()
	{
		return _castLeftTime;
	}

	public boolean isCriticalBlow()
	{
		return _isCriticalBlow;
	}

	public boolean doCast(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(!_isCastingNow.compareAndSet(false, true))
			return false;

		if(!doCast0(skillEntry, target, forceUse))
		{
			clearVars();
			return false;
		}
		return true;
	}

	private boolean doCast0(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(skillEntry == null)
			return false;

		final Skill skill = skillEntry.getTemplate();

		if(isDual())
		{
			if(!_actor.isDualCastEnable() || !skill.isDouble())
				return false;
		}

		final Creature aimingTarget;
		if(target != null)
			aimingTarget = target;
		else
			aimingTarget = skill.getAimingTarget(_actor, getTarget());

		if(aimingTarget == null)
			return false;

		_skillEntry = skillEntry;
		_target = aimingTarget.getRef();
		_forceUse = forceUse;

		if(skill.getReferenceItemId() > 0 && !_actor.consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
			return false;

		double mpConsume1 = skill.getMpConsume1();
		if(mpConsume1 > 0)
		{
			if(_actor.getCurrentMp() < mpConsume1)
			{
				_actor.sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return false;
			}
		}

		if(!skill.isHandler() && _actor.isPlayable())
		{
			if(skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if(skill.isItemConsumeFromMaster())
				{
					Player master = _actor.getPlayer();
					if(master == null)
						return false;

					if(ItemFunctions.getItemCount(master, skill.getItemConsumeId()) < skill.getItemConsume())
					{
						master.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						return false;
					}
				}
				else if(ItemFunctions.getItemCount((Playable) _actor, skill.getItemConsumeId()) < skill.getItemConsume())
				{
					_actor.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return false;
				}
			}
		}

		_actor.getListeners().onMagicUse(skill, aimingTarget, false);

		Location groundLoc = null;
		if(skill.getTargetType() == SkillTargetType.TARGET_GROUND)
		{
			if(_actor.isPlayer())
			{
				groundLoc = _actor.getPlayer().getGroundSkillLoc();
				if(groundLoc != null)
					_actor.setHeading(PositionUtils.calculateHeadingFrom(_actor.getX(), _actor.getY(), groundLoc.getX(), groundLoc.getY()), true);
			}
		}
		else if(_actor != aimingTarget)
		{
			_actor.setHeading(PositionUtils.calculateHeadingFrom(_actor, aimingTarget), true);
			_actor.sendPacket(new MoveToPawnPacket(_actor, aimingTarget, _actor.getDistance(aimingTarget)));
		}

		int hitTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcSkillCastSpd(_actor, skill, skill.getHitTime());
		int hitCancelTime = skill.isSkillTimePermanent() ? skill.getHitCancelTime() : Formulas.calcSkillCastSpd(_actor, skill, skill.getHitCancelTime());

		if(skill.isMagic() && !skill.isSkillTimePermanent() && _actor.getChargedSpiritshotPower() > 0)
		{
			hitTime = (int) (0.70 * hitTime);
			hitCancelTime = (int) (0.70 * hitCancelTime);
		}

		if(!skill.isSkillTimePermanent())
		{
			if(skill.isMagic())
			{
				int minCastTimeMagical = Math.min(Config.SKILLS_CAST_TIME_MIN_MAGICAL, skill.getHitTime());
				if(hitTime < minCastTimeMagical)
				{
					hitTime = minCastTimeMagical;
					hitCancelTime = 0;
				}
			}
			else
			{
				int minCastTimePhysical = Math.min(Config.SKILLS_CAST_TIME_MIN_PHYSICAL, skill.getHitTime());
				if(hitTime < minCastTimePhysical)
				{
					hitTime = minCastTimePhysical;
					hitCancelTime = 0;
				}
			}
		}

		_animationEndTime = System.currentTimeMillis() + hitTime;

		boolean criticalBlow = skill.calcCriticalBlow(_actor, aimingTarget);

		long reuseDelay = Math.max(0, Formulas.calcSkillReuseDelay(_actor, skill));
		if(reuseDelay > 10)
			_actor.disableSkill(skill, reuseDelay);

		if(!skill.isNotBroadcastable())
		{
			MagicSkillUse msu = new MagicSkillUse(_actor, aimingTarget, skill.getDisplayId(), skill.getDisplayLevel(), hitTime, reuseDelay, _castingType);
			msu.setReuseSkillId(skill.getReuseSkillId());
			msu.setGroundLoc(groundLoc);
			msu.setCriticalBlow(criticalBlow);
			if(_actor.isServitor()) // TODO: [Bonux] Переделать.
			{
				Servitor.UsedSkill servitorUsedSkill = ((Servitor) _actor).getUsedSkill();
				if(servitorUsedSkill != null && servitorUsedSkill.getSkill() == skill)
				{
					msu.setServitorSkillInfo(servitorUsedSkill.getActionId());
					((Servitor) _actor).setUsedSkill(null);
				}
			}
			_actor.broadcastPacket(msu);
		}

		if(skill.getTargetType() == SkillTargetType.TARGET_HOLY)
			aimingTarget.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _actor, 1);

		if(_actor.isPlayer())
		{
			if(skill.getSkillType() == SkillType.PET_SUMMON)
				_actor.sendPacket(SystemMsg.SUMMONING_YOUR_PET);
			else
				_actor.sendPacket(new SystemMessagePacket(SystemMsg.YOU_USE_S1).addSkillName(skill));
		}

		if(mpConsume1 > 0){
			if(skill.isMagic())
				mpConsume1 = _actor.getStat().calc(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume1, aimingTarget, skill);
			_actor.reduceCurrentMp(mpConsume1, null);
			}

		if(!skill.isHandler() && _actor.isPlayable())
		{
			if(skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if(skill.isItemConsumeFromMaster())
				{
					Player master = _actor.getPlayer();
					if(master != null)
						master.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), true);
				}
				else
					_actor.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), true);
			}
		}

		Location flyLoc = null;
		switch(skill.getFlyType())
		{
			case CHARGE:
				flyLoc = _actor.getFlyLocation(aimingTarget, skill);
				if(flyLoc != null)
					_actor.broadcastPacket(new FlyToLocationPacket(_actor, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
				break;
			case WARP_BACK:
			case WARP_FORWARD:
				flyLoc = _actor.getFlyLocation(_actor, skill);
				if(flyLoc != null)
					_actor.broadcastPacket(new FlyToLocationPacket(_actor, flyLoc, skill.getFlyType(), (skill.getFlyRadius() / hitTime) * 1000, skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
			break;
		}

		if(criticalBlow)
			_isCriticalBlow = true;

		if(flyLoc != null)
			_flyLoc = flyLoc;

		_castLeftTime = hitTime - hitCancelTime;

		_skillTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(), hitCancelTime);

		if(skill.isChanneling())
			_skillTickTask = ThreadPoolManager.getInstance().schedule(() -> onMagicTickTimer(), skill.getChannelingStart());

		skill.onStartCast(skillEntry, _actor, aimingTarget);
		_actor.useTriggers(aimingTarget, TriggerType.ON_START_CAST, null, skill, 0);
		return true;
	}

	private void onMagicTickTimer()
	{
		final SkillEntry skillEntry = getSkillEntry();
		if(skillEntry == null)
			return;

		final Creature aimingTarget = getTarget();
		final Skill skill = skillEntry.getTemplate();
		final Set<Creature> targets = skill.getTargets(skillEntry, _actor, aimingTarget, _forceUse);

		_targets = targets;

		if(!skill.isNotBroadcastable())
			_actor.broadcastPacket(new MagicSkillLaunchedPacket(_actor.getObjectId(), skillEntry.getDisplayId(), skillEntry.getDisplayLevel(), targets, _castingType));

		double mpConsumeTick = skill.getMpConsumeTick();
		if(mpConsumeTick > 0)
		{
			if(skill.isMusic())
			{
				double inc = mpConsumeTick / 2;
				double add = 0;
				for(Abnormal e : _actor.getAbnormalList())
				{
					if(e.getSkill().getId() != skillEntry.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30)
						add += inc;
				}
				mpConsumeTick += add;
				mpConsumeTick = _actor.getStat().calc(Stats.MP_DANCE_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);
			}
			else if(skill.isMagic())
				mpConsumeTick = _actor.getStat().calc(Stats.MP_MAGIC_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);
			else
				mpConsumeTick = _actor.getStat().calc(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);

			if(_actor.getCurrentMp() < mpConsumeTick && _actor.isPlayable())
			{
				_actor.sendPacket(SystemMsg.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP);
				_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
				onCastEndTime(false);
				return;
			}
			_actor.reduceCurrentMp(mpConsumeTick, null);
		}

		skill.onTickCast(_actor, targets);
		_actor.useTriggers(aimingTarget, TriggerType.ON_TICK_CAST, null, skill, 0);

		if(skill.getTickInterval() > 0)
			_skillTickTask = ThreadPoolManager.getInstance().schedule(() -> onMagicTickTimer(), skill.getTickInterval());
	}

	private void onMagicUseTimer()
	{
		final SkillEntry skillEntry = getSkillEntry();
		final Set<Creature> targets = getTargets();
		final Creature aimingTarget = getTarget();
		if(skillEntry == null || targets == null)
		{
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			clearVars();
			return;
		}

		Skill skill = skillEntry.getTemplate();
		switch(skill.getFlyType())
		{
			case CHARGE:
			case WARP_BACK:
			case WARP_FORWARD:
				if(_flyLoc != null)
					_actor.setLoc(_flyLoc);
				break;
		}

		if(!skill.isDebuff() && _actor.getAggressionTarget() != null)
			_forceUse = true;

		skill.checkTargetsEffectiveRange(_actor, targets); // Чистим цели, которые вышли за радиус 'effective_range'

		if(skill.oneTarget() && targets.isEmpty())
		{
			_actor.sendPacket(SystemMsg.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED);
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			onCastEndTime(false);
			return;
		}

		if(!skillEntry.checkCondition(_actor, aimingTarget, _forceUse, false, false))
		{
			if(skill.getSkillType() == SkillType.PET_SUMMON && _actor.isPlayer())
				_actor.getPlayer().setPetControlItem(null);
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			onCastEndTime(false);
			return;
		}

		if(skill.getCastRange() != -2 && skill.getSkillType() != SkillType.TAKECASTLE && !GeoEngine.canSeeTarget(_actor, aimingTarget))
		{
			_actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			onCastEndTime(false);
			return;
		}

		//must be player for usage with a clan.
		int clanRepConsume = skill.getClanRepConsume();
		if(clanRepConsume > 0)
			_actor.getPlayer().getClan().incReputation(-clanRepConsume, false, "clan skills");

		int fameConsume = skill.getFameConsume();
		if(fameConsume > 0)
			_actor.getPlayer().setFame(_actor.getPlayer().getFame() - fameConsume, "clan skills", true);

		int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
			_actor.setCurrentHp(Math.max(0, _actor.getCurrentHp() - hpConsume), false);

		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			if(skill.isMusic())
			{
				double inc = mpConsume2 / 2;
				double add = 0;
				for(Abnormal e : _actor.getAbnormalList())
				{
					if(e.getSkill().getId() != skill.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30)
						add += inc;
				}
				mpConsume2 += add;
				mpConsume2 = _actor.getStat().calc(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			else if(skill.isMagic())
				mpConsume2 = _actor.getStat().calc(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2 = _actor.getStat().calc(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);

			if(_actor.getCurrentMp() < mpConsume2 && _actor.isPlayable())
			{
				_actor.sendPacket(SystemMsg.NOT_ENOUGH_MP);
				_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
				onCastEndTime(false);
				return;
			}
			_actor.reduceCurrentMp(mpConsume2, null);
		}

		_actor.callSkill(aimingTarget, skillEntry, targets, true, false);

		if(_actor.getIncreasedForce() > 0)
		{
			int decreasedForce = skill.getNumCharges();
			if(decreasedForce <= 0)
				decreasedForce = skill.getCondCharges();
			if(decreasedForce > 15)
				decreasedForce = 5;
			if(decreasedForce > 0)
				_actor.setIncreasedForce(_actor.getIncreasedForce() - skill.getNumCharges());
		}

		switch(skill.getFlyType())
		{
			// @Rivelia. Targets fly types.
			case THROW_UP:
			case THROW_HORIZONTAL:
			case PUSH_HORIZONTAL:
			case PUSH_DOWN_HORIZONTAL:
				for(Creature target : targets)
				{
					Location flyLoc = target.getFlyLocation(_actor, skill);
					if(flyLoc == null)
						_log.warn(skill.getFlyType() + " have null flyLoc.");
					else
					{
						target.broadcastPacket(new FlyToLocationPacket(target, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
						target.setLoc(flyLoc);
					}
				}
				break;
			// @Rivelia. Caster fly types.
			case DUMMY:
				Creature dummyTarget = aimingTarget;
				if(skill.getTargetType() == SkillTargetType.TARGET_AURA)
					dummyTarget = _actor;

				Location flyLoc = _actor.getFlyLocation(dummyTarget, skill);
				if(flyLoc != null)
				{
					_actor.broadcastPacket(new FlyToLocationPacket(_actor, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
					_actor.setLoc(flyLoc);
				}
				/*else
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);*/
				break;
		}

		// @Rivelia.
		int skillCoolTime = 0;
		int chargeAddition = 0;

		// @Rivelia. Add the fly speed in the skill cooltime to make the travelling end before the creature can take action again.
		if(skill.getFlyType() == FlyType.CHARGE && skill.getFlySpeed() > 0)
			chargeAddition = (_actor.getDistance(aimingTarget) / skill.getFlySpeed()) * 1000;
		
		if(!skill.isSkillTimePermanent())
			skillCoolTime = Formulas.calcSkillCastSpd(_actor, skill, skill.getCoolTime() + chargeAddition);
		else
			skillCoolTime = skill.getCoolTime() + chargeAddition;

		if(skillCoolTime > 0)
			ThreadPoolManager.getInstance().schedule(() -> onCastEndTime(true), skillCoolTime);
		else
			onCastEndTime(true);
	}

	private void onCastEndTime(boolean success)
	{
		final SkillEntry skillEntry = getSkillEntry();
		final Creature target = getTarget();
		final Set<Creature> targets = getTargets();

		clearVars();

		_actor.onCastEndTime(skillEntry, target, targets, success);
	}

	private boolean canAbortCast()
	{
		return _targets == null;
	}

	public boolean abortCast(boolean force)
	{
		if(isCastingNow() && (force || canAbortCast()))
		{
			final SkillEntry skillEntry = getSkillEntry();
			if(skillEntry != null && skillEntry.getTemplate().isAbortable())
			{
				clearVars();
				return true;
			}
		}
		return false;
	}

	private void clearVars()
	{
		_isCastingNow.set(false);
		_target = HardReferences.emptyRef();
		_targets = null;
		_forceUse = false;
		_castLeftTime = 0;
		_animationEndTime = 0;
		_skillEntry = null;
		_isCriticalBlow = false;
		if(_skillTask != null)
		{
			_skillTask.cancel(false);
			_skillTask = null;
		}
		if(_skillTickTask != null)
		{
			_skillTickTask.cancel(false);
			_skillTickTask = null;
		}
		_flyLoc = null;
	}
}