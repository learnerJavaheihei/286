package l2s.gameserver.network.l2.s2c.blessing;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExBlessOptionPutItem extends L2GameServerPacket
{
	private Player _player;
	private int _itemObjId;
	
	public ExBlessOptionPutItem(Player player, int itemObjId)
	{
		_player = player;
		_itemObjId = itemObjId;
	}

	@Override
	protected final void writeImpl()
	{
		ItemInstance item = _player.getInventory().getItemByObjectId(_itemObjId);
		if (item == null)
		{
			writeC(0);
			return;
		}
		
		if (item.isBlessed() || !item.getTemplate().isBlessable())
		{
			writeC(0);
			return;
		}
		
		writeC(1); // success put or no
	}
}