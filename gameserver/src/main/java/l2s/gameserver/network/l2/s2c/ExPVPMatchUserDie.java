package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.TeamType;

/**
 * @author VISTALL
 */
public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private int _blueKills, _redKills;

	public ExPVPMatchUserDie(int blueKills, int redKills)
	{
		_blueKills = blueKills;
		_redKills = redKills;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_blueKills);
		writeD(_redKills);
	}
}