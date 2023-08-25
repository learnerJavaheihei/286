package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.ServerPacketOpcodes;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExMercenaryCastlewarCastleSiegeDefenderList extends L2GameServerPacket {
	private static class DefenderClan {
		private final Clan _clan;
		private final int _type;
		private final int _time;
		private final long _mercenaryReward;
		private final int _mercenariesCount;

		public DefenderClan(Clan clan, int type, int time, long mercenaryReward, int mercenariesCount) {
			_clan = clan;
			_type = type;
			_time = time;
			_mercenaryReward = mercenaryReward;
			_mercenariesCount = mercenariesCount;
		}
	}

	public static int OWNER = 1;
	public static int WAITING = 2;
	public static int ACCEPTED = 3;
	public static int REFUSE = 4;

	private final int _id;
	private final boolean _registrationValid;
	private final List<DefenderClan> _defenderClans;

	public ExMercenaryCastlewarCastleSiegeDefenderList(Castle castle) {
		_id = castle.getId();

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if (siegeEvent != null) {
			_registrationValid = !siegeEvent.isRegistrationOver() && castle.getOwner() != null;

			List<SiegeClanObject> defenders = siegeEvent.getObjects(SiegeEvent.DEFENDERS);
			List<SiegeClanObject> defendersWaiting = siegeEvent.getObjects(CastleSiegeEvent.DEFENDERS_WAITING);
			List<SiegeClanObject> defendersRefused = siegeEvent.getObjects(CastleSiegeEvent.DEFENDERS_REFUSED);
			_defenderClans = new ArrayList<>(defenders.size() + defendersWaiting.size() + defendersRefused.size());
			for (SiegeClanObject siegeClan : defenders)
				_defenderClans.add(new DefenderClan(siegeClan.getClan(), siegeClan.getClan() == castle.getOwner() ? OWNER : ACCEPTED, (int) (siegeClan.getDate() / 1000L), siegeClan.getParam(), 0));
			for (SiegeClanObject siegeClan : defendersWaiting)
				_defenderClans.add(new DefenderClan(siegeClan.getClan(), WAITING, (int) (siegeClan.getDate() / 1000L), siegeClan.getParam(), 0));
			for (SiegeClanObject siegeClan : defendersRefused)
				_defenderClans.add(new DefenderClan(siegeClan.getClan(), REFUSE, (int) (siegeClan.getDate() / 1000L), siegeClan.getParam(), 0));
		} else {
			_registrationValid = false;
			_defenderClans = Collections.emptyList();
		}
	}

	@Override
	protected final void writeImpl() {
		//dddddd(dSSddddQddSSd)
		writeD(_id);
		writeD(0x00); // Owner's view
		writeD(_registrationValid);
		writeD(0x00); // Page number
		writeD(_defenderClans.size());
		writeD(_defenderClans.size());
		for (DefenderClan defenderClan : _defenderClans) {
			Clan clan = defenderClan._clan;
			writeD(clan.getClanId());
			writeS(clan.getName());
			writeS(clan.getLeaderName());
			writeD(clan.getCrestId());
			writeD(defenderClan._time);
			writeD(defenderClan._type);
			writeD(defenderClan._mercenaryReward > 0); // Have mercenaries recruting
			writeQ(defenderClan._mercenaryReward); // Rawarding Rate %
			writeD(defenderClan._mercenariesCount); // Mercenaries count

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