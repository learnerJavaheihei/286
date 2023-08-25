package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class GMViewWarehouseWithdrawListPacket extends L2GameServerPacket
{
	private final int _type;
	private final ItemInstance[] _items;
	private String _charName;
	private long _charAdena;

	public GMViewWarehouseWithdrawListPacket(int type, Player cha)
	{
		_type = type;
		_charName = cha.getName();
		_charAdena = cha.getWarehouse().getAdena();
		_items = cha.getWarehouse().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeS(_charName);
			writeQ(_charAdena);
			writeD(_items.length);
		}
		else if(_type == 2)
		{
			writeD(_items.length);
			writeD(_items.length);
			for(ItemInstance temp : _items)
			{
				writeItemInfo(temp);
				writeD(temp.getObjectId());
			}
		}
	}
}