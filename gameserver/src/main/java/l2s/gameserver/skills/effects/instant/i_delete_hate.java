package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 */
public final class i_delete_hate extends i_abstract_effect
{
	public i_delete_hate(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(effected.isRaid())
			return false;

		if(!effected.isMonster())
			return false;

		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		MonsterInstance monster = (MonsterInstance) effected;
		monster.getAggroList().clear(true);
		if(monster.getAI() instanceof DefaultAI)
			((DefaultAI) monster.getAI()).setGlobalAggro(System.currentTimeMillis() + monster.getParameter("globalAggro", 10000L));	//TODO: Check this.
		monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
}