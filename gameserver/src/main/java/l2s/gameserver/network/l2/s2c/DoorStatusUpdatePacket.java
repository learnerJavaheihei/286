package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.DoorInstance;

public final class DoorStatusUpdatePacket extends L2GameServerPacket
{
	private final int _staticObjectId;
	private final int _objectId;
	private final int _isClosed;
	private final int _isEnemy;
	private final int _maxHp;
	private final int _currentHp;
	private final int _damageGrade;

	public DoorStatusUpdatePacket(DoorInstance door, Player player)
	{
		_staticObjectId = door.getDoorId();
		_objectId = door.getObjectId();
		_isClosed = door.isOpen() ? 0 : 1; //opened 0 /closed 1
		_isEnemy = door.isAutoAttackable(player) ? 1 : 0;
		_currentHp = (int) door.getCurrentHp();
		_maxHp = door.getMaxHp();
		_damageGrade = door.getDamage();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_isClosed);
		writeD(_damageGrade);
		writeD(_isEnemy);
		writeD(_staticObjectId);
		writeD(_currentHp);
		writeD(_maxHp);
	}
}