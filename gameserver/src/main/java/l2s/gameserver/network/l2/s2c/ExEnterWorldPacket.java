package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author Bonux
 **/
public class ExEnterWorldPacket extends L2GameServerPacket {
	private final int _serverTime;
	private final int _utcTimeDiff;

	public ExEnterWorldPacket() {
		long currentTimeMillis = System.currentTimeMillis();
		_serverTime = (int) TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis);
		TimeZone timeZone = TimeZone.getDefault();
		_utcTimeDiff = (int) -TimeUnit.MILLISECONDS.toSeconds(timeZone.getRawOffset() + (timeZone.inDaylightTime(new Date(currentTimeMillis)) ? timeZone.getDSTSavings() : 0));
	}

	@Override
	protected final void writeImpl() {
		writeD(_serverTime);
		writeD(_utcTimeDiff); // Time diff in minutes to UTC
		writeD(0x00); // UNK
		writeD(Config.GATEKEEPER_FREE); // UNK
	}
}