package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 */
public class i_soul_shot extends i_abstract_effect
{
	private final double _power;

	public i_soul_shot(EffectTemplate template)
	{
		super(template);
		_power = getParams().getDouble("power", 100.);
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		effected.sendPacket(SystemMsg.YOUR_SOULSHOTS_ARE_ENABLED);
		effected.setChargedSoulshotPower(_power);
	}
}