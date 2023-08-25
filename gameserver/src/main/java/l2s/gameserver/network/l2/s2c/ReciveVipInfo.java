package l2s.gameserver.network.l2.s2c;

import java.util.concurrent.TimeUnit;

import l2s.gameserver.data.xml.holder.VIPDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.VIPTemplate;

/**
 * @author Bonux
**/
public class ReciveVipInfo extends L2GameServerPacket
{
	private final int _vipLevel;
	private final long _vipPoints;
	private final int _timeLeftForConsume;
	private final long _vipPointsForNextLevel;
	private final long _pointsCountForConsume;
	private final int _vipLevelAfterConsume;
	private final long _totalConsumedPoints;

	public ReciveVipInfo(Player player)
	{
		_vipLevel = player.getVIP().getLevel();
		_vipPoints = player.getVIP().getPoints();
		_timeLeftForConsume = (int) player.getVIP().getPointsConsumeLeftTime(TimeUnit.SECONDS);
		_pointsCountForConsume = player.getVIP().getPointsConsumeCount();
		_totalConsumedPoints = player.getVIP().getTotalConsumedPoints();
		//
		VIPTemplate tempTemplate = VIPDataHolder.getInstance().getVIPTemplate(_vipLevel + 1);
		_vipPointsForNextLevel = tempTemplate == null ? Integer.MAX_VALUE : tempTemplate.getPoints();
		//
		tempTemplate = VIPDataHolder.getInstance().getVIPTemplateByPoints(_vipPoints - _pointsCountForConsume);
		_vipLevelAfterConsume = tempTemplate == null ? Math.max(0, _vipLevel - 1) : tempTemplate.getLevel();
	}

	@Override
	protected void writeImpl()
	{
		writeC(_vipLevel);	//	VIP Level
		writeQ(_vipPoints);	// VIP Points
		writeD(_timeLeftForConsume);	// Time left for consume VIP points
		writeQ(_vipPointsForNextLevel);	// VIP points for next level
		writeQ(_pointsCountForConsume);	// Points count for consume
		writeC(_vipLevelAfterConsume);	// VIP level after consume
		writeQ(_totalConsumedPoints);	// Total consumed points
	}
}