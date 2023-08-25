package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;

public class RelationChangedPacket extends L2GameServerPacket
{
	private class RelationChangedData
	{
		public int objectId;
		public boolean isAutoAttackable;
		public long relation;
		public int karma, pvpFlag;
	}

	public enum RelationChangedType {
		INSIDE_BATTLEFIELD,
		IN_PVP,
		CHAOTIC,
		IN_PARTY,
		PARTY_LEADER,
		SAME_PARTY,
		IN_PLEDGE,
		UNK,
		PLEDGE_LEADER,
		SAME_PLEDGE,
		SIEGE_PARTICIPANT,
		SIEGE_ATTACKER,
		UNK2,
		KILLED_BY_DEATH_KNIGHT,
		SIEGE_ALLY,
		SIEGE_ENEMY,
		SIEGE_DEFENDER,
		CLAN_WAR_ATTACKER, //mutual war
		CLAN_WAR_ATTACKED,//mutual war
		IN_ALLIANCE,
		ALLIANCE_LEADER,
		SAME_ALLIANCE;

		public boolean isClanWarState() {
			return this == CLAN_WAR_ATTACKED || this == CLAN_WAR_ATTACKER;
		}

		public long getRelationState() {
			return (long) Math.pow(2, ordinal()) * (isClanWarState() ? 10000_0000L : KILLED_BY_DEATH_KNIGHT == this ? 10000_0000L : 1L);
		}
	}
	
	// Masks
	private static final byte SEND_DEFAULT = (byte) 0x01;
	private static final byte SEND_ONE = (byte) 0x02;
	private static final byte SEND_MULTI = (byte) 0x04;

	private byte _mask = (byte) 0x00;

	private final List<RelationChangedData> _datas = new ArrayList<>();

	public RelationChangedPacket()
	{
		//
	}

	public RelationChangedPacket(Playable about, Player target)
	{
		add(about, target);
	}

	public void add(Playable about, Player target)
	{
		RelationChangedData data = new RelationChangedData();
		data.objectId = about.getObjectId();
		data.karma = about.getKarma();
		data.pvpFlag = about.getPvpFlag();
		data.isAutoAttackable = about.isAutoAttackable(target);
		data.relation = about.getRelation(target);

		_datas.add(data);

		if(_datas.size() > 1)
			_mask |= SEND_MULTI;
		else if(_datas.size() == 1)
			_mask |= SEND_ONE;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_mask);
		if((_mask & SEND_MULTI) == SEND_MULTI)
		{
			writeH(_datas.size());
			for(RelationChangedData data : _datas)
				writeRelation(data);
		}
		else if((_mask & SEND_ONE) == SEND_ONE)
			writeRelation(_datas.get(0));
		else if((_mask & SEND_DEFAULT) == SEND_DEFAULT)
			writeD(_datas.get(0).objectId);
	}
	
	private void writeRelation(RelationChangedData data)
	{
		writeD(data.objectId);
		writeQ(data.relation);
		writeC(data.isAutoAttackable);
		writeD(data.karma);
		writeC(data.pvpFlag);
	}
}