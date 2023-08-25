package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
//TODO: Доделать.
public class EffectHPDrain extends EffectHandler
{
	private final boolean _percent;

	public EffectHPDrain(EffectTemplate template)
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
			drained = effected.getMaxHp() / 100. * drained;

		drained = Math.min(drained, effected.getCurrentHp());
		if(drained <= 0)
			return;

		effected.setCurrentHp(Math.max(0., effected.getCurrentHp() - drained), false);
		effected.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DRAINED_YOU_OF_S2_HP).addName(effector).addInteger(Math.round(drained)));

		double newHp = effector.getCurrentHp() + drained;
		newHp = Math.max(0, Math.min(newHp, effector.getMaxHp() / 100. * effector.getStat().calc(Stats.HP_LIMIT, null, null)));

		double addToHp = newHp - effected.getCurrentHp();
		if(addToHp > 0)
		{
			effector.setCurrentHp(newHp, false);
			//TODO: Нужно ли какое-то сообщение для эффектора?
		}
	}
}