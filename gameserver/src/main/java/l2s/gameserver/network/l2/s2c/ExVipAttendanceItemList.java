package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.Collections;

import l2s.gameserver.data.xml.holder.AttendanceRewardHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.data.AttendanceRewardData;

/**
 * @author Bonux
**/
public class ExVipAttendanceItemList extends L2GameServerPacket
{
	private final int _indexToReceive;
	private final int _lastReceivedIndex;
	private final boolean _received;
	private final Collection<AttendanceRewardData> _rewards;
	
	public ExVipAttendanceItemList(Player player)
	{
		_indexToReceive = player.getAttendanceRewards().getNextRewardIndex();
		_lastReceivedIndex = player.getAttendanceRewards().getReceivedRewardIndex();
		_received = player.getAttendanceRewards().isReceived();
		_rewards = _indexToReceive > 0 ? AttendanceRewardHolder.getInstance().getRewards(player.hasPremiumAccount()) : Collections.emptyList();
	}

	@Override
	protected void writeImpl()
	{
		writeC(_indexToReceive); // ID to receive
		writeC(_lastReceivedIndex); // Received count
		writeD(0x00); // UNK
		writeD(0x00); // UNK
		writeC(0x01); // UNK
		writeC(!_received); // Not Received
		writeC(0xFA); // UNK
		writeC(_rewards.size()); // Items Count
		_rewards.forEach(reward ->
		{
			writeD(reward.getId()); // Item ID
			writeQ(reward.getCount()); // Item count
			writeC(reward.isUnknown()); // UNK
			writeC(reward.isBest()); // Is luxary
		});
		writeC(0x00); // VIP rewards
		writeD(0x00); // UNK
	}
}