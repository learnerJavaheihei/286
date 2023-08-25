package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.dao.CharacterTrainingCampDAO;
import l2s.gameserver.instancemanager.TrainingCampManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.TrainingCamp;

/**
 * @author Sdw
 */
public class NotifyTrainingRoomEnd extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		TrainingCamp trainingCamp = TrainingCampManager.getInstance().getTrainingCamp(activeChar);
		if(trainingCamp == null || !trainingCamp.isTraining() && !trainingCamp.isValid(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		trainingCamp.setEndTime(System.currentTimeMillis());
		CharacterTrainingCampDAO.getInstance().replace(activeChar.getAccountName(), trainingCamp);
		TrainingCampManager.getInstance().onExitTrainingCamp(activeChar);
	}
}