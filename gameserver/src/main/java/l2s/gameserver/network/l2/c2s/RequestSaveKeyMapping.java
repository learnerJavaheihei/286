package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExUISettingPacket;

/**
 * format: (ch)db
 */
public class RequestSaveKeyMapping extends L2GameClientPacket
{
	private byte[] _data;

	@Override
	protected boolean readImpl()
	{
		int length = readD();
		if(length > _buf.remaining() || length > Short.MAX_VALUE || length < 0)
		{
			_data = null;
			return false;
		}
		_data = new byte[length];
		readB(_data);
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _data == null)
			return;
		activeChar.setKeyBindings(_data);
		activeChar.sendPacket(new ExUISettingPacket(activeChar));
	}
}