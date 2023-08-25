package l2s.gameserver.network.l2.s2c.randomcraft;

import java.util.Map;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.RandomCraftInfo;
import l2s.commons.util.Rnd;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author nexvill
 */
public class ExCraftRandomMake extends L2GameServerPacket
{
	private Player _player;

	public ExCraftRandomMake(Player player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		// select random item
		int _slot = Rnd.get(5);
		RandomCraftInfo data = _player.getRandomCraftList().get(_slot);
		
		int resultId = data.getResultId() == 0 ? data.getId() : data.getResultId();

		writeC(0); // 0 for open window
		writeH(0x0F); // unk
		writeD(resultId); // item id
		writeQ(data.getCount()); // item count
		writeC(data.getEnchantLevel()); // enchant Level

		_player.getInventory().addItem(resultId, data.getCount(), data.getEnchantLevel());
		_player.sendPacket(new SystemMessagePacket(SystemMsg.CONGRATULATIONS_YOU_HAVE_RECEIVED_S1).addItemName(data.getId()));
		_player.setCraftPoints(_player.getCraftPoints() - 1, null);
		_player.setVar("didCraft", true);

		
		
		Map<Integer, RandomCraftInfo> list = _player.getRandomCraftList();
		for (int i = 0; i < 5; i++)
		{
			if (list.get(i).isLocked())
			{
				list.get(i).setRefreshToUnlockCount((byte) 20);
				list.get(i).setIsLocked(false);
			}
		}
		_player.setRandomCraftList(list);
		
		_player.sendPacket(new ExCraftRandomInfo(_player));
	}
}