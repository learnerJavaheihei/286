package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;

/**
 * @author Bonux
**/
public final class p_max_mp extends p_abstract_stat_effect
{
	private final boolean _heal;

	public p_max_mp(EffectTemplate template)
	{
		super(template, Stats.MAX_MP);
		_heal = getParams().getBool("heal", false);
	}

	@Override
	public void onApplied(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(!_heal || effected.isHealBlocked())
			return;

		double power = getValue();
		if(getModifierType() == StatModifierType.PER)
			power = power / 100. * effected.getMaxMp();

		if(power > 0)
		{
			//effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(power)); TODO: Проверить на оффе, должно ли быть сообщение.
			effected.setCurrentMp(effected.getCurrentMp() + power, false);
			StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_MP);
			effector.sendPacket(su);
			effected.sendPacket(su);
			effected.broadcastStatusUpdate();
			effected.sendChanges();
		}
	}
}