package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.tables.ClanTable;

public class ExShowCastleInfo extends L2GameServerPacket
{
	private List<CastleInfo> _infos = Collections.emptyList();

	public ExShowCastleInfo()
	{
		String ownerName;
		int id, tax, nextSiege, side;
		boolean inSiege;

		List<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		_infos = new ArrayList<CastleInfo>(castles.size());
		for(Castle castle : castles)
		{
			ownerName = ClanTable.getInstance().getClanName(castle.getOwnerId());
			id = castle.getId();
			tax = castle.getTaxPercent();
			side = castle.getResidenceSide().ordinal();
			if(castle.getSiegeEvent() != null)
			{
				nextSiege =  (int) (castle.getSiegeDate().getTimeInMillis() / 1000);
				inSiege = castle.getSiegeEvent().isInProgress();
			}
			else
			{
				nextSiege = 0;
				inSiege = false;
			}
			_infos.add(new CastleInfo(ownerName, id, tax, nextSiege, inSiege, side));
		}
	}

	@Override
	protected final void writeImpl()
	{
		// temp fix for world map, while castle siege only giran
		//writeD(_infos.size());
		writeD(5);
		for (int i = 1; i < 3; i++)
		{
			writeD(i);
			writeS(StringUtils.EMPTY);
			writeD(0);
			writeD(0);
			writeC(0);
			writeC(0);
		}
		for(CastleInfo info : _infos)
		{
			writeD(info._id);
			writeS(info._ownerName);
			writeD(info._tax);
			writeD(info._nextSiege);
			writeC(info._inSiege);
			writeC(info._side);
		}
		_infos.clear();
		for (int i = 4; i < 6; i++)
		{
			writeD(i);
			writeS(StringUtils.EMPTY);
			writeD(0);
			writeD(0);
			writeC(0);
			writeC(0);
		}
	}

	private static class CastleInfo
	{
		public String _ownerName;
		public int _id, _tax, _nextSiege, _side;
		public boolean _inSiege;

		public CastleInfo(String ownerName, int id, int tax, int nextSiege, boolean inSiege, int side)
		{
			_ownerName = ownerName;
			_id = id;
			_tax = tax;
			_nextSiege = nextSiege;
			_inSiege = inSiege;
			_side = side;
		}
	}
}