package l2s.gameserver.skills.effects;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.actor.OnAttackListener;
import l2s.gameserver.listener.actor.OnMagicUseListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * reworked by Bonux
**/
public final class EffectDispelOnHit extends EffectHandler
{
	private class EffectDispelOnHitImpl extends EffectHandler
	{
		private class AttackListener implements OnAttackListener, OnMagicUseListener
		{
			private final Abnormal _abnormal;
			private final HardReference<? extends Creature> _effectorRef;
			private final HardReference<? extends Creature> _effectedRef;

			public AttackListener(Abnormal abnormal, Creature effector, Creature effected)
			{
				_abnormal = abnormal;
				_effectorRef = effector.getRef();
				_effectedRef = effected.getRef();
			}

			@Override
			public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
			{
				// TODO: [Bonux] Проверить, должен ли распространяться эффект на магию. 
				if(!skill.isDebuff())
					return;

				EffectDispelOnHitImpl.this.onAttack(_abnormal, _effectorRef.get(), _effectedRef.get());
			}

			@Override
			public void onAttack(Creature actor, Creature target)
			{
				EffectDispelOnHitImpl.this.onAttack(_abnormal, _effectorRef.get(), _effectedRef.get());
			}
		}

		private AttackListener _listener;
		private int _hitCount = 0;

		public EffectDispelOnHitImpl(EffectTemplate template)
		{
			super(template);
		}

		private void onAttack(Abnormal abnormal, Creature effector, Creature effected)
		{
			_hitCount++;
			if(_hitCount >= _maxHitCount)
				effected.getAbnormalList().stop(getSkill(), false);
		}

		@Override
		protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
		{
			return !effected.isRaid();
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			_listener = new AttackListener(abnormal, effector, effected);
			effected.addListener(_listener);
		}

		@Override
		public void onExit(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.removeListener(_listener);
			_listener = null;
		}
	}

	private final int _maxHitCount;

	public EffectDispelOnHit(EffectTemplate template)
	{
		super(template);
		_maxHitCount = getTemplate().getParams().getInteger("max_hits", 0);
	}

	@Override
	public EffectHandler getImpl()
	{
		return new EffectDispelOnHitImpl(getTemplate());
	}
}