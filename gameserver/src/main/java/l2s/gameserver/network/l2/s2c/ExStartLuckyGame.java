package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.luckygame.LuckyGameData;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Eanseen
 * reworked by Bonux
**/
public class ExStartLuckyGame extends L2GameServerPacket
{
	private final int _type;
	private long _availableGamesCount;

	public ExStartLuckyGame(Player player, LuckyGameData data)
	{
		_type = data.getType().ordinal();

		if(data.getFeeItemId() == -1)
			_availableGamesCount = player.getPremiumPoints() / data.getFeeItemCount();
		else
			_availableGamesCount = ItemFunctions.getItemCount(player, data.getFeeItemId()) / data.getFeeItemCount();

		int gamesLimit = data.getGamesLimit();
		if(gamesLimit > 0)
		{
			int playedGamesCount = player.getVarInt(LuckyGameData.PLAYED_LUCKY_GAMES_VAR + data.getType().ordinal(), 0);
			_availableGamesCount = Math.max(0, Math.min(_availableGamesCount, gamesLimit - playedGamesCount));
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
		writeQ(_availableGamesCount);
	}
}