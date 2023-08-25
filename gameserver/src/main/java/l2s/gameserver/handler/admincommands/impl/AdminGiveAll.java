package l2s.gameserver.handler.admincommands.impl;

import java.util.HashSet;
import java.util.Set;

import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.taskmanager.DelayedItemsManager;
import l2s.gameserver.utils.ItemFunctions;

import org.napile.primitive.sets.IntSet;

public class AdminGiveAll implements IAdminCommandHandler
{
	enum Commands
	{
		admin_giveall,
		admin_giveall_online
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().UseGMShop)
			return false;

		switch(command)
		{
			case admin_giveall:
			{
				try
				{
					int id = Integer.parseInt(wordList[1]);
					long count = wordList.length >= 3 ? Long.parseLong(wordList[2]) : 1L;
					IntSet objIds = CharacterDAO.getInstance().getAllPlayersObjectIds();
					for(int objId : objIds.toArray())
					{
						if(objId == activeChar.getObjectId())
							continue;

						Player player = GameObjectsStorage.getPlayer(objId);
						if(player != null)
						{
							player.sendPacket(new CustomMessage("admincommandhandlers.AdminGiveAll.YouHaveBeenRewarded"));
							ItemFunctions.addItem(player, id, count);
						}
						else
							DelayedItemsManager.addDelayed(objId, id, count, 0, "GM reward by //giveall command.");
					}
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //giveall itemId count");
					return false;
				}
				return true;
			}
			case admin_giveall_online:
			{
				try
				{
					int id = Integer.parseInt(wordList[1]);
					long count = wordList.length >= 3 ? Long.parseLong(wordList[2]) : 1L;
					Set<String> ips = new HashSet<String>();
					for(Player player : GameObjectsStorage.getPlayers(false, false))
					{
						if(player == activeChar)
							continue;

						if(ips.contains(player.getIP()))
							continue;

						ips.add(player.getIP());
						player.sendPacket(new CustomMessage("admincommandhandlers.AdminGiveAll.YouHaveBeenRewarded"));
						ItemFunctions.addItem(player, id, count);	
					}
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //giveall_online itemId count");
					return false;
				}
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
