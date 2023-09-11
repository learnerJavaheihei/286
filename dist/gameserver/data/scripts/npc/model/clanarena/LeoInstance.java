package npc.model.clanarena;

import l2s.commons.collections.MultiValueSet;

import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

import instances.ClanArena;

/**
 * @author iqman
 * @reworked by Bonux
**/
public class LeoInstance extends NpcInstance
{
	private static final int INSTANCED_ZONE_ID = 194;

	public LeoInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showMainChatWindow(Player player, boolean firstTalk, Object... replace)
	{
		showChatWindow(player, "default/pledge_raid_keeper001.htm", firstTalk);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -1001)
		{
			if(reply == 1)
			{
				Reflection reflection = player.getActiveReflection();
				if(reflection == null)
				{
					// Попасть на Арену можно и после релога.
					for(Reflection r : ReflectionManager.getInstance().getAllByIzId(INSTANCED_ZONE_ID))
					{
						if(r.isVisitor(player))
						{
							player.setActiveReflection(reflection);
							reflection = r;
							break;
						}
					}
				}

				if(reflection != null)
				{
					if(player.canReenterInstance(INSTANCED_ZONE_ID))
						player.teleToLocation(reflection.getTeleportLoc(), reflection);
				}
				else
				{
					//TODO: Все условия и диалоги к ним обновить по оффу.
					String htmltext = checkArenaRequirements(player);
					if(htmltext != null)
					{
						showChatWindow(player, htmltext, false);
						return;
					}

					if(player.canEnterInstance(INSTANCED_ZONE_ID))
						ReflectionUtils.enterReflection(player, new ClanArena(), INSTANCED_ZONE_ID);
				}
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
	
	private String checkArenaRequirements(Player player)
	{
		if(!player.isClanLeader())
			return "default/pledge_raid_keeper002a.htm";

		Clan clan = player.getClan();
		if(clan == null)
			return "default/pledge_raid_keeper002a.htm";

		if(player.isGM())
			return null;

		Party party = player.getParty();
		CommandChannel commandChannel = party != null ? party.getCommandChannel() : null;
		if(commandChannel == null)
			return "default/pledge_raid_keeper002a.htm";

		if(!commandChannel.isLeaderCommandChannel(player))
			return "default/pledge_raid_keeper002a.htm";

		if(commandChannel.getMemberCount() > 40)
			return "default/pledge_raid_keeper002b.htm";

		for(Player member : commandChannel)
		{
			if(!member.isInRange(this, 1000))
				return "default/pledge_raid_keeper002c.htm";

			if(member.getClan() != clan)
				return "default/pledge_raid_keeper002d.htm";

			if(member.isInOlympiadMode() || Olympiad.isRegistered(member))
				return "default/pledge_raid_keeper002e.htm";

			if(!member.getReflection().isMain())
				return "default/pledge_raid_keeper002f.htm";
		}
		return null;
	}
}