package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FinishRotatingPacket;
import l2s.gameserver.network.l2.s2c.StartRotatingPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class i_p_attack extends i_abstract_effect
{
	private final boolean _directHp;
	private final boolean _turner;
	private final boolean _blow;
	private final boolean _static;

	public i_p_attack(EffectTemplate template)
	{
		super(template);

		_directHp = getParams().getBool("directHp", false);
		_turner = getParams().getBool("turner", false);
		_blow = getParams().getBool("blow", false);
		_static = getParams().getBool("static", false);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return !effected.isDead();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		if(_turner && !effected.isInvulnerable())
		{
			effected.broadcastPacket(new StartRotatingPacket(effected, effected.getHeading(), 1, 65535));
			effected.broadcastPacket(new FinishRotatingPacket(effected, effector.getHeading(), 65535));
			effected.setHeading(effector.getHeading());
			effected.sendPacket(new SystemMessagePacket(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(getSkill()));
		}

		final Creature realTarget = reflected ? effector : effected;

		double power = getValue();
		if(getSkill().getId() == 10300)
		{
			if(realTarget.isMonster() && !realTarget.isRaid())
				power = realTarget.getCurrentHp() - 1;
		}

		if(_static)
		{
			realTarget.reduceCurrentHp(power, effector, getSkill(), true, true, _directHp, true, false, false, power != 0, true, false, false, false, 0, false);
			return;
		}

		final AttackInfo info = Formulas.calcSkillPDamage(effector, realTarget, getSkill(), power, _blow, getSkill().isSSPossible());
		if(info == null)
			return;

		realTarget.reduceCurrentHp(info.damage, effector, getSkill(), true, true, _directHp, true, false, false, getTemplate().isInstant() && power != 0, getTemplate().isInstant(), info.crit || info.blow, false, false, info.elementalDamage, info.elementalCrit);

		if(!info.miss || info.damage >= 1)
		{
			double lethalDmg = Formulas.calcLethalDamage(effector, realTarget, getSkill());
			if(lethalDmg > 0)
				realTarget.reduceCurrentHp(lethalDmg, effector, getSkill(), true, true, false, false, false, false, false);
			else if(!reflected)
				realTarget.doCounterAttack(getSkill(), effector, _blow);
		}
	}
}