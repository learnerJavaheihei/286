package l2s.gameserver.instancemanager;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2s.gameserver.data.xml.holder.DoorHolder;
import l2s.gameserver.data.xml.holder.TimeRestrictFieldHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.templates.TimeRestrictFieldInfo;

public class ReflectionManager
{
	public static final Reflection MAIN = Reflection.createReflection(0);
	public static final Reflection PARNASSUS = Reflection.createReflection(-1);
	public static final Reflection GIRAN_HARBOR = Reflection.createReflection(-2);
	public static final Reflection JAIL = Reflection.createReflection(-3);
	public static final Reflection ANCIENT_PIRATES_TOMB = Reflection.createReflection(-1000);

	private static final ReflectionManager _instance = new ReflectionManager();

	public static ReflectionManager getInstance()
	{
		return _instance;
	}

	private final TIntObjectHashMap<Reflection> _reflections = new TIntObjectHashMap<Reflection>();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private ReflectionManager()
	{
		//
	}

	public void init()
	{
		add(MAIN);
		add(PARNASSUS);
		add(GIRAN_HARBOR);
		add(JAIL);
		add(ANCIENT_PIRATES_TOMB);

		// создаем в рефлекте все зоны, и все двери
		MAIN.init(DoorHolder.getInstance().getDoors(), ZoneHolder.getInstance().getZones());

		TimeRestrictFieldInfo field = TimeRestrictFieldHolder.getInstance().getFields().get(2);
		ANCIENT_PIRATES_TOMB.setTeleportLoc(field.getEnterLoc());
		ANCIENT_PIRATES_TOMB.setReturnLoc(field.getExitLoc());
		ANCIENT_PIRATES_TOMB.spawnByGroup("1002_ancient_pirates_tomb");

		JAIL.setCoreLoc(new Location(-114648, -249384, -2984));
	}

	public Reflection get(int id)
	{
		readLock.lock();
		try
		{
			return _reflections.get(id);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public Reflection add(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.put(ref.getId(), ref);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Reflection remove(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.remove(ref.getId());
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Reflection[] getAll()
	{
		readLock.lock();
		try
		{
			return _reflections.values(new Reflection[_reflections.size()]);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public List<Reflection> getAllByIzId(int izId)
	{
		List<Reflection> reflections = new ArrayList<Reflection>();

		readLock.lock();
		try
		{
			for(Reflection r : getAll())
			{
				if(r.getInstancedZoneId() == izId)
					reflections.add(r);
			}
		}
		finally
		{
			readLock.unlock();
		}
		return reflections;
	}

	public int getCountByIzId(int izId)
	{
		readLock.lock();
		try
		{
			int i = 0;
			for(Reflection r : getAll())
				if(r.getInstancedZoneId() == izId)
					i++;
			return i;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public int size()
	{
		return _reflections.size();
	}
}