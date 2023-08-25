package l2s.gameserver.skills;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.stats.funcs.Func;

import java.util.Set;

/**
 * @author VISTALL
 * @date 0:15/03.06.2011
 */
public class SkillEntry implements SkillInfo
{
	public static final SkillEntry[] EMPTY_ARRAY = new SkillEntry[0];

	private final SkillEntryType _entryType;
	private final Skill _skill;

	private boolean _disabled;

	protected SkillEntry(SkillEntryType key, Skill value)
	{
		_entryType = key;
		_skill = value;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean disabled)
	{
		_disabled = disabled;
	}

	public SkillEntryType getEntryType()
	{
		return _entryType;
	}

	@Override
	public Skill getTemplate()
	{
		return _skill;
	}

	@Override
	public int getId()
	{
		return _skill.getId();
	}

	@Override
	public int getDisplayId()
	{
		return _skill.getDisplayId();
	}

	@Override
	public int getLevel()
	{
		return _skill.getLevel();
	}

	@Override
	public int getDisplayLevel()
	{
		return _skill.getDisplayLevel();
	}

	public Skill.SkillType getSkillType()
	{
		return _skill.getSkillType();
	}

	public String getName()
	{
		return _skill.getName();
	}

	public String getName(Player player)
	{
		return _skill.getName(player);
	}

	public final boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return _skill.checkCondition(this, activeChar, target, forceUse, dontMove, first);
	}

	public final boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		return _skill.checkCondition(this, activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	public final SystemMsg checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first)
	{
		return _skill.checkTarget(this, activeChar, target, aimingTarget, forceUse, first);
	}

	public final SystemMsg checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first, boolean trigger)
	{
		return _skill.checkTarget(this, activeChar, target, aimingTarget, forceUse, first, trigger);
	}

	public final boolean getEffects(Creature effector, Creature effected)
	{
		return _skill.getEffects(/*this, */effector, effected);
	}

	public final boolean getEffects(Creature effector, Creature effected, int timeConst, double timeMult)
	{
		return _skill.getEffects(/*this, */effector, effected, timeConst, timeMult);
	}

	public final void onEndCast(Creature activeChar, Set<Creature> targets)
	{
		_skill.onEndCast(/*this, */activeChar, targets);
	}

	public boolean isAltUse()
	{
		return _skill.isAltUse(getEntryType());
	}

	public SkillEntry copyTo(SkillEntryType entryType)
	{
		return SkillEntry.makeSkillEntry(entryType, _skill);
	}

	public Func[] getStatFuncs()
	{
		return _skill.getStatFuncs(/*this*/);
	}

	public Skill getLockedSkill()
	{
		return null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		return hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode()
	{
		return _skill.hashCode();
	}

	@Override
	public String toString()
	{
		return _skill.toString();
	}

	public static SkillEntry makeSkillEntry(SkillEntryType entryType, Skill skill) {
		if(skill == null)
			return null;
		return new SkillEntry(entryType, skill);
	}

	public static SkillEntry makeSkillEntry(SkillEntryType entryType, int skillId, int skillLevel) {
		return makeSkillEntry(entryType, SkillHolder.getInstance().getSkill(skillId, skillLevel));
	}
}
