package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectInvulnerable extends EffectHandler
{
	public EffectInvulnerable(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		SkillEntry skillEntry = effected.getSkillCast(SkillCastingType.NORMAL).getSkillEntry();
		if(skillEntry != null && skillEntry.getSkillType() == SkillType.TAKECASTLE)
			return false;

		skillEntry = effected.getSkillCast(SkillCastingType.NORMAL_SECOND).getSkillEntry();
		if(skillEntry != null && skillEntry.getSkillType() == SkillType.TAKECASTLE)
			return false;

		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getInvulnerable().start(this);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getInvulnerable().stop(this);
	}
}