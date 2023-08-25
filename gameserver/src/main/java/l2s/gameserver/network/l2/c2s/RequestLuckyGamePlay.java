package l2s.gameserver.network.l2.c2s;

import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.LuckyGameHolder;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExBettingLuckyGameResult;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.luckygame.LuckyGameData;
import l2s.gameserver.templates.luckygame.LuckyGameItem;
import l2s.gameserver.templates.luckygame.LuckyGameType;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class RequestLuckyGamePlay extends L2GameClientPacket
{
	private int _typeId;
	private int _gamesCount;

	@Override
	protected boolean readImpl()
	{
		_typeId = readD();
		_gamesCount = Math.min(readD(), 50);
		return true;
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_gamesCount <= 0)
			return;

		if(!Config.ALLOW_LUCKY_GAME_EVENT)
			return;

		if(_typeId < 0 || _typeId >= LuckyGameType.VALUES.length)
			return;

		final LuckyGameData gameData = LuckyGameHolder.getInstance().getData(LuckyGameType.VALUES[_typeId]);
		if(gameData == null)
			return;

		if(player.getWeightPenalty() >= 3 || player.getInventoryLimit() * 0.8 < player.getInventory().getSize())
		{
			player.sendPacket(new ExBettingLuckyGameResult(ExBettingLuckyGameResult.INVALID_CAPACITY, gameData.getType()));
			player.sendPacket(SystemMsg.YOUR_INVENTORY_IS_EITHER_FULL_OR_OVERWEIGHT);
			return;
		}

		final int gamesLimit = gameData.getGamesLimit();
		final int playedGamesCount;

		if(gamesLimit > 0)
		{
			playedGamesCount = player.getVarInt(LuckyGameData.PLAYED_LUCKY_GAMES_VAR + gameData.getType().ordinal(), 0);

			_gamesCount = Math.min(_gamesCount, gamesLimit - playedGamesCount);
			if(_gamesCount <= 0)
				return;
		}
		else
			playedGamesCount = 0;

		if(gameData.getFeeItemId() == -1)
		{
			_gamesCount = Math.min(_gamesCount, (int) (player.getPremiumPoints() / gameData.getFeeItemCount()));
			if(_gamesCount <= 0)
				return;

			final int consumePointsCount = (int) (_gamesCount * gameData.getFeeItemCount());
			if(player.reducePremiumPoints(consumePointsCount))
			{
				if(gameData.getType() == LuckyGameType.LUXURY)
					player.sendPacket(new SystemMessagePacket(SystemMsg.ROUND_S1_OF_LUXURY_FORTUNE_READING_COMPLETE).addInteger(_gamesCount));
				else
					player.sendPacket(new SystemMessagePacket(SystemMsg.ROUND_S1_OF_FORTUNE_READING_COMPLETE).addInteger(_gamesCount));
			}
			else
			{
				player.sendPacket(new ExBettingLuckyGameResult(ExBettingLuckyGameResult.INVALID_ITEM_COUNT, gameData.getType()));
				return;
			}
		}
		else
		{
			_gamesCount = Math.min(_gamesCount, (int) (ItemFunctions.getItemCount(player, gameData.getFeeItemId()) / gameData.getFeeItemCount()));
			if(_gamesCount <= 0)
				return;

			final long consumeItemsCount = _gamesCount * gameData.getFeeItemCount();
			if(ItemFunctions.deleteItem(player, gameData.getFeeItemId(), consumeItemsCount, false))
			{
				if(gameData.getType() == LuckyGameType.LUXURY)
					player.sendPacket(new SystemMessagePacket(SystemMsg.ROUND_S1_OF_LUXURY_FORTUNE_READING_COMPLETE).addInteger(_gamesCount));
				else
					player.sendPacket(new SystemMessagePacket(SystemMsg.ROUND_S1_OF_FORTUNE_READING_COMPLETE).addInteger(_gamesCount));

				player.sendPacket(SystemMessagePacket.removeItems(gameData.getFeeItemId(), consumeItemsCount));
			}
			else
			{
				player.sendPacket(new ExBettingLuckyGameResult(ExBettingLuckyGameResult.INVALID_ITEM_COUNT, gameData.getType()));
				return;
			}
		}

		if(gamesLimit > 0)
		{
			SchedulingPattern reusePattern = gameData.getReusePattern();
			player.setVar(LuckyGameData.PLAYED_LUCKY_GAMES_VAR + gameData.getType().ordinal(), playedGamesCount + _gamesCount, reusePattern == null ? -1 : reusePattern.next(System.currentTimeMillis()));
		}

		final int serverGamesCount = ServerVariables.getInt(LuckyGameData.LUCKY_GAMES_COUNT_VAR + gameData.getType().ordinal(), 0);
		ServerVariables.set(LuckyGameData.LUCKY_GAMES_COUNT_VAR + gameData.getType().ordinal(), serverGamesCount + _gamesCount);

		final int personalGamesCount = player.getVarInt(LuckyGameData.LUCKY_GAMES_COUNT_VAR + gameData.getType().ordinal(), 0);
		player.setVar(LuckyGameData.LUCKY_GAMES_COUNT_VAR + gameData.getType().ordinal(), personalGamesCount + _gamesCount);

		TIntLongMap rewardsMap = new TIntLongHashMap();
		List<LuckyGameItem> rewards = new ArrayList<LuckyGameItem>();
		for(int i = 1; i <= _gamesCount; i++)
		{
			final boolean uniqueReward = ((serverGamesCount + i) % Config.LUCKY_GAME_UNIQUE_REWARD_GAMES_COUNT) == 0;
			final boolean additionalReward = ((personalGamesCount + i) % Config.LUCKY_GAME_ADDITIONAL_REWARD_GAMES_COUNT) == 0;

			LuckyGameItem reward = null;
			if(uniqueReward)
			{
				reward = LuckyGameData.rollItem(gameData.getUniqueRewards(), true);
				if(reward != null)
				{
					SystemMessagePacket sm;
					if(gameData.getType() == LuckyGameType.LUXURY)
						sm = new SystemMessagePacket(SystemMsg.CONGRATULATIONS_C1_HAS_OBTAINED_S2_OF_S3_IN_THE_LUXURY_FORTUNE_READING);
					else
						sm = new SystemMessagePacket(SystemMsg.CONGRATULATIONS_C1_HAS_OBTAINED_S2_OF_S3_THROUGH_FORTUNE_READING);

					sm.addName(player);
					sm.addItemName(reward.getId());
					sm.addLong(reward.getCount());

					Announcements.announceToAll(sm);
				}
			}
			if(reward == null && additionalReward)
				reward = LuckyGameData.rollItem(gameData.getAdditionalRewards(), true);
			if(reward == null)
				reward = LuckyGameData.rollItem(gameData.getCommonRewards(), false);

			if(reward != null)
			{
				rewards.add(reward);
				rewardsMap.put(reward.getId(), rewardsMap.get(reward.getId()) + reward.getCount());
			}
		}

		for(TIntLongIterator iterator = rewardsMap.iterator(); iterator.hasNext();)
		{
			iterator.advance();
			ItemFunctions.addItem(player, iterator.key(), iterator.value());
		}

		player.sendPacket(new ExBettingLuckyGameResult(player, gameData, rewards));
	}
}