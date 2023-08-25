package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;

import org.napile.primitive.pair.IntObjectPair;

public class ConfirmDlg extends L2GameClientPacket
{
	private int _answer, _requestId;

	@Override
	protected boolean readImpl()
	{
		readD();
		_answer = readD();
		_requestId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		IntObjectPair<OnAnswerListener> entry = activeChar.getAskListener(true);
		if(entry == null || entry.getKey() != _requestId)
			return;

		OnAnswerListener listener = entry.getValue();
		if(_answer == 1)
			listener.sayYes();
		else
			listener.sayNo();
	}
}