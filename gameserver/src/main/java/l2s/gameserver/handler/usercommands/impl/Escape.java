package l2s.gameserver.handler.usercommands.impl;
import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

/**
 * Support for /unstuck command
 */
public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 52 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		if(activeChar.isMovementDisabled() || activeChar.isInOlympiadMode())
			return false;

		if(activeChar.getTeleMode() != 0 || !activeChar.getPlayerAccess().UseTeleport)
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater"));
			return false;
		}

		if(activeChar.isInDuel() || activeChar.getTeam() != TeamType.NONE)
		{
			activeChar.sendMessage(new CustomMessage("common.RecallInDuel"));
			return false;
		}

		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.getMovement().stopMove();

		SkillEntry skillEntry;
		if(activeChar.getPlayerAccess().FastUnstuck)
			skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 2100, 1);
		else
			skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 2099, 1);

		if(skillEntry != null && skillEntry.checkCondition(activeChar, activeChar, false, false, true))
			activeChar.getAI().Cast(skillEntry, activeChar, false, true);

		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}