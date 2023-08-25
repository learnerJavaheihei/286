package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import java.util.concurrent.TimeUnit;

public class ExMercenaryCastlewarCastleSiegeHudInfo extends L2GameServerPacket {
	private static final int PREPARE_STATUS = 0;
	private static final int IN_PROGRESS_STATUS = 1;
	private static final int DONE_STATUS = 2;

	private final int castleId;
	private final int status;
	private final int currentTime;
	private final int prepareLeftTime;

	public ExMercenaryCastlewarCastleSiegeHudInfo(CastleSiegeEvent castleSiegeEvent) {
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

	@Override
	protected void writeImpl() {
		writeD(castleId);
		writeD(status);
		writeD(currentTime);
		writeD(prepareLeftTime);
	}
}
