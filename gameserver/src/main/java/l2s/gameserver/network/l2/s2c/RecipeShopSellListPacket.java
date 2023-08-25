package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.stats.Stats;

public class RecipeShopSellListPacket extends L2GameServerPacket
{
	private int objId, curMp, maxMp;
	private long adena;
	private Collection<ManufactureItem> createList;
	private final int chanceBonus;
	private final int critChance;

	public RecipeShopSellListPacket(Player buyer, Player manufacturer)
	{
		objId = manufacturer.getObjectId();
		curMp = (int) manufacturer.getCurrentMp();
		maxMp = manufacturer.getMaxMp();
		adena = buyer.getAdena();
		createList = manufacturer.getCreateList().values();
		chanceBonus = (int) manufacturer.getStat().calc(Stats.CRAFT_CHANCE, 0);
		critChance = (int) manufacturer.getStat().calc(Stats.CRIT_CRAFT_CHANCE, 0);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(objId);
		writeD(curMp);//Creator's MP
		writeD(maxMp);//Creator's MP
		writeQ(adena);
		writeD(createList.size());
		for(ManufactureItem mi : createList)
		{
			writeD(mi.getRecipeId());
			writeD(0x00); // Can craft
			writeQ(mi.getCost());
			writeF(chanceBonus); // Craft chance bonus
			writeC(critChance > 0 && mi.canCrit()); // Critical craft available
			writeF(critChance); //  Critical craft chance
		}
	}
}