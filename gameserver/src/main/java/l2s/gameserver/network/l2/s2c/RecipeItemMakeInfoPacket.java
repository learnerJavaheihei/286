package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.item.RecipeTemplate;

public class RecipeItemMakeInfoPacket extends L2GameServerPacket
{
	private final int _id;
	private final boolean _isCommon;
	private final int _status;
	private final int _curMP;
	private final int _maxMP;
	private final int chanceBonus;
	private final boolean canCrit;
	private final int critChance;

	public RecipeItemMakeInfoPacket(Player player, RecipeTemplate recipe, int status)
	{
		_id = recipe.getId();
		_isCommon = recipe.isCommon();
		_status = status;
		_curMP = (int) player.getCurrentMp();
		_maxMP = player.getMaxMp();
		chanceBonus = (int) player.getStat().calc(Stats.CRAFT_CHANCE, 0);
		critChance = (int) player.getStat().calc(Stats.CRIT_CRAFT_CHANCE, 0);
		canCrit = critChance > 0 && recipe.canCrit();

	}

	@Override
	protected final void writeImpl()
	{
		writeD(_id); //ID рецепта
		writeD(_isCommon ? 0x01 : 0x00);
		writeD(_curMP);
		writeD(_maxMP);
		writeD(_status); //итог крафта; 0xFFFFFFFF нет статуса, 0 удача, 1 провал
		writeF(0x00);	// Add Adena rate
		writeC(true);    // always true
		writeF(chanceBonus); // Craft chance bonus
		writeC(canCrit); // Critical craft available
		writeF(critChance); //  Critical craft chance
	}
}