package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectNegateMark extends EffectHandler
{
	private static final int MARK_OF_WEAKNESS = 11259;
	private static final int MARK_OF_PLAGUE = 11261;
	private static final int MARK_OF_TRICK = 11262;

	public EffectNegateMark(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		Skill skill = getSkill();

		byte markCount = 0;

		for(Abnormal a : effected.getAbnormalList())
		{
			int skillId = a.getSkill().getId();
			if(skillId == MARK_OF_WEAKNESS || skillId == MARK_OF_PLAGUE || skillId == MARK_OF_TRICK)
			{
				markCount = (byte) (markCount + 1);
				effected.getAbnormalList().stop(skillId);
			}
		}

		if(markCount > 0)
		{
			AttackInfo info = Formulas.calcMagicDam(effector, effected, skill, getSkill().isSSPossible(), !getSkill().isDeathlink());
			effected.reduceCurrentHp(info.damage * markCount, effector, skill, true, true, false, true, false, false, true, true, info.crit, info.miss, info.shld, info.elementalDamage, info.elementalCrit);
		}
	}
}
