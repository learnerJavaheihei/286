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
 * @author L2-scripts.com - (SanyaDC)
 */
public class BalthusKnightZaken extends Reflection
{
	private final int Zaken_RAID_NPC_ID = 29119;	// Закен

	//private final AtomicBoolean ZakenRaidSpawned = new AtomicBoolean(false);
	private final IntSet rewardedPlayers = new HashIntSet();

	public boolean spawnZakenmRaid(NpcInstance stoneNpc, Player player){

		NpcInstance zakenRaid = NpcUtils.spawnSingle(Zaken_RAID_NPC_ID, 55368, 219160, -3225, this);
		//NpcAI ai = zakenRaid.getAI();
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