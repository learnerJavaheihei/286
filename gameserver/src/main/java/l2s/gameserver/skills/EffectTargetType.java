package l2s.gameserver.skills;

import l2s.gameserver.model.Creature;

/**
 * @author Bonux
**/
public enum EffectTargetType
{
	NORMAL
	{
		public boolean checkTarget(Creature target)
		{
			return true;
		}
	},
	PVP
	{
		public boolean checkTarget(Creature target)
		{
			return target.isPlayable();
		}
	},
	PVE
	{
		public boolean checkTarget(Creature target)
		{
			return !target.isPlayable();
		}
	};

	public static final EffectTargetType[] VALUES = values();

	public abstract boolean checkTarget(Creature target);
}