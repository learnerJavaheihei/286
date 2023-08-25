package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class RequestTodoListHTML extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _tab;
	@SuppressWarnings("unused")
	private String _linkName;
	
	@Override
	protected boolean readImpl()
	{
		_tab = readC();
		_linkName = readS();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// TODO
	}
}