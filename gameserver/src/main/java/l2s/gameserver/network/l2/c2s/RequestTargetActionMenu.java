package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class RequestTargetActionMenu extends L2GameClientPacket
{
	private int _targetObjectId;

	@Override
	protected boolean readImpl()
	{
		_targetObjectId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		GameObject target = GameObjectsStorage.findObject(_targetObjectId);
		if(target == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!target.isTargetable(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != target)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setTarget(target);
	}
}