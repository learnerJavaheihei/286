package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.ServerPacketOpcodes;

import java.util.concurrent.TimeUnit;

public class ExMercenaryCastlewarCastleInfo extends L2GameServerPacket {
	private final int castleId;
	private final int ownerClanId;
	private final int ownerClanCrestId;
	private final String ownerClanName;
	private final String ownerLeaderName;
	private final int taxRate;
	private final long taxesAccumulated;
	private final long unk4;
	private final int siegeDate;

	public ExMercenaryCastlewarCastleInfo(Castle castle, long nearestSiegeTime) {
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
		taxRate = castle.getTaxPercent();
		taxesAccumulated = 0L;
		unk4 = 0;
		siegeDate = (int) TimeUnit.MILLISECONDS.toSeconds(nearestSiegeTime);
		//siegeDate = (int) (castle.getSiegeDate().getTimeInMillis() / 1000);
	}

	@Override
	protected void writeImpl() {
		writeD(castleId);
		writeD(ownerClanId); // UNK
		writeD(ownerClanCrestId); // UNK
		writeString(ownerClanName);
		writeString(ownerLeaderName);
		writeD(taxRate); // UNK
		writeQ(taxesAccumulated);
		writeQ(unk4); // UNK
		writeD(siegeDate);
	}
}
