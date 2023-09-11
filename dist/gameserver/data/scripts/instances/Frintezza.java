package instances;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.*;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

/**
 * @author pchayka
 * @reworked by Bonux
**/
public class Frintezza extends Reflection
{
	public static class NpcLocation extends Location
	{
		public int npcId;

		public NpcLocation()
		{
			//
		}

		public NpcLocation(int x, int y, int z, int heading, int npcId)
		{
			super(x, y, z, heading);
			this.npcId = npcId;
		}
	}

	// NPC's
	private static final int HALL_ALARM_DEVICE_ID = 18328;
	private static final int DARK_CHOIR_PLAYER_ID = 18339;
	private static final int WEAK_SCARLET_ID = 29046;
	private static final int STRONG_SCARLET_ID = 29047;

	// Door Group's
	private static final int[] A_HALL_DOORS = { 17130051, 17130052, 17130053, 17130054, 17130055, 17130056, 17130057, 17130058 };
	private static final int[] A_CORRIDOR_DOORS = { 17130042, 17130043 };
	private static final int[] B_HALL_DOORS = { 17130061, 17130062, 17130063, 17130064, 17130065, 17130066, 17130067, 17130068, 17130069, 17130070 };
	private static final int[] B_CORRIDOR_DOORS = { 17130045, 17130046 };

	// NPC Group's
	private static final int[] A_BLOCK_NPCS = { 18329, 18330, 18331, 18333 };
	private static final int[] B_BLOCK_NPCS = { 18334, 18335, 18336, 18337, 18338 };

	//The Boss
	private static final NpcLocation FRINTEZZA_SPAWN = new NpcLocation(-87784, -155090, -9080, 16048, 29045);

	// Weak Scarlet Van Halisha.
	private static final NpcLocation SCARLET_SPAWN_WEAK = new NpcLocation(-87784, -153288, -9176, 16384, 29046);

	// Portrait spawns - 4 portraits = 4 spawns
	private static final NpcLocation[] PORTRAIT_SPAWNS = {new NpcLocation(-86136, -153960, -9168, 35048, 29048),
			new NpcLocation(-86184, -152456, -9168, 28205, 29049),
			new NpcLocation(-89368, -152456, -9168, 64817, 29048),
			new NpcLocation(-89416, -153976, -9168, 57730, 29049)};

	// Demon spawns - 4 portraits = 4 demons
	private static final NpcLocation[] DEMON_SPAWNS = {new NpcLocation(-86136, -153960, -9168, 35048, 29050),
			new NpcLocation(-86184, -152456, -9168, 28205, 29051),
			new NpcLocation(-89368, -152456, -9168, 64817, 29051),
			new NpcLocation(-89416, -153976, -9168, 57730, 29050)};

	private int frintezzaStartDelay;
	private int frintezzaSongInterval;

	private NpcInstance _frintezzaDummy, frintezza, weakScarlet, strongScarlet;
	private NpcInstance[] portraits = new NpcInstance[4];
	private NpcInstance[] demons = new NpcInstance[4];
	private int _scarletMorph = 0;

	private DeathListener _deathListener = new DeathListener();
	private CurrentHpListener _currentHpListener = new CurrentHpListener();
	private ZoneListener _zoneListener = new ZoneListener();

	private ScheduledFuture<?> musicTask;

	private final IntSet _rewardedPlayers = new HashIntSet();

	@Override
	protected void onCreate()
	{
		StatsSet params = getInstancedZone().getAddParams();

		frintezzaStartDelay = params.getInteger("frintezza_start_delay", 300) * 1000;
		frintezzaSongInterval = params.getInteger("frintezza_song_interval", 30) * 1000;

		super.onCreate();

		getZone("[last_imperial_tomb_frintezza]").addListener(_zoneListener);

		for(NpcInstance n : getNpcs())
			n.addListener(_deathListener);

		blockUnblockNpcs(true, A_BLOCK_NPCS);
	}

	public boolean isRewardReceived(Player player)
	{
		return _rewardedPlayers.contains(player.getObjectId());
	}

	public void setRewardReceived(Player player)
	{
		_rewardedPlayers.add(player.getObjectId());
	}

	private class Spawn implements Runnable
	{
		private int _taskId = 0;

		public Spawn(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			try
			{
				switch(_taskId)
				{
					case 1: // spawn.
						List<Spawner> spawns = spawnByGroup("last_imperial_tomb_halisha_dummy");
						for(Spawner spawn : spawns) {
							NpcInstance npc = spawn.getFirstSpawned();
							if(npc != null) {
								_frintezzaDummy = npc;
								break;
							}
						}
						ThreadPoolManager.getInstance().schedule(new Spawn(2), 1000);
						break;
					case 2:
						closeDoor(B_CORRIDOR_DOORS[1]);
						frintezza = spawn(FRINTEZZA_SPAWN);
						showSocialActionMovie(frintezza, 500, 90, 0, 6500, 8000, 0);
						for(int i = 0; i < 4; i++)
						{
							portraits[i] = spawn(PORTRAIT_SPAWNS[i]);
							portraits[i].getFlags().getImmobilized().start(Frintezza.this);
							demons[i] = spawn(DEMON_SPAWNS[i]);
						}
						blockAll(true);
						ThreadPoolManager.getInstance().schedule(new Spawn(3), 6500);
						break;
					case 3:
						showSocialActionMovie(_frintezzaDummy, 1800, 90, 8, 6500, 7000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(4), 900);
						break;
					case 4:
						showSocialActionMovie(_frintezzaDummy, 140, 90, 10, 2500, 4500, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(5), 4000);
						break;
					case 5:
						showSocialActionMovie(frintezza, 40, 75, -10, 0, 1000, 0);
						showSocialActionMovie(frintezza, 40, 75, -10, 0, 12000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(6), 1350);
						break;
					case 6:
						frintezza.broadcastPacket(new SocialActionPacket(frintezza.getObjectId(), 2));
						ThreadPoolManager.getInstance().schedule(new Spawn(7), 7000);
						break;
					case 7:
						_frintezzaDummy.deleteMe();
						_frintezzaDummy = null;
						ThreadPoolManager.getInstance().schedule(new Spawn(8), 1000);
						break;
					case 8:
						showSocialActionMovie(demons[0], 140, 0, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(9), 2800);
						break;
					case 9:
						showSocialActionMovie(demons[1], 140, 0, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(10), 2800);
						break;
					case 10:
						showSocialActionMovie(demons[2], 140, 180, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(11), 2800);
						break;
					case 11:
						showSocialActionMovie(demons[3], 140, 180, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(12), 3000);
						break;
					case 12:
						showSocialActionMovie(frintezza, 240, 90, 0, 0, 1000, 0);
						showSocialActionMovie(frintezza, 240, 90, 25, 5500, 10000, 3);
						ThreadPoolManager.getInstance().schedule(new Spawn(13), 3000);
						break;
					case 13:
						showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(14), 700);
						break;
					case 14:
						showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(15), 1300);
						break;
					case 15:
						showSocialActionMovie(frintezza, 120, 180, 45, 1500, 10000, 0);
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
						ThreadPoolManager.getInstance().schedule(new Spawn(16), 1500);
						break;
					case 16:
						showSocialActionMovie(frintezza, 520, 135, 45, 8000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(17), 7500);
						break;
					case 17:
						showSocialActionMovie(frintezza, 1500, 110, 25, 10000, 13000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(18), 9500);
						break;
					case 18:
						weakScarlet = spawn(SCARLET_SPAWN_WEAK);
						block(weakScarlet, true);
						weakScarlet.addListener(_currentHpListener);
						weakScarlet.broadcastPacket(new MagicSkillUse(weakScarlet, weakScarlet, 5016, 1, 3000, 0));
						EarthQuakePacket eq = new EarthQuakePacket(weakScarlet.getLoc(), 50, 6);
						for(Player pc : getPlayers())
							pc.broadcastPacket(eq);
						showSocialActionMovie(weakScarlet, 1000, 160, 20, 6000, 6000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(19), 5500);
						break;
					case 19:
						showSocialActionMovie(weakScarlet, 800, 160, 5, 1000, 10000, 2);
						ThreadPoolManager.getInstance().schedule(new Spawn(20), 2100);
						break;
					case 20:
						showSocialActionMovie(weakScarlet, 300, 60, 8, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(21), 2000);
						break;
					case 21:
						showSocialActionMovie(weakScarlet, 1000, 90, 10, 3000, 5000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(22), 3000);
						break;
					case 22:
						for(Player pc : getPlayers())
							pc.leaveMovieMode();
						ThreadPoolManager.getInstance().schedule(new Spawn(23), 2000);
						break;
					case 23:
						blockAll(false);
						_scarletMorph = 1;
						musicTask = ThreadPoolManager.getInstance().schedule(new Music(), 5000);
						break;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private class Music implements Runnable
	{
		@Override
		public void run()
		{
			if(frintezza == null)
				return;
			int song = Math.max(1, Math.min(4, getSong()));
			NpcString song_name;
			switch(song)
			{
				case 1:
					song_name = NpcString.REQUIEM_OF_HATRED;
					break;
				case 2:
					song_name = NpcString.FRENETIC_TOCCATA;
					break;
				case 3:
					song_name = NpcString.FUGUE_OF_JUBILATION;
					break;
				case 4:
					song_name = NpcString.MOURNFUL_CHORALE_PRELUDE;
					break;
				default:
					return;
			}
			if(!frintezza.isBlocked())
			{
				frintezza.broadcastPacket(new ExShowScreenMessage(song_name, 3000, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
				frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, song, frintezzaSongInterval, 0));
				// Launch the song's effects (they start about 10 seconds after he starts to play)
				ThreadPoolManager.getInstance().schedule(new SongEffectLaunched(getSongTargets(song), song, 10000), 10000);
			}
			// Schedule a new song to be played in 30-40 seconds...
			musicTask = ThreadPoolManager.getInstance().schedule(new Music(), frintezzaSongInterval + Rnd.get(10000));
		}

		/**
		 * Depending on the song, returns the song's targets (either mobs or players)
		 */
		private Set<Creature> getSongTargets(int songId)
		{
			Set<Creature> targets = new HashSet<>();
			if(songId < 4) // Target is the minions
			{
				if(weakScarlet != null && !weakScarlet.isDead())
					targets.add(weakScarlet);
				if(strongScarlet != null && !strongScarlet.isDead())
					targets.add(strongScarlet);
				for(int i = 0; i < 4; i++)
				{
					if(portraits[i] != null && !portraits[i].isDead())
						targets.add(portraits[i]);
					if(demons[i] != null && !demons[i].isDead())
						targets.add(demons[i]);
				}
			}
			else
				// Target is the players
				for(Player pc : getPlayers())
					if(!pc.isDead())
						targets.add(pc);
			return targets;
		}

		/**
		 * returns the chosen symphony for Frintezza to play
		 * If the minions are injured he has 40% to play a healing song
		 * If they are all dead, he will only play harmful player symphonies
		 */
		private int getSong()
		{
			if(minionsNeedHeal())
				return 1;
			return Rnd.get(2, 4);
		}

		/**
		 * Checks if Frintezza's minions need heal (only major minions are checked) Return a "need heal" = true only 40% of the time
		 */
		private boolean minionsNeedHeal()
		{
			if(!Rnd.chance(40))
				return false;
			if(weakScarlet != null && !weakScarlet.isAlikeDead() && weakScarlet.getCurrentHp() < weakScarlet.getMaxHp() * 2 / 3)
				return true;
			if(strongScarlet != null && !strongScarlet.isAlikeDead() && strongScarlet.getCurrentHp() < strongScarlet.getMaxHp() * 2 / 3)
				return true;
			for(int i = 0; i < 4; i++)
			{
				if(portraits[i] != null && !portraits[i].isDead() && portraits[i].getCurrentHp() < portraits[i].getMaxHp() / 3)
					return true;
				if(demons[i] != null && !demons[i].isDead() && demons[i].getCurrentHp() < demons[i].getMaxHp() / 3)
					return true;
			}
			return false;
		}
	}

	/**
	 * The song was played, this class checks it's affects (if any)
	 */
	private class SongEffectLaunched implements Runnable
	{
		private final Set<Creature> _targets;
		private final int _song, _currentTime;

		/**
		 * @param targets		   - song's targets
		 * @param song			  - song id 1-5
		 * @param currentTimeOfSong - skills during music play are consecutive, repeating
		 */
		public SongEffectLaunched(Set<Creature> targets, int song, int currentTimeOfSong)
		{
			_targets = targets;
			_song = song;
			_currentTime = currentTimeOfSong;
		}

		@Override
		public void run()
		{
			if(frintezza == null)
				return;

			// If the song time is over stop this loop
			if(_currentTime > frintezzaSongInterval)
				return;

			// Skills are consecutive, so call them again
			SongEffectLaunched songLaunched = new SongEffectLaunched(_targets, _song, _currentTime + frintezzaSongInterval / 10);
			ThreadPoolManager.getInstance().schedule(songLaunched, frintezzaSongInterval / 10);
			frintezza.callSkill(frintezza, SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5008, _song), _targets, false, false);
		}
	}

	private NpcInstance spawn(NpcLocation loc)
	{
		return addSpawnWithoutRespawn(loc.npcId, loc, 0);
	}

	/**
	 * Shows a movie to the players in the lair.
	 *
	 * @param target	   - L2NpcInstance target is the center of this movie
	 * @param dist		 - int distance from target
	 * @param yaw		  - angle of movie (north = 90, south = 270, east = 0 , west = 180)
	 * @param pitch		- pitch > 0 looks up / pitch < 0 looks down
	 * @param time		 - fast ++ or slow -- depends on the value
	 * @param duration	 - How long to watch the movie
	 * @param socialAction - 1,2,3,4 social actions / other values do nothing
	 */
	private void showSocialActionMovie(NpcInstance target, int dist, int yaw, int pitch, int time, int duration, int socialAction)
	{
		if(target == null)
			return;

		for(Player pc : getPlayers()) {
			if (pc.getDistance(target) <= 2550) {
				pc.enterMovieMode();
				pc.specialCamera(target, dist, yaw, pitch, time, duration);
			} else
				pc.leaveMovieMode();
		}

		if(socialAction > 0 && socialAction < 5)
			target.broadcastPacket(new SocialActionPacket(target.getObjectId(), socialAction));
	}

	private void blockAll(boolean flag)
	{
		block(frintezza, flag);
		block(weakScarlet, flag);
		block(strongScarlet, flag);
		for(int i = 0; i < 4; i++)
		{
			block(portraits[i], flag);
			block(demons[i], flag);
		}
	}

	private void block(NpcInstance npc, boolean flag)
	{
		if(npc == null || npc.isDead())
			return;
		if(flag)
		{
			npc.abortAttack(true, false);
			npc.abortCast(true, true);
			npc.setTarget(null);
			if(npc.getMovement().isMoving())
				npc.getMovement().stopMove();
			npc.block();
		}
		else
			npc.unblock();

		if(flag)
			npc.getFlags().getInvulnerable().start(this);
		else
			npc.getFlags().getInvulnerable().stop(this);
	}

	private class SecondMorph implements Runnable
	{
		private int _taskId = 0;

		public SecondMorph(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			try
			{
				switch(_taskId)
				{
					case 1:
						int angle = Math.abs((weakScarlet.getHeading() < 32768 ? 180 : 540) - (int) (weakScarlet.getHeading() / 182.044444444));
						for(Player pc : getPlayers())
							pc.enterMovieMode();
						blockAll(true);
						showSocialActionMovie(weakScarlet, 500, angle, 5, 500, 15000, 0);
						ThreadPoolManager.getInstance().schedule(new SecondMorph(2), 2000);
						break;
					case 2:
						weakScarlet.broadcastPacket(new SocialActionPacket(weakScarlet.getObjectId(), 1));
						weakScarlet.setCurrentHp(weakScarlet.getMaxHp() * 3 / 4, false);
						weakScarlet.broadcastCharInfo();
						ThreadPoolManager.getInstance().schedule(new SecondMorph(3), 5500);
						break;
					case 3:
						weakScarlet.broadcastPacket(new SocialActionPacket(weakScarlet.getObjectId(), 4));
						blockAll(false);
						Skill skill = SkillHolder.getInstance().getSkill(5017, 1);
						skill.getEffects(weakScarlet, weakScarlet);
						for(Player pc : getPlayers())
							pc.leaveMovieMode();
						break;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private class ThirdMorph implements Runnable
	{
		private int _taskId = 0;
		private int _angle = 0;

		public ThirdMorph(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			try
			{
				switch(_taskId)
				{
					case 1:
						_angle = Math.abs((weakScarlet.getHeading() < 32768 ? 180 : 540) - (int) (weakScarlet.getHeading() / 182.044444444));
						for(Player pc : getPlayers())
							pc.enterMovieMode();
						blockAll(true);
						frintezza.broadcastPacket(new MagicSkillCanceled(frintezza.getObjectId()));
						frintezza.broadcastPacket(new SocialActionPacket(frintezza.getObjectId(), 4));
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(2), 100);
						break;
					case 2:
						showSocialActionMovie(frintezza, 250, 120, 15, 0, 1000, 0);
						showSocialActionMovie(frintezza, 250, 120, 15, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(3), 6500);
						break;
					case 3:
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
						showSocialActionMovie(frintezza, 500, 70, 15, 3000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(4), 3000);
						break;
					case 4:
						showSocialActionMovie(frintezza, 2500, 90, 12, 6000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(5), 3000);
						break;
					case 5:
						showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 1000, 0);
						showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(6), 500);
						break;
					case 6:
						weakScarlet.doDie(weakScarlet);
						showSocialActionMovie(weakScarlet, 450, _angle, 14, 8000, 8000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(7), 6250);
						break;
					case 7:
						NpcLocation loc = new NpcLocation();
						loc.set(weakScarlet.getLoc());
						loc.npcId = STRONG_SCARLET_ID;
						weakScarlet.deleteMe();
						weakScarlet = null;
						strongScarlet = spawn(loc);
						strongScarlet.addListener(_deathListener);
						block(strongScarlet, true);
						showSocialActionMovie(strongScarlet, 450, _angle, 12, 500, 14000, 2);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(9), 5000);
						break;
					case 9:
						blockAll(false);
						for(Player pc : getPlayers())
							pc.leaveMovieMode();
						Skill skill = SkillHolder.getInstance().getSkill(5017, 1);
						skill.getEffects(strongScarlet, strongScarlet);
						break;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private class Die implements Runnable
	{
		private int _taskId = 0;

		public Die(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			try
			{
				switch(_taskId)
				{
					case 1:
						blockAll(true);
						int _angle = Math.abs((strongScarlet.getHeading() < 32768 ? 180 : 540) - (int) (strongScarlet.getHeading() / 182.044444444));
						showSocialActionMovie(strongScarlet, 300, _angle - 180, 5, 0, 7000, 0);
						showSocialActionMovie(strongScarlet, 200, _angle, 85, 4000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Die(2), 7500);
						break;
					case 2:
						showSocialActionMovie(frintezza, 100, 120, 5, 0, 7000, 0);
						showSocialActionMovie(frintezza, 100, 90, 5, 5000, 15000, 0);
						ThreadPoolManager.getInstance().schedule(new Die(3), 6000);
						break;
					case 3:
						showSocialActionMovie(frintezza, 900, 90, 25, 7000, 10000, 0);
						frintezza.doDie(frintezza);
						frintezza = null;
						ThreadPoolManager.getInstance().schedule(new Die(4), 7000);
						break;
					case 4:
						for(Player pc : getPlayers())
							pc.leaveMovieMode();
						cleanUp();
						spawnByGroup("last_imperial_tomb_teleporter");
						break;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void cleanUp()
	{
		startCollapseTimer(5, true);

		for(NpcInstance n : getNpcs())
			n.deleteMe();
	}

	// Hack: ToRemove when doors will operate normally in reflections
	private void blockUnblockNpcs(boolean block, int[] npcArray)
	{
		for(NpcInstance n : getNpcs())
			if(ArrayUtils.contains(npcArray, n.getNpcId()))
			{
				if(block)
				{
					n.block();
					n.getFlags().getInvulnerable().start(this);
				}
				else
				{
					n.unblock();
					n.getFlags().getInvulnerable().stop(this);
				}
			}
	}

	public class CurrentHpListener implements OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			if(actor.isDead() || actor != weakScarlet)
				return;

			double newHp = actor.getCurrentHp() - damage;
			double maxHp = actor.getMaxHp();
			switch(_scarletMorph)
			{
				case 1:
					if(newHp < 0.75 * maxHp)
					{
						_scarletMorph = 2;
						ThreadPoolManager.getInstance().schedule(new SecondMorph(1), 1100);
					}
					break;
				case 2:
					if(newHp < 0.1 * maxHp)
					{
						_scarletMorph = 3;
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(1), 2000);
					}
					break;
			}
		}
	}

	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(self.isNpc())
			{
				if(self.getNpcId() == HALL_ALARM_DEVICE_ID)
				{
					for(int i = 0; i < A_HALL_DOORS.length; i++)
						openDoor(A_HALL_DOORS[i]);

					blockUnblockNpcs(false, A_BLOCK_NPCS);

					for(NpcInstance n : getNpcs()) {
						if (ArrayUtils.contains(A_BLOCK_NPCS, n.getNpcId()))
							n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getPlayers().get(Rnd.get(getPlayers().size())), 200);
					}
				}
				else if(ArrayUtils.contains(A_BLOCK_NPCS, self.getNpcId()))
				{
					//ToCheck: find easier way
					for(NpcInstance n : getNpcs())
						if(ArrayUtils.contains(A_BLOCK_NPCS, n.getNpcId()) && !n.isDead())
							return;
					for(int i = 0; i < A_CORRIDOR_DOORS.length; i++)
						openDoor(A_CORRIDOR_DOORS[i]);

					blockUnblockNpcs(true, B_BLOCK_NPCS);
				}
				else if(self.getNpcId() == DARK_CHOIR_PLAYER_ID)
				{
					for(NpcInstance n : getNpcs()) {
						if (n.getNpcId() == DARK_CHOIR_PLAYER_ID && !n.isDead())
							return;
					}

					for(int i = 0; i < B_HALL_DOORS.length; i++)
						openDoor(B_HALL_DOORS[i]);

					blockUnblockNpcs(false, B_BLOCK_NPCS);
				}
				else if(ArrayUtils.contains(B_BLOCK_NPCS, self.getNpcId()))
				{
					//ToCheck: find easier way
					for(NpcInstance n : getNpcs()) {
						if ((ArrayUtils.contains(B_BLOCK_NPCS, n.getNpcId()) || ArrayUtils.contains(A_BLOCK_NPCS, n.getNpcId())) && !n.isDead())
							return;
					}

					for(int i = 0; i < B_CORRIDOR_DOORS.length; i++)
						openDoor(B_CORRIDOR_DOORS[i]);

					ThreadPoolManager.getInstance().schedule(new Spawn(1), frintezzaStartDelay);
				}
				else if(self.getNpcId() == WEAK_SCARLET_ID)
				{
					self.decayMe();
				}
				else if(self.getNpcId() == STRONG_SCARLET_ID)
				{
					ThreadPoolManager.getInstance().schedule(new Die(1), 10);
					setReenterTime(System.currentTimeMillis(), true);
				}
			}
		}
	}

	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			//
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(cha.isNpc() && (cha.getNpcId() == WEAK_SCARLET_ID || cha.getNpcId() == STRONG_SCARLET_ID))
			{
				cha.teleToLocation(new Location(-87784, -153304, -9176));
				((NpcInstance) cha).getAggroList().clear(true);
				cha.setCurrentHpMp(cha.getMaxHp(), cha.getMaxMp());
				cha.broadcastCharInfo();
			}
		}
	}

	@Override
	protected void onCollapse()
	{
		super.onCollapse();

		if(musicTask != null)
			musicTask.cancel(true);
	}
}