package l2s.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import l2s.commons.math.SafeMath;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.stats.Stats;

/**
 * @reworked by Bonux
**/
public class RewardGroup implements Cloneable
{
	private double _chance;

	private final String _time;
	private final int _startHour;
	private final int _startMinute;
	private final int _endHour;
	private final int _endMinute;

	private boolean _isAdena = true; // Шанс фиксирован, растет только количество
	private boolean _notRate = false; // Рейты вообще не применяются
	private List<RewardData> _items = new ArrayList<RewardData>();

	public RewardGroup(double chance, String time)
	{
		setChance(chance);

		_time = time;

		if(_time != null)
		{
			int[][] timeArr = StringArrayUtils.stringToIntArray2X(_time, "-", ":");
			if(timeArr.length > 0)
			{
				_startHour = timeArr[0].length > 0 ? timeArr[0][0] : 0;
				_startMinute = timeArr[0].length > 1 ? timeArr[0][1] : 0;
			}
			else
			{
				_startHour = 0;
				_startMinute = 0;
			}

			if(timeArr.length > 1)
			{
				_endHour = timeArr[1].length > 0 ? timeArr[1][0] : 23;
				_endMinute = timeArr[1].length > 1 ? timeArr[1][1] : 59;
			}
			else
			{
				_endHour = 23;
				_endMinute = 59;
			}
		}
		else
		{
			_startHour = 0;
			_startMinute = 0;
			_endHour = 23;
			_endMinute = 59;
		}
	}

	public boolean notRate()
	{
		return _notRate;
	}

	public void setNotRate(boolean notRate)
	{
		_notRate = notRate;
	}

	public double getChance()
	{
		return _chance;
	}

	public void setChance(double chance)
	{
		_chance = Math.min(chance, RewardList.MAX_CHANCE);
	}

	public boolean checkTime(long timeInMillis)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		if(_startHour <= _endHour)
			return checkTime(_startHour, _startMinute, _endHour, _endMinute, hour, minute);
		if(checkTime(_startHour, _startMinute, 23, 59, hour, minute))
			return true;
		if(checkTime(0, 0, _endHour, _endMinute, hour, minute))
			return true;
		return false;
	}

	private static boolean checkTime(int minHour, int minMinute, int maxHour, int maxMinute, int currentHour, int currentMinute)
	{
		if(currentHour > minHour && currentHour < maxHour)
			return true;
		if(currentHour == minHour && currentMinute >= minMinute && (currentHour < maxHour || currentMinute <= maxMinute))
			return true;
		if((currentHour > minHour || currentMinute >= minMinute) && currentHour == maxHour && currentMinute <= maxMinute)
			return true;
		return false;
	}

	public boolean isAdena()
	{
		return _isAdena;
	}

	public void setIsAdena(boolean isAdena)
	{
		_isAdena = isAdena;
	}

	public void addData(RewardData item)
	{
		if(!item.getItem().isAdena())
			_isAdena = false;
		_items.add(item);
	}

	/**
	 * Возвращает список вещей
	 */
	public List<RewardData> getItems()
	{
		return _items;
	}

	/**
	 * Возвращает полностью независимую копию группы
	 */
	@Override
	public RewardGroup clone()
	{
		RewardGroup ret = new RewardGroup(_chance, _time);
		for(RewardData i : _items)
			ret.addData(i.clone());
		return ret;
	}

	/**
	 * Функция используется в основном механизме расчета дропа, выбирает одну/несколько вещей из группы, в зависимости от рейтов
	 * 
	 */
	public List<RewardItem> roll(RewardType type, Player player, double penaltyMod, NpcInstance npc)
	{
		if(!checkTime(System.currentTimeMillis()))
			return Collections.emptyList();

		switch(type)
		{
			case NOT_RATED_GROUPED:
			case NOT_RATED_NOT_GROUPED:
				return rollItems(penaltyMod, 1.0, 1.0);
			case EVENT_GROUPED:
				// TODO: Дропать ли с РБ и миньонов?
				if(npc != null && npc.getReflection().isDefault() && !npc.isRaid() && (npc.getLeader() == null || !npc.getLeader().isRaid()))
					return rollItems(penaltyMod * player.getDropChanceMod() / Config.DROP_CHANCE_MODIFIER, player.getRateItems() / Config.RATE_DROP_ITEMS_BY_LVL[player.getLevel()], player.getDropCountMod() / Config.DROP_COUNT_MODIFIER);
				return Collections.emptyList();
			case SWEEP:
				return rollItems(penaltyMod * player.getSpoilChanceMod(), player.getRateSpoil() * (npc != null ? npc.getStat().calc(Stats.SPOIL_RATE_MULTIPLIER, 1., player, null) : 1.), player.getSpoilCountMod());
			case RATED_GROUPED:
				if(isAdena())
					return rollAdena(penaltyMod, player.getRateAdena() * (npc != null ? npc.getStat().calc(Stats.ADENA_RATE_MULTIPLIER, 1., player, null) : 1.));

				return rollItems(penaltyMod * npc.getDropChanceMod(player), (npc != null ? npc.getRewardRate(player) * npc.getStat().calc(Stats.DROP_RATE_MULTIPLIER, 1., player, null) : player.getRateItems()), npc.getDropCountMod(player));
			default:
				return Collections.emptyList();
		}
	}

	private List<RewardItem> rollAdena(double mod, double rate)
	{
		if(notRate())
		{
			mod = Math.min(mod, 1.);
			rate = 1.;
		}

		if(mod > 0 && rate > 0)
		{
			if(getChance() > Rnd.get(RewardList.MAX_CHANCE))
			{
				List<RewardItem> rolledItems = new ArrayList<RewardItem>();
				for(RewardData data : getItems())
				{
					RewardItem item = data.rollAdena(mod, rate);
					if(item != null)
						rolledItems.add(item);
				}

				if(rolledItems.isEmpty())
					return Collections.emptyList();

				List<RewardItem> result = new ArrayList<RewardItem>();
				for(int i = 0; i < Config.MAX_DROP_ITEMS_FROM_ONE_GROUP; i++)
				{
					RewardItem rolledItem = Rnd.get(rolledItems);
					if(rolledItems.remove(rolledItem))
						result.add(rolledItem);

					if(rolledItems.isEmpty())
						break;
				}
				return result;
			}
		}
		return Collections.emptyList();
	}

	private List<RewardItem> rollItems(double mod, double rate, double countMod)
	{
		if(notRate())
		{
			mod = Math.min(mod, 1.);
			rate = 1.;
		}

		if(mod > 0 && rate > 0)
		{
			double chance = getChance() * mod;
			if(chance > RewardList.MAX_CHANCE)
			{
				mod = (chance - RewardList.MAX_CHANCE) / getChance() + 1;
				chance = RewardList.MAX_CHANCE;
			}
			else
				mod = 1.;

			if(chance > 0)
			{
				int rolledCount = 0;
				int mult = (int) Math.ceil(rate);
				if(chance >= RewardList.MAX_CHANCE)
				{
					rolledCount = (int) rate;
					if(mult > rate)
					{
						if(chance * (rate - (mult - 1)) > Rnd.get(RewardList.MAX_CHANCE))
							rolledCount++;
					}
				}
				else
				{
					for(int n = 0; n < mult; n++) // TODO: Реально ли оптимизировать без цикла?
					{
						if(chance * Math.min(rate - n, 1.0) > Rnd.get(RewardList.MAX_CHANCE))
							rolledCount++;
					}
				}

				if(rolledCount > 0)
				{
					List<RewardItem> rolledItems = new ArrayList<RewardItem>();
					for(RewardData data : getItems())
					{
						RewardItem item = data.rollItem(mod, rolledCount, countMod);
						if(item != null)
							rolledItems.add(item);
					}

					if(rolledItems.isEmpty())
						return Collections.emptyList();

					List<RewardItem> result = new ArrayList<RewardItem>();
					for(int i = 0; i < Config.MAX_DROP_ITEMS_FROM_ONE_GROUP; i++)
					{
						RewardItem rolledItem = Rnd.get(rolledItems);
						if(rolledItems.remove(rolledItem))
							result.add(rolledItem);

						if(rolledItems.isEmpty())
							break;
					}
					return result;
				}
			}
		}
		return Collections.emptyList();
	}
}