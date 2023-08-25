package l2s.gameserver.network.l2.c2s;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.PositionUtils;

/**
 * @author SYS
 * @date 08/9/2007
 * Format: chdddddc
 *
 * Пример пакета:
 * D0
 * 2F 00
 * E4 35 00 00 x
 * 62 D1 02 00 y
 * 22 F2 FF FF z
 * 90 05 00 00 skill id
 * 00 00 00 00 ctrlPressed
 * 00 shiftPressed
 */
public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private Location _loc = new Location();
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0xd0
	 */
	@Override
	protected boolean readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_skillId = readD();
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

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		Skill skill = SkillHolder.getInstance().getSkill(_skillId, activeChar.getSkillLevel(_skillId));
		if(skill != null)
		{
			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.isTransformed() && !activeChar.getAllSkills().contains(skill))
				return;

			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
			activeChar.setHeading(PositionUtils.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _loc.x, _loc.y));
			activeChar.broadcastPacketToOthers(new ValidateLocationPacket(activeChar));
			activeChar.setGroundSkillLoc(_loc);
			activeChar.getAI().Cast(SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill), target, _ctrlPressed, _shiftPressed);
		}
		else
			activeChar.sendActionFailed();
	}
}