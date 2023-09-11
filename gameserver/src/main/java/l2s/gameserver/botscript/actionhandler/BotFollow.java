
package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;

public class BotFollow implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isFollowMove())
		{
			return false;
		}
		Party party = actor.getParty();
		if(party == null)
		{
			return false;
		}
		if(simpleActionDisable || !movable)
		{
			return false;
		}
		Player leader = party.getPartyLeader();
		if(actor == leader)
		{
			return false;
		}
		double distance = leader.getDistance((GameObject) actor);
		int zDiff = Math.abs(leader.getZ() - actor.getZ());
		if(distance > 2500.0 || distance <= config.getFollowInstance() || zDiff >= 500)
		{
			return false;
		}
		actor.getMovement().moveToLocation(leader.getLoc(), config.getFollowInstance(), !actor.getVarBoolean("no_pf"));
		return true;
	}
}