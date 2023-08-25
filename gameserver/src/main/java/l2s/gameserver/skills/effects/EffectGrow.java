package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectGrow extends EffectHandler
{
	public EffectGrow(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) effected;
			npc.setCollisionHeightModifier(1.24);
			npc.setCollisionRadiusModifier(1.19);
		}
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) effected;
			npc.setCollisionHeightModifier(1.0);
			npc.setCollisionRadiusModifier(1.0);
		}
	}
}