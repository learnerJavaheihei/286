package l2s.gameserver.model.actor.instances.creature;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.gameserver.skills.SkillInfo;
import org.apache.commons.lang3.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.skills.skillclasses.Transformation;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @reworked by Bonux
**/
public final class AbnormalList implements Iterable<Abnormal>
{
	private static final Logger _log = LoggerFactory.getLogger(AbnormalList.class);

	public static final int NONE_SLOT_TYPE = -1;
	public static final int BUFF_SLOT_TYPE = 0;
	public static final int MUSIC_SLOT_TYPE = 1;
	public static final int TRIGGER_SLOT_TYPE = 2;
	public static final int DEBUFF_SLOT_TYPE = 3;

	private final Collection<Abnormal> _abnormals = new ConcurrentLinkedQueue<Abnormal>();

	private final Creature _owner;

	private final Lock _addAbnormalLock = new ReentrantLock();

	public AbnormalList(Creature owner)
	{
		_owner = owner;
	}

	@Override
	public Iterator<Abnormal> iterator()
	{
		return _abnormals.iterator();
	}

	public boolean contains(int skillId)
	{
		if(_abnormals.isEmpty())
			return false;

		for(Abnormal abnormal : _abnormals)
		{
			if(abnormal.getSkill().getId() == skillId)
				return true;
		}
		return false;
	}

	public boolean contains(SkillInfo skillInfo)
	{
		if(skillInfo == null)
			return false;
		return contains(skillInfo.getId());
	}

	public boolean contains(AbnormalType type)
	{
		if(type == null)
			return false;

		for(Abnormal abnormal : _abnormals)
		{
			if(abnormal.getAbnormalType() == type)
				return true;
		}
		return false;
	}

	public Collection<Abnormal> values()
	{
		return _abnormals;
	}

	public Abnormal[] toArray()
	{
		return _abnormals.toArray(new Abnormal[_abnormals.size()]);
	}

	public int getCount(int skillId)
	{
		int result = 0;

		if(_abnormals.isEmpty())
			return 0;

		for(Abnormal abnormal : _abnormals)
		{
			if(abnormal.getSkill().getId() == skillId)
				result++;
		}
		return result;
	}

	public int getCount(SkillInfo skillInfo)
	{
		if(skillInfo == null)
			return 0;
		return getCount(skillInfo.getId());
	}

	public int getCount(AbnormalType type)
	{
		int result = 0;

		if(_abnormals.isEmpty())
			return 0;

		for(Abnormal abnormal : _abnormals)
		{
			if(type == abnormal.getAbnormalType())
				result++;
		}
		return result;
	}

	public int getAbnormalLevel(AbnormalType type)
	{
		int result = -1;

		if(_abnormals.isEmpty())
			return -1;

		for(Abnormal abnormal : _abnormals)
		{
			if(type != abnormal.getAbnormalType())
				continue;
			if(result > abnormal.getAbnormalLvl())
				continue;
			result = abnormal.getAbnormalLvl();
		}
		return result;
	}

	public int size()
	{
		return _abnormals.size();
	}

	public boolean isEmpty()
	{
		return _abnormals.isEmpty();
	}

	private void checkSlotLimit(Abnormal newAbnormal)
	{
		if(_abnormals.isEmpty())
			return;

		int slotType = getSlotType(newAbnormal);
		if(slotType == NONE_SLOT_TYPE)
			return;

		int size = 0;
		TIntSet skillIds = new TIntHashSet();
		for(Abnormal e : _abnormals)
		{
			if(e.getSkill().equals(newAbnormal.getSkill())) // мы уже имеем эффект от этого скилла
				return;

			if(!skillIds.contains(e.getSkill().getId()))
			{
				int subType = getSlotType(e);
				if(subType == slotType)
				{
					size++;
					skillIds.add(e.getSkill().getId());
				}
			}
		}

		int limit = 0;
		switch(slotType)
		{
			case BUFF_SLOT_TYPE:
				limit = _owner.getBuffLimit();
				break;
			case MUSIC_SLOT_TYPE:
				limit = Config.ALT_MUSIC_LIMIT;
				break;
			case DEBUFF_SLOT_TYPE:
				limit = Config.ALT_DEBUFF_LIMIT;
				break;
			case TRIGGER_SLOT_TYPE:
				limit = Config.ALT_TRIGGER_LIMIT;
				break;
		}

		if(size < limit)
			return;

		for(Abnormal e : _abnormals)
		{
			if(getSlotType(e) == slotType)
			{
				stop(e.getSkill().getId());
				break;
			}
		}
	}

	public static int getSlotType(Abnormal e)
	{
		if(e.getSkill().getBuffSlotType() == -2)
		{
			if(e.isHidden() || e.getSkill().isPassive() || e.getSkill().isToggle() || e.getSkill() instanceof Transformation || e.checkAbnormalType(AbnormalType.HP_RECOVER) || e.checkAbnormalType(AbnormalType.HERO_WEAPON))
				return NONE_SLOT_TYPE;
			else if(e.isOffensive())
				return DEBUFF_SLOT_TYPE;
			else if(e.getSkill().isMusic())
				return MUSIC_SLOT_TYPE;
			else if(e.getSkill().isTrigger())
				return TRIGGER_SLOT_TYPE;
			else
				return BUFF_SLOT_TYPE;
		}
		else
		{
			return e.getSkill().getBuffSlotType();
		}
	}

	public static boolean checkAbnormalType(Skill skill1, Skill skill2)
	{
		if(skill1.getId() == skill2.getId())
			return true;

		AbnormalType abnormalType1 = skill1.getAbnormalType();
		if(abnormalType1 == AbnormalType.NONE)
			return false;

		AbnormalType abnormalType2 = skill2.getAbnormalType();
		if(abnormalType2 == AbnormalType.NONE)
			return false;

		return abnormalType1 == abnormalType2;
	}

	public boolean add(Abnormal abnormal)
	{
		if(!abnormal.isTimeLeft())
			return false;

		Skill skill = abnormal.getSkill();
		if(skill == null)
			return false;

		boolean success = false;
		try
		{
			if(!_addAbnormalLock.tryLock(1000, TimeUnit.MILLISECONDS))
				return false;

			_owner.getStatsRecorder().block(); // Для того, чтобы не флудить пакетами.
			try
			{
				//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
				double hp = _owner.getCurrentHp();
				double mp = _owner.getCurrentMp();
				double cp = _owner.getCurrentCp();

				boolean suspended = false;

				if(!_abnormals.isEmpty() && (abnormal.isOfUseType(EffectUseType.NORMAL) || abnormal.isOfUseType(EffectUseType.SELF)))
				{
					if(skill.isToggle())
					{
						if(contains(skill))
							return false;

						if(skill.isToggleGrouped() && skill.getToggleGroupId() > 0)
						{
							for(Abnormal a : _abnormals)
							{
								if(!a.getSkill().isToggleGrouped())
									continue;

								if(skill.getToggleGroupId() == a.getSkill().getToggleGroupId())
								{
									if(!_owner.isDualCastEnable() || a.getSkill().getToggleGroupId() != 1)
										a.exit();
								}
							}
						}
					}
					else
					{
						AbnormalType abnormalType = abnormal.getAbnormalType();
						if(abnormalType == AbnormalType.NONE)
						{
							// Удаляем такие же эффекты
							for(Abnormal a : _abnormals)
							{
								if(a.getSkill().getId() == skill.getId())
								{
									// Если оставшаяся длительность старого эффекта больше чем длительность нового, то оставляем старый.
									/*if(abnormal.getTimeLeft() > a.getTimeLeft()) // Отключено для теста, вроде-бы так будет по оффу - отключенным.
										a.exit();
									// Если у старого эффекта уровень ниже, чем у нового, то заменяем новым.
									else */if(skill.getLevel() >= a.getSkill().getLevel())
										a.exit();
									else
										return false;
								}
							}
						}
						else
						{
							// Проверяем, нужно ли накладывать эффект, при совпадении StackType.
							// Новый эффект накладывается только в том случае, если у него больше StackOrder и больше длительность.
							// Если условия подходят - удаляем старый.
							for(Abnormal a : _abnormals)
							{
								if(a.checkBlockedAbnormalType(abnormalType))
									return false;

								if(abnormal.checkBlockedAbnormalType(a.getAbnormalType()))
								{
									a.exit();
									continue;
								}

								if(a.getEffector() != abnormal.getEffector() && abnormal.getAbnormalType().isStackable())
									continue;

								if(!checkAbnormalType(a.getSkill(), skill))
									continue;

								if(a.getSkill().isIrreplaceableBuff())
									return false;

								/*if(abnormal.getAbnormalLvl() == a.getAbnormalLvl()) // Отключено для теста, вроде-бы так будет по оффу - отключенным.
								{
									if(skill.getTargetType() != SkillTargetType.TARGET_SELF && abnormal.getTimeLeft() < a.getTimeLeft())
										return false;
								}
								else */if(abnormal.getAbnormalLvl() < a.getAbnormalLvl())
								{
									if(a.getSkill().isAbnormalInstant() && !skill.isAbnormalInstant())
									{
										suspended = true;
										break;
									}
									return false;
								}

								if(!a.getSkill().isAbnormalInstant() && skill.isAbnormalInstant())
									a.suspend();
								else
									a.exit();

								break;
							}
						}

						// Проверяем на лимиты бафов/дебафов
						checkSlotLimit(abnormal);
					}
				}

				success = _abnormals.add(abnormal);

				if(success)
				{
					if(!suspended)
						abnormal.start(); // Запускаем эффект
					else
						abnormal.suspend(); // Запускаем эффект в пассивном режиме
				}

				//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
				for(EffectHandler effect : abnormal.getEffects())
				{
					for(FuncTemplate ft : effect.getTemplate().getAttachedFuncs())
					{
						if(ft._stat == Stats.MAX_HP)
							_owner.setCurrentHp(hp, false);
						else if(ft._stat == Stats.MAX_MP)
							_owner.setCurrentMp(mp);
						else if(ft._stat == Stats.MAX_CP)
							_owner.setCurrentCp(cp);
					}
				}
			}
			finally
			{
				_owner.getStatsRecorder().unblock();
			}

			_owner.updateStats();
			_owner.updateAbnormalIcons();
			if (_owner.isPlayer())
				_owner.getPlayer().updateUserBonus();
		}
		catch(InterruptedException e)
		{
			_log.error("Error while adding new abnormal: " + e, e);
			return false;
		}
		finally
		{
			_addAbnormalLock.unlock();
		}
		return success;
	}

	/**
	 * Удаление эффекта из списка
	 *
	 * @param abnormal эффект для удаления
	 */
	public void remove(Abnormal abnormal)
	{
		if(abnormal == null)
			return;

		if(_abnormals.remove(abnormal))
		{
			if(abnormal.getSkill().isAbnormalInstant())
			{
				for(Abnormal a : _abnormals)
				{
					if(a.getAbnormalType() == abnormal.getAbnormalType())
					{
						if(a.isSuspended())
						{
							a.start();
							break;
						}
					}
				}
			}
			_owner.updateStats();
			_owner.updateAbnormalIcons();
			if (_owner.isPlayer())
				_owner.getPlayer().updateUserBonus();
		}
	}

	public int stopAll()
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(_owner.isSpecialAbnormal(a.getSkill()))
				continue;

			a.exit();
			removed++;
		}

		return removed;
	}

	public int stop(int skillId, int skillLvl)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(a.getSkill().getId() == skillId && a.getSkill().getLevel() == skillLvl)
			{
				a.exit();
				removed++;
			}
		}

		return removed;
	}

	public int stop(int skillId)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(a.getSkill().getId() == skillId)
			{
				a.exit();
				removed++;
			}
		}

		return removed;
	}

	public int stop(TIntSet skillIds)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(skillIds.contains(a.getSkill().getId()))
			{
				a.exit();
				removed++;
			}
		}

		return removed;
	}

	public int stop(AbnormalType type)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(a.getAbnormalType() == type)
			{
				a.exit();
				removed++;
			}
		}

		return removed;
	}

	public int stop(SkillInfo skillInfo, boolean checkLevel)
	{
		if(skillInfo == null)
			return 0;

		if(checkLevel)
			return stop(skillInfo.getId(), skillInfo.getLevel());
		return stop(skillInfo.getId());
	}

	/**
	 * Находит скиллы с указанным эффектом, и останавливает у этих скиллов все эффекты (не только указанный).
	 */
	@Deprecated
	public int stop(String name)
	{
		if(_abnormals.isEmpty())
			return 0;

		TIntSet skillIds = new TIntHashSet();
		for(Abnormal abnormal : _abnormals)
		{
			for(EffectHandler effect : abnormal.getEffects())
			{
				if(effect.getName().equalsIgnoreCase(name))
				{
					skillIds.add(effect.getSkill().getId());
					break;
				}
			}
		}

		int removed = 0;
		for(Abnormal abnormal : _abnormals)
		{
			if(skillIds.contains(abnormal.getSkill().getId()))
			{
				abnormal.exit();
				removed++;
			}
		}

		return removed;
	}
}