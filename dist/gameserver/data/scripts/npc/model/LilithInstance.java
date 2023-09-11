package npc.model;

import java.util.StringTokenizer;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.model.*;



/**
 * @author L2-scripts.com - (SanyaDC)
**/
public final class LilithInstance extends NpcInstance
{
	
	private static final Location TELEPORT_POSITION = new Location(185048, -10392, -5493);


	public LilithInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		
		if(cmd.equals("requestboss"))
		{
			if (player.getParty() == null || player.getParty().getCommandChannel() == null|| player.getParty().getCommandChannel().getMemberCount() < 90)
			{
				showChatWindow(player, "default/" + getNpcId() + "-noplayers.htm", false);
				return;
			}
			else
				enterLilith(player);
			//showChatWindow(player, "default/" + getNpcId() + "-1.htm", false);
				return;
		}
		
			super.onBypassFeedback(player, command);
	}
	
	public static void enterLilith(Player ccleader)
	{
		{
			if(ccleader == null)
				return;

			if(ccleader.getParty() == null || !ccleader.getParty().isInCommandChannel())
			{
				ccleader.sendPacket(SystemMsg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_ASSOCIATED_WITH_THE_CURRENT_COMMAND_CHANNEL);
				return;
			}

			CommandChannel cc = ccleader.getParty().getCommandChannel();

			
			if(cc.getChannelLeader() != ccleader)
			{
				ccleader.sendPacket(SystemMsg.ONLY_THE_ALLIANCE_CHANNEL_LEADER_CAN_ATTEMPT_ENTRY);
				return;
			}

			
			for(Player p : cc)
				if(p.isDead() || p.isFlying() || !p.isInRange(ccleader, 500) || p.getLevel() < 76)
				{
					ccleader.sendMessage("Command Channel member " + p.getName() + " doesn't meet the requirements to enter");
					return;
				}

			for(Player p : cc)
			{
				p.teleToLocation(TELEPORT_POSITION);

			}
		}
	}
	
	}