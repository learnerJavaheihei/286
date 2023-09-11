package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.NpcUtils;

import java.util.Collection;

/**
 * @author Bonux
 */
public final class AntharasAI extends AbstractAntharasAI
{
	private static final int MOVIE_7_TIMER_ID = 5007;
	private static final int MOVIE_8_TIMER_ID = 5008;

	private Zone zone = null;

	public AntharasAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn() {
		super.onEvtSpawn();
		zone = getActor().getReflection().getZone("[antharas_cave]");
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		for(NpcInstance minion : _minions)
			minion.deleteMe();

		super.onEvtDead(killer);
		addTimer(MOVIE_7_TIMER_ID, 10);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();
		if(timerId == MOVIE_7_TIMER_ID)
		{
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 1200, 20, -10, 0, 13000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_8_TIMER_ID, 13000);
		}
		else if(timerId == MOVIE_8_TIMER_ID)
		{
			for(Player player : getPlayersInsideLair())
			{
				player.leaveMovieMode();
				player.altOnMagicUse(player, SkillEntry.makeSkillEntry(SkillEntryType.NONE,23312, 1));
				player.sendPacket(new ExShowScreenMessage(NpcString.ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
			}
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected Collection<Player> getPlayersInsideLair()
	{
		NpcInstance actor = getActor();
		if (zone != null) {
			return zone.getInsidePlayers();
		}
		return World.getAroundPlayers(actor, 1000, 300);
	}

	@Override
	protected NpcInstance spawnMinion()
	{
		NpcInstance actor = getActor();
		return NpcUtils.spawnSingle(Rnd.chance(50) ? 29104 : 29069, Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()));
	}
}