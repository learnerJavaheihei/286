package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.skillclasses.Transformation;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class EffectVisualTransformation extends EffectHandler
{
	public EffectVisualTransformation(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isTransformImmune())
			return false;

		if(effected.isInFlyingTransform())
			return false;

		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getMuted().start(this);
		effected.getFlags().getAMuted().start(this);
		effected.getFlags().getPMuted().start(this);
		effected.abortCast(true, true);
		effected.abortAttack(true, true);
		effected.setVisualTransform((int) getValue());
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.setVisualTransform(null);
		effected.getFlags().getMuted().stop(this);
		effected.getFlags().getAMuted().stop(this);
		effected.getFlags().getPMuted().stop(this);
	}
}