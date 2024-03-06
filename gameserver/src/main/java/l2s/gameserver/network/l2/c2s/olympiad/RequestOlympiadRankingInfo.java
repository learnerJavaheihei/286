package l2s.gameserver.network.l2.c2s.olympiad;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadRankingInfo;

/**
 * @author NviX
 */
public class RequestOlympiadRankingInfo extends L2GameClientPacket {
	private int _tabId;
	private int _rankingType;
	private int _unk;
	private int _classId;
	private int _serverId;

	@Override
	protected boolean readImpl() {
		_tabId = readC();
		_rankingType = readC();
		_unk = readC(); // bCurrentSeason
		_classId = readD();
		_serverId = readD();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExOlympiadRankingInfo(activeChar, _tabId, _rankingType, _unk, _classId, _serverId));
	}
}