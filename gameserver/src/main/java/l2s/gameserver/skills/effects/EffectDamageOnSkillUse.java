package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

/*
*	Author SanyaDC
*/

public class EffectDamageOnSkillUse extends EffectHandler
{
	public EffectDamageOnSkillUse(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(!effected.isPlayer())
			return false;

		return true;
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isCastingNow()){
		double damage = getValue();
			if(Rnd.chance(5)){
				SkillEntry castingSkillEntry = effected.getSkillCast(SkillCastingType.NORMAL).getSkillEntry();
				if(castingSkillEntry != null && castingSkillEntry.getTemplate().isMagic())
				{
					effected.reduceCurrentHp(damage, effector, getSkill(), true, true, false, false, false, false, true, true, false, false, false, 0, false);

				}
			}
		}
		return true;
	}
}