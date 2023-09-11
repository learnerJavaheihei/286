package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.AbnormalsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BossBarrier extends Fighter
{
	public BossBarrier(NpcInstance actor)
	{
		super(actor);
	}
	int z = 0;
	int stage = 0;

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 32203, 1);

		NpcInstance actor = getActor();

		if(attacker != null && actor.getCurrentHpPercents() <=90 && stage == 0)
		{	actor.altUseSkill(skillEntry,actor);
			stage = 1;
		}
		if(attacker != null && actor.getCurrentHpPercents() <=60 && stage == 20)
		{	actor.altUseSkill(skillEntry,actor);
			stage = 21;
		}
		if(attacker != null && actor.getCurrentHpPercents() <=30 && stage == 30)
		{	actor.altUseSkill(skillEntry,actor);
			stage = 31;
		}

		if(actor.getAbnormalList().contains(AbnormalType.BOSSSHIELD) && stage ==1){
			addTask(1,15000);
			stage = 2;
		}
		if(actor.getAbnormalList().contains(AbnormalType.BOSSSHIELD) && stage ==21){
			addTask(1,15000);
			stage = 22;
		}
		if(actor.getAbnormalList().contains(AbnormalType.BOSSSHIELD) && stage ==31){
			addTask(1,15000);
			stage = 32;
		}
		if(attacker != null && stage == 2 || stage == 22 || stage == 32)
		{
			z=z+1;
		}
		if(z >= 500 && stage == 2)
		{
			stage = 20;
			z = 0;
			Deb();
		}
		else if(z >= 500 && stage == 22)
		{
			stage = 30;
			z = 0;
			Deb();
		}
		else if(z >= 500 && stage == 32)
		{
			stage = 40;
			z = 0;
			Deb();
		}
		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();

		if(timerId == 1 && z < 500 && stage == 2  || stage == 22  || stage == 32)
		{
			actor.setCurrentHpMp(actor.getMaxHp(), actor.getMaxMp());
			stage = 0;
			z = 0;
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}

	public void Deb() {
		NpcInstance actor = getActor();
		final List<Abnormal> abnormals = new ArrayList<Abnormal>(actor.getAbnormalList().values());
		Collections.sort(abnormals, AbnormalsComparator.getInstance());
		Collections.reverse(abnormals);
		for (Abnormal abnormal : abnormals) {
			if (!abnormal.isCancelable())
				continue;

			Skill effectSkill = abnormal.getSkill();
			if (effectSkill == null)
				continue;

			if (effectSkill.isToggle())
				continue;

			if (effectSkill.isPassive())
				continue;


			abnormal.exit();
		}
	}
	

}