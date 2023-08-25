package l2s.gameserver.skills.effects.instant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class i_dispel_by_slot extends i_abstract_effect
{
	private final AbnormalType _abnormalType;
	private final int _maxAbnormalLvl;

	public i_dispel_by_slot(EffectTemplate template)
	{
		super(template);

		_abnormalType = AbnormalType.valueOf(getParams().getString("abnormal_type", AbnormalType.NONE.toString()).toUpperCase());
		if(_abnormalType == AbnormalType.NONE)
			_maxAbnormalLvl = 0;
		else
			_maxAbnormalLvl = getParams().getInteger("max_abnormal_level", 0);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return _maxAbnormalLvl != 0;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final Creature target = isSelf() ? effector : effected;

		final List<Abnormal> abnormals = new ArrayList<Abnormal>(target.getAbnormalList().values());
		Collections.sort(abnormals, AbnormalsComparator.getInstance()); // ToFix: Comparator to HF
		Collections.reverse(abnormals);

		for(Abnormal abnormal : abnormals)
		{
			/*if(!abnormal.isCancelable())
				continue;*/

			Skill effectSkill = abnormal.getSkill();
			if(effectSkill == null)
				continue;

			if(effectSkill.isToggle())
				continue;

			if(effectSkill.isPassive())
				continue;

			if(target.isSpecialAbnormal(effectSkill))
				continue;

			if(abnormal.getAbnormalType() != _abnormalType)
				continue;

			if(_maxAbnormalLvl != -1 && abnormal.getAbnormalLvl() > _maxAbnormalLvl)
				continue;

			abnormal.exit();

			if(!abnormal.isHidden())
				target.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
		}
	}

	protected boolean isSelf()
	{
		return false;
	}
}