package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.PackageSendableListPacket;

/**
 * @author VISTALL
 * @date 20:35/16.05.2011
 */
public class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected boolean readImpl()
	{
		_objectId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new PackageSendableListPacket(1, _objectId, player));
		player.sendPacket(new PackageSendableListPacket(2, _objectId, player));
	}
}
