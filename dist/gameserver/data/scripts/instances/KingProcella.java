package instances;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.npc.OnSpawnListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import java.util.concurrent.ScheduledFuture;

/**
 * @author nexvill
 */
public class KingProcella extends Reflection
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
	
	private class MobDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature victim, Creature killer)
		{
			if (victim.isMonster() && ((victim.getNpcId() == PROCELLA_GUARDIAN_1) || (victim.getNpcId() == PROCELLA_GUARDIAN_2) || (victim.getNpcId() == PROCELLA_GUARDIAN_3)))
			{
				if (getNpcs(true, PROCELLA_GUARDIAN_1, PROCELLA_GUARDIAN_2, PROCELLA_GUARDIAN_3).isEmpty())
				{
					for (NpcInstance npc : getNpcs())
					{
						if (!npc.isDead() && npc.getNpcId() == PROCELLA)
						{
							if (npc.getFlags().getInvisible().get() == true)
							{
								npc.stopInvisible(false);
								ThreadPoolManager.getInstance().schedule(() ->
								{
									Location loc = new Location(212663, 179421, -15486);
									addSpawnWithoutRespawn(PROCELLA_GUARDIAN_1, loc, 0);
									loc = new Location(213258, 179822, -15486);
									addSpawnWithoutRespawn(PROCELLA_GUARDIAN_2, loc, 0);
									loc = new Location(212558, 179974, -15486);
									addSpawnWithoutRespawn(PROCELLA_GUARDIAN_3, loc, 0);
								}, 300000L + Rnd.get(-15000, 15000));
							}
						}
					}
				}
			}
			else
			{
				victim.removeListener(_mobDeathListener);
			}
		}
	}
	
	private class SpawnListener implements OnSpawnListener
	{
		@Override
		public void onSpawn(NpcInstance actor)
		{
			if ((actor.getNpcId() == PROCELLA_GUARDIAN_1) || (actor.getNpcId() == PROCELLA_GUARDIAN_2) || (actor.getNpcId() == PROCELLA_GUARDIAN_3))
			{
				for (NpcInstance npc : getNpcs())
				{
					if (npc.getNpcId() == PROCELLA)
					{
						npc.startInvisible(false);
					}
				}
			}
			else if ((actor.getNpcId() == PROCELLA_STORM) && (_checkPlayersInRadius == null))
			{
				_checkPlayersInRadius = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
				{
					for (Player player : getPlayers())
					{
						if (!player.isDead())
						{
							for (NpcInstance npc : getNpcs())
							{
								if (npc.getNpcId() == PROCELLA)
								{
									if (player.getDistance3D(npc) < 300)
									{
										npc.setTarget(player);
										if (!player.getAbnormalList().contains(50043))
										{
											npc.abortAttack(true, false);
											npc.abortCast(false, false);
											npc.doCast(HURRICANE_BOLT_LV_1, player, true);
										}
									}
								}
							}
						}
					}
				}, 1000L, 1000L);
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
	
	// Npcs
	private static final int PROCELLA = 29107;
	private static final int PROCELLA_GUARDIAN_1 = 29112;
	private static final int PROCELLA_GUARDIAN_2 = 29113;
	private static final int PROCELLA_GUARDIAN_3 = 29114;
	private static final int PROCELLA_STORM = 29115;
	// Skills
	private static final SkillEntry HURRICANE_SUMMON = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50042, 1);
	private static final SkillEntry HURRICANE_BOLT_LV_1 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50043, 1);
	// Misc
	protected Zone _instanceZone;
	private final OnSpawnListener _spawnListener = new SpawnListener();
	private final OnDeathListener _playerDeathListener = new PlayerDeathListener();
	private final OnDeathListener _mobDeathListener = new MobDeathListener();
	private final OnZoneEnterLeaveListener _instanceZoneListener = new ZoneListener();
	
	private ScheduledFuture<?> _spawnStorms = null;
	private ScheduledFuture<?> _checkPlayersInRadius = null;
	private int stormsCount = 0;
	
	
	@Override
	protected void onCreate()
	{
		_instanceZone = getZone("[king_procella]");

		_instanceZone.addListener(_instanceZoneListener);
		
		super.onCreate();
		
		startChallenge();
	}
	
	@Override
	protected void onCollapse()
	{
		super.onCollapse();
		cleanup();
	}
	
	@Override
	public void addObject(GameObject o)
	{
		super.addObject(o);

		if(o.isMonster())
			((Creature) o).addListener(_mobDeathListener);
			((Creature) o).addListener(_spawnListener);
	}
	
	protected void cleanup()
	{
		if(_checkPlayersInRadius != null)
			_checkPlayersInRadius.cancel(true);
		if (_spawnStorms != null)
			_spawnStorms.cancel(true);
	}
	
	protected void startChallenge()
	{
		_spawnStorms = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
		{
			if (stormsCount < 16)
			{
				Location loc;
				for (NpcInstance npc : getNpcs())
				{
					if (npc.getNpcId() == PROCELLA)
					{
						npc.doCast(HURRICANE_SUMMON, npc, true);
						loc = npc.getLoc();
						NpcInstance storm = addSpawnWithoutRespawn(PROCELLA_STORM, loc, 0);
						storm.setRandomWalk(true);
						stormsCount++;
					}
				}
			}
		}, 5000L, 60000L);
		
		ThreadPoolManager.getInstance().schedule(() ->
		{
			Location loc = new Location(212663, 179421, -15486);
			addSpawnWithoutRespawn(PROCELLA_GUARDIAN_1, loc, 0);
			loc = new Location(213258, 179822, -15486);
			addSpawnWithoutRespawn(PROCELLA_GUARDIAN_2, loc, 0);
			loc = new Location(212558, 179974, -15486);
			addSpawnWithoutRespawn(PROCELLA_GUARDIAN_3, loc, 0);
		}, 300000L + Rnd.get(-15000, 15000));
	}
}