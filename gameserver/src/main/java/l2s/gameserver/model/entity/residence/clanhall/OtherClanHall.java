package l2s.gameserver.model.entity.residence.clanhall;

import l2s.gameserver.model.entity.residence.ClanHallType;
import l2s.gameserver.templates.StatsSet;

/**
 * @author Bonux
**/
public class OtherClanHall extends NormalClanHall
{
	public OtherClanHall(StatsSet set)
	{
		super(set);
	}

	@Override
	public ClanHallType getClanHallType()
	{
		return ClanHallType.OTHER;
	}
}