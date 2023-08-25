package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritInfo;

public class RequestExElementalSpiritInfo extends L2GameClientPacket
{
	private int _unk;

	@Override
	protected boolean readImpl()
	{
		_unk = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.ELEMENTAL_SYSTEM_ENABLED)
		{
			activeChar.sendActionFailed();
			return;
		}

		if((activeChar.getClassLevel().ordinal() < ClassLevel.SECOND.ordinal()) || (activeChar.getLevel() < 40))
		{
			activeChar.sendPacket(SystemMsg.UNABLE_TO_OPEN_ATTRIBUTE_AFTER_THE_THIRD_CLASS_CHANGE);
			return;
		}

		activeChar.sendPacket(new ExElementalSpiritInfo(activeChar, _unk));
	}
}