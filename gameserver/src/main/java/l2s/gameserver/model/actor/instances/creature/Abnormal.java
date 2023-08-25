package l2s.gameserver.model.actor.instances.creature;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.actor.OnAttackListener;
import l2s.gameserver.listener.actor.OnMagicUseListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadSpelledInfoPacket;
import l2s.gameserver.skills.*;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.taskmanager.EffectTaskManager;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @reworked by Bonux
**/
// TODO: Сделать чтобы статты и триггеры хранились в абнормале, а не в эффектах.
public final class Abnormal implements Runnable, Comparable<Abnormal>
{
	private static final Logger _log = LoggerFactory.getLogger(Abnormal.class);

	//Состояние, при котором работает задача запланированного эффекта
	private static final int SUSPENDED = -1;
	private static final int STARTING = 0;
	private static final int ACTING = 1;
	private static final int FINISHED = 2;

	private final Creature _effector;
	private final Creature _effected;
	private final Skill _skill;
	private final Env _env;
	private final EffectUseType _useType;

	private final Collection<EffectHandler> _effects = new ConcurrentLinkedQueue<EffectHandler>();

	// the current state
	private final AtomicInteger _state;

	private final boolean _saveable;

	private Future<?> _effectTask;

	// period, milliseconds
	private long _startTimeMillis = Long.MAX_VALUE;

	private int _duration;
	private int _timeLeft;

	public Abnormal(Creature effector, Creature effected, Skill skill, EffectUseType useType, boolean saveable)
	{
		_effector = effector;
		_effected = effected;
		_skill = skill;
		_env = new Env(effector, effected, skill);
		_useType = useType;

		_duration = Math.min(Integer.MAX_VALUE, Math.max(0, getSkill().getAbnormalTime() < 0 ? Integer.MAX_VALUE : getSkill().getAbnormalTime()));
		_timeLeft = _duration;

		_state = new AtomicInteger(STARTING);
		_saveable = saveable;

		for(EffectTemplate template : getSkill().getEffectTemplates(getUseType()))
		{
			if(template.isInstant() || !isOfUseType(template.getUseType())) // На всякий случай
				continue;

			if(!template.getTargetType().checkTarget(effected))
				continue;

			_effects.add(template.getHandler().getImpl());
		}
	}

	public Abnormal(Creature effector, Creature effected, Abnormal abnormal)
	{
		this(effector, effected, abnormal.getSkill(), abnormal.getUseType(), true);
	}

	public Env getEnv()
	{
		return _env;
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public AbnormalType getAbnormalType()
	{
		return getSkill().getAbnormalType();
	}

	public int getAbnormalLvl()
	{
		return getSkill().getAbnormalLvl();
	}

	public Creature getEffector()
	{
		return _effector;
	}

	public Creature getEffected()
	{
		return _effected;
	}

	public boolean isReflected()
	{
		return _env.reflected;
	}

	/**
	 * Возвращает время старта эффекта, если время не установлено, возвращается текущее
	 */
	public long getStartTime()
	{
		return _startTimeMillis;
	}

	/** Возвращает оставшееся время в секундах. */
	public int getTimeLeft()
	{
		return _timeLeft;
	}

	public void setTimeLeft(int value)
	{
		_timeLeft = Math.max(0, Math.min(value, _duration));
	}

	/** Возвращает true, если осталось время для действия эффекта */
	public boolean isTimeLeft()
	{
		return getTimeLeft() > 0;
	}

	public Collection<EffectHandler> getEffects()
	{
		return _effects;
	}

	public boolean isActive()
	{
		return getState() == ACTING;
	}

	public boolean isSuspended()
	{
		return getState() == SUSPENDED;
	}

	public boolean checkAbnormalType(AbnormalType abnormal)
	{
		AbnormalType abnormalType = getAbnormalType();
		if(abnormalType == AbnormalType.NONE)
			return false;

		return abnormal == abnormalType;
	}

	public boolean checkAbnormalType(Abnormal effect)
	{
		return checkAbnormalType(effect.getAbnormalType());
	}

	public boolean isFinished()
	{
		return getState() == FINISHED;
	}

	private int getState()
	{
		return _state.get();
	}

	private boolean setState(int oldState, int newState)
	{
		return _state.compareAndSet(oldState, newState);
	}

	private ActionDispelListener _listener;

	private class ActionDispelListener implements OnAttackListener, OnMagicUseListener
	{
		@Override
		public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
		{
			if(getSkill().isDoNotDispelOnSelfBuff() && !skill.isDebuff())
				return;
			exit();
		}

		@Override
		public void onAttack(Creature actor, Creature target)
		{
			exit();
		}
	}

	private boolean checkCondition()
	{
		for(EffectHandler effect : getEffects())
		{
			if(!effect.checkConditionImpl(this, getEffector(), getEffected()))
				return false;

			int chance = effect.getTemplate().getChance();
			if(chance >= 0 && !Rnd.chance(chance))
				return false;
		}
		return true;
	}

	private boolean checkActingCondition()
	{
		for(EffectHandler effect : getEffects())
		{
			if(!effect.checkActingConditionImpl(this, getEffector(), getEffected()))
				return false;
		}
		return true;
	}

	/** Notify started */
	private void onStart()
	{
		if(getSkill().isAbnormalCancelOnAction() && getEffected().isPlayable())
			getEffected().addListener(_listener = new ActionDispelListener());
		if(getEffected().isPlayer() && !getSkill().canUseTeleport())
			getEffected().getPlayer().getPlayerAccess().UseTeleport = false;

		for(AbnormalEffect abnormal : getSkill().getAbnormalEffects())
			getEffected().startAbnormalEffect(abnormal);

		for(EffectHandler effect : getEffects())
		{
			effect.onStart(this, getEffector(), getEffected());

			getEffected().getStat().addFuncs(effect.getStatFuncs());
			getEffected().addTriggers(effect.getTemplate());

			//tigger on start
			getEffected().useTriggers(getEffected(), TriggerType.ON_START_EFFECT, null, getSkill(), effect.getTemplate(), 0);
		}
	}

	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR><BR>
	 */
	private void onExit()
	{
		if(getSkill().isAbnormalCancelOnAction())
			getEffected().removeListener(_listener);
		if(getEffected().isPlayer())
		{
			if(checkAbnormalType(AbnormalType.HP_RECOVER))
				getEffected().sendPacket(new ShortBuffStatusUpdatePacket());
			if(!getSkill().canUseTeleport() && !getEffected().getPlayer().getPlayerAccess().UseTeleport)
				getEffected().getPlayer().getPlayerAccess().UseTeleport = true;
		}

		for(AbnormalEffect abnormal : getSkill().getAbnormalEffects())
		{
			if(abnormal != AbnormalEffect.NONE)
				getEffected().stopAbnormalEffect(abnormal);
		}

		for(EffectHandler effect : getEffects())
		{
			effect.onExit(this, getEffector(), getEffected());

			getEffected().getStat().removeFuncsByOwner(effect);
			getEffected().removeTriggers(effect.getTemplate());

			//tigger on exit
			getEffected().useTriggers(getEffected(), TriggerType.ON_EXIT_EFFECT, null, getSkill(), effect.getTemplate(), 0);
		}
	}

	private void stopEffectTask()
	{
		if(_effectTask != null)
		{
			_effectTask.cancel(false);
			_effectTask = null;
		}
	}

	private void startEffectTask()
	{
		if(_effectTask == null)
		{
			_startTimeMillis = System.currentTimeMillis();
			_effectTask = EffectTaskManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		}
	}

	public void restart()
	{
		_timeLeft = getDuration();

		stopEffectTask();
		startEffectTask();
	}

	public boolean apply(Creature aimingTarget)
	{
		if(_effects.isEmpty())
			return false;

		if(getEffected().isDead() && !getSkill().isPreservedOnDeath()) //why alike dead?
			return false;

		if(!checkCondition())
			return false;

		if(getEffector() != getEffected() && isOfUseType(EffectUseType.NORMAL))
		{
			if(getEffected().isEffectImmune(getEffector()))
				return false;

			if(getEffected().isBuffImmune() && !isOffensive() || getEffected().isDebuffImmune() && isOffensive())
			{
				for(Abnormal abnormal : getEffected().getAbnormalList())
				{
					if(abnormal.checkDebuffImmunity())
						break;
				}

				if(!isHidden() && !getSkill().isHideStartMessage())
				{
					if(getEffected() == aimingTarget)
					{
						getEffector().sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(getEffected()).addSkillName(getSkill().getDisplayId(), getSkill().getDisplayLevel()));
						getEffector().sendPacket(new ExMagicAttackInfo(getEffector().getObjectId(), getEffected().getObjectId(), ExMagicAttackInfo.RESISTED));
					}
				}
				return false;
			}
		}

		if(!getEffected().getAbnormalList().add(this))
			return false;

		if(!isHidden() && !getSkill().isHideStartMessage())
			getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(getDisplayId(), getDisplayLevel()));

		return true;
	}

	/**
	 * Переводит эффект в "фоновый" режим, эффект может быть запущен методом schedule
	 */
	public void suspend()
	{
		// Эффект создан, запускаем задачу в фоне
		if(setState(STARTING, SUSPENDED))
			startEffectTask();
		else if(setState(ACTING, SUSPENDED))
		{
			synchronized (this)
			{
				onExit();
			}
		}
	}

	/**
	 * Запускает задачу эффекта, в случае если эффект успешно добавлен в список
	 */
	public void start()
	{
		if(setState(SUSPENDED, ACTING))
		{
			synchronized (this)
			{
				onStart();
			}
		}
		else if(setState(STARTING, ACTING))
		{
			synchronized (this)
			{
				onStart();
				startEffectTask();
			}
		}
	}

	@Override
	public void run()
	{
		_timeLeft--;

		if(getState() == SUSPENDED)
		{
			if(isTimeLeft())
				return;

			exit();
			return;
		}

		boolean successActing = true;

		if(getState() == ACTING)
		{
			if(isTimeLeft())
			{
				if(checkActingCondition())
				{
					for(EffectHandler effect : getEffects())
					{
						if((getTimeLeft() % effect.getInterval()) == 0)
						{
							successActing = effect.onActionTime(this, getEffector(), getEffected());
							if(!successActing)
								break;
						}
					}

					if(successActing)
						return;
				}
			}
		}

		if(getDuration() == Integer.MAX_VALUE) // Если вдруг закончится время у безконечного эффекта.
		{
			if(checkActingCondition())
			{
				for(EffectHandler effect : getEffects())
				{
					if((getDuration() % effect.getInterval()) == 0)
					{
						successActing = effect.onActionTime(this, getEffector(), getEffected());
						if(!successActing)
							break;
					}
				}

				if(successActing)
				{
					_timeLeft = getDuration();
					return;
				}
			}
		}

		if(setState(ACTING, FINISHED))
		{
			if(checkActingCondition())
			{
				for(EffectHandler effect : getEffects())
				{
					if((getDuration() % effect.getInterval()) == 0)
						effect.onActionTime(this, getEffector(), getEffected());
				}
			}

			synchronized(this)
			{
				stopEffectTask();
				onExit();
			}

			boolean lastEffect = getEffected().getAbnormalList().getCount(getSkill()) == 1;
			boolean msg = successActing && !isHidden() && lastEffect;

			getEffected().getAbnormalList().remove(this);

			// Отображать сообщение только для последнего оставшегося эффекта скилла
			if(msg)
				getEffected().sendPacket(new SystemMessage(SystemMessage.S1_HAS_WORN_OFF).addSkillName(getDisplayId(), getDisplayLevel()));

			if(lastEffect)
				getSkill().onAbnormalTimeEnd(getEffector(), getEffected());

			//tigger on finish
			for(EffectHandler effect : getEffects())
				getEffected().useTriggers(getEffected(), TriggerType.ON_FINISH_EFFECT, null, getSkill(), effect.getTemplate(), 0);
		}
	}

	/**
	 * Завершает эффект и все связанные, удаляет эффект из списка эффектов
	 */
	public void exit()
	{
		//Эффект запланирован на запуск, удаляем
		if(setState(STARTING, FINISHED))
			getEffected().getAbnormalList().remove(this);
		//Эффект работает в "фоне", останавливаем задачу в планировщике
		else if(setState(SUSPENDED, FINISHED))
			stopEffectTask();
		else if(setState(ACTING, FINISHED))
		{
			synchronized (this)
			{
				stopEffectTask();
				onExit();
			}
			getEffected().getAbnormalList().remove(this);
		}
	}

	public void addIcon(AbnormalStatusUpdatePacket abnormalStatus)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		abnormalStatus.addEffect(getDisplayId(), getDisplayLevel(), getAbnormalType().getClientId(), duration);
	}

	public void addIcon(ExAbnormalStatusUpdateFromTargetPacket abnormalStatus)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		abnormalStatus.addEffect(getEffector().getObjectId(), getDisplayId(), getDisplayLevel(), getAbnormalType().getClientId(), duration);
	}

	public void addPartySpelledIcon(PartySpelledPacket ps)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		ps.addPartySpelledEffect(getDisplayId(), getDisplayLevel(), getAbnormalType().getClientId(), duration);
	}

	public void addOlympiadSpelledIcon(Player player, ExOlympiadSpelledInfoPacket os)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		os.addSpellRecivedPlayer(player);
		os.addEffect(getDisplayId(), getDisplayLevel(), getAbnormalType().getClientId(), duration);
	}

	@Override
	public int compareTo(Abnormal obj)
	{
		if(obj.equals(this))
			return 0;
		return 1;
	}

	public boolean isCancelable()
	{
		return getSkill().isCancelable() && !isHidden();
	}

	public boolean isSelfDispellable()
	{
		return getSkill().isSelfDispellable() && !isHidden();
	}

	public int getId()
	{
		return getSkill().getId();
	}

	public int getLevel()
	{
		return getSkill().getLevel();
	}

	public int getDisplayId()
	{
		return getSkill().getDisplayId();
	}

	public int getDisplayLevel()
	{
		return getSkill().getDisplayLevel();
	}

	@Override
	public String toString()
	{
		return "Skill: " + getSkill() + ", state: " + getState() + ", active : " + isActive();
	}

	// TODO Переделать
	public boolean checkBlockedAbnormalType(AbnormalType abnormal)
	{
		for(EffectHandler effect : getEffects())
		{
			if(effect.checkBlockedAbnormalType(this, getEffector(), getEffected(), abnormal))
				return true;
		}
		return false;
	}

	// TODO Переделать
	public boolean checkDebuffImmunity()
	{
		for(EffectHandler effect : getEffects())
		{
			if(effect.checkDebuffImmunity(this, getEffector(), getEffected()))
				return true;
		}
		return false;
	}

	// TODO Переделать
	public boolean isHidden()
	{
		if(getDisplayId() < 0)
			return true;
		for(EffectHandler effect : getEffects())
		{
			if(effect.isHidden())
				return true;
		}
		return false;
	}

	// TODO Переделать
	public boolean isSaveable()
	{
		if(!_saveable || !getSkill().isSaveable() || getTimeLeft() < Config.ALT_SAVE_EFFECTS_REMAINING_TIME || isHidden())
			return false;

		for(EffectHandler effect : getEffects())
		{
			if(!effect.isSaveable())
				return false;
		}
		return true;
	}

	public EffectUseType getUseType()
	{
		return _useType;
	}

	public boolean isOfUseType(EffectUseType useType)
	{
		return _useType == useType;
	}

	public boolean isOffensive()
	{
		if(isOfUseType(EffectUseType.SELF))
			return getSkill().isSelfDebuff();
		else
			return getSkill().isDebuff();
	}

	public int getDuration()
	{
		return _duration;
	}

	public void setDuration(int value)
	{
		_duration = Math.min(Integer.MAX_VALUE, Math.max(0, value));
		_timeLeft = _duration;
	}

	public boolean isHideTime()
	{
		return getSkill().isAbnormalHideTime() || getDuration() == Integer.MAX_VALUE;
	}

	public boolean canReplaceAbnormal(Skill skill, int minLeftTime)
	{
		if(skill.isPassive())
			return false;
		if(!skill.hasEffects(EffectUseType.NORMAL))
			return false;
		if(checkBlockedAbnormalType(skill.getAbnormalType()))
			return false;
		if(!AbnormalList.checkAbnormalType(getSkill(), skill)) // такого скилла нет
			return true;
		if(getAbnormalLvl() < skill.getAbnormalLvl()) // старый слабее
			return true;
		if(getTimeLeft() > minLeftTime) // старый не слабее и еще не кончается - ждем
			return false;
		return true;
	}
}