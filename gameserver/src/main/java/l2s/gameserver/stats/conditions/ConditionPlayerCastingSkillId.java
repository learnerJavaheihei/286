package l2s.gameserver.stats.conditions;

import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Env;

public class ConditionPlayerCastingSkillId extends Condition
{
	private final int _skillId;

	public ConditionPlayerCastingSkillId(int skillId)
	{
		_skillId = skillId;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		SkillEntry skillEntry = env.character.getSkillCast(SkillCastingType.NORMAL).getSkillEntry();
		if(skillEntry != null && skillEntry.getId() == _skillId)
			return true;

		skillEntry = env.character.getSkillCast(SkillCastingType.NORMAL_SECOND).getSkillEntry();
		if(skillEntry != null && skillEntry.getId() == _skillId)
			return true;

		return false;
	}
}