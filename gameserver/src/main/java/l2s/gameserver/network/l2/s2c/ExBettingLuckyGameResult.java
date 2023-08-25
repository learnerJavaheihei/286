package l2s.gameserver.network.l2.s2c;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.luckygame.LuckyGameData;
import l2s.gameserver.templates.luckygame.LuckyGameItem;
import l2s.gameserver.templates.luckygame.LuckyGameType;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Eanseen
 * reworked by Bonux
**/
public class ExBettingLuckyGameResult extends L2GameServerPacket
{
	public static final int INVALID_CAPACITY = -2;
	public static final int INVALID_ITEM_COUNT = -1;
	public static final int DISABLED = 0;
	public static final int SUCCESS = 1;

	private final int _result;
	private final int _type;
	private long _availableGamesCount;
	private final List<LuckyGameItem> _items;

	public ExBettingLuckyGameResult(Player player, LuckyGameData data, List<LuckyGameItem> items)
	{
		_result = SUCCESS;
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

		_items = items;
	}

	public ExBettingLuckyGameResult(int result, LuckyGameType type)
	{
		_result = result;
		_type = type.ordinal();
		_availableGamesCount = 0L;
		_items = Collections.emptyList();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
		writeD(_type);
		writeD((int) _availableGamesCount);
		writeD(_items.size()); // кол-во
		for(LuckyGameItem item : _items)
		{
			writeD(item.isFantastic() ? 2 : 0);
			writeD(item.getId());
			writeD((int) item.getCount());
		}
	}
}