package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.FinishRotatingPacket;
import l2s.gameserver.network.l2.s2c.StartRotatingPacket;

import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_align_direction extends i_abstract_effect
{
	public i_align_direction(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) effected;
			if(npc.isPeaceNpc() || npc.getNpcId() == 35062 || npc.isRaid())
				return false;

			NpcInstance leader = npc.getLeader();
			if(leader != null && leader.isRaid())
				return false;
		}
		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		effected.broadcastPacket(new StartRotatingPacket(effected, effected.getHeading(), 1, 65535));
		effected.broadcastPacket(new FinishRotatingPacket(effected, effector.getHeading(), 65535));
		effected.setHeading(effector.getHeading());
	}
}