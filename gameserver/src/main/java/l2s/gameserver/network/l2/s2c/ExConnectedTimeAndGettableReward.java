package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

/**
 * @author Bonux
**/
public class ExConnectedTimeAndGettableReward extends L2GameServerPacket
{
	private int _count = 0;
	
	public ExConnectedTimeAndGettableReward(Player player)
	{	
		for(DailyMissionTemplate missionTemplate : player.getDailyMissionList().getAvailableMissions())
		{
			DailyMission mission = player.getDailyMissionList().get(missionTemplate);
			if ((mission.getStatus() == DailyMissionStatus.AVAILABLE) && mission.getCurrentProgress() >= mission.getRequiredProgress())
				_count++;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(_count);	// Unclaimed rewards
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0);
	}
}