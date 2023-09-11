package npc.model.heavenlyrift;

import java.util.StringTokenizer;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

import manager.HeavenlyRift;

/**
 * @reworked by Bonux
 */
public class DimensionalVortexInstance extends NpcInstance
{
	public static final int REQUIRED_ITEM_ID = 49759;	// Небесный Осколок

	public DimensionalVortexInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		if(cmd.equals("tryenter"))
		{
			if(ItemFunctions.getItemCount(player, REQUIRED_ITEM_ID) >= 1)
			{
				if(HeavenlyRift.getInstance().isInProgress())
				{
					showChatWindow(player, "default/" + getNpcId() + "-4.htm", false);
					return;
				}

				if(player.isGM())
				{
					if(HeavenlyRift.getInstance().start(1))
					{
						ItemFunctions.deleteItem(player, REQUIRED_ITEM_ID, 1, true);
						player.teleToLocation(112685, 13362, 10966);
					}
					return;
				}
				
				if(!player.isInParty())
				{
					player.sendPacket(SystemMsg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
					return;
				}

				Party party = player.getParty();
				if(!party.isLeader(player))
				{
					player.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
					return;
				}
				
				for(Player partyMember : party.getPartyMembers())
				{
					if(!player.isInRange(partyMember.getLoc(), 1000))
					{
						SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
						sm.addName(partyMember);
						party.broadcastToPartyMembers(player, sm);
						return;
					}
				}

				if(HeavenlyRift.getInstance().start())
				{
					ItemFunctions.deleteItem(player, REQUIRED_ITEM_ID, 1, true);

					for(Player partyMember : party.getPartyMembers())
						partyMember.teleToLocation(112685, 13362, 10966);
				}
			}
			else
			{
				showChatWindow(player, "default/" + getNpcId() + "-3.htm", false);
			}
		}	
		else if(cmd.equals("exchange"))
		{
			long count_have = ItemFunctions.getItemCount(player, 49767);
			if(count_have < 10) //exchange ratio 10:1
			{
				showChatWindow(player, "default/" + getNpcId() + "-2.htm", true);
				return;	
			}

			if(count_have % 10 != 0) //odd
				count_have -= count_have % 10;

			long to_give = count_have / 10;

			ItemFunctions.deleteItem(player, 49767, count_have); //will notify
			ItemFunctions.addItem(player, 49759, to_give); //will notify
		}
		else
			super.onBypassFeedback(player, command);
	}
}