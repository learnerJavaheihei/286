package l2s.gameserver.model.actor.stat;

import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author Bonux
**/
public class NpcStat extends CreatureStat
{
	public NpcStat(NpcInstance owner)
	{
		super(owner);
	}

	@Override
	public NpcInstance getOwner()
	{
		return (NpcInstance) _owner;
	}

	@Override
	public int getElementalAttackPower(ElementalElement element)
	{
		//TODO: Realese.
		return -1;
	}

	@Override
	public int getElementalDefence(ElementalElement element)
	{
		//TODO: Realese.
		return 0;
	}

	@Override
	public int getElementalCritRate(ElementalElement element)
	{
		//TODO: Realese.
		return 0;
	}

	@Override
	public int getElementalCritAttack(ElementalElement element)
	{
		//TODO: Realese.
		return 0;
	}
}