package l2s.gameserver.core;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public class DeleteObjectPacket extends L2GameServerPacket
{
	private int _objectId;

	public DeleteObjectPacket(GameObject obj)
	{
		_objectId = obj.getObjectId();
	}

	public DeleteObjectPacket(int objectId)
	{
		_objectId = objectId;
	}

	protected final void writeImpl()
	{
		Player activeChar = (this.getClient()).getActiveChar();
		if(activeChar == null || activeChar.getObjectId() == _objectId)
		{
			return;
		}
		this.writeD(_objectId);
		this.writeD(1);
	}

	public String getType()
	{
		return String.valueOf(super.getType()) + " " + GameObjectsStorage.findObject(_objectId) + " (" + _objectId + ")";
	}
}