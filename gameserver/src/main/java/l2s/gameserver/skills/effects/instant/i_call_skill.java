package l2s.gameserver.skills.effects.instant;

import java.util.Set;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_call_skill extends i_abstract_effect
{
	private final SkillEntry _skillEntry;
	private final int _maxIncreaseLevel;

	public i_call_skill(EffectTemplate template)
	{
		super(template);

		int[] skill = getParams().getIntegerArray("skill", "-");
		_skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill[0], skill.length >= 2 ? skill[1] : 1);
		_maxIncreaseLevel = getParams().getInteger("max_increase_level", 0);
	}

	public SkillEntry getSkillEntry() {
		return _skillEntry;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		if(_skillEntry == null)
			return;

		SkillEntry tempSkillEntry = _skillEntry;
		Skill skill = tempSkillEntry.getTemplate();
		Creature aimTarget = skill.getAimingTarget(effector, effected);
		if(aimTarget != null && _maxIncreaseLevel > 0)
		{
			Skill hasSkill = null;
			for(Abnormal effect : aimTarget.getAbnormalList())
			{
				if(effect.getSkill().getId() == skill.getId())
				{
					hasSkill = effect.getSkill(); //taking the first one only.
					break;
				}
			}

			if(hasSkill == null)
			{
				loop: for(Servitor servitor : aimTarget.getServitors())
				{
					for(Abnormal effect : servitor.getAbnormalList())
					{
						if(effect.getSkill().getId() == skill.getId())
						{
							hasSkill = effect.getSkill(); //taking the first one only.
							break loop;
						}
					}
				}
			}

			if(hasSkill != null)
			{
				Skill newSkill = SkillHolder.getInstance().getSkill(skill.getId(), Math.min(_maxIncreaseLevel, hasSkill.getLevel() + 1));
				if(newSkill != null)
					skill = newSkill;
				else
					skill = hasSkill;
				tempSkillEntry = SkillEntry.makeSkillEntry(_skillEntry.getEntryType(), skill);
			}
		}

		if(skill.getReuseDelay() > 0 && effector.isSkillDisabled(skill))
			return;

		if(tempSkillEntry.checkCondition(effector, aimTarget, true, true, true, false, true))
		{
			Set<Creature> targets = skill.getTargets(tempSkillEntry, effector, aimTarget, false);

			if(!skill.isNotBroadcastable() && !effector.isCastingNow())
			{
				for(Creature cha : targets)
				{
					if(cha != null)
						effector.broadcastPacket(new MagicSkillUse(effector, cha, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0));
				}
			}

			effector.callSkill(aimTarget, tempSkillEntry, targets, false, true);
			effector.disableSkill(skill, skill.getReuseDelay());
		}
	}
}
