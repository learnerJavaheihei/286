package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.LuckyGameHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.luckygame.LuckyGameData;
import l2s.gameserver.templates.luckygame.LuckyGameType;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class ReciveVipLuckyGameInfo extends L2GameServerPacket
{
	private final boolean _enabled;
	private long _availableNormalGames = 0;
	private long _availablePremiumGames = 0;

	public ReciveVipLuckyGameInfo(Player player)
	{
		if(Config.ALLOW_LUCKY_GAME_EVENT)
		{
			_enabled = true;

			LuckyGameData data = LuckyGameHolder.getInstance().getData(LuckyGameType.NORMAL);
			if(data != null)
			{
				if(data.getFeeItemId() == -1)
					_availableNormalGames = player.getPremiumPoints() / data.getFeeItemCount();
				else
					_availableNormalGames = ItemFunctions.getItemCount(player, data.getFeeItemId()) / data.getFeeItemCount();

				int gamesLimit = data.getGamesLimit();
				if(gamesLimit > 0)
				{
					int playedGamesCount = player.getVarInt(LuckyGameData.PLAYED_LUCKY_GAMES_VAR + data.getType().ordinal(), 0);
					_availableNormalGames = Math.max(0, Math.min(_availableNormalGames, gamesLimit - playedGamesCount));
				}
			}

			data = LuckyGameHolder.getInstance().getData(LuckyGameType.LUXURY);
			if(data != null)
			{
				if(data.getFeeItemId() == -1)
					_availablePremiumGames = player.getPremiumPoints() / data.getFeeItemCount();
				else
					_availablePremiumGames = ItemFunctions.getItemCount(player, data.getFeeItemId()) / data.getFeeItemCount();

				int gamesLimit = data.getGamesLimit();
				if(gamesLimit > 0)
				{
					int playedGamesCount = player.getVarInt(LuckyGameData.PLAYED_LUCKY_GAMES_VAR + data.getType().ordinal(), 0);
					_availablePremiumGames = Math.max(0, Math.min(_availablePremiumGames, gamesLimit - playedGamesCount));
				}
			}
		}
		else
		{
			_enabled = false;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_enabled);
		writeD((int) _availableNormalGames);
		writeD((int) _availablePremiumGames);
	}
}