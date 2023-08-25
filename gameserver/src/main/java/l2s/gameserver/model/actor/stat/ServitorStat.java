package l2s.gameserver.model.actor.stat;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.base.ElementalElement;

/**
 * @author Bonux
**/
public class ServitorStat extends CreatureStat
{
	public ServitorStat(Servitor owner)
	{
		super(owner);
	}

	@Override
	public Servitor getOwner()
	{
		return (Servitor) _owner;
	}

	@Override
	public int getElementalAttackPower(ElementalElement element)
	{
		Player owner = getOwner().getPlayer();
		if(owner != null)
			return owner.getStat().getElementalAttackPower(element);
		return -1;
	}

	@Override
	public int getElementalDefence(ElementalElement element)
	{
		Player owner = getOwner().getPlayer();
		if(owner != null)
			return owner.getStat().getElementalDefence(element);
		return 0;
	}

	@Override
	public int getElementalCritRate(ElementalElement element)
	{
		Player owner = getOwner().getPlayer();
		if(owner != null)
			return owner.getStat().getElementalCritRate(element);
		return 0;
	}

	@Override
	public int getElementalCritAttack(ElementalElement element)
	{
		Player owner = getOwner().getPlayer();
		if(owner != null)
			return owner.getStat().getElementalCritAttack(element);
		return 0;
	}
}