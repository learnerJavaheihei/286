package l2s.gameserver.model.actor.instances.player;

import java.util.Objects;

import l2s.gameserver.data.xml.holder.ElementalDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritInfo;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritGetExp;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.elemental.ElementalLevelData;
import l2s.gameserver.templates.elemental.ElementalEvolution;
import l2s.gameserver.templates.elemental.ElementalTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
 */
public class Elemental
{
	private static final Logger _log = LoggerFactory.getLogger(Elemental.class);

	private final ElementalTemplate _template;

	private ElementalEvolution _evolution = null;
	private ElementalLevelData _levelData = null;

	private long _minExp = 0L;
	private long _exp = 0L;

	private int _attackPoints = 0;
	private int _defencePoints = 0;
	private int _critRatePoints = 0;
	private int _critAttackPoints = 0;

	public Elemental(ElementalElement element)
	{
		_template = ElementalDataHolder.getInstance().getTemplate(element);
		try
		{
			Objects.requireNonNull(_template);
		}
		catch(Exception e)
		{
			_log.error("Elemental: Not found template for element: " + element, e);
			return;
		}
		setEvolutionLevel(0);
	}

	public ElementalTemplate getTemplate()
	{
		return _template;
	}

	public ElementalEvolution getEvolution()
	{
		return _evolution;
	}

	public ElementalLevelData getLevelData()
	{
		return _levelData;
	}

	public ElementalElement getElement()
	{
		return _template.getElement();
	}

	public int getElementId()
	{
		return getElement().getId();
	}

	public int getEvolutionLevel()
	{
		return _evolution.getLevel();
	}

	public boolean setEvolutionLevel(int value)
	{
		ElementalEvolution evolution;
		try
		{
			evolution = Objects.requireNonNull(_template.getEvolution(value));
		}
		catch(Exception e)
		{
			_log.error("Elemental: Not found evolution for evolution level: " + value + ", element: " + getElement(), e);
			return false;
		}

		_evolution = evolution;
		_exp = 0;
		return setLevel(1);
	}

	public int getLevel()
	{
		return _levelData.getLevel();
	}

	public boolean setLevel(int level)
	{
		ElementalLevelData levelData;
		try
		{
			levelData = Objects.requireNonNull(_evolution.getLevelData(level));
		}
		catch(Exception e)
		{
			_log.error("Elemental: Not found level data for level: " + level + ", evolution level: " + getEvolutionLevel() + ", element: " + getElement(), e);
			return false;
		}

		_levelData = levelData;
		_minExp = _evolution.getMinExp(level);
		_exp = Math.max(_exp, getMinExp());
		_exp = Math.min(_exp, getMaxExp());
		return true;
	}

	public long getMinExp()
	{
		return _minExp;
	}

	public long getMaxExp()
	{
		return getLevelData().getExp();
	}

	public long getExp()
	{
		return _exp;
	}

	public void addExp(Player owner, long exp)
	{
		int oldLevel = getLevel();

		addExp(exp);

		owner.sendPacket(new SystemMessagePacket(SystemMsg.OBTAINED_S2_ATTRIBUTE_XP_OF_S1).addLong(exp).addElementName(getElement().getId() - 1));

		int newLevel = getLevel();
		if(oldLevel != newLevel)
		{
			owner.sendPacket(new SystemMessagePacket(SystemMsg.S1_ATTRIBUTE_SPIRIT_BECAME_LEVEL_S2).addElementName(getElement().getId() - 1).addInteger(newLevel));
			owner.sendPacket(new ExElementalSpiritInfo(owner, 0));
			owner.getListeners().onElementalLevelChange(this, oldLevel, newLevel);
		}
		else
			owner.sendPacket(new ExElementalSpiritGetExp(getElement().getId(), getExp()));
	}

	public void addExp(long value)
	{
		if(value > 0)
			setExp(getExp() + value);
	}

	public void setExp(long value)
	{
		if(_exp == value)
			return;

		_exp = value;

		if(_exp > getMaxExp() || _exp < getMinExp())
		{
			int level = _evolution.getLevelByExp(_exp);
			if(level != getLevel())
			{
				if(setLevel(level))
					return;
			}
		}
		_exp = Math.max(_exp, getMinExp());
		_exp = Math.min(_exp, getMaxExp());
	}

	public int getAttackPoints()
	{
		return _attackPoints;
	}

	public void setAttackPoints(int value)
	{
		_attackPoints = value;
	}

	public int getDefencePoints()
	{
		return _defencePoints;
	}

	public void setDefencePoints(int value)
	{
		_defencePoints = value;
	}

	public int getCritRatePoints()
	{
		return _critRatePoints;
	}

	public void setCritRatePoints(int value)
	{
		_critRatePoints = value;
	}

	public int getCritAttackPoints()
	{
		return _critAttackPoints;
	}

	public void setCritAttackPoints(int value)
	{
		_critAttackPoints = value;
	}

	public void resetPoints()
	{
		_attackPoints = 0;
		_defencePoints = 0;
		_critRatePoints = 0;
		_critAttackPoints = 0;
	}

	public int getMaxPoints()
	{
		return (getEvolutionLevel() - 1) * 10 + getLevel();
	}

	public int getUsedPoints()
	{
		return _attackPoints + _defencePoints + _critRatePoints + _critAttackPoints;
	}

	public int getAvailablePoints()
	{
		return getMaxPoints() - getUsedPoints();
	}

	@Override
	public String toString()
	{
		return "Elemental[element=" + getElement() + "]";
	}
}