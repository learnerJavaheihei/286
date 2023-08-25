package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class ExMercenaryCastlewarCastleSiegeAttackerList extends L2GameServerPacket {
	private final int _id;
	private final boolean _registrationValid;
	private List<SiegeClanObject> _clans;

	public ExMercenaryCastlewarCastleSiegeAttackerList(Castle castle) {
		_id = castle.getId();

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if (siegeEvent != null) {
			_registrationValid = !siegeEvent.isRegistrationOver();
			_clans = siegeEvent.getObjects(SiegeEvent.ATTACKERS);
		} else {
			_registrationValid = false;
			_clans = Collections.emptyList();
		}
	}

	@Override
	protected final void writeImpl() {
		//dddddd(dSSdddQddSSd)
		writeD(_id);
		writeD(0x00); // Owner's view
		writeD(_registrationValid);
		writeD(0x00); // Page number
		writeD(_clans.size());
		writeD(_clans.size());
		for (SiegeClanObject siegeClan : _clans) {
			Clan clan = siegeClan.getClan();
			writeD(clan.getClanId());
			writeS(clan.getName());
			writeS(clan.getLeaderName());
			writeD(clan.getCrestId());
			writeD((int) (siegeClan.getDate() / 1000L));
			writeD(siegeClan.getParam() > 0); // Have mercenaries recruting
			writeQ(siegeClan.getParam()); // Rawarding Rate %
			writeD(0x00); // Mercenaries count

			Alliance alliance = clan.getAlliance();
			if (alliance != null) {
				writeD(alliance.getAllyId());
				writeS(alliance.getAllyName());
				writeS(alliance.getAllyLeaderName());
				writeD(alliance.getAllyCrestId());
			} else {
				writeD(0x00);
				writeS(StringUtils.EMPTY);
				writeS(StringUtils.EMPTY);
				writeD(0x00);
			}
		}
	}
}