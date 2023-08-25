package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExSpawnEmitterPacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_add_soul extends i_abstract_effect
{
	private final int _type;

	public i_add_soul(EffectTemplate template)
	{
		super(template);
		_type = getParams().getInteger("soul_type");
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return effector.isPlayer();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		Player player = effector.getPlayer();
		if(getValue() > 0 && effected != player)
			if (_type == 0)
				player.broadcastPacket(new ExSpawnEmitterPacket(effected, player, 2));
			else
				player.broadcastPacket(new ExSpawnEmitterPacket(effected, player, 3));
		player.setConsumedSouls(player.getConsumedSouls(_type) + (int) getValue(), _type);
	}
}
