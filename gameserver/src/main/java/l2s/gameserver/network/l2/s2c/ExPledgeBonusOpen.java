package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.utils.PledgeBonusUtils;

/**
 * @author Bonux
**/
public class ExPledgeBonusOpen extends L2GameServerPacket
{
	private int _attendanceProgress = 0;
	private int _huntingProgress = 0;
	private int _yesterdayAttendanceReward = 0;
	private int _yesterdayHuntingReward = 0;
	private int _yesterdayAttendanceRewardId = 0;
	private int _yesterdayHuntingRewardId = 0;
	private boolean _attendanceRewardReceivable = false;
	private boolean _huntingRewardReceivable = false;

	public ExPledgeBonusOpen(Player player)
	{
		if(!Config.EX_USE_PLEDGE_BONUS)
			return;

		Clan clan = player.getClan();
		if(clan == null)
			return;

		_attendanceProgress = clan.getAttendanceProgress();
		_huntingProgress = clan.getHuntingProgress();
		_yesterdayAttendanceReward = clan.getYesterdayAttendanceReward();
		_yesterdayHuntingReward = clan.getYesterdayHuntingReward();
		_yesterdayAttendanceRewardId = PledgeBonusUtils.ATTENDANCE_REWARDS.get(_yesterdayAttendanceReward);
		_yesterdayHuntingRewardId = PledgeBonusUtils.HUNTING_REWARDS.get(_yesterdayHuntingReward);
		_attendanceRewardReceivable = _yesterdayAttendanceRewardId > 0 && PledgeBonusUtils.isAttendanceRewardAvailable(player);
		_huntingRewardReceivable = _yesterdayHuntingRewardId > 0 && PledgeBonusUtils.isHuntingRewardAvailable(player);
	}

	@Override
	protected final void writeImpl()
	{
		//ddcdccddcdcc
		writeD(PledgeBonusUtils.MAX_ATTENDANCE_PROGRESS);    // Attendance requirement (max)
		writeD(_attendanceProgress);    // Current amount
		writeC(0x00);	// Reward Type (0 - Skill, 1 - Item)
		writeD(_yesterdayAttendanceRewardId);    // Yesterday's reward
		writeC(_yesterdayAttendanceReward);    // Reward level
		writeC(_attendanceRewardReceivable);    // Receivable
		writeD(PledgeBonusUtils.MAX_HUNTING_PROGRESS);    // Hunting requirement (max)
		writeD(_huntingProgress);    // Current amount
		writeC(0x01);	// Reward Type (0 - Skill, 1 - Item)
		writeD(_yesterdayHuntingRewardId);    // Yesterday's reward
		writeC(_yesterdayHuntingReward);    // Reward level
		writeC(_huntingRewardReceivable);    // Receivable
	}
}