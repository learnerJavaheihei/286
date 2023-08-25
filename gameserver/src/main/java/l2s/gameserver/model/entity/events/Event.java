package l2s.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.logging.LoggerObject;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.event.OnStartStopListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.InitableObject;
import l2s.gameserver.model.entity.events.objects.OpenableObject;
import l2s.gameserver.model.entity.events.objects.SpawnableObject;
import l2s.gameserver.model.entity.events.objects.TaskObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TimeUtils;

import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

/**
 * @author VISTALL
 * @date 12:54/10.12.2010
 */
public abstract class Event extends LoggerObject
{
	private class ListenerListImpl extends ListenerList<Event>
	{
		public void onStart()
		{
			for(Listener<Event> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStart(Event.this);
		}

		public void onStop()
		{
			for(Listener<Event> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStop(Event.this);
		}
	}

	public static final String EVENT = "event";

	// actions
	protected final IntObjectMap<List<EventAction>> _onTimeActions = new TreeIntObjectMap<List<EventAction>>();
	protected final Map<String, List<EventAction>> _onActActions = new HashMap<String, List<EventAction>>();
	protected final List<EventAction> _onStartActions = new ArrayList<EventAction>(0);
	protected final List<EventAction> _onStopActions = new ArrayList<EventAction>(0);
	protected final List<EventAction> _onInitActions = new ArrayList<EventAction>(0);
	// objects
	protected final Map<Object, List<Object>> _objects = new HashMap<Object, List<Object>>(0);

	protected final MultiValueSet<String> _params;
	protected final int _id;
	protected final String _name;

	protected final ListenerListImpl _listenerList = new ListenerListImpl();

	protected IntObjectMap<ItemInstance> _banishedItems = Containers.emptyIntObjectMap();

	private List<Future<?>> _tasks = null;

	protected Event(MultiValueSet<String> set)
	{
		_params = set;
		_id = set.getInteger("id");
		_name = set.getString("name");
	}

	protected Event(int id, String name)
	{
		_params = new MultiValueSet<String>(0);
		_id = id;
		_name = name;
	}

	public void initEvent()
	{
		callActions(_onInitActions);

		reCalcNextTime(true);

		printInfo();
	}

	public void startEvent()
	{
		callActions(_onStartActions);

		_listenerList.onStart();
	}

	public void stopEvent(boolean force)
	{
		callActions(_onStopActions);

		_listenerList.onStop();
	}

	public void printInfo()
	{
		final long startSiegeMillis = startTimeMillis();

		if(startSiegeMillis == 0)
			info(getName() + " time - undefined");
		else
			info(getName() + " time - " + TimeUtils.toSimpleFormat(startSiegeMillis));
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getId() + ";" + getName() + "]";
	}

	//===============================================================================================================
	//												Actions
	//===============================================================================================================

	protected void callActions(List<EventAction> actions)
	{
		for(EventAction action : actions)
			action.call(this);
	}

	public void addOnStartActions(List<EventAction> start)
	{
		_onStartActions.addAll(start);
	}

	public void addOnStopActions(List<EventAction> start)
	{
		_onStopActions.addAll(start);
	}

	public void addOnInitActions(List<EventAction> start)
	{
		_onInitActions.addAll(start);
	}

	public void addOnTimeAction(int time, EventAction action)
	{
		List<EventAction> list = _onTimeActions.get(time);
		if(list != null)
			list.add(action);
		else
		{
			List<EventAction> actions = new ArrayList<EventAction>(1);
			actions.add(action);
			_onTimeActions.put(time, actions);
		}
	}

	public void addOnTimeActions(int time, List<EventAction> actions)
	{
		if(actions.isEmpty())
			return;

		for(EventAction action : actions)
			addOnTimeAction(time, action);
	}

	public void timeActions(int time)
	{
		List<EventAction> actions = _onTimeActions.get(time);
		if(actions == null)
		{
			info("Undefined time : " + time);
			return;
		}
		callActions(actions);
	}

	public int[] timeActions()
	{
		return _onTimeActions.keySet().toArray();
	}

	public void addOnActAction(String act, EventAction action)
	{
		List<EventAction> list = _onActActions.get(act.toLowerCase());
		if(list != null)
			list.add(action);
		else
		{
			List<EventAction> actions = new ArrayList<EventAction>(1);
			actions.add(action);
			_onActActions.put(act.toLowerCase(), actions);
		}
	}

	public void addOnActActions(String act, List<EventAction> actions)
	{
		if(actions.isEmpty())
			return;

		for(EventAction action : actions)
			addOnActAction(act, action);
	}

	public void actActions(String act)
	{
		List<EventAction> actions = _onActActions.get(act.toLowerCase());
		if(actions == null)
		{
			info("Undefined act : " + act);
			return;
		}
		callActions(actions);
	}

	//===============================================================================================================
	//												Tasks
	//===============================================================================================================

	public synchronized void registerActions()
	{
		final long t = startTimeMillis();
		if(t == 0)
			return;

		if(_tasks == null)
			_tasks = new ArrayList<Future<?>>(_onTimeActions.size());

		final long c = System.currentTimeMillis();
		for(int key : _onTimeActions.keySet().toArray())
		{
			long time = t + key * 1000L;
			EventTimeTask wrapper = new EventTimeTask(this, key);
			if(time <= c)
				ThreadPoolManager.getInstance().execute(wrapper);
			else
			{
				Future<?> task = ThreadPoolManager.getInstance().schedule(wrapper, time - c);
				if(task != null)
					_tasks.add(task);
			}
		}
	}

	public synchronized void clearActions()
	{
		if(_tasks != null)
		{
			for(Future<?> f : _tasks)
				f.cancel(false);

			_tasks.clear();
		}

		for(List<Object> list : _objects.values())
		{
			for(Object o : list)
			{
				if(o instanceof TaskObject)
				{
					((TaskObject) o).cancel(this);
				}
			}
		}
	}

	//===============================================================================================================
	//												Objects
	//===============================================================================================================

	public boolean containsObjects(Object name)
	{
		return _objects.get(name) != null;
	}

	@SuppressWarnings("unchecked")
	public <O> List<O> getObjects(Object name)
	{
		List<Object> objects = _objects.get(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	@SuppressWarnings("unchecked")
	public <O> O getFirstObject(Object name)
	{
		List<Object> objects = getObjects(name);
		return objects.size() > 0 ? (O) objects.get(0) : null;
	}

	public void addObject(Object name, Object object)
	{
		if(object == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.add(object);
		else
		{
			list = new CopyOnWriteArrayList<Object>();
			list.add(object);
			_objects.put(name, list);
		}
	}

	public void removeObject(Object name, Object o)
	{
		if(o == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.remove(o);
	}

	@SuppressWarnings("unchecked")
	public <O> List<O> removeObjects(Object name)
	{
		List<Object> objects = _objects.remove(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	public void addObjects(Object name, Collection<?> objects)
	{
		if(objects.isEmpty())
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.addAll(objects);
		else
			_objects.put(name, new CopyOnWriteArrayList<Object>(objects));
	}


	public Map<Object, List<Object>> getObjects()
	{
		return _objects;
	}

	public void spawnAction(Object name, boolean spawn)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
			{
				if(spawn)
					((SpawnableObject) object).spawnObject(this, getReflection());
				else
					((SpawnableObject) object).despawnObject(this, getReflection());
			}
	}

	public void respawnAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
				((SpawnableObject) object).respawnObject(this, getReflection());
	}

	public void openAction(Object name, boolean open)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
		{
			if(object instanceof OpenableObject)
			{
				if(open)
					((OpenableObject) object).openObject(this);
				else
					((OpenableObject) object).closeObject(this);
			}
		}
	}

	public void zoneAction(Object name, boolean active)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof ZoneObject)
				((ZoneObject) object).setActive(active, this);
	}

	public void initAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof InitableObject)
				((InitableObject) object).initObject(this);
	}

	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(EVENT))
		{
			if(start)
				startEvent();
			else
				stopEvent(false);
		}
	}

	public void refreshAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
				((SpawnableObject) object).refreshObject(this, getReflection());
	}

	public void taskAction(Object name, boolean schedule)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
		{
			if(object instanceof TaskObject)
			{
				if(schedule)
					((TaskObject) object).schedule(this);
				else
					((TaskObject) object).cancel(this);
			}
		}
	}

	//===============================================================================================================
	//												Abstracts
	//===============================================================================================================

	public abstract void reCalcNextTime(boolean onInit);

	public abstract EventType getType();

	protected abstract long startTimeMillis();

	//===============================================================================================================
	//												Broadcast
	//===============================================================================================================
	public void broadcastToWorld(IBroadcastPacket packet)
	{
		for(Player player : GameObjectsStorage.getPlayers(false, false))
			player.sendPacket(packet);
	}

	//===============================================================================================================
	//												Getters & Setters
	//===============================================================================================================
	public MultiValueSet<String> getParams()
	{
		return _params;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public GameObject getCenterObject()
	{
		return null;
	}

	public Reflection getReflection()
	{
		return ReflectionManager.MAIN;
	}

	public long getRelation(Player thisPlayer, Player target, long oldRelation)
	{
		return oldRelation;
	}

	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		return oldRelation;
	}

	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		//
	}

	public Location getRestartLoc(Player player, RestartType type)
	{
		return null;
	}

	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force, boolean nextAttackCheck)
	{
		return false;
	}

	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		return null;
	}

	public SystemMsg canUseItem(Player player, ItemInstance item)
	{
		return null;
	}

	public boolean canUseSkill(Creature caster, Creature target, Skill skill)
	{
		return true;
	}
	
	public boolean canUseTeleport(Player player)
	{
		return true;
	}

	public boolean isInProgress()
	{
		return false;
	}

	public void findEvent(Player player)
	{
		//
	}

	public void announce(int id, String value, int time)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented announce");
	}

	public void teleportPlayers(String teleportWho)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented teleportPlayers");
	}

	public boolean ifVar(String name)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented ifVar");
	}

	public List<Player> itemObtainPlayers()
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented itemObtainPlayers");
	}

	public void giveItem(Player player, int itemId, long count)
	{
		switch(itemId)
		{
			case ItemTemplate.ITEM_ID_FAME:
				player.setFame(player.getFame() + (int) count, toString(), true);
				break;
			default:
				ItemFunctions.addItem(player, itemId, count);
				break;
		}
	}

	public List<Player> broadcastPlayers(int range)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented broadcastPlayers");
	}

	/**
	 * @param active   кто ресает
	 * @param target   кого ресает
	 * @param force    ктрл зажат ли
	 * @param quiet  если тру, мессаги об ошибке непосылаются
	 * @return  возращает можно ли реснуть цень
	 */
	public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
	{
		throw new UnsupportedOperationException(getClass().getName() + " not implemented canResurrect");
	}

	//===============================================================================================================
	//											setEvent helper
	//===============================================================================================================
	public void onAddEvent(GameObject o)
	{
		//
	}

	public void onRemoveEvent(GameObject o)
	{
		//
	}

	//===============================================================================================================
	//											Banish items
	//===============================================================================================================
	public void addBanishItem(ItemInstance item)
	{
		if(_banishedItems == Containers.<ItemInstance>emptyIntObjectMap())
			_banishedItems = new CHashIntObjectMap<ItemInstance>();

		_banishedItems.put(item.getObjectId(), item);
	}

	public void removeBanishItems()
	{
		Iterator<IntObjectPair<ItemInstance>> iterator = _banishedItems.entrySet().iterator();
		while(iterator.hasNext())
		{
			IntObjectPair<ItemInstance> entry = iterator.next();
			iterator.remove();

			ItemInstance item = ItemsDAO.getInstance().load(entry.getKey());
			if(item != null)
			{
				if(item.getOwnerId() > 0)
				{
					GameObject object = GameObjectsStorage.findObject(item.getOwnerId());
					if(object != null && object.isPlayable())
					{
						((Playable) object).getInventory().destroyItem(item);
						object.getPlayer().sendPacket(SystemMessagePacket.removeItems(item));
					}
				}
				item.delete();
			}
			else
				item = entry.getValue();

			item.deleteMe();
		}
	}

	//===============================================================================================================
	//											 Listeners
	//===============================================================================================================
	public void addListener(Listener<Event> l)
	{
		_listenerList.add(l);
	}

	public void removeListener(Listener<Event> l)
	{
		_listenerList.remove(l);
	}

	//===============================================================================================================
	//											Object
	//===============================================================================================================
	public void cloneTo(Event e)
	{
		for(EventAction a : _onInitActions)
			e._onInitActions.add(a);

		for(EventAction a : _onStartActions)
			e._onStartActions.add(a);

		for(EventAction a : _onStopActions)
			e._onStopActions.add(a);

		for(Map.Entry<String, List<EventAction>> entry : _onActActions.entrySet())
			e.addOnActActions(entry.getKey(), entry.getValue());

		for(IntObjectPair<List<EventAction>> entry : _onTimeActions.entrySet())
			e.addOnTimeActions(entry.getKey(), entry.getValue());

		for(Map.Entry<Object, List<Object>> entry : _objects.entrySet())
			e.addObjects(entry.getKey(), entry.getValue());
	}

	public String getVisibleName(Player player, Player observer)
	{
		return null;
	}

	public String getVisibleTitle(Player player, Player observer)
	{
		return null;
	}

	public Integer getVisibleNameColor(Player player, Player observer)
	{
		return null;
	}

	public Integer getVisibleTitleColor(Player player, Player observer)
	{
		return null;
	}

	public boolean isPledgeVisible(Player player, Player observer)
	{
		return true;
	}

	public boolean checkCondition(Creature creature, Class<? extends Condition> conditionClass)
	{
		return true;
	}

	public Boolean isInZoneBattle(Creature creature)
	{
		return null;
	}

	public Boolean isInvisible(Creature creature, GameObject observer)
	{
		return null;
	}

	public boolean canJoinParty(Player inviter, Player target)
	{
		return true;
	}

	public boolean canLeaveParty(Player player)
	{
		return true;
	}

	public void checkTargetsForSkill(Skill skill, Set<Creature> targets, Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		//
	}

	public final long getForceStartTime()
	{
		int minTime = 0;
		for(int time : timeActions())
		{
			if(time < minTime)
				minTime = time;
		}
		return System.currentTimeMillis() + (Math.abs(minTime) * 1000L) + 1000L;
	}

	public boolean isForceScheduled()
	{
		return false;
	}

	public boolean forceScheduleEvent()
	{
		return false;
	}

	public boolean forceCancelEvent()
	{
		return false;
	}
}
