package handler.dailymissions;

import java.util.Collection;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.DailyMissionsHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.network.l2.s2c.ExConnectedTimeAndGettableReward;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

/**
 * @author Bonux
**/
public abstract class ProgressDailyMissionHandler extends ScriptDailyMissionHandler
{
	@Override
	public DailyMissionStatus getStatus(Player player, DailyMission mission)
	{
		if(mission.isCompleted())
			return DailyMissionStatus.COMPLETED;
		if(mission.getCurrentProgress() >= mission.getRequiredProgress())
			return DailyMissionStatus.AVAILABLE;
		return DailyMissionStatus.NOT_AVAILABLE;
	}

	protected void progressMission(Player player, int value, boolean increase, int levelCondition)
	{
		if(!Config.EX_USE_TO_DO_LIST)
			return;

		Collection<DailyMissionTemplate> missionTemplates = player.getDailyMissionList().getAvailableMissions();
		int notGettedRewards = 0;
		for(DailyMissionTemplate missionTemplate : missionTemplates)
		{
			if(missionTemplate.getHandler() != this)
				continue;

			DailyMission mission = player.getDailyMissionList().get(missionTemplate);
			
			if (missionTemplate.getCompletedMission() != 0)
			{
				DailyMissionTemplate completedMissionTemplate = DailyMissionsHolder.getInstance().getMission(missionTemplate.getCompletedMission());
				if (completedMissionTemplate == null)
					continue;
				
				DailyMission completedMission = player.getDailyMissionList().get(completedMissionTemplate);
				if (completedMission.getStatus() != DailyMissionStatus.COMPLETED)
					continue;
			}
			
			if ((mission.getStatus() == DailyMissionStatus.AVAILABLE) && mission.getCurrentProgress() >= mission.getRequiredProgress())
				notGettedRewards++;
			
			if(mission.isCompleted())
				continue;

			int minLevel = missionTemplate.getMinLevel();
			int maxLevel = missionTemplate.getMaxLevel();
			if(levelCondition > 0 && (levelCondition < minLevel || levelCondition > maxLevel))
				continue;

			int playerLevel = player.getLevel();
			if(playerLevel < minLevel || playerLevel > maxLevel)
				continue;

			if(increase)
			{
				mission.setValue(mission.getValue() + value);
			}
			else
			{
				if(mission.getValue() == value)
					continue;

				mission.setValue(value);
			}
		}
		if (notGettedRewards > 0)
			player.sendPacket(new ExConnectedTimeAndGettableReward(player));
	}
}
