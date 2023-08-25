package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.StatsSet;

public class ClanGate extends Skill
{
	public ClanGate(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!super.checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first, sendMsg, trigger))
			return false;

		if(!activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return false;
		}

		IBroadcastPacket msg = Call.canSummonHere(player);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return false;
		}

		return true;
	}

	@Override
	public void onEndCast(Creature activeChar, Set<Creature> targets)
	{
		super.onEndCast(activeChar, targets);

		if(!activeChar.isPlayer())
			return;

		Player player = (Player) activeChar;
		Clan clan = player.getClan();
		clan.broadcastToOtherOnlineMembers(SystemMsg.COURT_MAGICIAN_THE_PORTAL_HAS_BEEN_CREATED, player);
	}
}
