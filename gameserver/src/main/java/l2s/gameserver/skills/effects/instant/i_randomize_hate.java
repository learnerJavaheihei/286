package l2s.gameserver.skills.effects.instant;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author L2-scripts.com - (SanyaDC)
 */
public class i_randomize_hate extends i_abstract_effect
{
	public i_randomize_hate(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(effected.isRaid())
			return false;
		return effected.isMonster();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{

		List<Creature> targetList = new ArrayList<>();
		if ((effected == effector) || !effected.isAttackable(effector))
		{
			return;
		}
		for (Creature target : effected.getAroundCharacters(700,500)) {
			if(target.isPlayer() && target!=effector)
				targetList.add(target);

		}
		if (targetList.isEmpty())
		{
			return;
		}
		
		Creature target2 = targetList.get(Rnd.get(targetList.size()));
		if(target2==effector)
			return;

		effected.abortAttack(true,false);
		effected.setTarget(target2);
		effected.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target2, 3000);
		effected.getAI().setAttackTarget(target2);
		effected.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target2, null);
		
	}
}
