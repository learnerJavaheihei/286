package l2s.gameserver.model.actor.instances.player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.templates.cubic.CubicSkillInfo;
import l2s.gameserver.templates.cubic.CubicTargetType;
import l2s.gameserver.templates.cubic.CubicTemplate;
import l2s.gameserver.utils.TimeUtils;

/**
 * @author Bonux
**/
public class Cubic implements Runnable
{
	protected final Player _owner;
	protected final CubicTemplate _template;
	protected final Skill _skill;

	protected ScheduledFuture<?> _task;
	protected ScheduledFuture<?> _castTask;

	private int _delay;
	private int _lifeTime = 0;
	private int _count;

	private long _reuse = 0L;

	public Cubic(Player owner, CubicTemplate template, Skill skill)
	{
		_owner = owner;
		_template = template;
		_skill = skill;
		_delay = _template.getDelay();
		_count = _template.getMaxCount();
	}

	public int getId()
	{
		return _template.getId();
	}

	public int getSlot()
	{
		return _template.getSlot();
	}

	public Player getOwner()
	{
		return _owner;
	}

	public CubicTemplate getTemplate()
	{
		return _template;
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public void init()
	{
		_owner.addCubic(this);

		if(_task == null)
			_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
	}

	public void delete()
	{
		if(_task != null)
		{
			_task.cancel(true);
			_task = null;
		}

		if(_castTask != null)
		{
			_castTask.cancel(true);
			_castTask = null;
		}

		_owner.removeCubic(getSlot());
	}

	@Override
	public void run()
	{
		_lifeTime++;

		if(_template.getDuration() > 0 && _lifeTime >= _template.getDuration())
		{
			delete();
			return;
		}

		if(_castTask != null)
			return;

		CubicSkillInfo skill = _template.getTimeSkill(_lifeTime);
		if(skill != null)
		{
			if(doCastSkill(skill))
				return;
		}

		if(_reuse > System.currentTimeMillis())
			return;

		boolean success = false;

		skill = _template.getRandomSkill();
		if(skill != null)
			success = doCastSkill(skill);

		if(success)
			_count -= 1;

		if(_count <= 0)
		{
			switch(_template.getUseUp())
			{
				case INCREASE_DELAY:
				{
					_count = _template.getMaxCount();
					_delay *= 5;
					break;
				}
				case DISPELL:
				{
					delete();
					break;
				}
			}
		}

		if(success && _delay > 0)
			_reuse = System.currentTimeMillis() + _delay * 1000L;
	}

	private boolean doCastSkill(final CubicSkillInfo skillInfo)
	{
		if(getOwner().isAlikeDead())
			return false;

		if(getOwner().isAfraid())
			return false;

		if(isSkillDisabled(skillInfo))
			return false;

		final CubicTargetType targetType = getTargetType(skillInfo);
		final Creature target = targetType.getTarget(this, skillInfo);
		if(target == null)
			return false;

		int chance;
		switch(targetType)
		{
			case HEAL:
				chance = skillInfo.getChance((int) target.getCurrentHpPercents());
				break;
			case MANA_HEAL:
				chance = skillInfo.getChance((int) target.getCurrentMpPercents());
				break;
			default:
				chance = skillInfo.getChance();
				break;
		}

		if(chance < 100 && !Rnd.chance(chance))
			return false;

		final Skill skill = skillInfo.getSkill();

		if(!skill.isHandler())
		{
			if(skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if(!_owner.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false))
					return false;
			}
		}

		disableSkill(skillInfo);

		final Creature aimTarget = target;
		final Set<Creature> targets = new HashSet<Creature>(1);
		targets.add(aimTarget);

		final int hitTime = Math.max(isAgathion() ? 0 : 2000, skill.getHitTime());

		// TODO: Вообще не понятно как правильно посылать пакет каста скилла.
		if(!skill.isNotBroadcastable())
			_owner.broadcastPacket(new MagicSkillUse(_owner, target, skill.getDisplayId(), skill.getDisplayLevel(), hitTime, 0));

		_castTask = ThreadPoolManager.getInstance().schedule(() ->
		{
			_owner.callSkill(aimTarget, SkillEntry.makeSkillEntry(SkillEntryType.CUBIC, skill), targets, false, false);

			if(skill.isDebuff() && aimTarget.isNpc())
			{
				if(aimTarget.paralizeOnAttack(_owner))
				{
					if(Config.PARALIZE_ON_RAID_DIFF)
						_owner.paralizeMe(aimTarget);
				}
				else
				{
					if(skill.getEffectPoint() < 0)
						aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, _owner, skill, skill.getEffectPoint());
				}
			}
			_castTask = null;
		}, hitTime);
		return true;
	}

	private CubicTargetType getTargetType(CubicSkillInfo info)
	{
		switch(_template.getTargetType())
		{
			case BY_SKILL:
				return info.getTargetType();
		}
		return _template.getTargetType();
	}

	public boolean canCastSkill(Creature target, Skill skill, boolean party)
	{
		if(_owner == target)
			return true;

		if(skill.getCastRange() == -2)
			return true;

		if(!GeoEngine.canSeeTarget(_owner, target)) // Для кубиков это обязательное условие.
			return false;

		if(skill.getCastRange() == -1)
		{
			if(!_owner.isInRangeZ(target, party ? Config.ALT_PARTY_DISTRIBUTION_RANGE : 1500))
				return false;
			return true;
		}

		int range = Math.max(10, skill.getCastRange()) + (int) _owner.getMinDistance(target);
		range += 40; // Погрешность, если атакующий добежал к цели используя погрешность.
		if(!_owner.isInRangeZ(target, range))
			return false;

		return true;
	}

	public boolean isSkillDisabled(CubicSkillInfo info)
	{
		switch(info.getReuseType())
		{
			case DEFAULT:
				return _owner.isSkillDisabled(info.getSkill());
			case DAILY:
				return _owner.isSharedGroupDisabled(info.getSkill().getReuseHash());
		}
		return false;
	}

	public void disableSkill(CubicSkillInfo info)
	{
		switch(info.getReuseType())
		{
			case DEFAULT:
			{
				int reuse = info.getSkill().getReuseDelay();
				if(reuse > 0)
					_owner.disableSkill(info.getSkill(), info.getSkill().getReuseDelay());
				break;
			}
			case DAILY:
			{
				_owner.addSharedGroupReuse(info.getSkill().getReuseHash(), new TimeStamp(info.getSkill().getReuseHash(), TimeUtils.DAILY_DATE_PATTERN.next(System.currentTimeMillis()), 0));
				break;
			}
		}
	}

	public boolean isCubic()
	{
		return true;
	}

	public boolean isAgathion()
	{
		return false;
	}
}