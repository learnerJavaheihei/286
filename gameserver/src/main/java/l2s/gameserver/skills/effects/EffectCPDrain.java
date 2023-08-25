package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
//TODO: Доделать.
public class EffectCPDrain extends EffectHandler
{
	private final boolean _percent;

	public EffectCPDrain(EffectTemplate template)
	{
		super(template);
		_percent = getParams().getBool("percent", false);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead() || effected.isRaid())
			return false;
		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead())
			return;

		if(effected == effector)
			return;

		double drained = getValue();
		if(_percent)
			drained = effected.getMaxCp() / 100. * drained;

		drained = Math.min(drained, effected.getCurrentCp());
		if(drained <= 0)
			return;

		effected.setCurrentCp(Math.max(0., effected.getCurrentCp() - drained));

		double newCp = effector.getCurrentCp() + drained;
		newCp = Math.max(0, Math.min(newCp, effector.getMaxCp() / 100. * effector.getStat().calc(Stats.CP_LIMIT, null, null)));

		double addToCp = newCp - effected.getCurrentCp();
		if(addToCp > 0)
			effector.setCurrentCp(newCp);
	}
}