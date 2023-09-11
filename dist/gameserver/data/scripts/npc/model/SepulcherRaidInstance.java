package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import manager.FourSepulchersSpawn;

public class SepulcherRaidInstance extends RaidBossInstance
{
	public int mysteriousBoxId = 0;

	public SepulcherRaidInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		Player player = killer.getPlayer();
		if(player != null)
			giveCup(player);

		super.onDeath(killer);
	}

	@Override
	protected void onDelete()
	{
		FourSepulchersSpawn.spawnEmperorsGraveNpc(this, mysteriousBoxId);
		super.onDelete();
	}

	private void giveCup(Player player)
	{
		int cupId = 0;
		int oldBrooch = 7262;

		switch(getNpcId())
		{
			case 25339:
				cupId = 7256;
				break;
			case 25342:
				cupId = 7257;
				break;
			case 25346:
				cupId = 7258;
				break;
			case 25349:
				cupId = 7259;
				break;
		}

		Party party = player.getParty();
		if(party != null) {
			for (Player mem : party.getPartyMembers()) {
				if (mem.getInventory().getItemByItemId(oldBrooch) == null && player.isInRange(mem, Config.ALT_PARTY_DISTRIBUTION_RANGE))
					ItemFunctions.addItem(mem, cupId, 1/*, "Give sup for party by SepulcherRaidInstance"*/);
			}
		}
		else
		{
			if(player.getInventory().getItemByItemId(oldBrooch) == null)
				ItemFunctions.addItem(player, cupId, 1/*, "Give sup for player by SepulcherRaidInstance"*/);
		}
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}