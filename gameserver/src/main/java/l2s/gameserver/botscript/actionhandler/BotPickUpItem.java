package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.botscript.MonsterSelectUtil;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.Geometry;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.ItemFunctions;

import java.util.stream.Collectors;

public class BotPickUpItem implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean checkMovable, boolean checkActionDisable)
	{
		MonsterInstance mob;
		if(!config.isPickUpItem())
		{
			return false;
		}
		if(!checkMovable)
		{
			return false;
		}
		if(!config.isPickUpFirst() && (mob = MonsterSelectUtil.findHatingMeMonster(actor)) != null)
		{
			return false;
		}
		if(actor.getAI().getNextAction() == PlayableAI.AINextAction.PICKUP)
		{
			return true;
		}
		Party party = actor.getParty();
		if(party != null)
		{
			config = this.getConfig(party.getPartyLeader());
		}
		for(ItemInstance item : World.getAroundObjects(actor).stream().filter(obj -> obj.isItem()).map(obj -> (ItemInstance) obj).collect(Collectors.toList()))
		{
			if(item == null || !ItemFunctions.checkIfCanPickup(actor, item) || !Geometry.calc(actor, item) || !(actor.getDistance(item) <= 1000.0) || !GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), item.getX(), item.getY(),item.getZ(), actor.getGeoIndex()))
				continue;
			actor.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item, null);
			return true;
		}
		return false;
	}
}