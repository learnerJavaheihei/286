package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectKnockBack extends EffectFlyAbstract
{
	public EffectKnockBack(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isThrowAndKnockImmune())
		{
			effected.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		int curX = effected.getX();
		int curY = effected.getY();
		int curZ = effected.getZ();

		double dx = effector.getX() - curX;
		double dy = effector.getY() - curY;
		double dz = effector.getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);

		if(distance > 2000.0D)
			return;

		int offset = Math.min((int) distance + getFlyRadius(), 1400);
		offset = (int) (offset + Math.abs(dz));

		if(offset < 5)
			offset = 5;

		if(distance < 1.0D)
			return;

		double sin = dy / distance;
		double cos = dx / distance;

		int x = (effector.getX() - (int) (offset * cos));
		int y = (effector.getY() - (int) (offset * sin));
		int z = effected.getZ();

		Location destiny = GeoEngine.moveCheck(effected.getX(), effected.getY(), effected.getZ(), x, y, effected.getGeoIndex());
		if(destiny == null)
			return;

		x = destiny.getX();
		y = destiny.getY();

		if(effected.getFlags().getKnockBacked().start(this))
		{
			effected.abortAttack(true, true);
			effected.abortCast(true, true);
			effected.getMovement().stopMove();

			effected.getAI().notifyEvent(CtrlEvent.EVT_KNOCK_BACK, effected);

			effected.broadcastPacket(new FlyToLocationPacket(effected, new Location(x, y, z), FlyToLocationPacket.FlyType.PUSH_HORIZONTAL, getFlySpeed(), getFlyDelay(), getFlyAnimationSpeed()));
			effected.setXYZ(x, y, z);
		}
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.getFlags().getKnockBacked().stop(this))
		{
			if(!effected.isPlayer())
				effected.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
}