package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
 * @author nexvill
 */
public class ExBowActionTo extends L2GameServerPacket
{
	private int obj_id;

	public ExBowActionTo(Player player)
	{
		obj_id = player.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);
	}
}