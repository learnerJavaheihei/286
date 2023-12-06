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
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.AbnormalsComparator;

import static l2s.gameserver.skills.AbnormalType.INVINCIBILITY;

/**
 * @author Bonux
**/
public class i_dispel_by_category extends i_abstract_effect
{
	private static enum AbnormalCategory
	{
		slot_buff,
		slot_debuff
	}

	private final AbnormalCategory _abnormalCategory;
	private final int _dispelChance;
	private final int _maxCount;

	public i_dispel_by_category(EffectTemplate template)
	{
		super(template);

		_abnormalCategory = getParams().getEnum("abnormal_category", AbnormalCategory.class);
		_dispelChance = getParams().getInteger("dispel_chance", 100);
		_maxCount = getParams().getInteger("max_count", 0);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(_dispelChance == 0 || _maxCount == 0)
			return false;
		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final List<Abnormal> effects = new ArrayList<Abnormal>(effected.getAbnormalList().values());
		Collections.sort(effects, AbnormalsComparator.getInstance()); // ToFix: Comparator to HF
		Collections.reverse(effects);

		if(_abnormalCategory == AbnormalCategory.slot_debuff)
		{
			int dispelled = 0;
			for(Abnormal abnormal : effects)
			{
				if(!abnormal.isCancelable())
					continue;

				Skill effectSkill = abnormal.getSkill();
				if(effectSkill == null)
					continue;

				if(!abnormal.isOffensive())
					continue;

				if(effectSkill.isToggle())
					continue;

				if(effectSkill.isPassive())
					continue;

				if(effected.isSpecialAbnormal(effectSkill))
					continue;

				if(effectSkill.getMagicLevel() <= 0)
					continue;

				if(_maxCount <= 0 || dispelled >= _maxCount)
					break;
				if(Rnd.chance(_dispelChance))
				{
					abnormal.exit();
					dispelled++;
					if(!abnormal.isHidden())
						effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
				}
			}
		}
		else if(_abnormalCategory == AbnormalCategory.slot_buff && !effected.getAbnormalEffects().contains(AbnormalEffect.INVINCIBILITY))
		{
			int dispelled = 0;
			for(Abnormal abnormal : effects)
			{
				if(!abnormal.isCancelable())
					continue;

				Skill effectSkill = abnormal.getSkill();
				if(effectSkill == null)
					continue;

				if(abnormal.isOffensive())
					continue;

				if(effectSkill.isToggle())
					continue;

				if(effectSkill.isPassive())
					continue;

				if(effected.isSpecialAbnormal(effectSkill))
					continue;

				if(effectSkill.getMagicLevel() <= 0)
					continue;

				if(_maxCount <= 0 || dispelled >= _maxCount)
					break;

				if(Formulas.calcCancelSuccess(effector, effected, _dispelChance, getSkill(), abnormal))
				{
					abnormal.exit();
					dispelled++;
					if(!abnormal.isHidden())
						effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
				}
			}
		}
	}
}