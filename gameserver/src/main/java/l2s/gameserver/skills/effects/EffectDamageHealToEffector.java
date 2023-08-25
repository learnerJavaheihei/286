package l2s.gameserver.skills.effects;

import java.util.Collections;
import java.util.List;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.effects.tick.t_hp;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * DOT effect with additional damahe heal gift: if effected player recieves damage, then effector and his pets will recieve HP heal with some percent.
 * Percent of absorbed heal is setted by stat damageHealToEffector.
 *
 * @author Yorie
 */
public class EffectDamageHealToEffector extends EffectHandler
{
	public class EffectDamageHealToEffectorImpl extends t_hp
	{
		private class DamageListener implements OnCurrentHpDamageListener
		{
			private final HardReference<? extends Creature> _effectorRef;
			private final HardReference<? extends Creature> _effectedRef;

			public DamageListener(Creature effector, Creature effected)
			{
				_effectorRef = effector.getRef();
				_effectedRef = effected.getRef();
			}

			@Override
			public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
			{
				Creature effector = _effectorRef.get();
				if(effector == null)
					return;

				final List<Servitor> servitors = _healServitors ? effector.getServitors() : Collections.emptyList();

				final double hp = (damage * _hpAbsorbPercent / 100) / (servitors.size() + 1);
				final double mp = (damage * _mpAbsorbPercent / 100) / (servitors.size() + 1);

				for(Servitor servitor : servitors)
				{
					if(hp > 0)
						servitor.setCurrentHp(servitor.getCurrentHp() + hp, false);
					
					if(mp > 0)
						servitor.setCurrentMp(servitor.getCurrentMp() + mp);
				}

				if(hp > 0)
				{
					effector.setCurrentHp(effector.getCurrentHp() + hp, false);
					effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addInteger(Math.round(hp)));
				}

				if(mp > 0)
				{
					effector.setCurrentMp(effector.getCurrentMp() + mp);
					effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(Math.round(mp)));
				}
			}
		}

		private DamageListener _damageListener;

		public EffectDamageHealToEffectorImpl(EffectTemplate template)
		{
			super(template);
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			_damageListener = new DamageListener(effector, effected);
			effected.addListener(_damageListener);
		}

		@Override
		public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
		{
			if(effected.isDead())
				effected.removeListener(_damageListener);

			return super.onActionTime(abnormal, effector, effected);
		}

		@Override
		public void onExit(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.removeListener(_damageListener);
		}
	}

	private final int _hpAbsorbPercent;
	private final int _mpAbsorbPercent;
	private final boolean _healServitors;

	public EffectDamageHealToEffector(EffectTemplate template)
	{
		super(template);
		_hpAbsorbPercent = getTemplate().getParams().getInteger("hp_absorb_percent", 0);
		_mpAbsorbPercent = getTemplate().getParams().getInteger("mp_absorb_percent", 0);
		_healServitors = getTemplate().getParams().getBool("heal_servitors", false);
	}

	@Override
	public EffectHandler getImpl()
	{
		return new EffectDamageHealToEffectorImpl(getTemplate());
	}
}