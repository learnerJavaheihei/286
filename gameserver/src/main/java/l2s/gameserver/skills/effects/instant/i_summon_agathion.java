package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.data.xml.holder.AgathionHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Agathion;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.agathion.AgathionTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public class i_summon_agathion extends i_abstract_effect
{
	private static final Logger _log = LoggerFactory.getLogger(i_summon_agathion.class);

	private final AgathionTemplate _template;

	public i_summon_agathion(EffectTemplate template)
	{
		super(template);

		int id = getTemplate().getParams().getInteger("id", (int) getValue());
		int unk = getTemplate().getParams().getInteger("unk", 1);
		_template = AgathionHolder.getInstance().getTemplate(id);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(!effected.isPlayer())
			return false;

		if(_template == null)
		{
			_log.warn(getClass().getSimpleName() + ": Cannot find agathion template for skill: ID[" + getSkill().getId() + "], LEVEL[" + getSkill().getLevel() + "]!");
			return false;
		}

		Player player = effected.getPlayer();
		if(player.getAgathion() != null)
		{
			player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
			return false;
		}
		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		Player player = effected.getPlayer();
		if(player == null)
			return;

		Agathion agathion = new Agathion(player, _template, getSkill());
		agathion.init();
	}
}