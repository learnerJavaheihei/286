package l2s.gameserver.skills.effects.instant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.AbnormalsComparator;

/**
 * @author Bonux
**/
public class i_dispel_all extends i_abstract_effect
{
	public i_dispel_all(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final List<Abnormal> abnormals = new ArrayList<Abnormal>(effected.getAbnormalList().values());
		Collections.sort(abnormals, AbnormalsComparator.getInstance()); // ToFix: Comparator to HF
		Collections.reverse(abnormals);

		for(Abnormal abnormal : abnormals)
		{
			if(!abnormal.isCancelable())
				continue;

			Skill effectSkill = abnormal.getSkill();
			if(effectSkill == null)
				continue;

			if(effectSkill.isToggle())
				continue;

			if(effectSkill.isPassive())
				continue;

			if(effected.isSpecialAbnormal(effectSkill))
				continue;

			abnormal.exit();

			if(!abnormal.isHidden())
				effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
		}
	}
}