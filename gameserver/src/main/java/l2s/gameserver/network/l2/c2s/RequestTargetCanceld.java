package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;

public class RequestTargetCanceld extends L2GameClientPacket
{
	private int _unselect;

	/**
	 * packet type id 0x48
	 * format:		ch
	 */
	@Override
	protected boolean readImpl()
	{
		_unselect = readH();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getAggressionTarget() != null) // TODO: [Bonux] Проверить это условие.
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isLockedTarget())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(_unselect == 0)
		{
			SkillEntry skillEntry = null;
			if(activeChar.getSkillCast(SkillCastingType.NORMAL).isCastingNow())
				skillEntry = activeChar.getSkillCast(SkillCastingType.NORMAL).getSkillEntry();

			SkillEntry dualSkillEntry = null;
			if(activeChar.getSkillCast(SkillCastingType.NORMAL_SECOND).isCastingNow())
				dualSkillEntry = activeChar.getSkillCast(SkillCastingType.NORMAL_SECOND).getSkillEntry();

			if(skillEntry != null || dualSkillEntry != null)
			{
				boolean force = skillEntry != null && (skillEntry.getTemplate().isHandler() || skillEntry.getTemplate().getHitTime() > 1000);
				if(!force)
					force = dualSkillEntry != null && (dualSkillEntry.getTemplate().isHandler() || dualSkillEntry.getTemplate().getHitTime() > 1000);
				activeChar.abortCast(force, false);
			}
			else if(activeChar.getTarget() != null)
				activeChar.setTarget(null);
		}
		else if(activeChar.getTarget() != null)
			activeChar.setTarget(null);
	}
}