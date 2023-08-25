package l2s.gameserver.network.l2.s2c.olympiad;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.ServerPacketOpcodes;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

import java.util.Calendar;

public class ExOlympiadMyRankingInfo extends L2GameServerPacket {
	private final int cycleYear;
	private final int cycleMonth;
	private final int cycle;
	private final int points;

	public ExOlympiadMyRankingInfo(Player player) {
		Calendar calendar = Calendar.getInstance();
		cycleYear = calendar.get(Calendar.YEAR);
		cycleMonth = calendar.get(Calendar.MONTH);
		cycle = Olympiad.getCurrentCycle();
		points = Olympiad.getParticipantPoints(player.getObjectId());
	}

	@Override
	protected void writeImpl() {
		writeD(cycleYear);
		writeD(cycleMonth);
		writeD(cycle);
		writeD(0x00); // Place on current cycle ?
		writeD(0x00); // Wins
		writeD(0x00); // Loses
		writeD(points);
		writeD(0x00); // Place on previous cycle
		writeD(0x00); // win count & lose count previous cycle? lol
		writeD(0x00); // ??
		writeD(0x00); // Points on previous cycle
		writeD(0x00); // Hero counts
		writeD(0x00); // Legend counts
		writeD(0x00); // change to 1 causes shows nothing
	}
}
