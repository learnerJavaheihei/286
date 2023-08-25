package l2s.gameserver.skills.effects;import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * Иммобилизует и парализует на время действия.
 * @author Diamond
 * @date 24.07.2007
 * @time 5:32:46
 */
public final class EffectUntouchable extends EffectHandler
{
	public EffectUntouchable(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.setMeditated(true);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.setMeditated(false);
	}
}