package ai;

import instances.BalthusKnightAntharas;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.listener.actor.OnChangeCurrentHpListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.templates.npc.WalkerRoute;
import l2s.gameserver.templates.npc.WalkerRoutePoint;
import l2s.gameserver.templates.npc.WalkerRouteType;
import l2s.gameserver.utils.NpcUtils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
**/
public final class BalthusKnightsAntharasAI extends AbstractAntharasAI
{
	private class ChangeCurrentHpListenert implements OnChangeCurrentHpListener
	{
		@Override
		public void onChangeCurrentHp(Creature actor, double oldHp, double newHp)
		{
			NpcInstance npc = getActor();
			if(actor != npc)
				return;

			if(npc.getCurrentHpPercents() <= 25)
			{
				if(!_isQuail.compareAndSet(false, true))
					return;

				npc.removeListener(this);

				for(NpcInstance minion : _minions)
					minion.deleteMe();

				for(Player player : getPlayersInsideLair())
					player.sendPacket(new ExShowScreenMessage(NpcString.ANTHARAS_IS_TRYING_TO_ESCAPE, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, true));

				Reflection reflection = npc.getReflection();
				reflection.setReenterTime(System.currentTimeMillis(), false);
				reflection.startCollapseTimer(5, true);

				clearTasks();
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

				npc.abortCast(true, false);
				npc.abortAttack(true, false);
				npc.getMovement().stopMove();

				setWalkerRoute(QUAIL_WALKER_ROUTE);
			}
		}
	}

	protected static final int PREPARE_MOVIE_TIMER_ID = 5000;
	protected static final int MOVIE_1_TIMER_ID = 5001;
	protected static final int MOVIE_2_TIMER_ID = 5002;
	protected static final int MOVIE_3_TIMER_ID = 5003;
	protected static final int MOVIE_4_TIMER_ID = 5004;
	protected static final int MOVIE_5_TIMER_ID = 5005;
	protected static final int MOVIE_6_TIMER_ID = 5006;

	private static final WalkerRoute WALKER_ROUTE_1 = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	private static final WalkerRoute WALKER_ROUTE_2 = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	static
	{
		WALKER_ROUTE_1.addPoint(new WalkerRoutePoint(new Location(181911, 114835, -7678), new NpcString[0], -1, 0, true, false));
		WALKER_ROUTE_2.addPoint(new WalkerRoutePoint(new Location(179112, 114888, -7712), new NpcString[0], -1, 0, true, false));
	}

	private static final WalkerRoute QUAIL_WALKER_ROUTE = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	static
	{
		QUAIL_WALKER_ROUTE.addPoint(new WalkerRoutePoint(new Location(180232, 114968, -7712), new NpcString[0], -1, 0, true, false));
	}

	private static final int RASH_NPC_ID = 31716;	// Лаш - Адъютант Поддержки

	private final ChangeCurrentHpListenert _changeCurrentHpListenert = new ChangeCurrentHpListenert();
	private final AtomicBoolean _isQuail = new AtomicBoolean(false);

	public BalthusKnightsAntharasAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtFinishWalkerRoute(int routeId)
	{
		super.onEvtFinishWalkerRoute(routeId);

		if(routeId == WALKER_ROUTE_1.getId())
		{
			addTimer(MOVIE_1_TIMER_ID, 2000);
		}
		else if(routeId == WALKER_ROUTE_2.getId())
		{
			NpcInstance actor = getActor();
			actor.setAggroRange(actor.getTemplate().aggroRange);
			_minionsSpawnTime = System.currentTimeMillis() + 120000L;
		}
		else if(routeId == QUAIL_WALKER_ROUTE.getId())
		{
			NpcInstance actor = getActor();
			NpcUtils.spawnSingle(RASH_NPC_ID, actor.getLoc(), actor.getReflection());
			actor.deleteMe();
		}
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();
		if(timerId == PREPARE_MOVIE_TIMER_ID)
		{
			actor.getAI().setWalkerRoute(WALKER_ROUTE_1);
		}
		else if(timerId == MOVIE_1_TIMER_ID)
		{
			actor.getFlags().getImmobilized().start(this);

			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 700, 13, -19, 0, 20000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_2_TIMER_ID, 3000);
		}
		else if(timerId == MOVIE_2_TIMER_ID)
		{
			// do social.
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), 1));

			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 700, 13, 0, 6000, 20000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_3_TIMER_ID, 10000);
		}
		else if(timerId == MOVIE_3_TIMER_ID)
		{
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), 2));
			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 3700, 0, -3, 0, 10000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_4_TIMER_ID, 200);
		}
		else if(timerId == MOVIE_4_TIMER_ID)
		{
			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 1100, 0, -3, 22000, 30000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_5_TIMER_ID, 10800);
		}
		else if(timerId == MOVIE_5_TIMER_ID)
		{
			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 1100, 0, -3, 300, 7000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_6_TIMER_ID, 7000);
		}
		else if(timerId == MOVIE_6_TIMER_ID)
		{
			// reset camera.
			for(Player player : getPlayersInsideLair())
			{
				player.leaveMovieMode();
				//player.sendPacket(new ExShowScreenMessage(NpcString.ANTHARAS_YOU_CANNOT_HOPE_TO_DEFEAT_ME, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
				player.sendPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_A", 1, actor.getObjectId(), actor.getLoc()));
			}
			actor.getFlags().getImmobilized().stop(this);
			actor.getFlags().getInvulnerable().stop(this);
			actor.getAI().setWalkerRoute(WALKER_ROUTE_2);
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();
		Reflection reflection = actor.getReflection();
		if(reflection instanceof BalthusKnightAntharas)
		{
			actor.addListener(_changeCurrentHpListenert);
			actor.setAggroRange(0);
			actor.getFlags().getInvulnerable().start(this);

			addTimer(PREPARE_MOVIE_TIMER_ID, ((BalthusKnightAntharas) reflection).getAntharasStartDelay() * 1000);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		if(_isQuail.get())
			return;

		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		if(_isQuail.get())
			return;

		super.onEvtAggression(target, aggro);
	}

	@Override
	protected boolean createNewTask()
	{
		if(_isQuail.get())
			return false;

		return super.createNewTask();
	}

	@Override
	protected Collection<Player> getPlayersInsideLair()
	{
		NpcInstance actor = getActor();
		Reflection reflection = actor.getReflection();
		if(!reflection.isMain())
			return reflection.getPlayers();

		return World.getAroundPlayers(actor);
	}

	@Override
	protected NpcInstance spawnMinion()
	{
		NpcInstance actor = getActor();
		return NpcUtils.spawnSingle(Rnd.chance(50) ? 29190 : 29103, Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()), actor.getReflection());
	}
}