package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class damage_block_count extends EffectHandler
{
	private double _barrierValues;

	public damage_block_count(EffectTemplate template)
	{
		super(template);
		_barrierValues = getParams().getDouble("value", 0.);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if (!effected.isPlayer())
			return false;

		Player player = effected.getPlayer();
		if (player == null)
			return false;

		if (effected.isDead() || effector.isDead())
			return false;

		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.setBarrier(_barrierValues);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected) {
		effected.setBarrier(0);
	}
	@Override
	public EffectHandler getImpl()
	{
		return this;
	}
}