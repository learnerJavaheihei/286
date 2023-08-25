package l2s.gameserver.network.l2.s2c.olympiad;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.utils.SkillUtils;

public class ExOlympiadSpelledInfoPacket extends L2GameServerPacket
{
	// chdd(dhd)
	private int char_obj_id = 0;
	private List<Abnormal> _effects;

	class Abnormal
	{
		int skillId;
		int dat;
		int abnormalType;
		int duration;

		public Abnormal(int skillId, int dat, int abnormalType, int duration)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.abnormalType = abnormalType;
			this.duration = duration;
		}
	}

	public ExOlympiadSpelledInfoPacket()
	{
		_effects = new ArrayList<Abnormal>();
	}

	public void addEffect(int skillId, int dat, int abnormalType, int duration)
	{
		_effects.add(new Abnormal(skillId, dat, abnormalType, duration));
	}

	public void addSpellRecivedPlayer(Player cha)
	{
		if(cha != null)
			char_obj_id = cha.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(char_obj_id);
		writeD(_effects.size());
		for(Abnormal temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.dat);
			writeD(temp.abnormalType);
			writeOptionalD(temp.duration);
		}
	}
}