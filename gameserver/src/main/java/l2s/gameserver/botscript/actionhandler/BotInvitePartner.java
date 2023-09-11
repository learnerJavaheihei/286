package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.JoinPartyPacket;

public class BotInvitePartner implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		BotConfigImp botConfig = (BotConfigImp) config;
		if(botConfig.getPartyMemberHolder().isEmpty())
		{
			return false;
		}
		if(actor.isLogoutStarted())
		{
			return false;
		}
		if(actor.getParty() != null && actor.getParty().getPartyLeader() != actor)
		{
			return false;
		}
		botConfig.getPartyMemberHolder().forEach((name, invite) -> {
			if(invite.booleanValue())
			{
				BotConfigImp targetBotConfig;
				Player target = GameObjectsStorage.getPlayer(name);
				if(target == null)
				{
					return;
				}
				if(target.getParty() != null)
				{
					return;
				}
				if(target.isInOlympiadMode() || target.isInOfflineMode())
				{
					return;
				}
				if(target.getUptime() > 5000L && (targetBotConfig = (BotConfigImp) BotEngine.getInstance().getBotConfig(target)).getLeaderName().equals(actor.getName()))
				{
					Party party = actor.getParty();
					if(party == null)
					{
						party = new Party(actor, botConfig.getLootType().ordinal());
						actor.setParty(party);
					}
					if(party.getMemberCount() < Party.MAX_SIZE)
					{
						target.joinParty(party, false);
						actor.sendPacket((IBroadcastPacket) JoinPartyPacket.SUCCESS);
					}
				}
			}
		});
		return false;
	}
}