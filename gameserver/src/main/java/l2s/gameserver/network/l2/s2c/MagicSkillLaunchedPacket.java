package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.Collections;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillCastingType;

public class MagicSkillLaunchedPacket extends L2GameServerPacket
{
	private final int _casterId;
	private final int _skillId;
	private final int _skillLevel;
	private final Collection<Creature> _targets;
	private final SkillCastingType _castingType;

	public MagicSkillLaunchedPacket(int casterId, int skillId, int skillLevel, Creature target, SkillCastingType castingType)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = Collections.singletonList(target);
		_castingType = castingType;
	}

	public MagicSkillLaunchedPacket(int casterId, int skillId, int skillLevel, Collection<Creature> targets, SkillCastingType castingType)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
		_castingType = castingType;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_castingType.getClientBarId()); // Casting bar type: 0 - default, 1 - default up, 2 - blue, 3 - green, 4 - red.
		writeD(_casterId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.size());
		for(Creature target : _targets)
		{
			if(target != null)
				writeD(target.getObjectId());
		}
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		if(player != null)
		{
			if(player.isNotShowBuffAnim())
				return _casterId == player.getObjectId() ? super.packet(player) : null;
		}

		return super.packet(player); 
	}
}