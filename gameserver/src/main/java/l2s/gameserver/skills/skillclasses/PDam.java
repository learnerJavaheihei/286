package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FinishRotatingPacket;
import l2s.gameserver.network.l2.s2c.StartRotatingPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.templates.StatsSet;

public class PDam extends Skill
{
	private final boolean _directHp;
	private final boolean _turner;
	private final boolean _blow;
	private final boolean _static;
	private final int _num_attacks;

	public PDam(StatsSet set)
	{
		super(set);
		_directHp = set.getBool("directHp", false);
		_turner = set.getBool("turner", false);
		_blow = set.getBool("blow", false);
		_static = set.getBool("static", false);
		_num_attacks = set.getInteger("num_attacks", 1);
	}

	@Override
	public boolean calcCriticalBlow(Creature caster, Creature target)
	{
		if(_blow)
			return Formulas.calcBlow(caster, target, this);
		return false;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(target.isDead())
			return;

		if(_turner && !target.isInvulnerable())
		{
			target.broadcastPacket(new StartRotatingPacket(target, target.getHeading(), 1, 65535));
			target.broadcastPacket(new FinishRotatingPacket(target, activeChar.getHeading(), 65535));
			target.setHeading(activeChar.getHeading());
			target.sendPacket(new SystemMessagePacket(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(this));
		}

		final Creature realTarget = reflected ? activeChar : target;

		double power = getPower();
		if(_static)
		{
			realTarget.reduceCurrentHp(power, activeChar, this, true, true, _directHp, true, false, false, power != 0, true, false, false, false, 0, false);
			return;
		}
		
		for(int i = 0; i < _num_attacks; i++)
		{
			final AttackInfo info = Formulas.calcSkillPDamage(activeChar, realTarget, this, power, _blow, isSSPossible());
			if(info == null)
				return;

			realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, _directHp, true, false, false, power != 0, true, info.crit || info.blow, false, false, info.elementalDamage, info.elementalCrit);

			if(i == 0 && (!info.miss || info.damage >= 1))
			{
				double lethalDmg = Formulas.calcLethalDamage(activeChar, realTarget, this);
				if(lethalDmg > 0)
					realTarget.reduceCurrentHp(lethalDmg, activeChar, this, true, true, false, false, false, false, false);
				else if(!reflected)
					realTarget.doCounterAttack(this, activeChar, _blow);
			}
		}
	}
}