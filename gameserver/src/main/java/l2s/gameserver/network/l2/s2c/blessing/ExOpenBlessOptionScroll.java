package l2s.gameserver.network.l2.s2c.blessing;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExOpenBlessOptionScroll extends L2GameServerPacket
{
	private ItemInstance _scroll;
	private Player _player;
	
	public ExOpenBlessOptionScroll(Player player, ItemInstance scroll)
	{
		_player = player;
		_scroll = scroll;
	}

	@Override
	protected final void writeImpl()
	{
		_player.setBlessingScroll(_scroll);
		writeD(_scroll.getItemId()); // scroll id
	}
}