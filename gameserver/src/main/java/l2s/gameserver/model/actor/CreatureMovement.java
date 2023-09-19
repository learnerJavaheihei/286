package l2s.gameserver.model.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.PlayableAI.AINextAction;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geodata.GeoMove;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.listener.actions.OnArrivedAction;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks.NotifyAITask;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.network.l2.c2s.RequestRestartPoint;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.MyUtilsFunction;
import l2s.gameserver.utils.PositionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatureMovement
{
	private static final Logger _log = LoggerFactory.getLogger(CreatureMovement.class);
	private static int first = 0 ;
	private final Creature _actor;

	private final Lock _moveLock = new ReentrantLock();
	private final Location _movingDestTempPos = new Location();
	private final List<List<Location>> _targetRecorder = new ArrayList<List<Location>>();

	private volatile HardReference<? extends Creature> _followTarget = HardReferences.emptyRef();

	private boolean _isMoving;
	private boolean _isKeyboardMoving;
	private boolean _isPathfindMoving;
	private OnArrivedAction _onArrivedAction;
	private boolean _isFollow;
	private boolean _forestalling;
	private Future<?> _moveTask;
	private double _moveTaskAllDist, _moveTaskDoneDist;
	private List<Location> _moveList;
	private Location _destination;
	/**
	 * при moveToLocation используется для хранения геокоординат в которые мы двигаемся для того что бы избежать повторного построения одного и того же пути
	 * при followToCharacter используется для хранения мировых координат в которых находилась последний раз преследуемая цель для отслеживания необходимости перестраивания пути
	 */
	private int _moveOffset;
	private int _followCounter;
	private int _previousSpeed = 0;
	private long _followTimestamp;
	private long _startMoveTime;

	public CreatureMovement(Creature actor)
	{
		_actor = actor;
	}

	public Lock getMoveLock()
	{
		return _moveLock;
	}

	public boolean isMoving()
	{
		return _isMoving;
	}

	public boolean isKeyboardMoving()
	{
		return _isKeyboardMoving;
	}

	public boolean isPathfindMoving()
	{
		return _isPathfindMoving;
	}

	public boolean isFollow()
	{
		return _isFollow;
	}

	public int getMoveOffset()
	{
		return _moveOffset;
	}

	public void setMoveOffset(int value)
	{
		_moveOffset = value;
	}

	public Location getDestination()
	{
		if(_destination == null)
			return new Location(0,0,0);
		return _destination;
	}

	public int getMoveTickInterval()
	{
		return (_actor.isPlayer() && !_actor.isVisualTransformed() ? 16000 : 32000) / Math.max(_actor.getMoveSpeed(), 1);
	}

	public Creature getFollowTarget()
	{
		return _followTarget.get();
	}

	public void setFollowTarget(Creature target)
	{
		_followTarget = target == null ? HardReferences.<Creature> emptyRef() : target.getRef();
	}

	public void setMoveTaskDist(double dist)
	{
		_moveTaskAllDist = dist;
		_moveTaskDoneDist = 0.;
	}

	/**
	 * Возвращает позицию цели, в которой она будет через пол секунды.
	 */
	public Location getIntersectionPoint(Creature target)
	{
		if(!PositionUtils.isFacing(_actor, target, 90))
			return new Location(target.getX(), target.getY(), target.getZ());
		double angle = PositionUtils.convertHeadingToDegree(target.getHeading()); // угол в градусах
		double radian = Math.toRadians(angle - 90); // угол в радианах
		double range = target.getMoveSpeed() / 2; // расстояние, пройденное за 1 секунду, равно скорости. Берем половину.
		return new Location((int) (target.getX() - range * Math.sin(radian)), (int) (target.getY() + range * Math.cos(radian)), target.getZ());
	}

	private Location setSimplePath(Location dest)
	{
		List<Location> _moveList = GeoMove.constructMoveList(_actor.getLoc(), dest);
		if(_moveList.isEmpty())
			return null;
		_targetRecorder.clear();
		_targetRecorder.add(_moveList);
		return _moveList.get(_moveList.size() - 1);
	}

	private void moveNext(boolean firstMove)
	{
		if(!isMoving() || _actor.isMovementDisabled())
		{
			stopMove();
			return;
		}

		_previousSpeed = _actor.getMoveSpeed();
		if(_previousSpeed <= 0)
		{
			stopMove();
			return;
		}

		if(!firstMove)
		{
			Location dest = _destination.clone();
			if(dest != null)
			{
				_actor.setLoc(dest, true);
				_actor.getListeners().onMove(dest); // TODO: Подходящее ли место?
			}
		}

		if(_targetRecorder.isEmpty())
		{
			boolean follow = isFollow();
			CtrlEvent ctrlEvent = follow ? CtrlEvent.EVT_ARRIVED_TARGET : CtrlEvent.EVT_ARRIVED;
			OnArrivedAction onArrivedAction = _onArrivedAction;
			stopMove(false);
			if(onArrivedAction != null)
				onArrivedAction.onArrived(_actor, _actor.getLoc(), follow);
			ThreadPoolManager.getInstance().execute(new NotifyAITask(_actor, ctrlEvent));
			return;
		}

		_moveList = _targetRecorder.remove(0);
		Location begin = _moveList.get(0).clone().geo2world();
		Location end = _moveList.get(_moveList.size() - 1).clone().geo2world();

		// TODO: Придумать лучше способ.
		if(!_actor.isFlying() && !_actor.isInBoat() && !_actor.isInWater() && !_actor.isBoat() && !GeoEngine.canMoveToCoord(_actor.getX(), _actor.getY(), _actor.getZ(), end.x, end.y, end.z, _actor.getGeoIndex()))
		{
			stopMove();
			return;
		}

		_destination = end;
		int distance = (_actor.isFlying() || _actor.isInWater()) ? begin.getDistance3D(end) : begin.getDistance(end); //клиент при передвижении не учитывает поверхность

		if(distance != 0)
			_actor.setHeading(PositionUtils.calculateHeadingFrom(begin.x, begin.y, _destination.x, _destination.y));

		setMoveTaskDist(distance);

		_actor.broadcastMove();
		_startMoveTime = _followTimestamp = System.currentTimeMillis();
		_moveTask = ThreadPoolManager.getInstance().schedule(this::updatePosition, getMoveTickInterval());
	}

	private Location buildPathTo(int x, int y, int z, int offset, boolean pathFind)
	{
		return buildPathTo(x, y, z, offset, null, false, pathFind);
	}

	private Location buildPathTo(int x, int y, int z, int offset, Creature follow, boolean forestalling, boolean pathFind)
	{
		int geoIndex = _actor.getGeoIndex();

		Location dest;

		if(forestalling && follow != null && follow.getMovement().isMoving())
			dest = getIntersectionPoint(follow);
		else
			dest = new Location(x, y, z);

		if(_actor.isInBoat() || _actor.isBoat() || !Config.ALLOW_GEODATA)
		{
			PositionUtils.applyOffset(_actor, dest, offset);
			return setSimplePath(dest);
		}

		if(_actor.isFlying() || _actor.isInWater())
		{
			PositionUtils.applyOffset(_actor, dest, offset);

			Location nextloc;

			if(_actor.isFlying())
			{
				if(GeoEngine.canSeeCoord(_actor, dest.x, dest.y, dest.z, true))
					return setSimplePath(dest);

				// DS: При передвижении обсервера клавишами клиент шлет очень далекие (дистанция больше 2000) координаты,
				// поэтому обычная процедура проверки не работает. Используем имитацию плавания в воде.
				if(_actor.isObservePoint())
					nextloc = GeoEngine.moveInWaterCheck(_actor, dest.x, dest.y, dest.z, new int[]{ Integer.MIN_VALUE, Integer.MAX_VALUE });
				else
					nextloc = GeoEngine.moveCheckInAir(_actor, dest.x, dest.y, dest.z);
				if(nextloc != null && !nextloc.equals(_actor.getX(), _actor.getY(), _actor.getZ()))
					return setSimplePath(nextloc);
			}
			else
			{
				nextloc = GeoEngine.moveInWaterCheck(_actor, dest.x, dest.y, dest.z, _actor.getWaterZ());
				if(nextloc == null)
					return null;

				List<Location> _moveList = GeoMove.constructMoveList(_actor.getLoc(), nextloc.clone());
				_targetRecorder.clear();
				if(!_moveList.isEmpty())
					_targetRecorder.add(_moveList);

				int dz = dest.z - nextloc.z;
				// если пытаемся выбратся на берег, считаем путь с точки выхода до точки назначения
				if(dz > 0 && dz < 128)
				{
					_moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, geoIndex, false);
					if(_moveList != null) // null - до конца пути дойти нельзя
					{
						if(!_moveList.isEmpty()) // уже стоим на нужной клетке
							_targetRecorder.add(_moveList);
					}
				}

				if(!_moveList.isEmpty())
					return _moveList.get(_moveList.size() - 1);
			}
			return null;
		}

		List<Location> _moveList = GeoEngine.MoveList(_actor.getX(), _actor.getY(), _actor.getZ(), dest.x, dest.y, geoIndex, true); // onlyFullPath = true - проверяем весь путь до конца
		if(_moveList != null) // null - до конца пути дойти нельзя
		{
			if(_moveList.size() < 2) // уже стоим на нужной клетке
				return null;
			PositionUtils.applyOffset(_moveList, offset);
			if(_moveList.size() < 2) // уже стоим на нужной клетке
				return null;
			_targetRecorder.clear();
			_targetRecorder.add(_moveList);
			return _moveList.get(_moveList.size() - 1);
		}

		// Фейковые игроки ВСЕГДА передвигаются с поиском пути.
		if(pathFind || _actor.isFakePlayer())
		{
			List<List<Location>> targets = GeoMove.findMovePath(_actor.getX(), _actor.getY(), _actor.getZ(), dest.getX(), dest.getY(), dest.getZ(), _actor, geoIndex);
			if(!targets.isEmpty())
			{
				_moveList = targets.remove(targets.size() - 1);
				PositionUtils.applyOffset(_moveList, offset);
				if(!_moveList.isEmpty())
					targets.add(_moveList);
				if(!targets.isEmpty())
				{
					_targetRecorder.clear();
					_targetRecorder.addAll(targets);
					for(int i = targets.size() - 1; i >= 0; i--)
					{
						List<Location> target = targets.get(i);
						if(!target.isEmpty())
						{
							_isPathfindMoving = true;
							return target.get(target.size() - 1);
						}
					}

					if(pathFind)
						return null;
				}
			}
		}

		if(!_actor.isFakePlayer() || !pathFind)
		{
			// расчитываем путь куда сможем дойти
			PositionUtils.applyOffset(_actor, dest, offset);

			_moveList = GeoEngine.MoveList(_actor.getX(), _actor.getY(), _actor.getZ(), dest.x, dest.y, geoIndex, false); // onlyFullPath = false - идем до куда можем
			if(_moveList != null && _moveList.size() > 1) // null - нет геодаты, empty - уже стоим на нужной клетке
			{
				_targetRecorder.clear();
				_targetRecorder.add(_moveList);
				return _moveList.get(_moveList.size() - 1);
			}
		}

		return null;
	}

	public boolean followToCharacter(Creature target, int offset, boolean forestalling)
	{
		return followToCharacter(target.getLoc(), target, offset, forestalling);
	}

	public boolean followToCharacter(Location loc, Creature target, int offset, boolean forestalling)
	{
		getMoveLock().lock();
		try
		{
			if(_actor.isMovementDisabled() || target == null || _actor.isInBoat() && !_actor.isInShuttle() || target.isInvisible(_actor))
				return false;

			if(_actor.getReflection() != target.getReflection())
				return false;

			if(_actor.getDistance(target) > 5000) // TODO: Вынести в конфиг?!?
				return false;

			offset = Math.max(offset, 10);
			if(isFollow() && target == getFollowTarget() && offset == getMoveOffset())
				return true;

			if(Math.abs(_actor.getZ() - target.getZ()) > 1000 && !_actor.isFlying())
				return false;

			_actor.getAI().clearNextAction();

			stopMove(false);

			_actor.deactivateGeoControl();

			if(buildPathTo(loc.x, loc.y, loc.z, 0, target, forestalling, !target.isDoor()) != null)
				_movingDestTempPos.set(loc.x, loc.y, loc.z);
			else
			{
				_actor.activateGeoControl();
				return false;
			}

			_isMoving = true;
			_isKeyboardMoving = false;
			_isFollow = true;
			_forestalling = forestalling;
			setMoveOffset(offset);
			_followCounter = 0;
			setFollowTarget(target);

			moveNext(true);

			return true;
		}
		finally
		{
			getMoveLock().unlock();
		}
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding, true, false, -1);
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding, OnArrivedAction onArrivedAction)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding, true, false, -1, onArrivedAction);
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding, int maxDestRange)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding, true, false, maxDestRange);
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding, boolean cancelNextAction, boolean keyboard)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding, cancelNextAction, keyboard, -1);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding)
	{
		return moveToLocation(x_dest, y_dest, z_dest, offset, pathfinding, true, false, -1);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding, boolean cancelNextAction, boolean keyboard)
	{
		return moveToLocation(x_dest, y_dest, z_dest, offset, pathfinding, cancelNextAction, keyboard, -1);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding, boolean cancelNextAction, boolean keyboard, int maxDestRange)
	{
		return moveToLocation(x_dest, y_dest, z_dest, offset, pathfinding, cancelNextAction, keyboard, maxDestRange, null);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding, boolean cancelNextAction, boolean keyboard, int maxDestRange, OnArrivedAction onArrivedAction)
	{
		getMoveLock().lock();
		try
		{
			offset = Math.max(offset, 0);
			Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
			if(isMoving() && !isFollow() && _movingDestTempPos.equals(dst_geoloc))
			{
				_actor.sendActionFailed();
				return true;
			}

			if(_actor.isMovementDisabled())
			{
				_actor.getAI().setNextAction(AINextAction.MOVE, new Location(x_dest, y_dest, z_dest), offset, pathfinding && !keyboard, false);
				_actor.sendActionFailed();
				return false;
			}

			_actor.getAI().clearNextAction();

			if(_actor.isPlayer())
			{
				if(cancelNextAction)
					_actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			}

			stopMove(false);

			_actor.deactivateGeoControl();

			dst_geoloc = buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding && !keyboard);
			if(dst_geoloc != null)
			{
				if(maxDestRange == -1)
					_movingDestTempPos.set(dst_geoloc);
				else
				{
					Location dst_loc = dst_geoloc.geo2world();
					if(PositionUtils.checkIfInRange(maxDestRange + offset, x_dest, y_dest, z_dest, dst_loc.x, dst_loc.y, dst_loc.z, true))
						_movingDestTempPos.set(dst_geoloc);
					else
					{
						_actor.activateGeoControl();
						_actor.sendActionFailed();
						return false;
					}
				}
			}
			else
			{
				_actor.activateGeoControl();
				_actor.sendActionFailed();
				return false;
			}

			_isMoving = true;
			_isKeyboardMoving = keyboard;
			_onArrivedAction = onArrivedAction;

			moveNext(true);

			return true;
		}
		finally
		{
			getMoveLock().unlock();
		}
	}
	static Map<Integer, ScheduledFuture<?>> restartPointTasks = new HashMap<>();
	static List<ScheduledFuture<?>> informQueue = new ArrayList<ScheduledFuture<?>>();
	public boolean updatePosition()
	{
		getMoveLock().lock();
		try
		{
			if(!isMoving())
				return false;

			if(_actor.isMovementDisabled())
			{
				stopMove();
				return false;
			}

			int speed = _actor.getMoveSpeed();
			if(speed <= 0)
			{
				stopMove();
				return false;
			}

			Creature follow = null;
			if(isFollow())
			{
				follow = getFollowTarget();
				if(follow == null || follow.isInvisible(_actor))
				{
					stopMove();
					return false;
				}
				if(_actor.isInRangeZ(follow, getMoveOffset()) && GeoEngine.canSeeTarget(_actor, follow))
				{
					OnArrivedAction onArrivedAction = _onArrivedAction;
					stopMove();
					if(onArrivedAction != null)
						onArrivedAction.onArrived(_actor, _actor.getLoc(), true);
					ThreadPoolManager.getInstance().execute(new NotifyAITask(_actor, CtrlEvent.EVT_ARRIVED_TARGET));
					return false;
				}
			}

			if(_moveTaskAllDist <= 0)
			{
				moveNext(false);
				return true;
			}

			long now = System.currentTimeMillis();

			_moveTaskDoneDist += (now - _startMoveTime) * _previousSpeed / 1000.;

			double done = Math.max(0, _moveTaskDoneDist / _moveTaskAllDist);
			if(done >= 1)
			{
				moveNext(false);
				return true;
			}

			if(_actor.isMovementDisabled())
			{
				stopMove();
				return false;
			}

			int index = Math.max(0, Math.min(_moveList.size() - 1, (int) (_moveList.size() * done)));
			Location loc = _moveList.get(index).clone().geo2world();
			if(!_actor.isFlying() && !_actor.isInBoat() && !_actor.isInWater() && !_actor.isBoat())
			{
				if(loc.z - _actor.getZ() > 256)
				{
					String bug_text = "geo bug 1 at: " + _actor.getLoc() + " => " + loc.x + "," + loc.y + "," + loc.z + "\tAll path: " + _moveList.get(0) + " => " + _moveList.get(_moveList.size() - 1);
					Log.add(bug_text, "geo");
					stopMove();
					return false;
				}
			}
			if (Config.enablePremiumAccountPeaceZone && _actor.isPlayer()  && _actor.getLevel() > 10) {
				if (!MyUtilsFunction.isPremiumAccountPeaceZone(_actor.getPlayer(),loc)){
					first += 1;
					String[] msgs =new String[4];
					if (first==1) {
						_actor.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_LEFT_THE_PEACEFUL_ZONE));
						for (int i = 0; i<=3; i++) {
							msgs[i] = "角色等级超过10级的非会员将不能在非安全区移动,你将在「"+ (3-i) +"」秒后传送回附近的城镇！";
							int finalI = i;
							ScheduledFuture<?> schedule = ThreadPoolManager.getInstance().schedule(new Runnable() {
								@Override
								public void run() {
									_actor.getPlayer().sendMessage(msgs[finalI]);
								}
							}, 1000L * (i + 1));
							informQueue.add(schedule);
						}
						ScheduledFuture<?> restartPointTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
							@Override
							public void run() {
								RequestRestartPoint.requestRestart(_actor.getPlayer(), RestartType.TO_VILLAGE);
								first = 0;
								informQueue.clear();
							}
						}, 4000L);
						restartPointTasks.put(_actor.getObjectId(),restartPointTask);
						return false;
					}
				}else{
					ScheduledFuture<?> task = restartPointTasks.get(_actor.getObjectId());
					if (task!=null){
						_actor.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ENTERED_A_PEACEFUL_ZONE));
						first = 0;
						task.cancel(true);
						restartPointTasks.remove(_actor.getObjectId());
					}
					if (informQueue.size()>0){
						for (ScheduledFuture<?> scheduledFuture : informQueue) {
							scheduledFuture.cancel(false);
						}
						informQueue.clear();
					}

				}
			}

			// Проверяем, на всякий случай
			if(loc == null || _actor.isMovementDisabled())
			{
				stopMove();
				return false;
			}

			_actor.setLoc(loc, true);

			// В процессе изменения координат, мы остановились
			if(_actor.isMovementDisabled())
			{
				stopMove();
				return false;
			}

			if(isFollow())
			{
				Location followLoc = follow.getLoc();
				if(_movingDestTempPos.getDistance3D(followLoc) != 0)
				{
					_followCounter++;
					if(Math.abs(_actor.getZ() - loc.z) > 1000 && !_actor.isFlying())
					{
						_actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
						stopMove();
						return false;
					}

					if(_followCounter == 5)
					{
						if(buildPathTo(followLoc.x, followLoc.y, followLoc.z, 0, follow, _forestalling, !follow.isDoor()) != null)
							_movingDestTempPos.set(followLoc.x, followLoc.y, followLoc.z);
						else
						{
							stopMove();
							return false;
						}
						moveNext(true);
						_followCounter = 0;
						return true;
					}
				 }
			}

			_previousSpeed = speed;
			_startMoveTime = now;
			_moveTask = ThreadPoolManager.getInstance().schedule(this::updatePosition, getMoveTickInterval());
			return true;
		}
		catch(Exception e)
		{
			_log.error("", e);
			return false;
		}
		finally
		{
			getMoveLock().unlock();
		}
	}

	/**
	 * Останавливает движение и рассылает StopMove
	 */
	public void stopMove()
	{
		stopMove(true);
	}

	/**
	 * Останавливает движение
	 *
	 * @param stop - рассылать ли StopMove
	 */
	public void stopMove(boolean stop)
	{
		if(!isMoving())
			return;

		getMoveLock().lock();
		try
		{
			if(!isMoving())
				return;

			_isMoving = false;
			_isKeyboardMoving = false;
			_isFollow = false;
			_isPathfindMoving = false;
			_onArrivedAction = null;

			if(_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}

			_destination = null;
			_moveList = null;

			_targetRecorder.clear();

			if(stop)
				_actor.broadcastStopMove();
			/*else
				_actor.sendActionFailed();*/

			_actor.activateGeoControl();
		}
		finally
		{
			getMoveLock().unlock();
		}
	}
}
