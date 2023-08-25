package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
//TODO: Доделать.
public class EffectMPDrain extends EffectHandler
{
	private final boolean _percent;

	public EffectMPDrain(EffectTemplate template)
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
			drained = effected.getMaxMp() / 100. * drained;

		drained = Math.min(drained, effected.getCurrentMp());
		if(drained <= 0)
			return;

		effected.setCurrentMp(Math.max(0., effected.getCurrentMp() - drained), false);
		StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.DAMAGED, StatusUpdatePacket.CUR_MP);
		effector.sendPacket(su);
		effected.sendPacket(su);
		effected.broadcastStatusUpdate();
		effected.sendChanges();
		effected.sendPacket(new SystemMessagePacket(SystemMsg.S2S_MP_HAS_BEEN_DRAINED_BY_C1).addInteger(Math.round(drained)).addName(effector));

		double newMp = effector.getCurrentMp() + drained;
		newMp = Math.max(0, Math.min(newMp, effector.getMaxMp() / 100. * effector.getStat().calc(Stats.MP_LIMIT, null, null)));

		double addToMp = newMp - effected.getCurrentMp();
		if(addToMp > 0)
		{
			effector.setCurrentMp(newMp, false);
			effector.sendPacket(new StatusUpdate(effector, effector, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_MP));
			effector.broadcastStatusUpdate();
			effector.sendChanges();
			//TODO: Нужно ли какое-то сообщение для эффектора?
		}
	}
}