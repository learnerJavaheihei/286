package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.InstantClanHallAuctionEvent;

import java.util.concurrent.TimeUnit;

public class ExMercenaryCastlewarCastleSiegeHudInfo extends L2GameServerPacket {
	private static final int PREPARE_STATUS = 0;
	private static final int IN_PROGRESS_STATUS = 1;
	private static final int DONE_STATUS = 2;

	private  int castleId = 0;
	private  int status = 0;
	private  int currentTime = 0;
	private  int prepareLeftTime = 0;

	public <T> ExMercenaryCastlewarCastleSiegeHudInfo(T event) {
		if (event instanceof CastleSiegeEvent) {
			CastleSiegeEvent castleSiegeEvent = (CastleSiegeEvent) event;
			castleId = castleSiegeEvent.getResidence().getId();
			status = castleSiegeEvent.isInProgress() ? IN_PROGRESS_STATUS : (!castleSiegeEvent.isRegistrationOver() ? PREPARE_STATUS : DONE_STATUS);
			currentTime = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			if (status == PREPARE_STATUS) {
				prepareLeftTime = (int) TimeUnit.MILLISECONDS.toSeconds(castleSiegeEvent.getSiegeDate().getTimeInMillis() - System.currentTimeMillis());
			} else if (status == IN_PROGRESS_STATUS) {
				prepareLeftTime = (int) (TimeUnit.MINUTES.toSeconds(60) + (int) TimeUnit.MILLISECONDS.toSeconds(castleSiegeEvent.getSiegeDate().getTimeInMillis() - System.currentTimeMillis()));
			} else {
				prepareLeftTime = (int) TimeUnit.MILLISECONDS.toSeconds(castleSiegeEvent.getSiegeDate().getTimeInMillis() - System.currentTimeMillis());
			}
		}
		else if (event instanceof InstantClanHallAuctionEvent) {
			InstantClanHallAuctionEvent instantClanHallAuctionEvent = (InstantClanHallAuctionEvent) event;
			castleId = instantClanHallAuctionEvent.getResidence().getId();
			status = instantClanHallAuctionEvent.isInProgress() ? IN_PROGRESS_STATUS : (!instantClanHallAuctionEvent.isRegistrationOver() ? PREPARE_STATUS : DONE_STATUS);
			currentTime = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			if (status == PREPARE_STATUS) {
				prepareLeftTime = (int) TimeUnit.MILLISECONDS.toSeconds(instantClanHallAuctionEvent.getSiegeDate().getTimeInMillis() - System.currentTimeMillis());
			} else if (status == IN_PROGRESS_STATUS) {
				prepareLeftTime = (int) (TimeUnit.MINUTES.toSeconds(60) + (int) TimeUnit.MILLISECONDS.toSeconds(instantClanHallAuctionEvent.getSiegeDate().getTimeInMillis() - System.currentTimeMillis()));
			} else {
				prepareLeftTime = (int) TimeUnit.MILLISECONDS.toSeconds(instantClanHallAuctionEvent.getSiegeDate().getTimeInMillis() - System.currentTimeMillis());
			}
		}
		
	}

	@Override
	protected void writeImpl() {
		writeD(castleId);
		writeD(status);
		writeD(currentTime);
		writeD(prepareLeftTime);
	}
}
