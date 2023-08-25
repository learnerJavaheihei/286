package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectMute extends EffectHandler
{
	public EffectMute(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return !effected.isRaid();
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.getFlags().getMuted().start(this))
		{
			SkillEntry castingSkillEntry = effected.getSkillCast(SkillCastingType.NORMAL).getSkillEntry();
			if(castingSkillEntry != null && castingSkillEntry.getTemplate().isMagic())
			{
				effected.abortCast(true, true);
				return;
			}

			castingSkillEntry = effected.getSkillCast(SkillCastingType.NORMAL_SECOND).getSkillEntry();
			if(castingSkillEntry != null && castingSkillEntry.getTemplate().isMagic())
				effected.abortCast(true, true);
		}
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getMuted().stop(this);
	}
}