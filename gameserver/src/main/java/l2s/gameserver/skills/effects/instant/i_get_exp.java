package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_get_exp extends i_abstract_effect
{
	private final long _power;
	private final int _percentPower;
	private final int _percentPowerMaxLvl;

	public i_get_exp(EffectTemplate template)
	{
		super(template);
		_power = getParams().getLong("power");
		_percentPower = getParams().getInteger("percent_power", 0);
		_percentPowerMaxLvl = getParams().getInteger("percent_power_max_lvl", 0);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return effected.isPlayer();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final Player player = effected.getPlayer();

		long power = _power;
		if(_percentPowerMaxLvl != 0 && player.getLevel() < _percentPowerMaxLvl)
			power = (long) (player.getExp() / 100. * _percentPower);

		player.addExpAndSp(power, 0);
	}
}
