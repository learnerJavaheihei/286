package l2s.gameserver.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.math.random.RndSelector;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.AggroList.AggroInfo;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.Stats;
//import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.NpcUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAI extends NpcAI
{
	protected static final Logger _log = LoggerFactory.getLogger(DefaultAI.class);

	public static enum TaskType
	{
		MOVE,
		ATTACK,
		CAST,
		BUFF
	}

	public static final int TaskDefaultWeight = 10000;

	public static class Task
	{
		public TaskType type;
		public SkillEntry skillEntry;
		public HardReference<? extends Creature> target;
		public Location loc;
		public boolean pathfind;
		public boolean teleportIfCantMove;
		public long weight = TaskDefaultWeight;
		public int p1;
		public int p2;
		public final long addTime;

		public Task()
		{
			addTime = System.currentTimeMillis();
		}
	}

	@Override
	public void addTaskCast(Creature target, SkillEntry skillEntry)
	{
		Task task = new Task();
		task.type = TaskType.CAST;
		task.target = target.getRef();
		task.skillEntry = skillEntry;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskBuff(Creature target, SkillEntry skillEntry)
	{
		Task task = new Task();
		task.type = TaskType.BUFF;
		task.target = target.getRef();
		task.skillEntry = skillEntry;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskAttack(Creature target, SkillEntry skillEntry, int weight)
	{
		Task task = new Task();
		task.type = skillEntry == null ? TaskType.ATTACK : (skillEntry.getTemplate().isDebuff() ? TaskType.CAST : TaskType.BUFF);
		task.target = target.getRef();
		task.skillEntry = skillEntry;
		task.weight = weight;
		if(skillEntry != null)
			_globalAggro = 0;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskMove(Location loc, boolean pathfind, boolean teleportIfCantMove)
	{
		Task task = new Task();
		task.type = TaskType.MOVE;
		task.loc = loc;
		task.pathfind = pathfind;
		task.teleportIfCantMove = teleportIfCantMove;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addUseSkillDesire(Creature target, SkillEntry skillEntry, int p1, int p2, long desire)
	{
		Task task = new Task();
		task.type = skillEntry.getTemplate().isDebuff() ? TaskType.CAST : TaskType.BUFF;
		task.target = target.getRef();
		task.skillEntry = skillEntry;
		task.p1 = p1;
		task.p2 = p2;
		task.weight = desire;
		_globalAggro = 0;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addAttackDesire(Creature target, int p1, long desire)
	{
		Task task = new Task();
		task.type = TaskType.ATTACK;
		task.target = target.getRef();
		task.p1 = p1;
		task.weight = desire;
		_globalAggro = 0;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addMoveAroundDesire(int p1, long desire)
	{
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !Rnd.chance(p1))
			return;

		Task task = new Task();
		task.type = TaskType.MOVE;
		task.p1 = p1;

		Location loc = getActor().getSpawnedLoc();
		if(loc == null)
			loc = getActor().getLoc();

		task.loc = Location.findPointToStay(loc, 50, 150, getActor().getGeoIndex());
		task.pathfind = true;
		task.weight = desire;
		_tasks.add(task);
		_def_think = true;
	}

	private static class TaskComparator implements Comparator<Task>
	{
		private static final Comparator<Task> instance = new TaskComparator();

		public static final Comparator<Task> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(Task o1, Task o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			if(o1.weight == o2.weight)
				return Long.compare(o2.addTime, o1.addTime);
			return Long.compare(o2.weight, o1.weight);
		}
	}

	public static class NearestTargetComparator implements Comparator<Creature>
	{
		private final HardReference<? extends Creature> _creatureRef;

		public NearestTargetComparator(Creature creature)
		{
			_creatureRef = creature.getRef();
		}

		@Override
		public int compare(Creature o1, Creature o2)
		{
			if(o1 == null || o2 == null)
				return 0;

			if(o1 == o2)
				return 0;

			if(o1.getObjectId() == o2.getObjectId())
				return 0;

			Creature creature = _creatureRef.get();
			if(creature == null)
				return 0;

			return Integer.compare(creature.getDistance3D(o1), creature.getDistance3D(o2));
		}
	}

	protected class Teleport implements Runnable
	{
		Location _destination;

		public Teleport(Location destination)
		{
			_destination = destination;
		}

		@Override
		public void run()
		{
			clientStopMoving();
			_pathfindFails = 0;

			getActor().teleToLocation(_destination.x, _destination.y, GeoEngine.getLowerHeight(_destination, getActor().getGeoIndex()));
		}
	}

	protected class RunningTask implements Runnable
	{
		@Override
		public void run()
		{
			getActor().setRunning();
			_runningTask = null;
		}
	}

	protected class MadnessTask implements Runnable
	{
		@Override
		public void run()
		{
			getActor().getFlags().getConfused().stop();
			_madnessTask = null;
		}
	}

	public final int MAX_HATE_RANGE = 2000;

	private int _maxPursueRange;

	protected ScheduledFuture<?> _runningTask;
	protected ScheduledFuture<?> _madnessTask;

	/** Показывает, есть ли задания */
	protected boolean _def_think = false;

	/** The L2NpcInstance aggro counter */
	protected long _globalAggro;

	protected long _randomAnimationEnd;
	protected int _pathfindFails;

	/** Список заданий */
	protected final NavigableSet<Task> _tasks = new ConcurrentSkipListSet<Task>(TaskComparator.getInstance());

	protected final Skill[] _damSkills, _dotSkills, _debuffSkills, _healSkills, _buffSkills, _stunSkills;

	protected long _checkAggroTimestamp = 0;

	protected long _lastFactionNotifyTime = 0;
	protected final long _minFactionNotifyInterval;

	protected final Comparator<Creature> _nearestTargetComparator;

	private ScheduledFuture<?> _followTask;

	protected Object _intention_arg0 = null, _intention_arg1 = null;

	private static final int MAX_PATHFIND_FAILS = 3;
	private static final int TELEPORT_TIMEOUT = 10000;
	private static final int MAX_ATTACK_TIMEOUT = 15000;

	private boolean _isSearchingMaster;

	private boolean _canRestoreOnReturnHome;

	private long _lastRaidPvpZoneCheck;

	private Location _lastLeaderPos = null;

	public DefaultAI(NpcInstance actor)
	{
		super(actor);

		_damSkills = actor.getTemplate().getDamageSkills();
		_dotSkills = actor.getTemplate().getDotSkills();
		_debuffSkills = actor.getTemplate().getDebuffSkills();
		_buffSkills = actor.getTemplate().getBuffSkills();
		_stunSkills = actor.getTemplate().getStunSkills();
		_healSkills = actor.getTemplate().getHealSkills();

		_nearestTargetComparator = new NearestTargetComparator(actor);

		// Preload some AI params
		_maxPursueRange = actor.getParameter("max_pursue_range", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : actor.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : Config.MAX_PURSUE_RANGE);
		_minFactionNotifyInterval = actor.getParameter("FactionNotifyInterval", 1000);
		_isSearchingMaster = actor.getParameter("searchingMaster", false);
		_canRestoreOnReturnHome = actor.getParameter("restore_on_return_home", actor.isRaid() && !actor.isBoss());
	}

	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		super.changeIntention(intention, arg0, arg1);
		_intention_arg0 = arg0;
		_intention_arg1 = arg1;
	}

	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention_arg0 = null;
		_intention_arg1 = null;
		super.setIntention(intention, arg0, arg1);
	}

	/**
	 * Определяет, может ли этот тип АИ видеть персонажей в режиме Silent Move.
	 * @param target L2Playable цель
	 * @return true если цель видна в режиме Silent Move
	 */
	protected boolean canSeeInSilentMove(Playable target)
	{
		if(getActor().getParameter("canSeeInSilentMove", false))
			return true;
		return !target.isSilentMoving();
	}

	protected boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE && getIntention() != CtrlIntention.AI_INTENTION_WALKER_ROUTE || !isGlobalAggro())
			return false;

		if(target.isAlikeDead())
			return false;

		if(!target.isTargetable(actor))
			return false;

		if(target.isPlayable())
		{
			if(!canSeeInSilentMove((Playable) target))
				return false;
			if(actor.getFaction().containsName("varka_silenos_clan") && target.getPlayer().getVarka() > 0)
				return false;
			if(actor.getFaction().containsName("ketra_orc_clan") && target.getPlayer().getKetra() > 0)
				return false;
			/*if(target.getMovement().isFollow() && !target.isPlayer() && target.getMovement().getFollowTarget() != null && target.getMovement().getFollowTarget().isPlayer())
					return;*/
			//if(actor.isMonster() && target.isInPeaceZone())
				//return false;
			if(((Playable) target).isInNonAggroTime())
				return false;
			if(target.isPlayer())
			{
				Player player = target.getPlayer();
				if(player.isGMInvisible())
					return false;
				if(player.isInAwayingMode() && !Config.AWAY_PLAYER_TAKE_AGGRO)
					return false;
				if(!player.isActive())
					return false;
				if(actor.isMonster() || actor instanceof DecoyInstance)
				{
					if(player.isInStoreMode() || player.isInOfflineMode())
						return false;
				}
			}
		}

		if(!isInAggroRange(target))
			return false;
		if(!canAttackCharacter(target))
			return false;
		if(!GeoEngine.canSeeTarget(actor, target))
			return false;

		return true;
	}

	protected boolean isInAggroRange(Creature target)
	{
		NpcInstance actor = getActor();
		AggroInfo ai = actor.getAggroList().get(target);
		if(ai != null && ai.hate > 0)
		{
			if(!target.isInRangeZ(actor.getSpawnedLoc(), getMaxHateRange()))
				return false;
		}
		else if(!isAggressive() || !target.isInRangeZ(actor.getSpawnedLoc(), getAggroRange()))
			return false;

		return true;
	}

	protected void setIsInRandomAnimation(long time)
	{
		_randomAnimationEnd = System.currentTimeMillis() + time;
	}

	protected boolean randomAnimation()
	{
		if(isHaveRandomActions())
			return false;

		NpcInstance actor = getActor();

		if(actor.getParameter("noRandomAnimation", false))
			return false;

		if(actor.hasRandomAnimation() && !actor.isActionsDisabled() && !actor.getMovement().isMoving() && !actor.isInCombat() && Rnd.chance(Config.RND_ANIMATION_RATE) && !actor.isKnockDowned() && !actor.isKnockBacked() && !actor.isFlyUp())
		{
			setIsInRandomAnimation(3000);
			actor.onRandomAnimation();
			return true;
		}
		return false;
	}

	protected boolean randomWalk()
	{
		return !getActor().getMovement().isMoving() && maybeMoveToHome(false);
	}

	/**
	 * @return true если действие выполнено, false если нет
	 */
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isMinion())
		{
			// Миньон удаляется при переходе в активный режим, если лидер мертв (оффлайк).
			NpcInstance leader = actor.getLeader();
			if(leader != null && !leader.isVisible())
			{
				actor.deleteMe();
				return false;
			}
		}

		if(super.thinkActive())
			return true;

		if(actor.isActionsDisabled())
			return true;

		if(_randomAnimationEnd > System.currentTimeMillis())
			return true;

		if(_def_think)
		{
			if(doTask())
				clearTasks();
			return true;
		}

		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;

			boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", isAggressive() ? 100 : 0));
			int radius = !actor.getAggroList().isEmpty() ? getMaxPursueRange() : (aggressive ? (getAggroRange() > 0 ? getAggroRange() : 1000) : 0);
			if(radius > 0)
			{
				List<Creature> targets = World.getAroundAttackableCreatures(actor, radius, 250);
				try
				{
					Collections.sort(targets, _nearestTargetComparator);
				}
				catch(Exception e)
				{
					// Заглушка против глюка явы: Comparison method violates its general contract!
				}

				for(Creature target : targets)
				{
					if(aggressive || actor.getAggroList().get(target) != null)
					{
						if(checkAggression(target))
						{
							notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 2);
							return true;
						}
					}
				}
			}
		}

		if(actor.isMinion() && actor.isSpawnLeaderDepends())
		{
			NpcInstance leader = actor.getLeader();
			if(leader != null)
			{
				Location leaderPos = leader.getLoc();
				if(actor.getDistance(leader) > getMaxPursueRange() || !GeoEngine.canSeeTarget(actor, leader))
				{
					_lastLeaderPos = leaderPos;
					actor.teleToLocation(leader.getRndMinionPosition());
					return true;
				}

				if((_lastLeaderPos == null || !_lastLeaderPos.equals(leader.getLoc())))
				{
					_lastLeaderPos = leaderPos;
					addTaskMove(leader.getRndMinionPosition(), true, false);
					return true;
				}
			}
		}

		notifyEvent(CtrlEvent.EVT_NO_DESIRE);
		return true;
	}

	@Override
	protected void onEvtNoDesire()
	{
		if(randomAnimation())
			return;

		randomWalk();
	}

	@Override
	protected void onIntentionIdle()
	{
		NpcInstance actor = getActor();

		// Удаляем все задания
		clearTasks();

		actor.getMovement().stopMove();
		actor.getAggroList().clear(true);
		setAttackTarget(null);

		super.onIntentionIdle();
	}

	@Override
	protected void onIntentionActive()
	{
		NpcInstance actor = getActor();

		actor.getMovement().stopMove();
		actor.setLastAttackTime(-1);

		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		{
			switchAITask(_activeAITaskDelay);
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}

		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		NpcInstance actor = getActor();

		// Удаляем все задания
		clearTasks();

		actor.getMovement().stopMove();
		setAttackTarget(target);
		setGlobalAggro(0);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			switchAITask(_attackAITaskDelay);
		}

		onEvtThink();
	}

	@Override
	public boolean canAttackCharacter(Creature target)
	{
		return getActor().canAttackCharacter(target);
	}

	protected boolean isAggressive()
	{
		return getActor().isAggressive();
	}

	protected int getAggroRange()
	{
		return getActor().getAggroRange();
	}

	protected boolean checkTarget(Creature target, int range)
	{
		NpcInstance actor = getActor();
		if(target == null || actor == target || target.isAlikeDead() || !actor.isInRangeZ(target, range) || !target.isTargetable(actor))
			return false;

		if(target.isPlayable() && ((Playable) target).isInNonAggroTime())
			return false;

		// если не видим чаров в хайде - не атакуем их
		final boolean hidden = target.isInvisible(actor);

		if(!hidden && actor.isConfused())
			return true;

		//В состоянии атаки атакуем всех, на кого у нас есть хейт
		if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			if(canAttackCharacter(target) || target.getAI().canAttackCharacter(actor))
			{
				AggroInfo ai = actor.getAggroList().get(target);
				if(ai != null)
				{
					if(hidden)
					{
						ai.hate = 0; // очищаем хейт
						return false;
					}
					return ai.hate > 0;
				}
				return false;
			}
		}

		if(hidden)
			return false;

		return canAttackCharacter(target);
	}

	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;

		if(!actor.isInRange(actor.getSpawnedLoc(), getMaxPursueRange()))
		{
			returnHomeAndRestore(actor.isRunning());
			return;
		}

		if(!actor.isRunning() && _runningTask == null) // Если моб не бежит атаковать, то меняем ходьбу на бег.
			actor.setRunning();

		if(doTask())
		{
			if(!actor.isAttackingNow() && !actor.isCastingNow())
			{
				if(!createNewTask())
				{
					if(actor.getLastAttackTime() > 0) {
						if(System.currentTimeMillis() > (actor.getLastAttackTime() + MAX_ATTACK_TIMEOUT))
							returnHome(false);
					}
					else {
						changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					}
				}
			}
		}
	}

	@Override
	protected void onEvtSpawn()
	{
		setGlobalAggro(System.currentTimeMillis() + getActor().getParameter("globalAggro", 10000L));

		if(getActor().isMinion() && getActor().getLeader() != null)
			_isGlobal = getActor().getLeader().getAI().isGlobalAI();
	}

	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrived()
	{
		onEvtThink();
		super.onEvtArrived();
	}

	protected boolean tryMoveToTarget(Creature target)
	{
		return tryMoveToTarget(target, 10 + (int) getActor().getMinDistance(target));
	}

	protected boolean tryMoveToTarget(Creature target, int range)
	{
		NpcInstance actor = getActor();

		if(!actor.isInRange(actor.getSpawnedLoc(), getMaxPursueRange()))
		{
			returnHomeAndRestore(actor.isRunning());
			return false;
		}

		if(actor.getMovement().followToCharacter(target, range, true))
			return true;

		// Не гонимся за персонажем, если мы его уже не видем.
		if(!GeoEngine.canSeeTarget(actor, target))
			return false;

		_pathfindFails++;

		if(_pathfindFails >= getMaxPathfindFails() && (System.currentTimeMillis() > (actor.getLastAttackTime() + TELEPORT_TIMEOUT)))
		{
			_pathfindFails = 0;

			if(target.isPlayable())
			{
				AggroInfo hate = actor.getAggroList().get(target);
				if(hate == null || hate.hate < 100 || (!actor.getReflection().isMain() && actor.isRaid())) // bless freya
				{
					returnHome(false);
					return false;
				}
			}

			Location targetLoc = target.getLoc();
			Location loc = GeoEngine.moveCheckForAI(targetLoc, actor.getLoc(), actor.getGeoIndex());
			if(loc == null || !GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getGeoIndex())) // Для подстраховки
				loc = targetLoc;
			actor.teleToLocation(loc);
			return true;
		}

		return false;
	}

	protected boolean maybeNextTask(Task currentTask)
	{
		// Следующее задание
		_tasks.remove(currentTask);
		// Если заданий больше нет - определить новое
		if(_tasks.size() == 0)
			return true;
		return false;
	}

	protected boolean doTask()
	{
		if(!_def_think)
			return true;

		Task currentTask = _tasks.pollFirst();
		if(currentTask == null)
		{
			clearTasks();
			return true;
		}

		NpcInstance actor = getActor();
		if(actor.isDead() || actor.isAttackingNow() || actor.isCastingNow())
			return false;

		switch(currentTask.type)
		{
		// Задание "прибежать в заданные координаты"
			case MOVE:
			{
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				if(actor.isInRange(currentTask.loc, 100))
					return maybeNextTask(currentTask);

				if(actor.getMovement().isMoving())
					return false;

				if(!actor.getMovement().moveToLocation(currentTask.loc, 0, currentTask.pathfind))
				{
					if(currentTask.teleportIfCantMove) {
						actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
						ThreadPoolManager.getInstance().schedule(new Teleport(currentTask.loc), 500L);
						return false;
					} else
						return maybeNextTask(currentTask);
				}
				break;
			}
			// Задание "добежать - ударить"
			case ATTACK:
			{
				Creature target = currentTask.target.get();
				if(target == null)
					return true;

				if(!checkTarget(target, getMaxHateRange()))
					return true;

				setAttackTarget(target);

				int range = Math.max(10, actor.getPhysicalAttackRange()) + (int) actor.getMinDistance(target);
				if(actor.isInRangeZ(target, range + 32) && GeoEngine.canSeeTarget(actor, target))
				{
					if(actor.isAttackingDisabled())
						return false;

					clientStopMoving();
					_pathfindFails = 0;
					actor.doAttack(target);
					return maybeNextTask(currentTask);
				}

				if(actor.getMovement().isMoving())
					return Rnd.chance(25);

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target, range);
				break;
			}
			// Задание "добежать - атаковать скиллом"
			case CAST:
			{
				Creature target = currentTask.target.get();
				if(target == null)
					return true;

				SkillEntry skillEntry = currentTask.skillEntry;
				if(skillEntry == null)
					return true;

				Skill skill = skillEntry.getTemplate();
				if(actor.isMuted(skill) || actor.isSkillDisabled(skill) || actor.isUnActiveSkill(skill.getId()))
					return true;

				if(skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA)
				{
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(skillEntry, actor, false);
					return maybeNextTask(currentTask);
				}

				if(!checkTarget(target, getMaxHateRange()))
					return true;

				setCastTarget(target);

				int range = Math.max(10, actor.getMagicalAttackRange(skill)) + (int) actor.getMinDistance(target);
				if(actor.isInRangeZ(target, range + 32) && GeoEngine.canSeeTarget(actor, target))
				{
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(skillEntry, target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if(actor.getMovement().isMoving())
					return Rnd.chance(10);

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target, range);
				break;
			}
			// Задание "добежать - применить скилл"
			case BUFF:
			{
				Creature target = currentTask.target.get();
				if(target == null)
					return true;

				SkillEntry skillEntry = currentTask.skillEntry;
				if(skillEntry == null)
					return true;

				Skill skill = skillEntry.getTemplate();
				if(actor.isMuted(skill) || actor.isSkillDisabled(skill) || actor.isUnActiveSkill(skill.getId()))
					return true;

				if(skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF || skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA)
				{
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(skillEntry, actor, false);
					return maybeNextTask(currentTask);
				}

				if(target == null || target.isAlikeDead() || !actor.isInRange(target, 2000))
					return true;

				int range = Math.max(10, actor.getMagicalAttackRange(skill)) + (int) actor.getMinDistance(target);
				if(actor.isInRangeZ(target, range + 32) && GeoEngine.canSeeTarget(actor, target))
				{
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(skillEntry, target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if(actor.getMovement().isMoving())
					return Rnd.chance(10);

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target, range);
				break;
			}
		}

		return false;
	}

	protected boolean createNewTask()
	{
		return false;
	}

	protected boolean defaultNewTask()
	{
		clearTasks();

		NpcInstance actor = getActor();
		Creature target;
		if(actor == null || (target = prepareTarget()) == null)
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				return maybeMoveToHome(true);
			}
			return false;
		}
		return chooseTaskAndTargets(null, target, actor.getDistance(target));
	}

	@Override
	protected void onEvtThink()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		// TODO: Переделать, чтобы было в виде ПвП зоны.
		if(Config.BATTLE_ZONE_AROUND_RAID_BOSSES_RANGE > 0 && actor.isRaid())
		{
			if(System.currentTimeMillis() > (_lastRaidPvpZoneCheck + 1000))
			{
				_lastRaidPvpZoneCheck = System.currentTimeMillis();
				for(Player player : World.getAroundPlayers(actor, Config.BATTLE_ZONE_AROUND_RAID_BOSSES_RANGE, 200))
				{
					player.startPvPFlag(null);
					player.setLastPvPAttack(System.currentTimeMillis() - Config.PVP_TIME + 21000);
				}
			}
		}

		if(actor.isActionsDisabled() || actor.isAfraid())
			return;

		if(_randomAnimationEnd > System.currentTimeMillis())
			return;

		if(!_thinking.tryLock())
			return;

		try
		{
			lookNeighbor(actor.getAggroRange(), false);

			if(!Config.BLOCK_ACTIVE_TASKS && (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || getIntention() == CtrlIntention.AI_INTENTION_WALKER_ROUTE))
				thinkActive();
			else if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				thinkAttack();
			else if(getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
				thinkFollow();
			else if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
				thinkReturnHome();
		}
		finally
		{
			_thinking.unlock();
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();

		int transformer = actor.getParameter("transformOnDead", 0);
		int chance = actor.getParameter("transformChance", 100);
		if(transformer > 0 && Rnd.chance(chance))
		{
			NpcInstance npc = NpcUtils.spawnSingle(transformer, actor.getLoc(), actor.getReflection());

			if(killer != null && killer.isPlayable())
			{
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
				killer.setTarget(npc);
			}
		}

		super.onEvtDead(killer);

		notifyFriendsOnDie(killer);
	}

	@Override
	protected void onEvtClanAttacked(NpcInstance member, Creature attacker, int damage)
	{
		if(damage > 0)
		{
			if(Math.abs(attacker.getZ() - getActor().getZ()) > 400)
				return;

			notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, (int) Math.max(1, ((double) damage * 0.25 + 0.5)));
		}
	}

	@Override
	protected void onEvtClanDied(NpcInstance member, Creature killer)
	{
		//
	}

	@Override
	protected void onEvtPartyAttacked(NpcInstance minion, Creature attacker, int damage)
	{
		onEvtClanAttacked(minion, attacker, damage);
	}

	@Override
	protected void onEvtPartyDied(NpcInstance minion, Creature killer)
	{
		onEvtClanDied(minion, killer);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return;

		NpcInstance actor = getActor();
		if(attacker == null || actor.isDead())
			return;

		if(attacker.isConfused())
			return;

		Player player = attacker.getPlayer();

		if(player != null)
		{
			List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
			if(quests != null)
			{
				for(QuestState qs : quests)
					qs.getQuest().notifyAttack(actor, qs);
			}
		}

		if(damage <= 0)
			return;

		if(attacker.isInvisible(actor))
			return;

		if(!canAttackCharacter(attacker))
			return;

		Creature myTarget = attacker;
		if(attacker.isServitor())
		{
			final Player summoner = attacker.getPlayer();
			if(summoner != null)
			{
				if(_isSearchingMaster) // моб ищет и атакует хозяина саммона
					myTarget = summoner;
				else // Обычно 1 хейт добавляется хозяину суммона, чтобы после смерти суммона моб накинулся на хозяина.
					actor.getAggroList().addDamageHate(summoner, 0, 1);
			}
		}
		else if(attacker.isSymbolInstance())
			myTarget = attacker.getPlayer();

		if(myTarget == null)
			myTarget = attacker;

		//Добавляем только хейт, урон, если атакующий - игровой персонаж, будет добавлен в L2NpcInstance.onReduceCurrentHp
		actor.getAggroList().addDamageHate(myTarget, 0, (int) myTarget.getStat().calc(Stats.DAMAGE_HATE_BONUS, damage));

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			if(!actor.isRunning())
				startRunningTask(_attackAITaskDelay);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, myTarget);
		}

		if(actor.isRaid())
		{
			((RaidBossInstance) actor).startRaidBerserkTask();
		}

		if(actor.isArenaRaid())
		{
			if(actor.hasMinions())
				actor.getMinionList().spawnMinions();
		}

		notifyFriendsOnAttack(attacker, skill, damage);
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return;

		NpcInstance actor = getActor();
		if(attacker == null || actor.isDead())
			return;

		if(attacker.isConfused())
			return;

		Creature myTarget = attacker;
		if(aggro > 0)
		{
			if(attacker.isServitor())
			{
				final Player summoner = attacker.getPlayer();
				if(summoner != null)
				{
					if(_isSearchingMaster) // моб ищет и атакует хозяина саммона
						myTarget = summoner;
					else // Обычно 1 хейт добавляется хозяину суммона, чтобы после смерти суммона моб накинулся на хозяина.
						actor.getAggroList().addDamageHate(summoner, 0, 1);
				}
			}
			else if(attacker.isSymbolInstance())
				myTarget = attacker.getPlayer();
		}

		if(myTarget == null)
			myTarget = attacker;

		//Добавляем только хейт, урон, если атакующий - игровой персонаж, будет добавлен в L2NpcInstance.onReduceCurrentHp
		actor.getAggroList().addDamageHate(myTarget, 0, aggro);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			startRunningTask(_attackAITaskDelay);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, myTarget);
		}
	}

	protected boolean maybeMoveToHome(boolean force)
	{
		NpcInstance actor = getActor();
		if(actor.isDead() || actor.isMovementDisabled())
			return false;

		Location sloc = actor.getSpawnedLoc();
		boolean isInRange = actor.isInRangeZ(sloc, Config.MAX_DRIFT_RANGE);

		if(!force)
		{
			boolean randomWalk = hasRandomWalk();

			// Random walk or not?
			if(randomWalk && (!Config.RND_WALK || !Rnd.chance(Config.RND_WALK_RATE)))
				return false;


			if(!randomWalk && isInRange)
				return false;
		}

		Location pos = Location.findPointToStay(actor, sloc, 0, Config.MAX_DRIFT_RANGE);

		actor.setWalking();

		// Телепортируемся домой, только если далеко от дома
		if(!actor.getMovement().moveToLocation(pos, 0, true) && !isInRange && !(actor instanceof DecoyInstance))
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
			ThreadPoolManager.getInstance().schedule(new Teleport(sloc), 500L);
		}
		return true;
	}

	@Override
	public boolean returnHomeAndRestore(boolean running)
	{
		NpcInstance actor = getActor();
		if(returnHome(running, actor.isRaid() ? Config.ALWAYS_TELEPORT_HOME_RB : Config.ALWAYS_TELEPORT_HOME, running, true))
		{
			if(canRestoreOnReturnHome())
			{
				actor.setCurrentHpMp(actor.getMaxHp(), actor.getMaxMp());
			}
			return true;
		}
		return false;
	}

	protected boolean returnHome(boolean running)
	{
		return returnHome(true, false, running, false);
	}

	protected boolean teleportHome()
	{
		return returnHome(true, true, false, false);
	}

	protected boolean returnHome(boolean clearAggro, boolean teleport, boolean running, boolean force)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;

		if(actor.isMovementDisabled())
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			return false;
		}

		if(actor.isMinion())
		{
			// Миньон удаляется при переходе в активный режим, если лидер мертв (оффлайк).
			NpcInstance leader = actor.getLeader();
			if(leader != null && !leader.isVisible())
			{
				actor.deleteMe();
				return false;
			}
		}

		Location sloc = actor.getSpawnedLoc();
		if(actor.isInRangeZ(sloc, 32))
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			return false;
		}

		if(!teleport && getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return false;

		// Удаляем все задания
		clearTasks();
		actor.getMovement().stopMove();

		if(clearAggro)
			actor.getAggroList().clear(true);

		setAttackTarget(null);

		if(teleport)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
			ThreadPoolManager.getInstance().schedule(new Teleport(sloc), 500L);
		}
		else if(force)
		{
			setIntention(CtrlIntention.AI_INTENTION_RETURN_HOME, running);
		}
		else
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

			if(running)
				actor.setRunning();
			else
				actor.setWalking();

			addTaskMove(sloc, true, false);
		}
		return true;
	}

	@Override
	protected void onIntentionReturnHome(boolean running)
	{
		NpcInstance actor = getActor();

		if(running)
			actor.setRunning();
		else
			actor.setWalking();

		changeIntention(CtrlIntention.AI_INTENTION_RETURN_HOME, null, null);

		onEvtThink();
	}

	private void thinkReturnHome()
	{
		clearTasks();

		NpcInstance actor = getActor();
		Location spawnLoc = actor.getSpawnedLoc();
		if(actor.isInRange(spawnLoc, Math.min(getMaxPursueRange(), 100)))
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		else
		{
			addTaskMove(spawnLoc, true, true);
			doTask();
		}
	}

	protected boolean canRestoreOnReturnHome()
	{
		return _canRestoreOnReturnHome;
	}

	protected Creature prepareTarget()
	{
		NpcInstance actor = getActor();

		if(actor.isConfused())
			return getAttackTarget();

		Creature agressionTarget = actor.getAggressionTarget();
		if(agressionTarget != null)
			return agressionTarget;

		// Для "двинутых" боссов, иногда, выбираем случайную цель
		if(Rnd.chance(actor.getParameter("isMadness", 0)))
		{
			Creature randomHated = actor.getAggroList().getRandomHated(getMaxHateRange());
			if(randomHated != null && Math.abs(actor.getZ() - randomHated.getZ()) < 1000) // Не прыгаем к случайной цели если слишком большая разница Z.
			{
				setAttackTarget(randomHated);
				if(_madnessTask == null)
				{
					actor.getFlags().getConfused().start();
					_madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 10000);
				}
				return randomHated;
			}
		}

		// Новая цель исходя из агрессивности
		List<Creature> hateList = actor.getAggroList().getHateList(-1);
		Creature hated = null;
		for(Creature cha : hateList)
		{
			//Не подходит, очищаем хейт
			if(!checkTarget(cha, getMaxHateRange()))
			{
				actor.getAggroList().remove(cha, true);
				continue;
			}
			hated = cha;
			break;
		}

		if(hated != null)
		{
			setAttackTarget(hated);
			return hated;
		}

		return null;
	}

	protected boolean canUseSkill(Skill skill, Creature target, double distance)
	{
		NpcInstance actor = getActor();
		if(skill == null || skill.isNotUsedByAI())
			return false;

		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF && target != actor)
			return false;

		int castRange = skill.getAOECastRange();
		if(castRange <= 200 && distance > 200)
			return false;

		if(actor.isSkillDisabled(skill) || actor.isMuted(skill) || actor.isUnActiveSkill(skill.getId()))
			return false;

		double mpConsume2 = skill.getMpConsume2();
		if(skill.isMagic())
			mpConsume2 = actor.getStat().calc(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, skill);
		else
			mpConsume2 = actor.getStat().calc(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, skill);
		if(actor.getCurrentMp() < mpConsume2)
			return false;

		if(target.getAbnormalList().contains(skill.getId()))
			return false;

		return true;
	}

	protected boolean canUseSkill(Skill sk, Creature target)
	{
		return canUseSkill(sk, target, 0);
	}

	protected Skill[] selectUsableSkills(Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return null;

		Skill[] ret = null;
		int usable = 0;

		for(Skill skill : skills)
			if(canUseSkill(skill, target, distance))
			{
				if(ret == null)
					ret = new Skill[skills.length];
				ret[usable++] = skill;
			}

		if(ret == null || usable == skills.length)
			return ret;

		if(usable == 0)
			return null;

		ret = Arrays.copyOf(ret, usable);
		return ret;
	}

	protected static SkillEntry selectTopSkillByDamage(Creature actor, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		if(skills.length == 1)
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, skills[0]);

		Skill oneTargetSkill = null;
		for(Skill skill : skills)
		{
			if(skill.oneTarget())
			{
				if(oneTargetSkill == null || skill.getCastRange() >= distance && (distance / oneTargetSkill.getCastRange()) < (distance / skill.getCastRange()))
					oneTargetSkill = skill;
			}
		}

		if(oneTargetSkill != null && oneTargetSkill.getCastRange() > 300 && distance < 200)
			oneTargetSkill = null;

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);

		double weight;
		for(Skill skill : skills)
		{
			if(!skill.oneTarget())
			{
				weight = skill.getSimpleDamage(actor, target) / 10 + (distance / skill.getCastRange() * 100);
				if(weight < 1.)
					weight = 1.;
				rnd.add(skill, (int) weight);
			}
		}

		Skill aoeSkill = rnd.select();

		if(aoeSkill == null)
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, oneTargetSkill);

		if(oneTargetSkill == null)
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, aoeSkill);

		if(Rnd.chance(90))
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, oneTargetSkill);
		else
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, aoeSkill);
	}

	protected static SkillEntry selectTopSkillByDebuff(Creature actor, Creature target, double distance, Skill[] skills) //FIXME
	{
		if(skills == null || skills.length == 0)
			return null;

		if(skills.length == 1)
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, skills[0]);

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		double weight;
		for(Skill skill : skills)
		{
			if(skill.getSameByAbnormalType(target) != null)
				continue;
			if((weight = 100. * skill.getAOECastRange() / distance) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return SkillEntry.makeSkillEntry(SkillEntryType.NONE, rnd.select());
	}

	protected static SkillEntry selectTopSkillByBuff(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		if(skills.length == 1)
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, skills[0]);

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		double weight;
		for(Skill skill : skills)
		{
			if(skill.getSameByAbnormalType(target) != null)
				continue;
			if((weight = skill.getPower()) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return SkillEntry.makeSkillEntry(SkillEntryType.NONE, rnd.select());
	}

	protected static SkillEntry selectTopSkillByHeal(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		double hpReduced = target.getMaxHp() - target.getCurrentHp();
		if(hpReduced < 1)
			return null;

		if(skills.length == 1)
			return SkillEntry.makeSkillEntry(SkillEntryType.NONE, skills[0]);

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		double weight;
		for(Skill skill : skills)
		{
			if((weight = Math.abs(skill.getPower() - hpReduced)) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return SkillEntry.makeSkillEntry(SkillEntryType.NONE, rnd.select());
	}

	protected void addDesiredSkill(Map<SkillEntry, Integer> skillMap, Creature target, double distance, SkillEntry[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return;
		for(SkillEntry sk : skills)
			addDesiredSkill(skillMap, target, distance, sk);
	}

	protected void addDesiredSkill(Map<SkillEntry, Integer> skillMap, Creature target, double distance, SkillEntry skillEntry)
	{
		if(skillEntry == null || target == null)
			return;
		Skill skill = skillEntry.getTemplate();
		if(!canUseSkill(skill, target))
			return;
		int weight = (int) -Math.abs(skill.getAOECastRange() - distance);
		if(skill.getAOECastRange() >= distance)
			weight += 1000000;
		else if(skill.isNotTargetAoE() && skill.getTargets(skillEntry, getActor(), target, false).size() == 0)
			return;
		skillMap.put(skillEntry, weight);
	}

	protected void addDesiredHeal(Map<SkillEntry, Integer> skillMap, SkillEntry[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		NpcInstance actor = getActor();
		double hpReduced = actor.getMaxHp() - actor.getCurrentHp();
		double hpPercent = actor.getCurrentHpPercents();
		if(hpReduced < 1)
			return;
		int weight;
		for(SkillEntry sk : skills) {
			Skill skill = sk.getTemplate();
			if (canUseSkill(skill, actor) && skill.getPower() <= hpReduced) {
				weight = (int) skill.getPower();
				if (hpPercent < 50)
					weight += 1000000;
				skillMap.put(sk, weight);
			}
		}
	}

	protected void addDesiredBuff(Map<SkillEntry, Integer> skillMap, SkillEntry[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		NpcInstance actor = getActor();
		for(SkillEntry sk : skills)
			if(canUseSkill(sk.getTemplate(), actor))
				skillMap.put(sk, 1000000);
	}

	protected SkillEntry selectTopSkill(Map<SkillEntry, Integer> skillMap)
	{
		if(skillMap == null || skillMap.isEmpty())
			return null;
		int nWeight, topWeight = Integer.MIN_VALUE;
		for(SkillEntry next : skillMap.keySet())
			if((nWeight = skillMap.get(next)) > topWeight)
				topWeight = nWeight;
		if(topWeight == Integer.MIN_VALUE)
			return null;

		SkillEntry[] skills = new SkillEntry[skillMap.size()];
		nWeight = 0;
		for(Map.Entry<SkillEntry, Integer> e : skillMap.entrySet())
		{
			if(e.getValue() < topWeight)
				continue;
			skills[nWeight++] = e.getKey();
		}
		return skills[Rnd.get(nWeight)];
	}

	protected boolean chooseTaskAndTargets(SkillEntry skillEntry, Creature target, double distance)
	{
		NpcInstance actor = getActor();

		// Использовать скилл если можно, иначе атаковать
		if(skillEntry != null)
		{
			Skill skill = skillEntry.getTemplate();
			// Проверка цели, и смена если необходимо
			if(actor.isMovementDisabled() && distance > skill.getAOECastRange() + 60)
			{
				target = null;
				if(skill.isDebuff())
				{
					LazyArrayList<Creature> targets = LazyArrayList.newInstance();
					for(Creature cha : actor.getAggroList().getHateList(getMaxHateRange()))
					{
						if(!checkTarget(cha, skill.getAOECastRange() + 60) || !canUseSkill(skill, cha))
							continue;
						targets.add(cha);
					}
					if(!targets.isEmpty())
						target = targets.get(Rnd.get(targets.size()));
					LazyArrayList.recycle(targets);
				}
			}

			if(target == null)
				return false;

			// Добавить новое задание
			if(skill.isDebuff())
				addTaskCast(target, skillEntry);
			else
				addTaskBuff(target, skillEntry);
			return true;
		}

		// Смена цели, если необходимо
		if(actor.isMovementDisabled() && distance > actor.getPhysicalAttackRange() + 32)
		{
			target = null;
			LazyArrayList<Creature> targets = LazyArrayList.newInstance();
			for(Creature cha : actor.getAggroList().getHateList(getMaxHateRange()))
			{
				if(!checkTarget(cha, actor.getPhysicalAttackRange() + 32))
					continue;
				targets.add(cha);
			}
			if(!targets.isEmpty())
				target = targets.get(Rnd.get(targets.size()));
			LazyArrayList.recycle(targets);
		}

		if(target == null)
			return false;

		// Добавить новое задание
		addTaskAttack(target);
		return true;
	}

	protected void clearTasks()
	{
		_def_think = false;
		_tasks.clear();
	}

	/** переход в режим бега через определенный интервал времени */
	protected void startRunningTask(long interval)
	{
		NpcInstance actor = getActor();
		if(_runningTask == null && !actor.isRunning())
			_runningTask = ThreadPoolManager.getInstance().schedule(new RunningTask(), interval);
	}

	protected boolean isGlobalAggro()
	{
		if(_globalAggro == 0)
			return true;
		if(_globalAggro <= System.currentTimeMillis())
		{
			_globalAggro = 0;
			return true;
		}
		return false;
	}

	public void setGlobalAggro(long value)
	{
		_globalAggro = value;
	}

	protected boolean defaultThinkBuff(int rateSelf)
	{
		return defaultThinkBuff(rateSelf, 0);
	}

	/**
	 * Оповестить дружественные цели об атаке.
	 * @param attacker
	 * @param damage
	 */
	protected void notifyFriendsOnAttack(Creature attacker, Skill skill, int damage)
	{
		if(damage <= 0)
			return;

		NpcInstance actor = getActor();
		if((System.currentTimeMillis() - _lastFactionNotifyTime) > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();

			//Оповестить социальных мобов
			for(NpcInstance npc : activeFactionTargets())
				npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, actor, attacker, damage);
		}

		if(actor.isMinion())
		{
			//Оповестить лидера об атаке
			NpcInstance master = actor.getLeader();
			if(master != null)
			{
				if(!master.isDead() && master.isVisible())
					master.getAI().notifyEvent(CtrlEvent.EVT_PARTY_ATTACKED, actor, attacker, damage);

				//Оповестить минионов лидера об атаке
				if(master.hasMinions())
				{
					for(NpcInstance minion : master.getMinionList().getAliveMinions())
					{
						if(minion != actor)
							minion.getAI().notifyEvent(CtrlEvent.EVT_PARTY_ATTACKED, actor, attacker, damage);
					}
				}
			}
		}

		//Оповестить своих минионов об атаке
		if(actor.hasMinions())
		{
			for(NpcInstance minion : actor.getMinionList().getAliveMinions())
				minion.getAI().notifyEvent(CtrlEvent.EVT_PARTY_ATTACKED, actor, attacker, damage);
		}
	}

	protected void notifyFriendsOnDie(Creature killer)
	{
		NpcInstance actor = getActor();

		//Оповестить социальных мобов
		for(NpcInstance npc : activeFactionTargets())
			npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_DIED, actor, killer);

		if(actor.isMinion())
		{
			//Оповестить лидера об атаке
			NpcInstance master = actor.getLeader();
			if(master != null)
			{
				if(!master.isDead() && master.isVisible())
					master.getAI().notifyEvent(CtrlEvent.EVT_PARTY_DIED, actor, killer);

				//Оповестить минионов лидера об атаке
				if(master.hasMinions())
				{
					for(NpcInstance minion : master.getMinionList().getAliveMinions())
					{
						if(minion != actor)
							minion.getAI().notifyEvent(CtrlEvent.EVT_PARTY_DIED, actor, killer);
					}
				}
			}
		}

		//Оповестить своих минионов об атаке
		if(actor.hasMinions())
		{
			for(NpcInstance minion : actor.getMinionList().getAliveMinions())
				minion.getAI().notifyEvent(CtrlEvent.EVT_PARTY_DIED, actor, killer);
		}
	}

	protected List<NpcInstance> activeFactionTargets()
	{
		NpcInstance actor = getActor();
		if(actor.getFaction().isNone())
			return Collections.emptyList();

		final int range = actor.getFaction().getRange();
		List<NpcInstance> npcFriends = new LazyArrayList<NpcInstance>();
		for(NpcInstance npc : World.getAroundNpc(actor))
		{
			if(!npc.isDead())
			{
				if(npc.isInRangeZ(actor, range))
				{
					if(npc.isInFaction(actor))
						npcFriends.add(npc);
				}
			}
		}
		return npcFriends;
	}

	protected boolean defaultThinkBuff(int rateSelf, int rateFriends)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		//TODO сделать более разумный выбор баффа, сначала выбирать подходящие а потом уже рандомно 1 из них
		if(Rnd.chance(rateSelf))
		{
			double actorHp = actor.getCurrentHpPercents();

			Skill[] skills = actorHp < 50 ? selectUsableSkills(actor, 0, _healSkills) : selectUsableSkills(actor, 0, _buffSkills);
			if(skills == null || skills.length == 0)
				return false;

			Skill skill = skills[Rnd.get(skills.length)];
			addTaskBuff(actor, SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill));
			return true;
		}

		if(Rnd.chance(rateFriends))
		{
			for(NpcInstance npc : activeFactionTargets())
			{
				double targetHp = npc.getCurrentHpPercents();

				Skill[] skills = targetHp < 50 ? selectUsableSkills(actor, 0, _healSkills) : selectUsableSkills(actor, 0, _buffSkills);
				if(skills == null || skills.length == 0)
					continue;

				Skill skill = skills[Rnd.get(skills.length)];
				addTaskBuff(actor, SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill));
				return true;
			}
		}

		return false;
	}

	protected boolean defaultFightTask()
	{
		clearTasks();

		NpcInstance actor = getActor();
		if(actor.isDead() || actor.isAMuted())
			return false;

		Creature target;
		if((target = prepareTarget()) == null)
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				return maybeMoveToHome(true);
			}
			return false;
		}

		double distance = actor.getDistance(target);
		double targetHp = target.getCurrentHpPercents();
		double actorHp = actor.getCurrentHpPercents();

		Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
		Skill[] dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
		Skill[] debuff = targetHp > 10 ? Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null : null;
		Skill[] stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
		Skill[] heal = actorHp < 50 ? Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0, _healSkills) : null : null;
		Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0, _buffSkills) : null;

		RndSelector<Skill[]> rnd = new RndSelector<Skill[]>();
		if(!actor.isAMuted())
			rnd.add(null, getRatePHYS());
		rnd.add(dam, getRateDAM());
		rnd.add(dot, getRateDOT());
		rnd.add(debuff, getRateDEBUFF());
		rnd.add(heal, getRateHEAL());
		rnd.add(buff, getRateBUFF());
		rnd.add(stun, getRateSTUN());

		Skill[] selected = rnd.select();
		if(selected != null)
		{
			if(selected == dam || selected == dot)
				return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);

			if(selected == debuff || selected == stun)
				return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);

			if(selected == buff)
				return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);

			if(selected == heal)
				return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
		}

		// TODO сделать лечение и баф дружественных целей

		return chooseTaskAndTargets(null, target, distance);
	}

	public int getRatePHYS()
	{
		return 100;
	}

	public int getRateDOT()
	{
		return 0;
	}

	public int getRateDEBUFF()
	{
		return 0;
	}

	public int getRateDAM()
	{
		return 0;
	}

	public int getRateSTUN()
	{
		return 0;
	}

	public int getRateBUFF()
	{
		return 0;
	}

	public int getRateHEAL()
	{
		return 0;
	}

	public boolean getIsMobile()
	{
		return !getActor().getParameter("isImmobilized", false);
	}

	public int getMaxPathfindFails()
	{
		return MAX_PATHFIND_FAILS;
	}

	protected void thinkFollow()
	{
		NpcInstance actor = getActor();

		Creature target = (Creature) _intention_arg0;
		Integer offset = (Integer) _intention_arg1;

		//Находимся слишком далеко цели, либо цель не пригодна для следования
		if(target == null || target.isAlikeDead() || actor.getDistance(target) > 4000 || offset == null || actor.getReflection() != target.getReflection())
		{
			clientActionFailed();
			return;
		}

		//Уже следуем за этой целью
		if(actor.getMovement().isFollow() && actor.getMovement().getFollowTarget() == target)
		{
			clientActionFailed();
			return;
		}

		//Находимся достаточно близко или не можем двигаться - побежим потом ?
		if(actor.isInRange(target, offset + 16) || actor.isMovementDisabled())
			clientActionFailed();

		if(_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}

		_followTask = ThreadPoolManager.getInstance().schedule(new ThinkFollow(), 250L);
	}

	protected class ThinkFollow implements Runnable
	{
		@Override
		public void run()
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
				return;

			Creature target = (Creature) _intention_arg0;
			int offset = (_intention_arg1 != null && _intention_arg1 instanceof Integer) ? (Integer) _intention_arg1 : 0;

			NpcInstance actor = getActor();
			if(target == null || target.isAlikeDead() || actor.getDistance(target) > 4000 || actor.getReflection() != target.getReflection())
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}

			if(!actor.isInRange(target, offset + 16) && (!actor.getMovement().isFollow() || actor.getMovement().getFollowTarget() != target))
			{
				Location loc = new Location(target.getX() + 30, target.getY() + 30, target.getZ());
				actor.getMovement().followToCharacter(loc, target, offset, false);
			}
			_followTask = ThreadPoolManager.getInstance().schedule(this, 250L);
		}
	}

	public int getMaxPursueRange()
	{
		return Math.max(getAggroRange(), _maxPursueRange);
	}

	public void setMaxPursueRange(int value)
	{
		_maxPursueRange = value;
	}

	public int getMaxHateRange()
	{
		return Math.max(getAggroRange(), MAX_HATE_RANGE);
	}

	@Override
	protected void onEvtMostHatedChanged()
	{
		clearTasks();
		if(getActor().isAttackingNow())
			getActor().abortAttack(true, false);
		if(getActor().isCastingNow())
			getActor().abortCast(true, false);
		onEvtThink();
	}
}