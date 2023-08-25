package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectLDManaDamOverTime extends EffectHandler
{
	public EffectLDManaDamOverTime(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead() || effected.isRaid())
			return false;
		return true;
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead())
			return false;

		double manaDam = getValue();
		manaDam *= effected.getLevel() / 2.4;

		if(manaDam > effected.getCurrentMp() && getSkill().isToggle())
		{
			effected.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}

		effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}