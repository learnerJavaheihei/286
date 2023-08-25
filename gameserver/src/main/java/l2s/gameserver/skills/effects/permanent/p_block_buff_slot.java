package l2s.gameserver.skills.effects.permanent;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class p_block_buff_slot extends EffectHandler
{
	private final TIntSet _blockedAbnormalTypes;

	public p_block_buff_slot(EffectTemplate template)
	{
		super(template);

		_blockedAbnormalTypes = new TIntHashSet();

		String[] types = getParams().getString("abnormal_types", "").split(";");
		for(String type : types)
			_blockedAbnormalTypes.add(AbnormalType.valueOf(type.toUpperCase()).ordinal());
	}

	@Override
	public boolean checkBlockedAbnormalType(Abnormal abnormal, Creature effector, Creature effected, AbnormalType abnormalType)
	{
		if(_blockedAbnormalTypes.isEmpty())
			return false;

		return _blockedAbnormalTypes.contains(abnormalType.ordinal());
	}
}