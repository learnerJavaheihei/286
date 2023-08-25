package l2s.gameserver.skills.skillclasses;

import java.util.List;
import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.TrapInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.StatsSet;

public class TrapActivation extends Skill
{
	
	public final int _range;
	
	public TrapActivation(StatsSet set)
	{
		super(set);
		_range = set.getInteger("trapRange", 600);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first, sendMsg, trigger))
			return false;

		if(!activeChar.isPlayer())
			return false;

		List<TrapInstance> traps = activeChar.getPlayer().getTraps();
		if(traps.size() != 1)
			return false;

		if(activeChar.getDistance(traps.get(0)) > _range) //max range to cast.
			return false;

		return true;
	}

	@Override
	public void onEndCast(Creature activeChar, Set<Creature> targets)
	{
		super.onEndCast(activeChar, targets);

		if(!activeChar.isPlayer())
			return;

		List<TrapInstance> traps = activeChar.getPlayer().getTraps();
		if(traps.isEmpty())
			return;

		traps.get(0).selfDestroy();
	}
}