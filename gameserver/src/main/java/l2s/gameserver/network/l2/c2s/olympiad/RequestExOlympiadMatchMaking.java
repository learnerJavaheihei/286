package l2s.gameserver.network.l2.c2s.olympiad;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.olympiad.CompType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadMatchMakingResult;

/**
 * @author nexvill
 */
public class RequestExOlympiadMatchMaking extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		
		if(activeChar.isBaseClassActive())
		{
			if(activeChar.getClassLevel().ordinal() >= ClassLevel.SECOND.ordinal() && activeChar.getLevel() >= Config.OLYMPIAD_MIN_LEVEL)
			{
				Olympiad.addParticipant(activeChar);

				if(Olympiad.getParticipantPoints(activeChar.getObjectId()) > 0)
				{
					if(!activeChar.isQuestContinuationPossible(false))
					{
						activeChar.sendPacket(SystemMsg.CANNOT_APPLY_TO_PARTICIPATE_BECAUSE_YOUR_INVENTORY_SLOTS_OR_WEIGHT_EXCEEDED_80);
						return;
					}
					if (activeChar.getReflection() != ReflectionManager.MAIN)
					{
						activeChar.sendPacket(SystemMsg.CANNOT_APPLY_TO_PARTICIPATE_IN_A_MATCH_WHILE_IN_AN_INSTANCED_ZONE);
						return;
					}
					if (activeChar.isDead())
					{
						activeChar.sendPacket(SystemMsg.CANNOT_APPLY_TO_PARTICIPATE_IN_A_MATCH_WHILE_DEAD);
						return;
					}
					if (Olympiad.getDailyGameCounts(activeChar.getObjectId())[0] == 0)
					{
						activeChar.sendPacket(SystemMsg.YOUVE_USED_UP_ALL_YOUR_MATCHES);
						return;
					}

					if (Olympiad.registerParticipant(activeChar, CompType.NON_CLASSED))
						activeChar.sendPacket(new ExOlympiadMatchMakingResult(true));
				}
			}
			else
				activeChar.sendPacket(SystemMsg.YOU_MUST_LEVEL_70_OR_HIGHER_AND_HAVE_COMPLETED_THE_2ND_CLASS_TRANSFER_IN_ORDER_TO_PARTICIPATE_IN_A_MATCH);
		}
	}
}