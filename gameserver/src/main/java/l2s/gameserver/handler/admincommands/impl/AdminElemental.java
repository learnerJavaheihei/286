package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.network.l2.components.SystemMsg;

public class AdminElemental implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_add_elemental_exp,
		admin_add_el_exp
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditChar)
			return false;

		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}

		Player targetPlayer = target.getPlayer();

		if(!targetPlayer.canUseElementals())
		{
			activeChar.sendMessage("This target cannot use elementals!");
			return false;
		}

		switch(command)
		{
			case admin_add_elemental_exp:
			case admin_add_el_exp:
				try
				{
					int elementId = Integer.parseInt(wordList[1]);
					long exp = Long.parseLong(wordList[2]);
					Elemental elemental = targetPlayer.getElementalList().get(elementId);
					if(elemental == null)
					{
						activeChar.sendMessage("Cannot found elemental for element ID: " + elementId);
						return false;
					}
					elemental.addExp(targetPlayer, exp);
					activeChar.sendMessage("Added " + exp + " EXP for element ID: " + elementId + " for player: " + targetPlayer.getName());
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //add_elemental_exp element_id[1,2,3,4] exp");
					return false;
				}
				break;
		}

		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}