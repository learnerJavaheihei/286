package manager;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.utils.ReflectionUtils;
import l2s.gameserver.utils.NpcUtils;

/**
 * @reworked by Bonux
 */
public class HeavenlyRift
{
	public static final int HEAVENLY_RIFT_DURATION = 20;	// В минутах

	private static HeavenlyRift _instance;

	public static HeavenlyRift getInstance()
	{
		if(_instance == null)
			_instance = new HeavenlyRift();
		return _instance;
	}

	private final Zone _zone;
	private final AtomicBoolean _isInProgress = new AtomicBoolean(false);

	private ScheduledFuture<?> _clearZoneTask = null;

	private HeavenlyRift()
	{
		_zone = ReflectionUtils.getZone("[heavenly_rift]");	
	}

	public Zone getZone()
	{
		return _zone;
	}

	public boolean isInProgress()
	{
		return _isInProgress.get();
	}

	public boolean start(int delayInMinutes)
	{
		if(!_isInProgress.compareAndSet(false, true))
			return false;

		ServerVariables.set("heavenly_rift_complete", 0);
		ServerVariables.set("heavenly_rift_level", 0);

		_clearZoneTask = ThreadPoolManager.getInstance().schedule(() -> stop(), TimeUnit.MINUTES.toMillis(delayInMinutes));
		return true;
	}

	public boolean start()
	{
		return start(HEAVENLY_RIFT_DURATION);
	}

	public boolean stop()
	{
		if(!_isInProgress.compareAndSet(true, false))
			return false;

		if(_clearZoneTask != null)
		{
			_clearZoneTask.cancel(false);
			_clearZoneTask = null;
		}

		for(Creature creature : getZone().getObjects())
		{
			if(creature.isPlayer())
				creature.teleToLocation(114298, 13343, -5104);
			else if(creature.isNpc() && creature.getNpcId() != 30401)
				creature.decayMe();
		}
		return true;
	}

	public int getAliveNpcCount(int npcId)
	{
		int res = 0;
		for(NpcInstance npc : getZone().getInsideNpcs())
		{
			if(npc.getNpcId() == npcId && !npc.isDead())
				res++;
		}
		return res;
	}	

	public void startEvent20Bomb(Player player)
	{
		if(!isInProgress())
			return;

		getZone().broadcastPacket(new ExShowScreenMessage(NpcString.SET_OFF_BOMBS_AND_GET_TREASURE, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER), false);

		NpcUtils.spawnSingle(18003, 113352, 12936, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 113592, 13272, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 113816, 13592, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 113080, 13192, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 113336, 13528, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 113560, 13832, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112776, 13512, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 113064, 13784, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112440, 13848, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112728, 14104, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112760, 14600, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112392, 14456, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112104, 14184, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 111816, 14488, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112104, 14760, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112392, 15032, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 112120, 15288, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 111784, 15064, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 111480, 14824, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
		NpcUtils.spawnSingle(18003, 113144, 14216, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn
	}

	public void startEventTower(Player player)
	{
		if(!isInProgress())
			return;

		getZone().broadcastPacket(new ExShowScreenMessage(NpcString.PROTECT_THE_CENTRAL_TOWER_FROM_DIVINE_ANGELS, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER), false);

		NpcUtils.spawnSingle(18004, 112648, 14072, 10976, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn 30min

		ThreadPoolManager.getInstance().schedule(() ->
		{
			for(int i = 0 ; i < 40 ; i++)
			{
				NpcUtils.spawnSingle(20139, Location.findPointToStay(new Location(112696, 13960, 10958), 200, 500, ReflectionManager.MAIN.getGeoIndex()), _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn 30min CORD RND GET CENTRAL POINT
			}
		}, 10000L);
	}

	public void startEvent40Angels(Player player)
	{
		if(!isInProgress())
			return;

		getZone().broadcastPacket(new ExShowScreenMessage(NpcString.DESTROY_WEAKED_DIVINE_ANGELS, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER), false);

		for(int i = 0 ; i < 40 ; i++)
		{
			Location loc = Location.findPointToStay(new Location(112696, 13960, 10958), 200, 500, player.getGeoIndex());
			NpcUtils.spawnSingle(20139, loc, _clearZoneTask.getDelay(TimeUnit.MILLISECONDS)); //despawn 30min CORD RND GET CENTRAL POINT
		}		
	}
}
