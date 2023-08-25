package l2s.gameserver.network.l2.s2c.randomcraft;

import java.util.Map;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.RandomCraftInfo;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExCraftRandomRefresh extends L2GameServerPacket
{
	private Player _player;

	public ExCraftRandomRefresh(Player player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		if ((_player.getCraftPoints() < 1) || (_player.getAdena() < 50_000))
			return;
		else
		{
			if (!_player.getVarBoolean("didCraft", true))
			{
				_player.setCraftPoints(_player.getCraftPoints() - 1, null);
			}
			_player.getInventory().destroyItemByItemId(57, 50_000);
			if (_player.isOnlyGainPoints())
			{
				_player.getInventory().addItem(49674, 2);
				_player.setOnlyGainPoints(false);
			}
			
			_player.generateRandomCraftList();
			_player.setVar("didCraft", false);
		}
		writeC(0);
		_player.sendPacket(new ExCraftRandomInfo(_player));
	}
}