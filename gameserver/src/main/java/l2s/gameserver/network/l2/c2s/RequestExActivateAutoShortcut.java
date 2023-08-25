package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestExActivateAutoShortcut extends L2GameClientPacket
{
	private int _slot;
	private boolean _activate;

	@Override
	protected boolean readImpl()
	{
		_slot = readH();
		_activate = readC() > 0;
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.getAutoShortCuts().activate(_slot, _activate))
			return;

		player.sendActionFailed();
	}
}
