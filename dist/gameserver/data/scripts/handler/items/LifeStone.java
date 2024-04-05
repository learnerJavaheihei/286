package handler.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExPutIntensiveResultForVariationMake;
import l2s.gameserver.network.l2.s2c.ExShowVariationMakeWindow;

public class LifeStone extends SimpleItemHandler
{
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		switch(itemId)
		{
			case 70755: // Life Stone Lv. 1 - Hair Accessory
			case 70759: // Life Stone Lv. 1 - Hair Accessory (Event)
			case 70756: // Life Stone Lv. 2 - Hair Accessory
			case 70760: // Life Stone Lv. 2 - Hair Accessory (Event)
			case 70889: // Life Stone Lv. 1 - Ancient Kingdom
			case 70890: // Life Stone Lv. 1 - Elmoreden
			case 70891: // Life Stone Lv. 1 - Aden
			case 70892: // Life Stone Lv. 1 - Elmore
			case 70893: // Life Stone Lv. 1 - Ferios
			case 71126: // Life Stone Lv. 2 - Ancient Kingdom
			case 71127: // Life Stone Lv. 2 - Elmoreden
			case 71128: // Life Stone Lv. 2 - Aden
			case 71129: // Life Stone Lv. 2 - Elmore
			case 71130: // Life Stone Lv. 2 - Ferios
			case 94185: // Life Stone Lv. 1 - Weapon
			case 94186: // Life Stone Lv. 2 - Weapon
			case 94187: // Life Stone Lv. 1 - Armor
			case 94188: // Life Stone Lv. 2 - Armor
			case 94209: // Life Stone Lv. 1 - Weapon (Event)
			case 94210: // Life Stone Lv. 2 - Weapon (Event)
			case 94211: // Life Stone Lv. 1 - Armor (Event)
			case 94212: // Life Stone Lv. 2 - Armor (Event)
			case 94303: // Life Stone - Heroic Circlet
			case 94304: // Life Stone - Heroic Circlet (Event)
			case 94423: // Life Stone - Accessory
			case 94424: // Life Stone - Accessory (Event)
			case 80017: // 新增BOSS首飾生命石
				player.sendPacket(SystemMsg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
				player.sendPacket(new ExPutIntensiveResultForVariationMake(item.getObjectId(), itemId, 0, 0));
				break;
			default:
				return false;
		}

		return true;
	}
}
