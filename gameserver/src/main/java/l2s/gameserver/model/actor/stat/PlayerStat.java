package l2s.gameserver.model.actor.stat;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.stats.Stats;

/**
 * @author Bonux
**/
public class PlayerStat extends CreatureStat
{
	public PlayerStat(Player owner)
	{
		super(owner);
	}

	@Override
	public Player getOwner()
	{
		return (Player) _owner;
	}

	@Override
	public int getElementalAttackPower(ElementalElement element)
	{
		Elemental elemental = getOwner().getElementalList().get(element);
		if(elemental == null)
			return -1;

		int attack = elemental.getLevelData().getAttack();
		attack += elemental.getAttackPoints() * 5;

		Stats stat = null;
		switch(element)
		{
			case FIRE:
				stat = Stats.FIRE_ELEMENTAL_ATTACK;
				break;
			case WATER:
				stat = Stats.WATER_ELEMENTAL_ATTACK;
				break;
			case WIND:
				stat = Stats.WIND_ELEMENTAL_ATTACK;
				break;
			case EARTH:
				stat = Stats.EARTH_ELEMENTAL_ATTACK;
				break;
		}
		if(stat != null)
		{
			attack *= getMul(stat);
			attack += getAdd(stat);
		}
		return attack;
	}

	@Override
	public int getElementalDefence(ElementalElement element)
	{
		Elemental elemental = getOwner().getElementalList().get(element);
		if(elemental == null)
			return 0;

		int defence = elemental.getLevelData().getDefence();
		defence += elemental.getDefencePoints() * 5;

		Stats stat = null;
		switch(element)
		{
			case FIRE:
				stat = Stats.FIRE_ELEMENTAL_DEFENCE;
				break;
			case WATER:
				stat = Stats.WATER_ELEMENTAL_DEFENCE;
				break;
			case WIND:
				stat = Stats.WIND_ELEMENTAL_DEFENCE;
				break;
			case EARTH:
				stat = Stats.EARTH_ELEMENTAL_DEFENCE;
				break;
		}
		if(stat != null)
		{
			defence *= getMul(stat);
			defence += getAdd(stat);
		}
		return defence;
	}

	@Override
	public int getElementalCritRate(ElementalElement element)
	{
		Elemental elemental = getOwner().getElementalList().get(element);
		if(elemental == null)
			return 0;

		int critRate = elemental.getLevelData().getCritRate();
		critRate += elemental.getCritRatePoints();

		Stats stat = null;
		switch(element)
		{
			case FIRE:
				stat = Stats.FIRE_ELEMENTAL_CRIT_RATE;
				break;
			case WATER:
				stat = Stats.WATER_ELEMENTAL_CRIT_RATE;
				break;
			case WIND:
				stat = Stats.WIND_ELEMENTAL_CRIT_RATE;
				break;
			case EARTH:
				stat = Stats.EARTH_ELEMENTAL_CRIT_RATE;
				break;
		}
		if(stat != null)
		{
			critRate *= getMul(stat);
			critRate += getAdd(stat);
		}
		return critRate;
	}

	@Override
	public int getElementalCritAttack(ElementalElement element)
	{
		Elemental elemental = getOwner().getElementalList().get(element);
		if(elemental == null)
			return 0;

		int critAttack = elemental.getLevelData().getCritAttack();
		critAttack += elemental.getCritAttackPoints();

		Stats stat = null;
		switch(element)
		{
			case FIRE:
				stat = Stats.FIRE_ELEMENTAL_CRIT_ATTACK;
				break;
			case WATER:
				stat = Stats.WATER_ELEMENTAL_CRIT_ATTACK;
				break;
			case WIND:
				stat = Stats.WIND_ELEMENTAL_CRIT_ATTACK;
				break;
			case EARTH:
				stat = Stats.EARTH_ELEMENTAL_CRIT_ATTACK;
				break;
		}
		if(stat != null)
		{
			critAttack *= getMul(stat);
			critAttack += getAdd(stat);
		}
		return critAttack;
	}
}