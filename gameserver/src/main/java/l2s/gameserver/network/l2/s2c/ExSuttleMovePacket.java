package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.entity.boat.Shuttle;

/**
 * @author Bonux
**/
public class ExSuttleMovePacket extends L2GameServerPacket
{
	private final Shuttle _shuttle;
	private final Location _destination;

	public ExSuttleMovePacket(Shuttle shuttle)
	{
		_shuttle = shuttle;
		_destination = shuttle.getMovement().getDestination();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_shuttle.getBoatId()); // Shuttle ObjID
		writeD(_shuttle.getMoveSpeed()); // Speed
		writeD(_shuttle.getRotationSpeed()); // Rotation Speed
		writeD(_destination.getX()); // Destination X
		writeD(_destination.getY()); // Destination Y
		writeD(_destination.getZ()); // Destination Z
	}
}