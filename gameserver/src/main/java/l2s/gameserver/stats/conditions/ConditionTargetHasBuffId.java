package l2s.gameserver.stats.conditions;

import java.util.List;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.Env;

public final class ConditionTargetHasBuffId extends Condition
{
	private final int _id;
	private final int _level;

	public ConditionTargetHasBuffId(int id, int level)
	{
		_id = id;
		_level = level;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if(target == null)
			return false;

		for(Abnormal effect : target.getAbnormalList())
		{
			if(effect.getSkill().getId() != _id)
				continue;

			if(_level == -1)
				return true;

			if(effect.getSkill().getLevel() >= _level)
				return true;
		}

		return false;
	}

	@Override
	public void init()
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		if(_level == -1)
		{
			for(Skill skill : SkillHolder.getInstance().getSkills(_id)) {
				skill.setShowPlayerAbnormal(true);
				skill.setShowNpcAbnormal(true);
			}
		}
		else
		{
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, _id, _level);
			if(skillEntry != null) {
				skillEntry.getTemplate().setShowPlayerAbnormal(true);
				skillEntry.getTemplate().setShowNpcAbnormal(true);
			}
		}
	}
}
