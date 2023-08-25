package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.entity.boat.Shuttle;

/**
 * @author Bonux
**/
public class ExSuttleGetOnPacket extends L2GameServerPacket
{
	private int _playerObjectId, _shuttleId;
	private Location _loc;

	public ExSuttleGetOnPacket(Playable cha, Shuttle shuttle, Location loc)
	{
		_playerObjectId = cha.getObjectId();
		_shuttleId = shuttle.getBoatId();
		_loc = loc;
		if(_loc == null)
			_loc = cha.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_playerObjectId); // Player ObjID
		writeD(_shuttleId); // Shuttle ObjID
		writeD(_loc.x); // X in shuttle
		writeD(_loc.y); // Y in shuttle
		writeD(_loc.z); // Z in shuttle
	}
}