package l2s.gameserver.skills.effects;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class EffectIgnoreSkill extends EffectHandler
{
	private final TIntSet _ignoredSkill = new TIntHashSet();

	public EffectIgnoreSkill(EffectTemplate template)
	{
		super(template);

		String[] skills = getParams().getString("skillId", "").split(";");
		for(String skill : skills)
			_ignoredSkill.add(Integer.parseInt(skill));
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.addIgnoreSkillsEffect(this, _ignoredSkill);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.removeIgnoreSkillsEffect(this);
	}
}