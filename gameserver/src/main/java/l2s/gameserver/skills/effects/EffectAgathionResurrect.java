package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.effects.permanent.p_preserve_abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectAgathionResurrect extends p_preserve_abnormal
{
	public EffectAgathionResurrect(EffectTemplate template)
	{
		super(template);
	}
}