package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class p_raid_berserk extends EffectHandler
{
	public p_raid_berserk(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		return effected.isRaid();
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.broadcastPacket(new ExShowScreenMessage(NpcString.RAID_BOSS_WENT_BERSERK, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, true));
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.broadcastPacket(new ExShowScreenMessage(NpcString.RAID_BOSS_WENT_BACK_TO_NORMAL, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, true));
	}
}