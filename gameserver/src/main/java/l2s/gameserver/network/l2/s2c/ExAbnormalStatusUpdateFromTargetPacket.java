package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.utils.SkillUtils;

public class ExAbnormalStatusUpdateFromTargetPacket extends L2GameServerPacket
{
	public static final int INFINITIVE_EFFECT = -1;

	private List<Abnormal> _effects;
	private int _objectId;

	private static class Abnormal
	{
		public int effectorObjectId;
		public int skillId;
		public int skillLvl;
		public int abnormalType;
		public int duration;

		public Abnormal(int effectorObjectId, int skillId, int skillLvl, int abnormalType, int duration)
		{
			this.effectorObjectId = effectorObjectId;
			this.skillId = skillId;
			this.skillLvl = skillLvl;
			this.abnormalType = abnormalType;
			this.duration = duration;
		}
	}

	public ExAbnormalStatusUpdateFromTargetPacket(int objId)
	{
		_objectId = objId;
		_effects = new ArrayList<Abnormal>();
	}

	public void addEffect(int effectorObjectId, int skillId, int skillLvl, int abnormalType, int duration)
	{
		_effects.add(new Abnormal(effectorObjectId, skillId, skillLvl, abnormalType, duration));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		//writeH(0); // 286 classic/essence - not shown anymore abnormals on target
		//修复看到目标异常状态
		writeH(_effects.size());
		for(Abnormal temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.skillLvl);
			writeH(temp.abnormalType);
			writeOptionalD(temp.duration);
			writeD(temp.effectorObjectId); // Buffer OID
		}
	}
}