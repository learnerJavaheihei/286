package l2s.gameserver.skills.effects;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectBetray extends EffectHandler
{
	public EffectBetray(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return effected.isSummon();
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		Servitor summon = (Servitor) effected;
		summon.setDepressed(true);
		summon.getAI().Attack(summon.getPlayer(), true, false);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		Servitor summon = (Servitor) effected;
		summon.setDepressed(false);
		summon.getAI().setIntention(AI_INTENTION_ACTIVE);
	}
}