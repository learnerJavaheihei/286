package npc.model.clanarena;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;

import instances.ClanArena;

/**
 * @author Bonux
**/
public class ArenaMachineInstance extends NpcInstance
{
	public ArenaMachineInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showMainChatWindow(Player player, boolean firstTalk, Object... replace)
	{
		Reflection reflection = getReflection();
		if(!(reflection instanceof ClanArena))
			return;

		ClanArena clanArena = (ClanArena) reflection;
		if(!clanArena.isStarted())
		{
			if(!player.isClanLeader())
			{
				showChatWindow(player, "default/" + getNpcId() + "-not_leader.htm", firstTalk);
				return;
			}

			Clan clan = player.getClan();
			if(clan == null)
				return;

			if(clan.getArenaStage() > 0)
				showChatWindow(player, "default/" + getNpcId() + "a.htm", firstTalk);
			else
				showChatWindow(player, "default/" + getNpcId() + ".htm", firstTalk);
		}
		else
		{
			if(clanArena.getExtendedTimes() >= clanArena.getTimeExtendCount())
			{
				showChatWindow(player, "default/" + getNpcId() + "-no_extend_time.htm", firstTalk);
				return;
			}

			showChatWindow(player, "default/" + getNpcId() + "b.htm", firstTalk);
		}
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -1001)
		{
			Reflection reflection = getReflection();
			if(!(reflection instanceof ClanArena))
				return;

			ClanArena clanArena = (ClanArena) reflection;
			if(!clanArena.isStarted())
			{
				if(!player.isClanLeader())
					return;

				if(reply == 1)
				{
					clanArena.startArena(player.getClan(), false);
				}
				else if(reply == 2)
				{
					clanArena.startArena(player.getClan(), true);
				}
			}
			else
			{
				if(reply == 3)
				{
					clanArena.extendTime(this, player);
				}
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
}