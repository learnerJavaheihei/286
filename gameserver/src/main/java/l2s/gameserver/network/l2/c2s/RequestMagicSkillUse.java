package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0x39
	 * format:		cddc
	 */
	@Override
	protected boolean readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.setActive();

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, _magicId, activeChar.getSkillLevel(_magicId));
		if(skillEntry == null) {
			if(activeChar.getCostumeList().useCostume(_magicId))
				return;
		}

		if(skillEntry != null)
		{
			Skill skill = skillEntry.getTemplate();
			if(!(skill.isActive() || skill.isToggle()))
			{
				activeChar.sendActionFailed();
				return;
			}

			FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
			if(attachment != null && !attachment.canCast(activeChar, skill))
			{
				activeChar.sendActionFailed();
				return;
			}

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.isTransformed() && !activeChar.getAllSkills().contains(skillEntry))
			{
				activeChar.sendActionFailed();
				return;
			}

			if(skill.isToggle())
			{
				if(activeChar.getAbnormalList().contains(skill))
				{
					if(!skill.isNecessaryToggle())
					{
						if(activeChar.isSitting())
						{
							activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
							return;
						}
						activeChar.getAbnormalList().stop(skill.getId());
						activeChar.sendActionFailed();
					}
					activeChar.sendActionFailed();
					return;
				}
			}

			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			activeChar.setGroundSkillLoc(null);
			
			boolean isDarkVeilActive = false;
			for (Abnormal ab : activeChar.getAbnormalList().toArray())
			{
				if ((ab.getSkill().getId() == 398) && (ab.getSkill().getLevel() == 2))
				{
					isDarkVeilActive = true;
				}
			}
			if ((skillEntry.getId() == 45161) && isDarkVeilActive)
			{
				activeChar.getAI().Cast(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 45162, skillEntry.getLevel()), target, _ctrlPressed, _shiftPressed);
			}
			else if ((skillEntry.getId() == 45163) && isDarkVeilActive)
			{
				activeChar.getAI().Cast(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 45164, skillEntry.getLevel()), target, _ctrlPressed, _shiftPressed);
			}
			else
			{
				activeChar.getAI().Cast(skillEntry, target, _ctrlPressed, _shiftPressed);
			}
		}
		else
			activeChar.sendActionFailed();
	}
}