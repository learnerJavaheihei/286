package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class PledgeReceiveWarList extends L2GameServerPacket
{
	private Clan _clan;
	private int _page;

	public PledgeReceiveWarList(Clan clan, int page)
	{
		_clan = clan;
		_page = page;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_page);

		Collection<ClanWar> wars = _clan.getWars().valueCollection();

		writeD(wars.size());
		for(ClanWar war : wars)
		{
			writeS(war.getOpposingClan(_clan).getName());
			writeD(war.getClanWarState(_clan).ordinal());
			writeD(war.getPeriodDuration());
			writeD(war.getPointDiff(_clan));
			writeD(war.calculateWarProgress(_clan).ordinal());
			writeD(war.getKillToStart()); // Friends to start war left
		}
	}
}