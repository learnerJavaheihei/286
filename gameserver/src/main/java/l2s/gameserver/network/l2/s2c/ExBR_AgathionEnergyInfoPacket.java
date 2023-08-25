package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;

/**
 * @author VISTALL
 */
public class ExBR_AgathionEnergyInfoPacket extends L2GameServerPacket
{
	private int _size;
	private ItemInstance[] _itemList = null;

	public ExBR_AgathionEnergyInfoPacket(int size, ItemInstance... item)
	{
		_itemList = item;
		_size = size;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_size);
		for(ItemInstance item : _itemList)
		{
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeQ(0x200000);
			writeD(item.getAgathionEnergy());//current energy
			writeD(item.getTemplate().getAgathionMaxEnergy()); //max energy
		}
	}
}