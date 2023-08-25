package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse.ItemClassComparator;
import l2s.gameserver.model.items.Warehouse.WarehouseType;

public class WareHouseWithdrawListPacket extends L2GameServerPacket
{
	private final int _type;
	private final long _adena;
	private List<ItemInfo> _itemList = Collections.emptyList();
	private final int _whType;
	private final int _inventoryUsedSlots;

	public WareHouseWithdrawListPacket(int type, Player player, WarehouseType whType)
	{
		_type = type;
		_adena = player.getAdena();
		_whType = whType.ordinal();
		_inventoryUsedSlots = player.getInventory().getSize();

		ItemInstance[] items;
		switch(whType)
		{
			case PRIVATE:
				items = player.getWarehouse().getItems();
				break;
			case FREIGHT:
				items = player.getFreight().getItems();
				break;
			case CLAN:
			case CASTLE:
				items = player.getClan().getWarehouse().getItems();
				break;
			default:
				return;
		}

		_itemList = new ArrayList<ItemInfo>(items.length);
		Arrays.sort(items, ItemClassComparator.getInstance());
		for(ItemInstance item : items)
			_itemList.add(new ItemInfo(item));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			if(_whType == WarehouseType.FREIGHT.ordinal())
				writeH(1);
			else
				writeH(_whType);
			writeQ(_adena);
			writeD(_inventoryUsedSlots); //Количество занятых ячеек в инвентаре.
			writeD(_itemList.size());
		}
		else if(_type == 2)
		{
			writeH(0x00);
			/*if(_whType == WarehouseType.PRIVATE.ordinal() || _whType == WarehouseType.CLAN.ordinal())
			{
				if(_itemList.size() > 0)
					writeD(_itemList.get(0).getItemId());
				else
					writeD(0x00);
			}
			if(_whType == WarehouseType.CLAN.ordinal())
				writeD(0x00);*/
			writeD(_itemList.size());
			writeD(_itemList.size());
			for(ItemInfo item : _itemList)
			{
				writeItemInfo(item);
				writeD(item.getObjectId());
				writeD(0);
				writeD(0);
			}
		}
	}
}