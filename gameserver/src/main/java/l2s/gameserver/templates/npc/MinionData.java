package l2s.gameserver.templates.npc;

import l2s.gameserver.geometry.Territory;
import l2s.gameserver.templates.StatsSet;

/**
 * @author Bonux
**/
public class MinionData
{
	private final int _npcId;
	private final int _minionAmount;
	private final int _respawnTime;
	private final Territory _territory;

	private StatsSet _parameters = StatsSet.EMPTY;

	public MinionData(int npcId, String aiType, int minionAmount, int respawnTime, Territory territory)
	{
		_npcId = npcId;
		_minionAmount = minionAmount;
		_respawnTime = respawnTime;
		_territory = territory;

		if(aiType != null)
		{
			_parameters = new StatsSet();
			_parameters.set("ai_type", aiType);
		}
	}

	public int getMinionId()
	{
		return _npcId;
	}

	public int getAmount()
	{
		return _minionAmount;
	}

	public int getRespawnTime()
	{
		return _respawnTime;
	}

	public Territory getTerritory()
	{
		return _territory;
	}

	public StatsSet getParameters()
	{
		return _parameters;
	}
}