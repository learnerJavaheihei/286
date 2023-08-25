package l2s.gameserver.skills.effects.instant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.AbnormalsComparator;

/**
 * @author Bonux
**/
public class i_dispel_by_slot_probability extends i_abstract_effect
{
	private final AbnormalType _abnormalType;
	private final int _dispelChance;

	public i_dispel_by_slot_probability(EffectTemplate template)
	{
		super(template);

		_abnormalType = AbnormalType.valueOf(getParams().getString("abnormal_type", AbnormalType.NONE.toString()).toUpperCase());
		if(_abnormalType == AbnormalType.NONE)
			_dispelChance = 0;
		else
			_dispelChance = getParams().getInteger("dispel_chance", 100);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return _dispelChance != 0;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		//TODO: [Bonux] Проверить и добавить резисты кансила.

		final List<Abnormal> abnormals = new ArrayList<Abnormal>(effected.getAbnormalList().values());
		Collections.sort(abnormals, AbnormalsComparator.getInstance()); // ToFix: Comparator to HF
		Collections.reverse(abnormals);

		for(Abnormal abnormal : abnormals)
		{
			if(!abnormal.isCancelable())
				continue;

			Skill skill = abnormal.getSkill();
			if(skill == null)
				continue;

			if(skill.isToggle())
				continue;

			if(skill.isPassive())
				continue;

			/*if(effected.isSpecialAbnormal(skill))
				continue;*/

			if(abnormal.getAbnormalType() != _abnormalType)
				continue;

			if(Rnd.chance(_dispelChance))
			{
				abnormal.exit();

				if(!abnormal.isHidden())
					effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(skill));
			}
		}
	}
}