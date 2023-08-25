package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;

/**
 * @author Bonux
**/
public final class p_max_cp extends p_abstract_stat_effect
{
	private final boolean _restore;

	public p_max_cp(EffectTemplate template)
	{
		super(template, Stats.MAX_CP);
		_restore = getParams().getBool("restore", false);
	}

	@Override
	public void onApplied(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(!_restore || effected.isHealBlocked())
			return;

		double power = getValue();
		if(getModifierType() == StatModifierType.PER)
			power = power / 100. * effected.getMaxCp();

		if(power > 0)
		{
			//effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger(power)); TODO: Проверить на оффе, должно ли быть сообщение.
			effected.setCurrentCp(effected.getCurrentCp() + power, false);
		}
	}
}