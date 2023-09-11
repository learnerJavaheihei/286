package instances;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;

/**
 * @author nexvill
 */
public class KingPetram extends Reflection
{
	private class PlayerDeathListener implements OnDeathListener
	{

		@Override
		public void onDeath(Creature victim, Creature killer)
		{
			if(victim.isPlayer())
			{
				boolean exit = true;
				for(Player member : getPlayers())
				{
					if(!member.isDead())
					{
						exit = false;
						break;
					}
				}

				if(exit)
					ThreadPoolManager.getInstance().schedule(() -> clearReflection(5, true), 15000L);
			}
		}
	}
	
	private class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if(!cha.isPlayer())
				return;

			 if(zone == _instanceZone)
			{
				cha.addListener(_playerDeathListener);
			}
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(cha.isPlayer() && zone == _instanceZone)
				cha.removeListener(_playerDeathListener);
		}
	}
	
	protected Zone _instanceZone;
	private final OnDeathListener _playerDeathListener = new PlayerDeathListener();
	private final OnZoneEnterLeaveListener _instanceZoneListener = new ZoneListener();
	
	@Override
	protected void onCreate()
	{
		_instanceZone = getZone("[king_petram]");

		_instanceZone.addListener(_instanceZoneListener);
		
		super.onCreate();
	}
	
	@Override
	protected void onCollapse()
	{
		super.onCollapse();
	}
}