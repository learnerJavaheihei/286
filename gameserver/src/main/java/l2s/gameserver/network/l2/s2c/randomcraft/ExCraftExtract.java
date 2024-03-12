package l2s.gameserver.network.l2.s2c.randomcraft;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;

/**
 * @author nexvill
 */
public class ExCraftExtract extends L2GameServerPacket
{
	private Player _player;
	private int _count;
	private int[] _objectId, _itemCount;

	public ExCraftExtract(Player player, int count, int[] objectId, int[] itemCount)
	{
		_player = player;
		_count = count;
		_objectId = objectId;
		_itemCount = itemCount;
	}

	@Override
	protected final void writeImpl()
	{
		for (int i = 0; i < _count; i++) {
			if (_itemCount[i] <= 0) {
				_player.sendPacket(new ExShowScreenMessage("数据异常",5000,ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER,true));
				_player.sendPacket(new ExCraftInfo(_player));
				writeC(0);
				return;
			}
		}
		int points = 0;
		int fee = 0;
		for (int i = 0; i < _count; i++)
		{
			ItemInstance item = _player.getInventory().getItemByObjectId(_objectId[i]);
			if (item != null)
			{
				points += item.getTemplate().getGrindPoint() * _itemCount[i];
				fee += item.getTemplate().getGrindCommission() * _itemCount[i];
			}
		}
		if (_player.getAdena() < fee)
			return;
		
		for (int i = 0; i < _count; i++)
		{
			ItemInstance item = _player.getInventory().getItemByObjectId(_objectId[i]);
			if (item != null)
				_player.getInventory().destroyItem(item,_itemCount[i]);
			
		}
		_player.getInventory().destroyItemByItemId(57, fee);
		_player.setCraftGaugePoints(_player.getCraftGaugePoints() + points, null);
		writeC(0);
	}
}