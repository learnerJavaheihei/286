package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestExEndScenePlayer extends L2GameClientPacket
{
	private int _movieId;

	@Override
	protected boolean readImpl()
	{
		_movieId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.isInMovie() || activeChar.getMovieId() != _movieId)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.endScenePlayer(false);
	}
}