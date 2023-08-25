package l2s.gameserver.skills.effects.instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import l2s.commons.string.StringArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_call_random_skill extends i_abstract_effect
{
	private final List<SkillEntry> _skills = new ArrayList<SkillEntry>();

	public i_call_random_skill(EffectTemplate template)
	{
		super(template);

		int[][] skills = StringArrayUtils.stringToIntArray2X(getParams().getString("skills"), ";", "-");
		for(int[] skillArr : skills)
		{
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillArr[0], skillArr.length >= 2 ? skillArr[1] : 1);
			if(skillEntry != null)
				_skills.add(skillEntry);
		}
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		if(_skills.isEmpty())
			return;

		SkillEntry skillEntry = Rnd.get(_skills);
		if(skillEntry == null)
			return;

		Skill skill = skillEntry.getTemplate();
		if(skill.getReuseDelay() > 0 && effector.isSkillDisabled(skill))
			return;

		if(skillEntry.checkCondition(effector, effected, true, true, true, false, true))
		{
			Set<Creature> targets = skill.getTargets(skillEntry, effector, effected, false);

			if(!skill.isNotBroadcastable() && !effector.isCastingNow())
			{
				for(Creature cha : targets)
					effector.broadcastPacket(new MagicSkillUse(effector, cha, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0));
			}

			effector.callSkill(effected, skillEntry, targets, false, true);
			effector.disableSkill(skill, skill.getReuseDelay());
		}
	}
}
