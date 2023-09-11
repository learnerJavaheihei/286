package npc.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.MapUtils;

/**
 * @author nexvill
 */
public class FreyaInstance extends NpcInstance
{
	private static final Map<Player, Integer> _playerFireRage = new ConcurrentHashMap<>();
	private static final SkillEntry FREYA_SAFETY_ZONE = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50052, 1);

	public FreyaInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if (ask == -29109)
		{
			if (reply == 1)
			{
				Reflection reflection = getReflection();
				if (_playerFireRage.size() == 0)
				{
					for (Player ppl : reflection.getPlayers())
					{
						_playerFireRage.put(ppl, 0);
					}
				}
				
				if (player.getAbnormalList().contains(50050))
				{
					final int playerFireRage = _playerFireRage.getOrDefault(player, 0);
					if (playerFireRage < 5)
					{
						_playerFireRage.put(player, playerFireRage + 1);
						for (Creature ppl : this.getAroundCharacters(500, 500))
						{
							ppl.getAbnormalList().stop(50050);
						}
						doCast(FREYA_SAFETY_ZONE, player, true);
						sendMessage(this, reflection, "Bless with you. Lets finish fight!");
						return;
					}
					else
					{
						player.sendMessage("Freya: You cannot use my power again.");
						sendMessage(this, reflection, "You cannot use my power again.");
					}
					return;
				}
				sendMessage(this, reflection, "I help you only when you affected by Fire Rage skill.");
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
	
	private void sendMessage(NpcInstance npc, Reflection reflection, String text)
	{
		int rx = MapUtils.regionX(npc);
		int ry = MapUtils.regionY(npc);
		
		for (Player ppl : reflection.getPlayers())
		{
			int tx = MapUtils.regionX(ppl) - rx;
			int ty = MapUtils.regionY(ppl) - ry;

			if(tx*tx + ty*ty <= Config.SHOUT_SQUARE_OFFSET || ppl.isInRangeZ(npc, Config.CHAT_RANGE))
				ppl.sendPacket(new NSPacket(npc, ChatType.NPC_SHOUT, text));
		}
	}
}
