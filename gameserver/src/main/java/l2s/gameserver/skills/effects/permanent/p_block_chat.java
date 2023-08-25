package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class p_block_chat extends EffectHandler
{
	public p_block_chat(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return effected.isPlayer();
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getPlayer().getFlags().getChatBlocked().start(this);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getPlayer().getFlags().getChatBlocked().stop(this);
	}
}