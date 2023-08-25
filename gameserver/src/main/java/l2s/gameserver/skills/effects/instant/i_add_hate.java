package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_add_hate extends i_abstract_effect
{
	private final boolean _affectSummoner;

	public i_add_hate(EffectTemplate template)
	{
		super(template);
		_affectSummoner = getParams().getBool("affect_summoner", false);
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
		Creature target = effector;
		if(_affectSummoner)
		{
			Player owner = target.getPlayer();
			if(owner != null)
				target = owner;
		}

		if(getValue() > 0)
		{
			((MonsterInstance) effected).getAggroList().addDamageHate(target, 0, (int) getValue());
		}
		else if(getValue() < 0)
		{
			((MonsterInstance) effected).getAggroList().reduceHate(target, (int) -getValue());
		}
	}
}
