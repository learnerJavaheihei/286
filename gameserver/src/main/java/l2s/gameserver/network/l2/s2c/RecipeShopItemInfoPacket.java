package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.stats.Stats;

/**
 * dddddQ
 */
public class RecipeShopItemInfoPacket extends L2GameServerPacket
{
	private int _recipeId, _shopId, _curMp, _maxMp;
	private int _success = 0xFFFFFFFF;
	private long _price;
	private final int chanceBonus;
	private final boolean canCrit;
	private final int critChance;

	public RecipeShopItemInfoPacket(Player activeChar, Player manufacturer, int recipeId, long price, int success)
	{
		_recipeId = recipeId;
		_shopId = manufacturer.getObjectId();
		_price = price;
		_success = success;
		_curMp = (int) manufacturer.getCurrentMp();
		_maxMp = manufacturer.getMaxMp();
		chanceBonus = (int) manufacturer.getStat().calc(Stats.CRAFT_CHANCE, 0);
		critChance = (int) manufacturer.getStat().calc(Stats.CRIT_CRAFT_CHANCE, 0);
		canCrit = critChance > 0 && RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId).canCrit();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_shopId);
		writeD(_recipeId);
		writeD(_curMp);
		writeD(_maxMp);
		writeD(_success);
		writeQ(_price);
		writeC(0x00);	// Add Adena rate
		writeC(true);    // always true
		writeF(chanceBonus); // Craft chance bonus
		writeC(canCrit); // Critical craft available
		writeF(critChance); //  Critical craft chance
	}
}