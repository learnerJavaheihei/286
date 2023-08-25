package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.LuckyGameHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExStartLuckyGame;
import l2s.gameserver.templates.luckygame.LuckyGameData;
import l2s.gameserver.templates.luckygame.LuckyGameType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public final class RequestLuckyGameStartInfo extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestLuckyGameStartInfo.class);

	private int _typeId;

	protected boolean readImpl()
	{
		_typeId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(!Config.ALLOW_LUCKY_GAME_EVENT)
			return;

		if(_typeId < 0 || _typeId >= LuckyGameType.VALUES.length)
			return;

		final LuckyGameType type = LuckyGameType.VALUES[_typeId];
		final LuckyGameData gameData = LuckyGameHolder.getInstance().getData(type);
		if(gameData == null)
		{
			_log.warn("Cannot find data for lucky game TYPE[" + type + "]!");
			return;
		}

		player.sendPacket(new ExStartLuckyGame(player, gameData));
	}
}