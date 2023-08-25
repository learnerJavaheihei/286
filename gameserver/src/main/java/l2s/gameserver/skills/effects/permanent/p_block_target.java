package l2s.gameserver.skills.effects.permanent;

import java.util.List;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class p_block_target extends EffectHandler
{
	public p_block_target(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isRaid())
			return false;
		return effected.isTargetable(effector);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.setTargetable(false);

		effected.abortAttack(true, true);
		effected.abortCast(true, true);

		List<Creature> characters = World.getAroundCharacters(effected);
		for(Creature character : characters)
		{
			if(character.getTarget() != effected && character.getAI().getAttackTarget() != effected && character.getAI().getCastTarget() != effected)
				continue;

			if(character.isNpc())
				((NpcInstance) character).getAggroList().remove(effected, true);

			if(character.getTarget() == effected)
				character.setTarget(null);

			if(character.getAI().getAttackTarget() == effected)
				character.abortAttack(true, true);

			if(character.getAI().getCastTarget() == effected)
				character.abortCast(true, true);

			character.sendActionFailed();
			character.getMovement().stopMove();
			character.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}

	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.setTargetable(true);
	}
}