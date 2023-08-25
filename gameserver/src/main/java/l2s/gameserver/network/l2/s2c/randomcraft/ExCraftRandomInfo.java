package l2s.gameserver.network.l2.s2c.randomcraft;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.RandomCraftInfo;

/**
 * @author nexvill
 */
public class ExCraftRandomInfo extends L2GameServerPacket
{
	private Player _player;
	private Map<Integer, RandomCraftInfo> _randomCraftInfo = new HashMap<>();

	public ExCraftRandomInfo(Player player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		if (_player.getRandomCraftList().size() == 0)
		{
			_player.generateRandomCraftList();
		}
		
		if (_player.getCraftPoints() > 0)
		{
			_randomCraftInfo = _player.getRandomCraftList();

			if (_player.getVarBoolean("didCraft", true)) {
				writeD(5);
				for (int i = 0; i < 5; i++)
				{
					writeC(0);
					writeD(0);
					writeD(0);
					writeQ(0);
				}

				return;
			}

			writeD(5);
			for (int i : _randomCraftInfo.keySet())
			{
				RandomCraftInfo info = _randomCraftInfo.get(i);
				
				writeC(info.isLocked()); // locked slot or no
				writeD(info.getRefreshToUnlockCount()); // refresh count to unlock slot (max 20)
				writeD(info.getId()); // item id
				writeQ(info.getCount()); // item count
			}
		}
		else
		{
			writeD(5);
			for (int i = 0; i < 5; i++)
			{
				writeC(0);
				writeD(0);
				writeD(0);
				writeQ(0);
			}
		}
	}
}