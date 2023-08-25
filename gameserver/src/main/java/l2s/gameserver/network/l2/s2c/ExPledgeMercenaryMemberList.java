package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;

public class ExPledgeMercenaryMemberList extends L2GameServerPacket {
	private final int receiverObjectId;
	private final int castleId;
	private final int clanId;

	public ExPledgeMercenaryMemberList(Player player, int castleId, int clanId) {
		receiverObjectId = player.getObjectId();
		this.castleId = castleId;
		this.clanId = clanId;

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, castleId);
		if (castle != null) {
			CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
			if (siegeEvent != null) {
				SiegeClanObject siegeClanObject = siegeEvent.getSiegeClan(CastleSiegeEvent.ATTACKERS, clanId);
				if (siegeClanObject == null)
					siegeClanObject = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS, clanId);
				if (siegeClanObject != null) {
					//
				}
			}
		}

	}

	@Override
	protected final void writeImpl() {
		//stream(ddd(ddSd))
		writeD(castleId);
		writeD(clanId);
		writeD(0);
		/*for (CastleSiegeMercenaryObject mercenaryObject : mercenaries) {
			writeD(receiverObjectId == mercenaryObject.getPlayerObjectId());
			writeD(mercenaryObject.getPlayerObjectId()); // TODO: Check.
			writeString(mercenaryObject.getName());
			writeD(mercenaryObject.getClassId().ordinal());
		}*/
	}
}
