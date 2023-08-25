package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 */
public final class i_fishing_shot extends i_abstract_effect
{
	private final double _power;

	public i_fishing_shot(EffectTemplate template)
	{
		super(template);
		_power = getParams().getDouble("power", 100.);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return effected.isPlayer();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		effected.sendPacket(SystemMsg.YOUR_SPIRITSHOT_HAS_BEEN_ENABLED); // TODO: Check message.
		effected.getPlayer().setChargedFishshotPower(_power);
	}
}