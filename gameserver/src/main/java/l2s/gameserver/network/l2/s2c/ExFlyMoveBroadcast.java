package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.model.Player;
//import l2s.gameserver.templates.jump.JumpWay.WayType;

/**
 * @author Bonux
**/
public class ExFlyMoveBroadcast extends L2GameServerPacket
{
	private int _objId;
	//private final WayType _type;
	private final int _trackId;
	private ILocation _loc;
	private ILocation _destLoc;

	public ExFlyMoveBroadcast(Player player, /*WayType type, */int trackId, ILocation destLoc)
	{
		_objId = player.getObjectId();
		//_type = type;
		_trackId = trackId;
		_loc = player;
		_destLoc = destLoc;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);

		writeD(1/*_type.ordinal()*/);
		writeD(_trackId);

		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());

		writeD(0x00); //TODO: [Bonux]

		writeD(_destLoc.getX());
		writeD(_destLoc.getY());
		writeD(_destLoc.getZ());
	}
}
