package instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.templates.StatsSet;

import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

/**
 * @author Bonux
 */
public class BalthusKnightAntharas extends Reflection
{
	private int _antharasStartDelay;

	private final IntSet _rewardedPlayers = new HashIntSet();

	@Override
	protected void onCreate()
	{
		StatsSet params = getInstancedZone().getAddParams();

		_antharasStartDelay = params.getInteger("antharas_start_delay");

		super.onCreate();
	}

	public int getAntharasStartDelay()
	{
		return _antharasStartDelay;
	}

	public boolean isRewardReceived(Player player)
	{
		return _rewardedPlayers.contains(player.getObjectId());
	}

	public void setRewardReceived(Player player)
	{
		_rewardedPlayers.add(player.getObjectId());
	}
}