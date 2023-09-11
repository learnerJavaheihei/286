package handler.voicecommands;

import l2s.gameserver.config.xml.holder.VoteRewardConfigHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;
import l2s.gameserver.network.l2.components.CustomMessage;

import java.util.Collection;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 14.02.2019
 * Developed for L2-Scripts.com
 **/
public class VoteReward extends ScriptVoiceCommandHandler
{
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		Collection<VoteRewardSite> sites = VoteRewardConfigHolder.getInstance().getVoteRewardSites();
		if(sites.isEmpty())
			return false;

		boolean received = false;
		for(VoteRewardSite site :sites ) {
			if(site.isEnabled()) {
				if (site.tryGiveRewards(activeChar))
					received = true;
			}
		}

		if (!received)
			activeChar.sendPacket(new CustomMessage("votereward.not_have_votes"));

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VoteRewardConfigHolder.REWARD_COMMANDS;
	}
}
