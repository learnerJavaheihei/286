package l2s.gameserver.templates.item.upgrade;

import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 02.04.2019
 * Developed for L2-Scripts.com
 **/
public class UpgradeItemData extends ItemData {
	private final int enchantLevel;

	public UpgradeItemData(int id, long count, int enchantLevel)
	{
		super(id, count);
		this.enchantLevel = enchantLevel;
	}

	public int getEnchantLevel() {
		return enchantLevel;
	}
}
