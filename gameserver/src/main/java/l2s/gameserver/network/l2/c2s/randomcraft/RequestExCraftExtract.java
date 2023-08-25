package l2s.gameserver.network.l2.c2s.randomcraft;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.randomcraft.ExCraftExtract;

/**
 * @author nexvill
 */
public class RequestExCraftExtract extends L2GameClientPacket
{
	private int _count;
	private int[] _objectId = new int[128];
	private int[] _itemCount = new int[128];
	
	@Override
	protected boolean readImpl()
	{
		_count = readD();
		for (int i = 0; i < _count; i++)
		{
			int id = readD();
			int cnt = readD();
			_objectId[i] = id;
			_itemCount[i] = cnt;
			readD(); // 0
		}
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExCraftExtract(activeChar, _count, _objectId, _itemCount));
	}
}