package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 */
public final class i_refresh_instance extends i_abstract_effect
{
	public i_refresh_instance(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return effected.isPlayer();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		Player player = effected.getPlayer();
		if(player != null)
		{
			int instanceId = (int) getValue();
			if(instanceId == -1)
				player.removeAllInstanceReuses();
			else
				player.removeInstanceReuse(instanceId);
		}
	}
}