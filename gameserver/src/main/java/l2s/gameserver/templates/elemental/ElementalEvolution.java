package l2s.gameserver.templates.elemental;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.templates.item.data.ItemData;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

/**
 * @author Bonux
**/
public class ElementalEvolution implements Comparable<ElementalEvolution>
{
	private final int _id;
	private final int _level;
	private final IntObjectMap<ElementalLevelData> _levelDatas = new TreeIntObjectMap<ElementalLevelData>();
	private final List<ItemData> _riseLevelCost = new ArrayList<ItemData>();

	private int _maxAttackPoints = 0;
	private int _maxDefencePoints = 0;
	private int _maxCritRatePoints = 0;
	private int _maxCritAttackPoints = 0;

	private ElementalLevelData _minLevelData = null;
	private ElementalLevelData _maxLevelData = null;

	public ElementalEvolution(ElementalElement element, int evolutionLevel)
	{
		_id = element.getId() * 10 + evolutionLevel + 1;
		_level = evolutionLevel;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMaxLevel()
	{
		return _maxLevelData.getLevel();
	}

	public void setLimits(int maxAttackPoints, int maxDefencePoints, int maxCritRatePoints, int maxCritAttackPoints)
	{
		_maxAttackPoints = maxAttackPoints;
		_maxDefencePoints = maxDefencePoints;
		_maxCritRatePoints = maxCritRatePoints;
		_maxCritAttackPoints = maxCritAttackPoints;
	}

	public int getMaxAttackPoints()
	{
		return _maxAttackPoints;
	}

	public int getMaxDefencePoints()
	{
		return _maxDefencePoints;
	}

	public int getMaxCritRatePoints()
	{
		return _maxCritRatePoints;
	}

	public int getMaxCritAttackPoints()
	{
		return _maxCritAttackPoints;
	}

	public void addLevelData(ElementalLevelData data)
	{
		if(_minLevelData == null || _minLevelData.getLevel() >= data.getLevel())
			_minLevelData = data;
		if(_maxLevelData == null || _maxLevelData.getLevel() <= data.getLevel())
			_maxLevelData = data;
		_levelDatas.put(data.getLevel(), data);
	}

	public ElementalLevelData getLevelData(int level)
	{
		return _levelDatas.get(level);
	}

	public int getLevelByExp(long exp)
	{
		ElementalLevelData result = null;
		for(ElementalLevelData data : _levelDatas.valueCollection())
		{
			if(result == null || data.getExp() < result.getExp())
			{
				if(data.getExp() >= exp)
					result = data;
			}
		}

		if(result == null)
		{
			if(_minLevelData.getExp() >= exp)
				return _minLevelData.getLevel();
			if(_maxLevelData.getExp() <= exp)
				return _maxLevelData.getLevel();
		}
		return result.getLevel();
	}

	public long getMinExp(int level)
	{
		if(_minLevelData.getLevel() >= level)
			return 0;
		return getLevelData(level - 1).getExp() + 1L;
	}

	public long getMaxExp() {
		return _maxLevelData.getExp();
	}

	public ElementalLevelData[] getLevelDatas()
	{
		return _levelDatas.values(new ElementalLevelData[_levelDatas.size()]);
	}

	public void addRiseLevelCost(ItemData data)
	{
		_riseLevelCost.add(data);
	}

	public List<ItemData> getRiseLevelCost()
	{
		return _riseLevelCost;
	}

	@Override
	public int compareTo(ElementalEvolution o)
	{
		return Integer.compare(getLevel(), o.getLevel());
	}
}