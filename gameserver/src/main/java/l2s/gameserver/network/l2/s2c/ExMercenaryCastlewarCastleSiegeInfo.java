package l2s.gameserver.network.l2.s2c;

import java.util.concurrent.TimeUnit;

import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Clan;

public class ExMercenaryCastlewarCastleSiegeInfo extends L2GameServerPacket {
	private static final int PREPARE_STATUS = 0;
	private static final int IN_PROGRESS_STATUS = 1;
	private static final int DONE_STATUS = 2;

	private final int castleId;
	private final int ownerClanId;
	private final int ownerClanCrestId;
	private final String ownerClanName;
	private final String ownerLeaderName;
	private final int status;
	private final int attackersCount;
	private final int defendersCount;

	public ExMercenaryCastlewarCastleSiegeInfo(Castle castle) {
		castleId = castle.getId();
		Clan ownerClan = castle.getOwner();
		if (ownerClan != null) {
			ownerClanId = ownerClan.getClanId();
			ownerClanCrestId = ownerClan.getCrestId();
			ownerClanName = ownerClan.getName();
			ownerLeaderName = ownerClan.getLeaderName();
		} else {
			ownerClanId = 0;
			ownerClanCrestId = 0;
			ownerClanName = "";
			ownerLeaderName = "";
		}

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if (siegeEvent != null) {
			status = siegeEvent.isInProgress() ? IN_PROGRESS_STATUS : (!siegeEvent.isRegistrationOver() ? PREPARE_STATUS : /*siegeEvent.isInPrepare() ? PREPARE_STATUS : */DONE_STATUS);
			attackersCount = siegeEvent.getObjects(SiegeEvent.ATTACKERS).size();

			int defendersCount = siegeEvent.getObjects(SiegeEvent.DEFENDERS).size();
			defendersCount += siegeEvent.getObjects(CastleSiegeEvent.DEFENDERS_WAITING).size();
			defendersCount += siegeEvent.getObjects(CastleSiegeEvent.DEFENDERS_REFUSED).size();
			this.defendersCount = defendersCount;
		} else {
			status = DONE_STATUS;
			attackersCount = 0;
			defendersCount = 0;
		}
	}

	@Override
	protected void writeImpl() {
		writeD(castleId);
		writeD(ownerClanId); // UNK
		writeD(ownerClanCrestId); // UNK
		writeString(ownerClanName);
		writeString(ownerLeaderName);
		writeD(status);
		writeD(attackersCount); // Castle Siege Camp
		writeD(defendersCount); // Defenders' Camp
	}
}