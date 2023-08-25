package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.ExRotation;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.PositionUtils;

public class EffectShadowStep extends EffectFlyAbstract
{
	public EffectShadowStep(EffectTemplate template)
	{
		super(template);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		int px = effected.getX();
		int py = effected.getY();

		double ph = PositionUtils.convertHeadingToDegree(effected.getHeading()) + 180;
		if(ph > 360)
			ph -= 360;

		ph = (Math.PI * ph) / 180;
		
		int x = (int) (px + (30 * Math.cos(ph)));
		int y = (int) (py + (30 * Math.sin(ph)));
		int z = effected.getZ();

		Location destiny = GeoEngine.moveCheck(effector.getX(), effector.getY(), effector.getZ(), x, y, effector.getGeoIndex());
		if(destiny == null)
			return;

		x = destiny.getX();
		y = destiny.getY();

		effector.abortAttack(true, true);
		effector.abortCast(true, true);
		effector.getMovement().stopMove();

		effector.broadcastPacket(new FlyToLocationPacket(effector, new Location(x, y, z), FlyToLocationPacket.FlyType.DUMMY, getFlySpeed(), getFlyDelay(), getFlyAnimationSpeed()));
		effector.setXYZ(x, y, z);
		effector.setHeading(PositionUtils.calculateHeadingFrom(effector, effected));
		effector.broadcastPacket(new ExRotation(effector.getObjectId(), effector.getHeading()));
	}
}