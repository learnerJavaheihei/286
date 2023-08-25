package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 **/
public abstract class EffectRestore extends EffectHandler
{
	protected final boolean _ignoreBonuses;
	protected final boolean _percent;
	protected final boolean _staticPower;

	public EffectRestore(EffectTemplate template)
	{
		super(template);
		_ignoreBonuses = getParams().getBool("ignore_bonuses", false);
		_percent = getParams().getBool("percent", false);
		_staticPower = getParams().getBool("static_power", _percent || !template.isInstant());
	}
}