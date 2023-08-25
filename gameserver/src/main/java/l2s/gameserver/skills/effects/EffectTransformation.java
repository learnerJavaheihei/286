package l2s.gameserver.skills.effects;

import l2s.gameserver.data.xml.holder.TransformTemplateHolder;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.TransformType;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectTransformation extends EffectHandler
{
	public EffectTransformation(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected != effector)
			return false;

		if(!effected.isPlayer())
			return false;

		int transformId = (int) getValue();
		if(transformId > 0)
		{
			TransformTemplate template = TransformTemplateHolder.getInstance().getTemplate(effected.getSex(), transformId);
			if(template == null)
				return false;

			if(template.getType() == TransformType.FLYING && effected.getX() > -166168)
				return false;
		}

		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.setTransform((int) getValue());
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(getValue() > 0)
			effected.setTransform(null);
	}
}