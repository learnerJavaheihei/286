package l2s.gameserver.network.l2.s2c;

import org.napile.primitive.sets.IntSet;

import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author Bonux
 **/
public class ExRaidBossSpawnInfo extends L2GameServerPacket
{
	private final IntSet _aliveBosses;
	private int _count;
	private int[] _ids;

	public ExRaidBossSpawnInfo(int count, IntSet ids)
	{
		_aliveBosses = RaidBossSpawnManager.getInstance().getAliveRaidBosees();
		_count = count;
		_ids = ids.toArray();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_count);
		boolean baiumSpawned = false;
		for (NpcInstance npc : GameObjectsStorage.getNpcs())
		{
			if (npc.getNpcId() == 29025)
				baiumSpawned = true;
		}
		for (int i = 0; i < _count; i++)
		{
			if (_aliveBosses.contains(_ids[i]))
			{
				writeD(_ids[i]); // boss id
				writeD(1); // alive
				writeD(0); // time after death
			}
			else if ((_ids[i] == 29020) && baiumSpawned)
			{
				writeD(_ids[i]);
				writeD(1);
				writeD(0);
			}
			else
			{
				writeD(_ids[i]);
				writeD(0);
				writeD(0);
			}
		}
	}
}