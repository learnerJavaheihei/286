package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class p_block_debuff extends EffectHandler
{
	private class p_block_debuff_impl extends EffectHandler
	{
		private int _disabledDebuffs = 0;

		public p_block_debuff_impl(EffectTemplate template)
		{
			super(template);
		}

		@Override
		protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
		{
			return !effected.isRaid();
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.getFlags().getDebuffImmunity().start(this);
		}

		@Override
		public void onExit(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.getFlags().getDebuffImmunity().stop(this);
		}

		@Override
		public boolean checkDebuffImmunity(Abnormal abnormal, Creature effector, Creature effected)
		{
			if(_maxDebuffsDisabled > 0)
			{
				_disabledDebuffs++;

				if(effected.isPlayer() && effected.getPlayer().isGM())
					effected.sendMessage("DebuffImmunity: disabled_debuffs: " + _disabledDebuffs + " max_disabled_debuffs: " + _maxDebuffsDisabled);
				if(_disabledDebuffs >= _maxDebuffsDisabled)
				{
					effected.getAbnormalList().stop(getSkill(), false);
					if(effected.isPlayer() && effected.getPlayer().isGM())
						effected.sendMessage("DebuffImmunity: All disabled. Abnormal canceled.");
				}
			}
			return true;
		}
	}

	private final int _maxDebuffsDisabled;

	public p_block_debuff(EffectTemplate template)
	{
		super(template);
		_maxDebuffsDisabled = getTemplate().getParams().getInteger("max_disabled_debuffs", -1);
	}

	@Override
	public EffectHandler getImpl()
	{
		return new p_block_debuff_impl(getTemplate());
	}
}