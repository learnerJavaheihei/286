package instances;

import ai.AbstractBaiumAI;
import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.StatsSet;

import l2s.gameserver.utils.NpcUtils;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 */
public class BalthusKnightBaium extends Reflection
{
	private final int BAIUM_RAID_NPC_ID = 29099;	// Баюм

	private final AtomicBoolean baiumRaidSpawned = new AtomicBoolean(false);
	private final IntSet rewardedPlayers = new HashIntSet();

	public boolean spawnBaiumRaid(NpcInstance stoneNpc, Player player){
		if(!baiumRaidSpawned.compareAndSet(false, true))
			return false;

		Location loc = stoneNpc.getLoc();
		stoneNpc.getSpawn().deleteAll();

		NpcInstance baiumRaid = NpcUtils.spawnSingle(BAIUM_RAID_NPC_ID, loc, this);
		NpcAI ai = baiumRaid.getAI();
		if(ai instanceof AbstractBaiumAI){
			((AbstractBaiumAI) ai).setAwakener(player);
		}
		return true;
	}

	public boolean isRewardReceived(Player player)
	{
		return rewardedPlayers.contains(player.getObjectId());
	}

	public void setRewardReceived(Player player)
	{
		rewardedPlayers.add(player.getObjectId());
	}
}