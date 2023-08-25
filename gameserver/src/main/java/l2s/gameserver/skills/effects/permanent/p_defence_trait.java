package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.SkillTrait;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class p_defence_trait extends EffectHandler
{
	private final SkillTrait _type;
	private final double _power;

	public p_defence_trait(EffectTemplate template)
	{
		super(template);
		
		String traitName = getTemplate().getParams().getString("type").toUpperCase();
		if(traitName.startsWith("TRAIT_"))
			traitName = traitName.substring(6).trim();
		_type = SkillTrait.valueOf(traitName);
		_power = ((double) getTemplate().getParams().getInteger("power") + 100.) / 100.;
	}
	
	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(_power == 1.)
			return;

		/*final SkillTraitStates skillTraitStates = effected.getSkillTraitStates();
		synchronized(skillTraitStates.getDefenceTraits())
		{
			if(_power < 2.0)
			{
				skillTraitStates.getDefenceTraits()[_type.getId()] *= _power;
				skillTraitStates.getDefenceTraitsCount()[_type.getId()]++;
			}
			else
				skillTraitStates.getTraitsInvul()[_type.getId()]++;
		}*/
	}
	
	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(_power == 1.)
			return;

		/*final SkillTraitStates skillTraitStates = effected.getSkillTraitStates();
		synchronized(skillTraitStates.getDefenceTraits())
		{
			if(_power < 2.0)
			{
				skillTraitStates.getDefenceTraits()[_type.getId()] /= _power;
				skillTraitStates.getDefenceTraitsCount()[_type.getId()]--;
			}
			else
				skillTraitStates.getTraitsInvul()[_type.getId()]--;
		}*/
	}
}