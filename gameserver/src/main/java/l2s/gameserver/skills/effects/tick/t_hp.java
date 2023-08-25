package l2s.gameserver.skills.effects.tick;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class t_hp extends EffectHandler
{
	private final boolean _percent;

	public t_hp(EffectTemplate template)
	{
		super(template);
		_percent = getTemplate().getParams().getBool("percent", false);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		giveDamage(effector, effected, true);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		giveDamage(effector, effected, false);
		return true;
	}

	private void giveDamage(Creature effector, Creature effected, boolean first)
	{
		if(effected.isDead())
			return;

		double hp = getValue() * getInterval(); // В PTS скриптах сила эффекта указывается без учета интервала.
		if(_percent)
			hp = effected.getMaxHp() / 100 * hp;

		if(first)
		{
			if(getSkill().isMagic() && Formulas.calcMCrit(effector, effected, getSkill()))
				hp *= 10.; // TODO: Сверить с оффом.
			else
				return;
		}

		if(hp > 0)
		{
			double heal = effected.getCurrentHp() + hp;
			heal = Math.max(0, Math.min(heal, effected.getMaxHp() / 100. * effected.getStat().calc(Stats.HP_LIMIT, null, null)));
			heal = Math.max(0, heal - effected.getCurrentHp());
			effected.setCurrentHp(effected.getCurrentHp() + heal, false, false);
			StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP);
			effector.sendPacket(su);
			effected.sendPacket(su);
			effected.broadcastStatusUpdate();
			effected.sendChanges();
		}
		else if(hp < 0)
		{
			double damage = effector.getStat().calc(getSkill().isMagic() ? Stats.INFLICTS_M_DAMAGE_POWER : Stats.INFLICTS_P_DAMAGE_POWER, Math.abs(hp), effected, getSkill());
			damage = Math.min(damage, effected.getCurrentHp() - 1);

			if(getSkill().getAbsorbPart() > 0)
				effector.setCurrentHp(getSkill().getAbsorbPart() * Math.min(effected.getCurrentHp(), damage) + effector.getCurrentHp(), false);

			boolean awake = !effected.isNpc() && effected != effector; // TODO: Check this.
			boolean standUp = effected != effector; // TODO: Check this.
			boolean directHp = effector.isNpc() || effected == effector; // TODO: Check this.
			effected.reduceCurrentHp(damage, effector, getSkill(), awake, standUp, directHp, false, false, true, false);
		}
	}
}